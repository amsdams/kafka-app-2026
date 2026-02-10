#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Kafka Microservices - Build & Deploy${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to print colored messages
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}→ $1${NC}"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi
print_success "Docker is running"

# Clean previous build
print_info "Cleaning previous build..."
mvn clean > /dev/null 2>&1
print_success "Build cleaned"

# Build with Maven
print_info "Building with Maven..."
if mvn install -DskipTests; then
    print_success "Maven build successful"
else
    print_error "Maven build failed"
    exit 1
fi

# Stop existing containers
print_info "Stopping existing containers..."
docker-compose down -v > /dev/null 2>&1
print_success "Existing containers stopped"

# Build and start services
print_info "Building Docker images and starting services..."
if docker-compose up --build -d; then
    print_success "Services started successfully"
else
    print_error "Failed to start services"
    exit 1
fi

# Wait for services to be healthy
print_info "Waiting for services to be healthy..."
sleep 10

# Check Kafka
print_info "Checking Kafka..."
timeout 30 bash -c 'until docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 2>/dev/null; do sleep 2; done'
if [ $? -eq 0 ]; then
    print_success "Kafka is ready"
else
    print_error "Kafka failed to start"
    exit 1
fi

# Check Producer
print_info "Checking Producer Service..."
timeout 30 bash -c 'until curl -s http://localhost:8081/actuator/health | grep -q "UP"; do sleep 2; done'
if [ $? -eq 0 ]; then
    print_success "Producer Service is healthy"
else
    print_error "Producer Service is not healthy"
fi

# Check Consumer
print_info "Checking Consumer Service..."
timeout 30 bash -c 'until curl -s http://localhost:8082/actuator/health | grep -q "UP"; do sleep 2; done'
if [ $? -eq 0 ]; then
    print_success "Consumer Service is healthy"
else
    print_error "Consumer Service is not healthy"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${BLUE}Services:${NC}"
echo "  • Producer:  http://localhost:8081"
echo "  • Consumer:  http://localhost:8082"
echo "  • Kafka:     localhost:29092"
echo ""
echo -e "${BLUE}Health Checks:${NC}"
echo "  • Producer:  http://localhost:8081/actuator/health"
echo "  • Consumer:  http://localhost:8082/actuator/health"
echo ""
echo -e "${BLUE}API Documentation:${NC}"
echo "  • Swagger UI: http://localhost:8081/swagger-ui.html"
echo ""
echo -e "${BLUE}Metrics:${NC}"
echo "  • Producer:  http://localhost:8081/actuator/prometheus"
echo "  • Consumer:  http://localhost:8082/actuator/prometheus"
echo ""
echo -e "${YELLOW}To view logs:${NC}"
echo "  docker-compose logs -f"
echo ""
echo -e "${YELLOW}To stop services:${NC}"
echo "  docker-compose down"
echo ""
echo -e "${YELLOW}To enable monitoring (Prometheus + Grafana):${NC}"
echo "  docker-compose --profile monitoring up -d"
echo "  • Prometheus: http://localhost:9090"
echo "  • Grafana:    http://localhost:3000 (admin/admin)"
echo ""
