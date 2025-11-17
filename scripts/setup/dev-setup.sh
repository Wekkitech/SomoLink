#!/bin/bash
# SomoLink Development Environment Setup Script
# Sets up local development environment for all services

set -e

echo "🚀 SomoLink Development Environment Setup"
echo "==========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
check_prerequisites() {
    echo "📋 Checking prerequisites..."
    
    local missing_tools=()
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        missing_tools+=("Docker")
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        missing_tools+=("Docker Compose")
    fi
    
    # Check Node.js
    if ! command -v node &> /dev/null; then
        missing_tools+=("Node.js (v18+)")
    fi
    
    # Check Python
    if ! command -v python3 &> /dev/null; then
        missing_tools+=("Python (3.11+)")
    fi
    
    # Check Go
    if ! command -v go &> /dev/null; then
        missing_tools+=("Go (1.21+)")
    fi
    
    # Check pnpm (preferred) or npm
    if ! command -v pnpm &> /dev/null && ! command -v npm &> /dev/null; then
        missing_tools+=("pnpm or npm")
    fi
    
    if [ ${#missing_tools[@]} -ne 0 ]; then
        echo -e "${RED}❌ Missing required tools:${NC}"
        printf '%s\n' "${missing_tools[@]}"
        echo ""
        echo "Please install the missing tools and run this script again."
        exit 1
    fi
    
    echo -e "${GREEN}✓ All prerequisites met${NC}"
}

# Create necessary directories
create_directories() {
    echo ""
    echo "📁 Creating project directories..."
    
    mkdir -p data/postgres
    mkdir -p data/redis
    mkdir -p data/kafka
    mkdir -p data/mlflow
    mkdir -p logs
    mkdir -p models
    
    echo -e "${GREEN}✓ Directories created${NC}"
}

# Setup environment variables
setup_env() {
    echo ""
    echo "⚙️  Setting up environment variables..."
    
    if [ ! -f .env ]; then
        cp .env.example .env
        echo -e "${YELLOW}⚠ .env file created from .env.example${NC}"
        echo -e "${YELLOW}⚠ Please update .env with your actual values${NC}"
    else
        echo -e "${GREEN}✓ .env file already exists${NC}"
    fi
}

# Install Node.js dependencies
install_node_deps() {
    echo ""
    echo "📦 Installing Node.js dependencies..."
    
    # Use pnpm if available, otherwise npm
    if command -v pnpm &> /dev/null; then
        echo "Using pnpm..."
        pnpm install
    else
        echo "Using npm..."
        npm install
    fi
    
    echo -e "${GREEN}✓ Node.js dependencies installed${NC}"
}

# Install Python dependencies
install_python_deps() {
    echo ""
    echo "🐍 Installing Python dependencies..."
    
    # Create virtual environment if it doesn't exist
    if [ ! -d "venv" ]; then
        python3 -m venv venv
        echo -e "${GREEN}✓ Virtual environment created${NC}"
    fi
    
    # Activate virtual environment
    source venv/bin/activate
    
    # Upgrade pip
    pip install --upgrade pip
    
    # Install AI platform dependencies
    pip install -r services/ai-platform/requirements.txt
    
    # Install data ingestion dependencies
    pip install kafka-python psycopg2-binary redis prometheus-client
    
    echo -e "${GREEN}✓ Python dependencies installed${NC}"
}

# Install Go dependencies
install_go_deps() {
    echo ""
    echo "🔵 Installing Go dependencies..."
    
    cd services/edge-agent
    go mod download
    cd ../..
    
    echo -e "${GREEN}✓ Go dependencies installed${NC}"
}

# Setup database
setup_database() {
    echo ""
    echo "🗄️  Setting up databases..."
    
    # Start database services
    docker-compose up -d postgres redis
    
    # Wait for databases to be ready
    echo "Waiting for PostgreSQL to be ready..."
    sleep 5
    
    # Run migrations
    # (Would run actual migration scripts here)
    echo "Database migrations would run here in production"
    
    echo -e "${GREEN}✓ Databases setup complete${NC}"
}

# Start infrastructure services
start_infrastructure() {
    echo ""
    echo "🏗️  Starting infrastructure services..."
    
    docker-compose up -d postgres redis kafka prometheus grafana
    
    echo ""
    echo -e "${GREEN}✓ Infrastructure services started${NC}"
    echo ""
    echo "📊 Available services:"
    echo "   - PostgreSQL: localhost:5432"
    echo "   - Redis: localhost:6379"
    echo "   - Kafka: localhost:9092"
    echo "   - Prometheus: http://localhost:9090"
    echo "   - Grafana: http://localhost:3000 (admin/admin)"
}

# Generate development certificates
generate_certs() {
    echo ""
    echo "🔐 Generating development SSL certificates..."
    
    mkdir -p certs
    
    # Generate self-signed certificate
    if [ ! -f "certs/dev.crt" ]; then
        openssl req -x509 -newkey rsa:4096 -keyout certs/dev.key -out certs/dev.crt \
            -days 365 -nodes -subj "/CN=localhost"
        echo -e "${GREEN}✓ Development certificates generated${NC}"
    else
        echo -e "${GREEN}✓ Certificates already exist${NC}"
    fi
}

# Print next steps
print_next_steps() {
    echo ""
    echo "==========================================="
    echo -e "${GREEN}✅ Setup Complete!${NC}"
    echo "==========================================="
    echo ""
    echo "🎯 Next Steps:"
    echo ""
    echo "1. Update .env file with your configuration"
    echo "2. Start the services:"
    echo "   - API Gateway:       cd services/api-gateway && pnpm run start:dev"
    echo "   - AI Platform:       cd services/ai-platform && uvicorn api.main:app --reload"
    echo "   - School Dashboard:  cd apps/school-dashboard && pnpm dev"
    echo "   - Edge Agent:        cd services/edge-agent && go run cmd/main.go"
    echo ""
    echo "3. Access the applications:"
    echo "   - API Docs:    http://localhost:8000/docs"
    echo "   - Dashboard:   http://localhost:3000"
    echo "   - Prometheus:  http://localhost:9090"
    echo "   - Grafana:     http://localhost:3000 (admin/admin)"
    echo ""
    echo "📚 Documentation: ./docs/guides/developer-guide.md"
    echo ""
}

# Main execution
main() {
    check_prerequisites
    create_directories
    setup_env
    install_node_deps
    install_python_deps
    install_go_deps
    generate_certs
    setup_database
    start_infrastructure
    print_next_steps
}

# Run main function
main
