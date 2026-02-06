#!/bin/bash

echo "🚀 Starting Spring Boot Microservices with Kafka..."
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Clean up old containers
echo "🧹 Cleaning up old containers..."
docker-compose down -v

echo ""
echo "🏗️  Building and starting services..."
echo "   - Zookeeper"
echo "   - Kafka Broker"
echo "   - Producer Service (port 8081)"
echo "   - Consumer Service (port 8082)"
echo ""

# Build and start services
docker-compose up --build -d

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 15

# Check health
echo ""
echo "🏥 Checking service health..."

PRODUCER_HEALTH=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/events/health)
CONSUMER_HEALTH=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/api/consumer/health)

if [ "$PRODUCER_HEALTH" == "200" ]; then
    echo "✅ Producer Service is healthy"
else
    echo "❌ Producer Service is not responding (status: $PRODUCER_HEALTH)"
fi

if [ "$CONSUMER_HEALTH" == "200" ]; then
    echo "✅ Consumer Service is healthy"
else
    echo "❌ Consumer Service is not responding (status: $CONSUMER_HEALTH)"
fi

echo ""
echo "📊 Service URLs:"
echo "   Producer: http://localhost:8081/api/events/health"
echo "   Consumer: http://localhost:8082/api/consumer/health"
echo ""
echo "📝 Test the services with:"
echo '   curl -X POST http://localhost:8081/api/events/publish \'
echo '     -H "Content-Type: application/json" \'
echo '     -d '"'"'{"username":"test_user","email":"test@example.com","eventType":"USER_CREATED"}'"'"
echo ""
echo "📋 View logs with:"
echo "   docker-compose logs -f"
echo ""
echo "🛑 Stop services with:"
echo "   docker-compose down"
echo ""
echo "✨ All done! Services are running."
