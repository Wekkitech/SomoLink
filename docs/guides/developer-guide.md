# Developer Onboarding Guide

Welcome to the SomoLink development team! This guide will help you get started with the codebase and development workflow.

## 📋 Prerequisites

Before you begin, ensure you have the following installed on your development machine:

### Required Software

- **Git** 2.40+
- **Docker** 20.10+
- **Docker Compose** 2.0+
- **Node.js** 18+ and **pnpm** 8+
- **Python** 3.11+
- **Go** 1.21+
- **PostgreSQL Client** (psql)
- **VS Code** or your preferred IDE

### Recommended Tools

- **k9s** (Kubernetes CLI)
- **kubectl** (if working with Kubernetes)
- **Postman** or **Insomnia** (API testing)
- **TablePlus** or **pgAdmin** (Database GUI)
- **Redis Commander** (Redis GUI)

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/wekkitech/somolink.git
cd somolink
```

### 2. Environment Setup

```bash
# Copy environment template
cp .env.example .env

# Edit .env with your local configuration
# Most defaults work for local development
nano .env
```

### 3. Install Dependencies

```bash
# Install Node.js dependencies (frontend + API gateway)
pnpm install

# Install Python dependencies (AI platform)
cd services/ai-platform
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
cd ../..

# Install Go dependencies (Edge agent)
cd services/edge-agent
go mod download
cd ../..
```

### 4. Start Development Services

```bash
# Start core infrastructure (PostgreSQL, Redis, Kafka, etc.)
docker-compose up -d postgres redis kafka timescaledb minio prometheus grafana

# Wait for services to be healthy (about 30 seconds)
docker-compose ps

# Initialize databases
./scripts/setup/init-databases.sh
```

### 5. Start Development Servers

#### Terminal 1: API Gateway

```bash
cd services/api-gateway
pnpm run start:dev
# Runs on http://localhost:3001
```

#### Terminal 2: AI Platform

```bash
cd services/ai-platform
source venv/bin/activate
uvicorn api.main:app --reload --port 8000
# Runs on http://localhost:8000
```

#### Terminal 3: School Dashboard

```bash
cd apps/school-dashboard
pnpm run dev
# Runs on http://localhost:3000
```

#### Terminal 4: Edge Agent (Optional)

```bash
cd services/edge-agent
go run cmd/main.go --config configs/dev.yaml
```

### 6. Verify Installation

Visit the following URLs to confirm everything is running:

- **School Dashboard**: http://localhost:3000
- **API Gateway**: http://localhost:3001/health
- **AI Platform**: http://localhost:8000/docs (FastAPI Swagger)
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Kafka UI**: http://localhost:8080 (if using kafka-ui)

## 🏗️ Project Structure Overview

```
somolink/
├── services/           # Backend microservices
│   ├── edge-agent/    # Go-based edge device software
│   ├── ai-platform/   # Python ML services
│   ├── api-gateway/   # NestJS API gateway
│   ├── billing/       # Billing service
│   └── data-ingestion/# Telemetry ingestion
│
├── apps/              # Frontend applications
│   ├── school-dashboard/
│   ├── admin-dashboard/
│   └── community-portal/
│
├── libs/              # Shared libraries
│   ├── shared-types/  # TypeScript types
│   └── api-clients/   # Auto-generated clients
│
├── infrastructure/    # IaC and K8s configs
├── docs/             # Documentation
└── scripts/          # Utility scripts
```

## 🛠️ Development Workflow

### Git Workflow

We follow **Git Flow**:

1. **main** - Production-ready code
2. **develop** - Integration branch
3. **feature/** - Feature branches
4. **hotfix/** - Emergency fixes

#### Creating a Feature Branch

```bash
# Update develop
git checkout develop
git pull origin develop

# Create feature branch
git checkout -b feature/your-feature-name

# Make changes, commit
git add .
git commit -m "feat: add new solar forecasting model"

# Push and create PR
git push origin feature/your-feature-name
```

### Commit Message Convention

We use **Conventional Commits**:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code style (formatting)
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance tasks

**Examples**:

```bash
feat(api-gateway): add JWT authentication middleware
fix(edge-agent): resolve battery telemetry parsing error
docs(architecture): update deployment topology diagram
```

### Code Quality Checks

Before committing, run:

```bash
# Lint all code
pnpm lint

# Format code
pnpm format

# Run tests
pnpm test

# Type check TypeScript
pnpm type-check
```

We use **pre-commit hooks** to enforce quality:

```bash
# Install pre-commit hooks
pip install pre-commit
pre-commit install
```

## 🧪 Testing

### Unit Tests

```bash
# API Gateway (Jest)
cd services/api-gateway
pnpm test

# AI Platform (pytest)
cd services/ai-platform
pytest

