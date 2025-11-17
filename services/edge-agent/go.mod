module github.com/wekkitech/somolink/services/edge-agent

go 1.21

require (
	github.com/spf13/cobra v1.8.0
	github.com/spf13/viper v1.18.2
	go.uber.org/zap v1.26.0
	github.com/prometheus/client_golang v1.18.0
	github.com/gorilla/mux v1.8.1
	google.golang.org/grpc v1.60.1
	google.golang.org/protobuf v1.31.0
	github.com/golang/protobuf v1.5.3
	github.com/pkg/errors v0.9.1
	gopkg.in/yaml.v3 v3.0.1
)

require (
	// HTTP Client
	github.com/go-resty/resty/v2 v2.11.0
	
	// Database (SQLite for local cache/analytics)
	github.com/mattn/go-sqlite3 v1.14.19
	modernc.org/sqlite v1.28.0
	
	// MQTT (for IoT device communication)
	github.com/eclipse/paho.mqtt.golang v1.4.3
	
	// Serial Port Communication (MPPT controller)
	github.com/tarm/serial v0.0.0-20180830185346-98f6abe2eb07
	go.bug.st/serial v1.6.1
	
	// System Monitoring
	github.com/shirou/gopsutil/v3 v3.23.12
	
	// Configuration
	github.com/fsnotify/fsnotify v1.7.0
	github.com/hashicorp/hcl v1.0.0
	github.com/magiconair/properties v1.8.7
	github.com/mitchellh/mapstructure v1.5.0
	github.com/pelletier/go-toml/v2 v2.1.1
	github.com/sagikazarmark/locafero v0.4.0
	github.com/sagikazarmark/slog-shim v0.1.0
	github.com/sourcegraph/conc v0.3.0
	github.com/subosito/gotenv v1.6.0
	
	// Logging
	go.uber.org/multierr v1.11.0
	
	// Testing
	github.com/stretchr/testify v1.8.4
	github.com/davecgh/go-spew v1.1.2-0.20180830191138-d8f796af33cc
	github.com/pmezard/go-difflib v1.0.1-0.20181226105442-5d4384ee4fb2
	
	// Networking
	golang.org/x/net v0.19.0
	golang.org/x/sys v0.15.0
	golang.org/x/text v0.14.0
	
	// Metrics
	github.com/beorn7/perks v1.0.1
	github.com/cespare/xxhash/v2 v2.2.0
	github.com/matttproud/golang_protobuf_extensions/v2 v2.0.0
	github.com/prometheus/client_model v0.5.0
	github.com/prometheus/common v0.45.0
	github.com/prometheus/procfs v0.12.0
	
	// gRPC
	github.com/inconshreveable/mousetrap v1.1.0
	github.com/spf13/pflag v1.0.5
	google.golang.org/genproto/googleapis/rpc v0.0.0-20231212172506-995d672761c0
	
	// Utilities
	github.com/google/uuid v1.5.0
	github.com/robfig/cron/v3 v3.0.1
	
	// Cache
	github.com/patrickmn/go-cache v2.1.0+incompatible
	github.com/dgraph-io/ristretto v0.1.1
)

require (
	// ModemManager integration
	github.com/maltegrosse/go-modemmanager v0.1.0
	
	// Network management
	github.com/vishvananda/netlink v1.1.0
	github.com/vishvananda/netns v0.0.4
	
	// JSON processing
	github.com/tidwall/gjson v1.17.0
	github.com/tidwall/sjson v1.2.5
)
