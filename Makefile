.PHONY: help build up down logs clean test

# Default target
help:
	@echo "Kafka Microservices - Available Commands"
	@echo "========================================="
	@echo "make build        - Build Maven project"
	@echo "make up           - Start all services"
	@echo "make down         - Stop all services"
	@echo "make restart      - Restart all services"
	@echo "make logs         - View logs"
	@echo "make clean        - Clean everything"
	@echo "make test         - Send test messages"
	@echo "make monitoring   - Start with Prometheus & Grafana"
	@echo "make status       - Check service status"

# Build Maven project
build:
	@echo "Building Maven project..."
	@mvn clean install -DskipTests

# Start services
up: build
	@echo "Starting services..."
	@docker-compose up --build -d
	@echo "Waiting for services to be healthy..."
	@sleep 15
	@make status

# Stop services
down:
	@echo "Stopping services..."
	@docker-compose down -v

# Restart services
restart: down up

# View logs
logs:
	@docker-compose logs -f

# Clean everything
clean:
	@echo "Cleaning..."
	@mvn clean
	@docker-compose down -v
	@docker system prune -f

# Send test messages
test:
	@echo "Sending test user event..."
	@curl -X POST http://localhost:8081/api/producer/users \
		-H "Content-Type: application/json" \
		-d '{"username":"test-user","email":"test@example.com","eventType":"USER_CREATED"}' \
		| jq .
	@echo ""
	@echo "Sending test order event..."
	@curl -X POST http://localhost:8081/api/producer/orders \
		-H "Content-Type: application/json" \
		-d '{"userId":"user-123","productName":"Test Product","amount":99.99,"eventType":"ORDER_CREATED"}' \
		| jq .

# Start with monitoring
monitoring: build
	@echo "Starting services with monitoring..."
	@docker-compose --profile monitoring up --build -d
	@echo "Waiting for services to be healthy..."
	@sleep 20
	@make status
	@echo ""
	@echo "Monitoring URLs:"
	@echo "  Prometheus: http://localhost:9090"
	@echo "  Grafana:    http://localhost:3000 (admin/admin)"

# Check service status
status:
	@echo "Service Status:"
	@echo "==============="
	@docker-compose ps
	@echo ""
	@echo "Health Checks:"
	@echo "=============="
	@echo -n "Kafka:    "
	@docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1 && echo "✓ OK" || echo "✗ FAIL"
	@echo -n "Producer: "
	@curl -s http://localhost:8081/actuator/health | grep -q "UP" && echo "✓ UP" || echo "✗ DOWN"
	@echo -n "Consumer: "
	@curl -s http://localhost:8082/actuator/health | grep -q "UP" && echo "✓ UP" || echo "✗ DOWN"