# Edge Agent (Go test)
cd services/edge-agent
go test ./...
```

### Integration Tests

```bash
# Start test environment
docker-compose -f docker-compose.test.yml up -d

# Run integration tests
pnpm test:integration
```

### E2E Tests

```bash
cd apps/school-dashboard
pnpm test:e2e
```

## 🗄️ Database Management

### Migrations

#### PostgreSQL (Prisma)

```bash
cd services/api-gateway

# Create migration
npx prisma migrate dev --name add-user-table

# Apply migrations
npx prisma migrate deploy

# Generate Prisma Client
npx prisma generate
```

#### Python (Alembic)

```bash
cd services/ai-platform

# Create migration
alembic revision --autogenerate -m "add training features table"

# Apply migrations
alembic upgrade head
```

### Accessing Databases

```bash
# PostgreSQL
docker exec -it somolink-postgres psql -U somolink -d somolink

# TimescaleDB
docker exec -it somolink-timescale psql -U somolink -d telemetry

# Redis
docker exec -it somolink-redis redis-cli
```

## 🔍 Debugging

### API Gateway (NestJS)

```bash
# Debug mode
cd services/api-gateway
pnpm run start:debug

# Attach VS Code debugger
# Use launch.json configuration
```

### AI Platform (Python)

```bash
# Enable debug logging
export DEBUG=1
export LOG_LEVEL=debug

# Run with debugger
python -m pdb api/main.py
```

### Edge Agent (Go)

```bash
# Run with Delve debugger
dlv debug ./cmd/main.go -- --config configs/dev.yaml
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f api-gateway

# Kubernetes (if deployed)
kubectl logs -f deployment/api-gateway -n development
```

## 📊 Monitoring & Metrics

### Local Development

- **Prometheus**: http://localhost:9090
  - View metrics, create queries
  
- **Grafana**: http://localhost:3000
  - Pre-configured dashboards
  - Username: `admin`, Password: `admin`
  
- **Jaeger**: http://localhost:16686
  - Distributed tracing

### Useful Metrics Queries

**API Request Rate**:
```promql
rate(http_requests_total[5m])
```

**Solar Power Average**:
```promql
avg(solar_panel_power) by (device_id)
```

**P95 Latency**:
```promql
histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))
```

## 🔐 Security Best Practices

1. **Never commit secrets** - Use `.env` files (gitignored)
2. **Use environment variables** for all configuration
3. **Rotate API keys** regularly
4. **Enable 2FA** on GitHub account
5. **Review dependencies** for vulnerabilities:

```bash
# Node.js
pnpm audit

# Python
pip-audit

# Go
go list -json -m all | nancy sleuth
```

## 🤝 Getting Help

### Internal Resources

- **Documentation**: `/docs` directory
- **Architecture Diagrams**: `/docs/architecture`
- **API Specs**: `/docs/api`

### Team Communication

- **Slack**: #somolink-dev channel
- **Weekly Standups**: Monday 10 AM EAT
- **Code Reviews**: GitHub Pull Requests
- **Design Discussions**: GitHub Discussions

### External Resources

- [NestJS Documentation](https://docs.nestjs.com/)
- [FastAPI Documentation](https://fastapi.tiangolo.com/)
- [Next.js Documentation](https://nextjs.org/docs)
- [Go Documentation](https://go.dev/doc/)

## 📝 Common Tasks

### Adding a New API Endpoint

1. Define route in API Gateway
2. Add controller method
3. Update OpenAPI spec
4. Write unit tests
5. Update documentation

### Adding a New ML Model

1. Create model class in `services/ai-platform/models/`
2. Add training script in `training/`
3. Register in MLflow
4. Add API endpoint
5. Update model serving config

### Updating Environment Variables

1. Update `.env.example`
2. Update K8s secrets/configmaps
3. Update documentation
4. Notify team in Slack

## 🎯 Next Steps

Now that you're set up, here are some good first tasks:

1. **Fix a "good first issue"** - Check GitHub issues labeled `good-first-issue`
2. **Improve documentation** - Update outdated docs
3. **Write tests** - Add tests to improve coverage
4. **Review code** - Participate in PR reviews

## 🐛 Troubleshooting

### Port Already in Use

```bash
# Find and kill process
lsof -ti:3000 | xargs kill -9
```

### Database Connection Failed

```bash
# Restart PostgreSQL
docker-compose restart postgres

# Check logs
docker-compose logs postgres
```

### Docker Out of Space

```bash
# Clean up
docker system prune -a
docker volume prune
```

### Python Module Not Found

```bash
# Reinstall dependencies
pip install -r requirements.txt --force-reinstall
```

---

**Welcome aboard! 🚀**

If you have any questions, reach out to the team lead or post in #somolink-dev.
