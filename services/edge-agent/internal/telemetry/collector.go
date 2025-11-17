package telemetry

import (
	"context"
	"fmt"
	"os/exec"
	"strconv"
	"strings"
	"time"

	"github.com/wekkitech/somolink/services/edge-agent/pkg/config"
	"github.com/wekkitech/somolink/services/edge-agent/pkg/logger"
)

// TelemetryData represents collected device metrics
type TelemetryData struct {
	DeviceID      string                 `json:"device_id"`
	Timestamp     time.Time              `json:"timestamp"`
	Solar         SolarMetrics           `json:"solar"`
	Battery       BatteryMetrics         `json:"battery"`
	Network       NetworkMetrics         `json:"network"`
	System        SystemMetrics          `json:"system"`
	Learning      LearningMetrics        `json:"learning"`
	Custom        map[string]interface{} `json:"custom,omitempty"`
}

type SolarMetrics struct {
	PanelVoltage    float64 `json:"panel_voltage"`     // Volts
	PanelCurrent    float64 `json:"panel_current"`     // Amps
	PanelPower      float64 `json:"panel_power"`       // Watts
	Irradiance      float64 `json:"irradiance"`        // W/m²
	PanelTemp       float64 `json:"panel_temp"`        // Celsius
}

type BatteryMetrics struct {
	Voltage         float64 `json:"voltage"`           // Volts
	Current         float64 `json:"current"`           // Amps
	StateOfCharge   float64 `json:"state_of_charge"`   // Percentage
	Temperature     float64 `json:"temperature"`       // Celsius
	Health          float64 `json:"health"`            // Percentage
	CycleCount      int     `json:"cycle_count"`
}

type NetworkMetrics struct {
	SignalStrength  int     `json:"signal_strength"`   // dBm
	Bandwidth       float64 `json:"bandwidth"`         // Mbps
	Latency         float64 `json:"latency"`           // ms
	PacketLoss      float64 `json:"packet_loss"`       // Percentage
	ConnectedUsers  int     `json:"connected_users"`
	DataUsage       int64   `json:"data_usage"`        // Bytes
}

type SystemMetrics struct {
	CPUUsage        float64 `json:"cpu_usage"`         // Percentage
	MemoryUsage     float64 `json:"memory_usage"`      // Percentage
	DiskUsage       float64 `json:"disk_usage"`        // Percentage
	Uptime          int64   `json:"uptime"`            // Seconds
	Temperature     float64 `json:"temperature"`       // Celsius
}

type LearningMetrics struct {
	ActiveSessions  int     `json:"active_sessions"`
	ContentRequests int64   `json:"content_requests"`
	CacheHitRate    float64 `json:"cache_hit_rate"`    // Percentage
	BannedRequests  int     `json:"banned_requests"`   // Safe browsing blocks
}

// Collector handles telemetry data collection
type Collector struct {
	config config.TelemetryConfig
	log    logger.Logger
	deviceID string
}

// NewCollector creates a new telemetry collector
func NewCollector(cfg config.TelemetryConfig, log logger.Logger) *Collector {
	return &Collector{
		config:   cfg,
		log:      log,
		deviceID: cfg.DeviceID,
	}
}

// Collect gathers all telemetry data
func (c *Collector) Collect(ctx context.Context) (*TelemetryData, error) {
	data := &TelemetryData{
		DeviceID:  c.deviceID,
		Timestamp: time.Now().UTC(),
		Custom:    make(map[string]interface{}),
	}

	// Collect solar metrics (from MPPT controller via serial/I2C)
	solar, err := c.collectSolarMetrics(ctx)
	if err != nil {
		c.log.Warn("Failed to collect solar metrics", "error", err)
	} else {
		data.Solar = solar
	}

	// Collect battery metrics
	battery, err := c.collectBatteryMetrics(ctx)
	if err != nil {
		c.log.Warn("Failed to collect battery metrics", "error", err)
	} else {
		data.Battery = battery
	}

	// Collect network metrics
	network, err := c.collectNetworkMetrics(ctx)
	if err != nil {
		c.log.Warn("Failed to collect network metrics", "error", err)
	} else {
		data.Network = network
	}

	// Collect system metrics
	system, err := c.collectSystemMetrics(ctx)
	if err != nil {
		c.log.Warn("Failed to collect system metrics", "error", err)
	} else {
		data.System = system
	}

	// Collect learning metrics
	learning, err := c.collectLearningMetrics(ctx)
	if err != nil {
		c.log.Warn("Failed to collect learning metrics", "error", err)
	} else {
		data.Learning = learning
	}

	return data, nil
}

