# SomoLink Dataset Schemas

## Telemetry Data Schema

### TimescaleDB Hypertable: `telemetry_data`

```sql
CREATE TABLE telemetry_data (
    -- Primary Key & Timestamp
    time                    TIMESTAMPTZ NOT NULL,
    device_id               VARCHAR(50) NOT NULL,
    
    -- Solar Metrics
    solar_panel_voltage     DOUBLE PRECISION,
    solar_panel_current     DOUBLE PRECISION,
    solar_panel_power       DOUBLE PRECISION,
    solar_irradiance        DOUBLE PRECISION,
    solar_panel_temp        DOUBLE PRECISION,
    
    -- Battery Metrics
    battery_voltage         DOUBLE PRECISION,
    battery_current         DOUBLE PRECISION,
    battery_soc             DOUBLE PRECISION,  -- State of Charge (%)
    battery_temperature     DOUBLE PRECISION,
    battery_health          DOUBLE PRECISION,
    battery_cycle_count     INTEGER,
    
    -- Network Metrics
    network_signal_strength INTEGER,          -- dBm
    network_bandwidth       DOUBLE PRECISION, -- Mbps
    network_latency         DOUBLE PRECISION, -- ms
    network_packet_loss     DOUBLE PRECISION, -- %
    network_connected_users INTEGER,
    network_data_usage      BIGINT,           -- Bytes
    
    -- System Metrics
    system_cpu_usage        DOUBLE PRECISION, -- %
    system_memory_usage     DOUBLE PRECISION, -- %
    system_disk_usage       DOUBLE PRECISION, -- %
    system_uptime           BIGINT,           -- Seconds
    system_temperature      DOUBLE PRECISION, -- Celsius
    
    -- Learning Metrics
    learning_active_sessions INTEGER,
    learning_content_requests BIGINT,
    learning_cache_hit_rate DOUBLE PRECISION, -- %
    learning_banned_requests INTEGER,
    
    -- Metadata
    firmware_version        VARCHAR(20),
    data_quality_score      DOUBLE PRECISION,
    sync_status             VARCHAR(20),
    created_at              TIMESTAMPTZ DEFAULT NOW(),
    
    PRIMARY KEY (time, device_id)
);

-- Create hypertable
SELECT create_hypertable('telemetry_data', 'time');

-- Create indexes
CREATE INDEX idx_device_time ON telemetry_data (device_id, time DESC);
CREATE INDEX idx_battery_soc ON telemetry_data (battery_soc) WHERE battery_soc < 30;
CREATE INDEX idx_network_users ON telemetry_data (network_connected_users);

-- Create continuous aggregate for hourly averages
CREATE MATERIALIZED VIEW telemetry_hourly
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 hour', time) AS hour,
    device_id,
    AVG(solar_panel_power) AS avg_solar_power,
    AVG(battery_soc) AS avg_battery_soc,
    AVG(network_bandwidth) AS avg_bandwidth,
    SUM(network_connected_users) AS total_user_hours,
    AVG(learning_active_sessions) AS avg_sessions
FROM telemetry_data
GROUP BY hour, device_id;

-- Retention policy (keep raw data for 90 days)
SELECT add_retention_policy('telemetry_data', INTERVAL '90 days');
```

## Learning Analytics Schema

### Table: `learning_sessions`

```sql
CREATE TABLE learning_sessions (
    session_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id               VARCHAR(50) NOT NULL,
    school_id               VARCHAR(50) NOT NULL,
    user_id                 VARCHAR(50),          -- Anonymized student ID
    
    -- Session Timing
    started_at              TIMESTAMPTZ NOT NULL,
    ended_at                TIMESTAMPTZ,
    duration_minutes        INTEGER,
    
    -- Activity Metrics
    pages_visited           INTEGER,
    resources_accessed      INTEGER,
    downloads_count         INTEGER,
    cache_hits              INTEGER,
    cache_misses            INTEGER,
    
    -- Content Classification
    primary_subject         VARCHAR(50),          -- Math, Science, etc.
    secondary_subjects      TEXT[],
    content_level           VARCHAR(20),          -- Primary, Secondary, etc.
    
    -- Quality Indicators
    engagement_score        DOUBLE PRECISION,     -- 0-1
    completion_rate         DOUBLE PRECISION,     -- %
    interaction_quality     VARCHAR(20),          -- Low, Medium, High
    
    -- Network Quality
    avg_latency_ms          DOUBLE PRECISION,
    connection_quality      VARCHAR(20),
    disconnections          INTEGER,
    
    -- Metadata
    user_agent              TEXT,
    device_type             VARCHAR(50),
    created_at              TIMESTAMPTZ DEFAULT NOW(),
    
    CONSTRAINT fk_device FOREIGN KEY (device_id) 
        REFERENCES devices(device_id) ON DELETE CASCADE
);

CREATE INDEX idx_session_school_time ON learning_sessions (school_id, started_at DESC);
CREATE INDEX idx_session_user ON learning_sessions (user_id, started_at DESC);
CREATE INDEX idx_session_subject ON learning_sessions (primary_subject);
```

## Connected Learning Hours (CLH) View

