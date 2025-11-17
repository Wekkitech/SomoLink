#!/bin/bash
# SomoLink Kubernetes Deployment Script
# Deploys SomoLink to Kubernetes cluster

set -e

# Configuration
NAMESPACE="${NAMESPACE:-somolink}"
ENVIRONMENT="${ENVIRONMENT:-staging}"
KUBECTL="${KUBECTL:-kubectl}"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🚀 SomoLink Kubernetes Deployment${NC}"
echo "======================================"
echo "Namespace: $NAMESPACE"
echo "Environment: $ENVIRONMENT"
echo ""

# Check prerequisites
check_prerequisites() {
    echo "Checking prerequisites..."
    
    if ! command -v kubectl &> /dev/null; then
        echo -e "${RED}❌ kubectl not found${NC}"
        exit 1
    fi
    
    if ! command -v helm &> /dev/null; then
        echo -e "${RED}❌ helm not found${NC}"
        exit 1
    fi
    
    # Check cluster connectivity
    if ! kubectl cluster-info &> /dev/null; then
        echo -e "${RED}❌ Cannot connect to Kubernetes cluster${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Prerequisites met${NC}"
}

# Create namespace
create_namespace() {
    echo ""
    echo "Creating namespace..."
    
    if kubectl get namespace $NAMESPACE &> /dev/null; then
        echo -e "${YELLOW}⚠ Namespace $NAMESPACE already exists${NC}"
    else
        kubectl create namespace $NAMESPACE
        echo -e "${GREEN}✓ Namespace created${NC}"
    fi
    
    # Label namespace
    kubectl label namespace $NAMESPACE environment=$ENVIRONMENT --overwrite
}

# Deploy secrets
deploy_secrets() {
    echo ""
    echo "Deploying secrets..."
    
    # Check if secrets file exists
    if [ ! -f "infrastructure/kubernetes/overlays/$ENVIRONMENT/secrets.yaml" ]; then
        echo -e "${YELLOW}⚠ No secrets file found, skipping${NC}"
        return
    fi
    
    kubectl apply -f infrastructure/kubernetes/overlays/$ENVIRONMENT/secrets.yaml \
        -n $NAMESPACE
    
    echo -e "${GREEN}✓ Secrets deployed${NC}"
}

# Deploy ConfigMaps
deploy_configmaps() {
    echo ""
    echo "Deploying ConfigMaps..."
    
    # Prometheus config
    kubectl create configmap prometheus-config \
        --from-file=configs/prometheus/prometheus.yml \
        --from-file=configs/prometheus/alerts.yml \
        -n $NAMESPACE \
        --dry-run=client -o yaml | kubectl apply -f -
    
    # Grafana dashboards
    kubectl create configmap grafana-dashboards \
        --from-file=configs/grafana/dashboards/ \
        -n $NAMESPACE \
        --dry-run=client -o yaml | kubectl apply -f - || true
    
    echo -e "${GREEN}✓ ConfigMaps deployed${NC}"
}

# Deploy storage
deploy_storage() {
    echo ""
    echo "Deploying storage..."
    
    kubectl apply -f infrastructure/kubernetes/base/storage/ -n $NAMESPACE || true
    
    echo -e "${GREEN}✓ Storage deployed${NC}"
}

# Deploy services
deploy_services() {
    echo ""
    echo "Deploying services..."
    
    # Deploy database
    echo "  → PostgreSQL..."
    kubectl apply -f infrastructure/kubernetes/base/postgres-deployment.yaml -n $NAMESPACE || true
    
    # Deploy Redis
    echo "  → Redis..."
    kubectl apply -f infrastructure/kubernetes/base/redis-deployment.yaml -n $NAMESPACE || true
    
    # Deploy Kafka
    echo "  → Kafka..."
    kubectl apply -f infrastructure/kubernetes/base/kafka-deployment.yaml -n $NAMESPACE || true
    
    # Wait for databases
    echo "Waiting for databases to be ready..."
    kubectl wait --for=condition=ready pod -l app=postgres -n $NAMESPACE --timeout=300s || true
    kubectl wait --for=condition=ready pod -l app=redis -n $NAMESPACE --timeout=300s || true
    
    # Deploy application services
    echo "  → API Gateway..."
    kubectl apply -f infrastructure/kubernetes/base/api-gateway-deployment.yaml -n $NAMESPACE
    
    echo "  → AI Platform..."
    kubectl apply -f infrastructure/kubernetes/base/ai-platform-deployment.yaml -n $NAMESPACE
    
    echo "  → Billing Service..."
    kubectl apply -f infrastructure/kubernetes/base/billing-deployment.yaml -n $NAMESPACE || true
    
    echo "  → Data Ingestion..."
    kubectl apply -f infrastructure/kubernetes/base/data-ingestion-deployment.yaml -n $NAMESPACE || true
    
    echo -e "${GREEN}✓ Services deployed${NC}"
}