func (c *Collector) collectSolarMetrics(ctx context.Context) (SolarMetrics, error) {
	// In production, this would read from actual MPPT controller
	// Example using RS485/Modbus or I2C communication
	// Here we provide a stub implementation
	
	metrics := SolarMetrics{
		PanelVoltage: 18.5,  // Mock value
		PanelCurrent: 3.2,
		PanelPower:   59.2,
		Irradiance:   650.0,
		PanelTemp:    42.0,
	}

	// TODO: Implement actual MPPT communication
	// Example: victron := mppt.NewVictronController(serialPort)
	// metrics, err := victron.ReadMetrics()

	return metrics, nil
}

func (c *Collector) collectBatteryMetrics(ctx context.Context) (BatteryMetrics, error) {
	// Mock implementation - would read from BMS
	metrics := BatteryMetrics{
		Voltage:       12.6,
		Current:       -2.5,  // Negative = charging
		StateOfCharge: 75.0,
		Temperature:   28.0,
		Health:        95.0,
		CycleCount:    234,
	}

	// TODO: Implement BMS communication (Modbus/CAN)
	return metrics, nil
}

func (c *Collector) collectNetworkMetrics(ctx context.Context) (NetworkMetrics, error) {
	metrics := NetworkMetrics{}

	// Get signal strength from modem
	out, err := exec.CommandContext(ctx, "mmcli", "-m", "0", "--signal-get").Output()
	if err == nil {
		// Parse mmcli output for signal strength
		lines := strings.Split(string(out), "\n")
		for _, line := range lines {
			if strings.Contains(line, "rssi:") {
				parts := strings.Fields(line)
				if len(parts) > 1 {
					if val, err := strconv.ParseFloat(strings.TrimSuffix(parts[1], "dBm"), 64); err == nil {
						metrics.SignalStrength = int(val)
					}
				}
			}
		}
	}

	// Get connected WiFi users
	out, err = exec.CommandContext(ctx, "iw", "dev", "wlan0", "station", "dump").Output()
	if err == nil {
		metrics.ConnectedUsers = strings.Count(string(out), "Station")
	}

	// Get bandwidth and latency (simplified)
	metrics.Bandwidth = 5.2  // Mbps - would measure actual throughput
	metrics.Latency = 45.0   // ms
	metrics.PacketLoss = 0.5 // %

	return metrics, nil
}

func (c *Collector) collectSystemMetrics(ctx context.Context) (SystemMetrics, error) {
	metrics := SystemMetrics{}

	// CPU usage
	out, err := exec.CommandContext(ctx, "top", "-bn1").Output()
	if err == nil {
		lines := strings.Split(string(out), "\n")
		for _, line := range lines {
			if strings.Contains(line, "Cpu(s)") {
				// Parse CPU percentage
				metrics.CPUUsage = 35.5 // Simplified
				break
			}
		}
	}

	// Memory usage
	out, err = exec.CommandContext(ctx, "free", "-m").Output()
	if err == nil {
		lines := strings.Split(string(out), "\n")
		if len(lines) > 1 {
			// Parse memory values
			metrics.MemoryUsage = 42.0 // Simplified
		}
	}

	// Disk usage
	out, err = exec.CommandContext(ctx, "df", "-h", "/").Output()
	if err == nil {
		lines := strings.Split(string(out), "\n")
		if len(lines) > 1 {
			fields := strings.Fields(lines[1])
			if len(fields) >= 5 {
				usage := strings.TrimSuffix(fields[4], "%")
				if val, err := strconv.ParseFloat(usage, 64); err == nil {
					metrics.DiskUsage = val
				}
			}
		}
	}

	// System uptime
	out, err = exec.CommandContext(ctx, "cat", "/proc/uptime").Output()
	if err == nil {
		fields := strings.Fields(string(out))
		if len(fields) > 0 {
			if val, err := strconv.ParseFloat(fields[0], 64); err == nil {
				metrics.Uptime = int64(val)
			}
		}
	}

	// Temperature (from thermal zone)
	out, err = exec.CommandContext(ctx, "cat", "/sys/class/thermal/thermal_zone0/temp").Output()
	if err == nil {
		if val, err := strconv.ParseFloat(strings.TrimSpace(string(out)), 64); err == nil {
			metrics.Temperature = val / 1000.0 // Convert millidegrees to degrees
		}
	}

	return metrics, nil
}

func (c *Collector) collectLearningMetrics(ctx context.Context) (LearningMetrics, error) {
	// These would be collected from local analytics database
	metrics := LearningMetrics{
		ActiveSessions:  12,
		ContentRequests: 1543,
		CacheHitRate:    78.5,
		BannedRequests:  7,
	}

	// TODO: Query local SQLite analytics DB
	return metrics, nil
}
