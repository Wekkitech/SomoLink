# SomoLink 🌍⚡📡

[![CI/CD](https://github.com/wekkitech/somolink/workflows/CI/badge.svg)](https://github.com/wekkitech/somolink/actions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**AI-Powered, Solar-Driven Digital Learning Infrastructure**

Developed by [Wekkitech Limited](https://wekkitech.co.ke), Kenya

## 🎯 Mission

SomoLink provides reliable, intelligent, and community-sustained internet access to schools and nearby communities in underserved regions across Kenya and East Africa.

## 🏗️ Architecture Overview

SomoLink operates on four main layers:

1. **Edge Layer** - Solar-powered devices providing Wi-Fi, content caching, and telemetry
2. **AI & Data Layer** - ML models for solar forecasting, QoS optimization, and analytics
3. **Backend Services** - APIs for billing, data ingestion, and federated learning
4. **Frontend Layer** - Dashboards for schools, communities, and operators

## 📦 Monorepo Structure

```
somolink/
├── services/               # Backend microservices
│   ├── edge-agent/        # Go-based edge device software
│   ├── ai-platform/       # Python ML services
│   ├── api-gateway/       # NestJS API gateway
│   ├── billing/           # Billing and payments service
│   └── data-ingestion/    # Telemetry and analytics ingestion
├── apps/                  # Frontend applications
│   ├── school-dashboard/  # Next.js school interface
│   ├── admin-dashboard/   # Admin and operator dashboard
│   └── community-portal/  # Public community access portal
├── infrastructure/        # IaC and deployment configs
│   ├── terraform/         # Cloud infrastructure
│   ├── kubernetes/        # K8s manifests
│   └── helm/              # Helm charts
├── libs/                  # Shared libraries
│   ├── shared-types/      # TypeScript type definitions
│   ├── api-clients/       # Auto-generated API clients
│   └── utils/             # Common utilities
├── docs/                  # Documentation
└── scripts/               # Build and deployment scripts
```

## 🚀 Quick Start

### Prerequisites

- **Docker** 20.10+
- **Node.js** 18+ and **pnpm** 8+
- **Python** 3.11+
- **Go** 1.21+
- **Kubernetes** cluster (local: minikube/kind, cloud: GKE/EKS/AKS)
- **Terraform** 1.5+

### Local Development Setup

```bash
# Clone the repository
git clone https://github.com/wekkitech/somolink.git
cd somolink

# Run setup script
./scripts/setup/dev-environment.sh

# Start core services with Docker Compose
docker-compose up -d postgres redis kafka

# Install dependencies
pnpm install              # Frontend apps
pip install -r requirements.txt  # AI services
cd services/edge-agent && go mod download  # Edge agent

# Start development servers
pnpm dev                  # Starts all frontend apps
```

### Running Tests

```bash
# Run all tests
pnpm test

# Run service-specific tests
cd services/ai-platform && pytest
cd services/edge-agent && go test ./...
```

## 🧠 Core AI Capabilities

| Function | Model | Technology |
|----------|-------|------------|
| Solar Forecasting | LSTM + Weather APIs | PyTorch |
| QoS Optimization | Contextual Bandits | LightGBM |
| Anomaly Detection | Isolation Forest | scikit-learn |
| Learning Analytics | CLH Computation | Pandas + SQL |
| Content Filtering | Text Classification | FastText |
| Federated Learning | FL Orchestration | Flower Framework |

## 🔐 Security

- OAuth2 + JWT authentication
- Role-Based Access Control (RBAC)
- End-to-end HTTPS encryption
- OpenTelemetry observability
- Regular security audits

## 📊 Monitoring

- **Metrics**: Prometheus + Grafana
- **Logs**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Traces**: Jaeger (OpenTelemetry)
- **Alerts**: AlertManager

## 📚 Documentation

- [Architecture Overview](docs/architecture/overview.md)
- [API Documentation](docs/api/README.md)
- [Deployment Guide](docs/deployment/README.md)
- [Developer Guide](docs/guides/developer-guide.md)

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md).

## 📄 License

Apache 2.0 - see [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

Supported by:
- Kenya Education Network (KENET)
- Digital Opportunity Trust (DOT)
- Renewable Energy Ventures

---

**Built with ❤️ in Nairobi, Kenya**
