# Multi-stage Dockerfile for SomoLink Edge Agent
# Optimized for ARM64 and AMD64 architectures

# Stage 1: Builder
FROM golang:1.21-alpine AS builder

# Install build dependencies
RUN apk add --no-cache git make gcc musl-dev

# Set working directory
WORKDIR /build

# Copy go mod files
COPY go.mod go.sum ./
RUN go mod download

# Copy source code
COPY . .

# Build the application
# CGO is required for SQLite
ENV CGO_ENABLED=1
RUN go build -ldflags="-w -s" -o edge-agent ./cmd/main.go

# Stage 2: Runtime
FROM alpine:3.18

# Install runtime dependencies
RUN apk add --no-cache \
    ca-certificates \
    tzdata \
    curl \
    iw \
    wireless-tools \
    hostapd \
    dnsmasq \
    iptables

# Create non-root user
RUN addgroup -g 1000 edgeuser && \
    adduser -D -u 1000 -G edgeuser edgeuser

# Set working directory
WORKDIR /app

# Copy binary from builder
COPY --from=builder /build/edge-agent .

# Copy configuration template
COPY --from=builder /build/configs/edge-agent.yaml.example /etc/somolink/edge-agent.yaml

# Create necessary directories
RUN mkdir -p /var/lib/somolink /var/log/somolink && \
    chown -R edgeuser:edgeuser /var/lib/somolink /var/log/somolink /app

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Expose ports
EXPOSE 8080

# Switch to non-root user
USER edgeuser

# Start application
ENTRYPOINT ["/app/edge-agent"]
CMD ["--config", "/etc/somolink/edge-agent.yaml"]