```sql
CREATE VIEW connected_learning_hours AS
SELECT
    school_id,
    DATE_TRUNC('day', started_at) AS date,
    COUNT(DISTINCT session_id) AS total_sessions,
    COUNT(DISTINCT user_id) AS unique_learners,
    SUM(duration_minutes) / 60.0 AS total_clh,
    AVG(engagement_score) AS avg_engagement,
    AVG(completion_rate) AS avg_completion,
    
    -- Subject breakdown
    JSONB_OBJECT_AGG(
        primary_subject,
        SUM(duration_minutes) / 60.0
    ) FILTER (WHERE primary_subject IS NOT NULL) AS clh_by_subject,
    
    -- Quality metrics
    AVG(avg_latency_ms) AS avg_network_latency,
    SUM(disconnections) AS total_disconnections
FROM learning_sessions
WHERE ended_at IS NOT NULL
GROUP BY school_id, DATE_TRUNC('day', started_at);
```

## ML Training Data Schema

### Table: `ml_training_features`

```sql
CREATE TABLE ml_training_features (
    feature_id              BIGSERIAL PRIMARY KEY,
    created_at              TIMESTAMPTZ DEFAULT NOW(),
    
    -- Context
    device_id               VARCHAR(50) NOT NULL,
    timestamp               TIMESTAMPTZ NOT NULL,
    model_type              VARCHAR(50) NOT NULL,  -- solar, qos, anomaly
    
    -- Solar Features
    solar_features          JSONB,
    -- Example: {
    --   "hour_of_day": 14,
    --   "day_of_year": 180,
    --   "cloud_cover": 0.3,
    --   "temperature": 28.5,
    --   "humidity": 65,
    --   "historical_irradiance": [750, 780, 800, ...]
    -- }
    
    -- QoS Features
    qos_features            JSONB,
    -- Example: {
    --   "current_bandwidth": 5.2,
    --   "connected_users": 24,
    --   "time_of_day": "afternoon",
    --   "day_of_week": "Monday",
    --   "historical_usage": [...]
    -- }
    
    -- Anomaly Features
    anomaly_features        JSONB,
    
    -- Labels (for supervised learning)
    target_value            DOUBLE PRECISION,
    target_category         VARCHAR(50),
    is_anomaly              BOOLEAN,
    
    -- Data Quality
    feature_completeness    DOUBLE PRECISION,
    data_source             VARCHAR(50),
    validation_status       VARCHAR(20)
);

CREATE INDEX idx_training_model ON ml_training_features (model_type, timestamp DESC);
CREATE INDEX idx_training_device ON ml_training_features (device_id);
```

## Alerts & Events Schema

```sql
CREATE TABLE alerts (
    alert_id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id               VARCHAR(50) NOT NULL,
    
    -- Alert Details
    alert_type              VARCHAR(50) NOT NULL,  -- battery_low, network_down, etc.
    severity                VARCHAR(20) NOT NULL,  -- low, medium, high, critical
    title                   VARCHAR(255) NOT NULL,
    description             TEXT,
    
    -- Timing
    detected_at             TIMESTAMPTZ NOT NULL,
    resolved_at             TIMESTAMPTZ,
    acknowledged_at         TIMESTAMPTZ,
    acknowledged_by         VARCHAR(100),
    
    -- Context
    telemetry_snapshot      JSONB,
    affected_metrics        TEXT[],
    recommendations         TEXT[],
    
    -- Status
    status                  VARCHAR(20) DEFAULT 'active',  -- active, acknowledged, resolved
    auto_resolved           BOOLEAN DEFAULT false,
    
    created_at              TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_alerts_device ON alerts (device_id, detected_at DESC);
CREATE INDEX idx_alerts_status ON alerts (status) WHERE status = 'active';
CREATE INDEX idx_alerts_severity ON alerts (severity, detected_at DESC);
```

## Data Export Format (CSV)

### Telemetry Export

```csv
timestamp,device_id,solar_power_w,battery_soc_pct,network_bandwidth_mbps,connected_users,clh
2025-11-16T08:00:00Z,SLNK-001-KE,145.5,78,5.2,24,12.5
2025-11-16T09:00:00Z,SLNK-001-KE,167.2,82,5.8,28,15.3
2025-11-16T10:00:00Z,SLNK-001-KE,189.0,85,6.1,32,18.7
```

### Learning Analytics Export

```csv
date,school_id,total_clh,unique_learners,avg_engagement,primary_subjects
2025-11-16,SCH-001,156.5,45,0.78,"Math,Science,English"
2025-11-17,SCH-001,142.3,42,0.75,"Science,Geography,English"
```

## API Response Format (JSON)

### Telemetry Endpoint Response

```json
{
  "device_id": "SLNK-001-KE",
  "timestamp": "2025-11-16T10:30:00Z",
  "solar": {
    "panel_power": 145.5,
    "irradiance": 750,
    "panel_temp": 42.0
  },
  "battery": {
    "voltage": 12.6,
    "state_of_charge": 78,
    "health": 95
  },
  "network": {
    "signal_strength": -72,
    "bandwidth": 5.2,
    "connected_users": 24
  },
  "learning": {
    "active_sessions": 12,
    "connected_learning_hours": 156.5,
    "cache_hit_rate": 82
  }
}
```

### Solar Forecast Response

```json
{
  "device_id": "SLNK-001-KE",
  "forecast": [
    {
      "timestamp": "2025-11-16T11:00:00Z",
      "predicted_power": 178.5,
      "predicted_energy": 178.5,
      "confidence": 0.85
    }
  ],
  "confidence": 0.85,
  "metadata": {
    "model_version": "1.2.0",
    "features_used": ["panel_voltage", "irradiance", "temperature"],
    "weather_source": "OpenWeatherMap"
  }
}
```
