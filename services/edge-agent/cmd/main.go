package main

import (
	"context"
	"flag"
	"fmt"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/wekkitech/somolink/services/edge-agent/internal/cache"
	"github.com/wekkitech/somolink/services/edge-agent/internal/safebrowsing"
	"github.com/wekkitech/somolink/services/edge-agent/internal/sync"
	"github.com/wekkitech/somolink/services/edge-agent/internal/telemetry"
	"github.com/wekkitech/somolink/services/edge-agent/pkg/config"
	"github.com/wekkitech/somolink/services/edge-agent/pkg/logger"
)

var (
	version   = "dev"
	buildTime = "unknown"
	configPath = flag.String("config", "/etc/somolink/edge-agent.yaml", "Path to configuration file")
)

func main() {
	flag.Parse()

	// Initialize logger
	log := logger.New()
	log.Info("Starting SomoLink Edge Agent",
		"version", version,
		"buildTime", buildTime,
	)

	// Load configuration
	cfg, err := config.Load(*configPath)
	if err != nil {
		log.Error("Failed to load configuration", "error", err)
		os.Exit(1)
	}

	// Create context with cancellation
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	// Initialize components
	telemetryCollector := telemetry.NewCollector(cfg.Telemetry, log)
	cacheManager := cache.NewManager(cfg.Cache, log)
	safeBrowsingFilter := safebrowsing.NewFilter(cfg.SafeBrowsing, log)
	syncService := sync.NewService(cfg.Sync, log)

	// Start telemetry collection loop
	go func() {
		ticker := time.NewTicker(cfg.Telemetry.CollectionInterval)
		defer ticker.Stop()

		for {
			select {
			case <-ticker.C:
				if err := collectAndSendTelemetry(ctx, telemetryCollector, syncService); err != nil {
					log.Error("Telemetry collection failed", "error", err)
				}
			case <-ctx.Done():
				return
			}
		}
	}()

	// Start cache management
	go cacheManager.Start(ctx)

	// Start safe browsing filter
	go safeBrowsingFilter.Start(ctx)

	// Start sync service
	go syncService.Start(ctx)

	// Health check endpoint
	go startHealthServer(cfg.HealthPort, log)

	log.Info("Edge Agent running", "deviceID", cfg.DeviceID)

	// Wait for interrupt signal
	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, os.Interrupt, syscall.SIGTERM)
	<-sigChan

	log.Info("Shutting down gracefully...")
	cancel()

	// Give components time to shut down
	time.Sleep(2 * time.Second)
	log.Info("Shutdown complete")
}

func collectAndSendTelemetry(ctx context.Context, collector *telemetry.Collector, syncer *sync.Service) error {
	// Collect telemetry data
	data, err := collector.Collect(ctx)
	if err != nil {
		return fmt.Errorf("collect telemetry: %w", err)
	}

	// Queue for sync (will be uploaded when connection available)
	if err := syncer.QueueTelemetry(ctx, data); err != nil {
		return fmt.Errorf("queue telemetry: %w", err)
	}

	return nil
}

func startHealthServer(port int, log logger.Logger) {
	// Simple HTTP health check server
	// Implementation would use net/http
	log.Info("Health server started", "port", port)
}