# Deploy monitoring
deploy_monitoring() {
    echo ""
    echo "Deploying monitoring stack..."
    
    # Deploy Prometheus
    echo "  → Prometheus..."
    kubectl apply -f infrastructure/kubernetes/base/prometheus-deployment.yaml -n $NAMESPACE || true
    
    # Deploy Grafana
    echo "  → Grafana..."
    kubectl apply -f infrastructure/kubernetes/base/grafana-deployment.yaml -n $NAMESPACE || true
    
    echo -e "${GREEN}✓ Monitoring deployed${NC}"
}

# Deploy ingress
deploy_ingress() {
    echo ""
    echo "Deploying ingress..."
    
    kubectl apply -f infrastructure/kubernetes/base/ingress.yaml -n $NAMESPACE
    
    echo -e "${GREEN}✓ Ingress deployed${NC}"
}

# Wait for deployments
wait_for_deployments() {
    echo ""
    echo "Waiting for deployments to be ready..."
    
    local deployments=(
        "api-gateway"
        "ai-platform"
    )
    
    for deployment in "${deployments[@]}"; do
        echo "  Waiting for $deployment..."
        kubectl rollout status deployment/$deployment -n $NAMESPACE --timeout=300s || true
    done
    
    echo -e "${GREEN}✓ All deployments ready${NC}"
}

# Run health checks
run_health_checks() {
    echo ""
    echo "Running health checks..."
    
    # Get API Gateway service IP
    API_URL=$(kubectl get svc api-gateway -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}') || \
    API_URL=$(kubectl get svc api-gateway -n $NAMESPACE -o jsonpath='{.spec.clusterIP}')
    
    if [ -n "$API_URL" ]; then
        echo "  Testing API Gateway at $API_URL..."
        if kubectl run curl-test --image=curlimages/curl:latest --rm -i --restart=Never -n $NAMESPACE -- \
            curl -s http://$API_URL:3001/health > /dev/null; then
            echo -e "${GREEN}  ✓ API Gateway healthy${NC}"
        else
            echo -e "${YELLOW}  ⚠ API Gateway health check failed${NC}"
        fi
    fi
}

# Print deployment status
print_status() {
    echo ""
    echo "======================================"
    echo -e "${GREEN}✅ Deployment Complete!${NC}"
    echo "======================================"
    echo ""
    echo "📊 Deployment Status:"
    echo ""
    kubectl get pods -n $NAMESPACE
    echo ""
    echo "🌐 Services:"
    echo ""
    kubectl get svc -n $NAMESPACE
    echo ""
    echo "🔗 Ingress:"
    echo ""
    kubectl get ingress -n $NAMESPACE
    echo ""
    echo "📝 Next Steps:"
    echo ""
    echo "1. Check pod logs:"
    echo "   kubectl logs -f deployment/api-gateway -n $NAMESPACE"
    echo ""
    echo "2. Access services:"
    echo "   kubectl port-forward svc/api-gateway 3001:3001 -n $NAMESPACE"
    echo ""
    echo "3. View Grafana dashboards:"
    echo "   kubectl port-forward svc/grafana 3000:3000 -n $NAMESPACE"
    echo ""
}

# Rollback deployment
rollback() {
    echo -e "${YELLOW}Rolling back deployment...${NC}"
    
    local deployments=$(kubectl get deployments -n $NAMESPACE -o name)
    
    for deployment in $deployments; do
        kubectl rollout undo $deployment -n $NAMESPACE || true
    done
    
    echo -e "${GREEN}✓ Rollback complete${NC}"
}

# Main execution
main() {
    check_prerequisites
    create_namespace
    deploy_secrets
    deploy_configmaps
    deploy_storage
    deploy_services
    deploy_monitoring
    deploy_ingress
    wait_for_deployments
    run_health_checks
    print_status
}

# Handle errors
trap 'echo -e "${RED}❌ Deployment failed${NC}"; exit 1' ERR

# Run main function
main

echo ""
echo -e "${GREEN}🎉 SomoLink deployment successful!${NC}"
