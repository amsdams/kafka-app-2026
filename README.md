# Spring Boot Microservices with Kafka

A complete microservices architecture with Kafka messaging, featuring producer and consumer services, fully containerized with Docker.

## 📋 Architecture

```
┌─────────────────────┐      ┌──────────────┐      ┌─────────────────────┐
│  Producer Service   │─────▶│    Kafka     │─────▶│  Consumer Service   │
│    (Port 8081)      │      │   Broker     │      │    (Port 8082)      │
└─────────────────────┘      └──────────────┘      └─────────────────────┘
```

## 🛠️ Technologies

- **Spring Boot 4.0.2** ⚡ (Upgraded from 3.x)
- **Spring Kafka** (Latest compatible version)
- **Apache Kafka 7.5.0**
- **Docker & Docker Compose**
- **Maven**
- **Java 25**
- **GitHub Actions** (CI/CD pipelines)
- **Dependabot** (Automated dependency updates)

## 📁 Project Structure

```
.
├── docker-compose.yml
├── producer-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/example/producer/
│           │   ├── ProducerServiceApplication.java
│           │   ├── config/KafkaTopicConfig.java
│           │   ├── controller/ProducerController.java
│           │   ├── model/UserEvent.java
│           │   └── service/KafkaProducerService.java
│           └── resources/
│               └── application.yml
└── consumer-service/
    ├── Dockerfile
    ├── pom.xml
    └── src/
        └── main/
            ├── java/com/example/consumer/
            │   ├── ConsumerServiceApplication.java
            │   ├── config/KafkaConsumerConfig.java
            │   ├── controller/ConsumerController.java
            │   ├── model/UserEvent.java
            │   └── service/KafkaConsumerService.java
            └── resources/
                └── application.yml
```

## 🚀 Getting Started

### Prerequisites

- Docker and Docker Compose installed
- Java 25+ (for local development)
- Maven 3.6+ (for local development)

### Running with Docker Compose

1. **Build and start all services:**

```bash
docker-compose up --build
```

This will start:
- Kafka broker (ports 9092, 29092)
- Producer service (port 8081)
- Consumer service (port 8082)

2. **Check services are running:**

```bash
# Check producer
curl http://localhost:8081/api/events/health

# Check consumer
curl http://localhost:8082/api/consumer/health
```

## 📤 Testing the Services

### 1. Publish a message to Kafka

```bash
curl -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "eventType": "USER_CREATED"
  }'
```

### 2. Check consumer logs

The consumer service will automatically process the message. Check logs:

```bash
docker logs -f consumer-service
```

You should see output like:
```
Received message from partition: 0, offset: 0
Processing event: UserEvent(id=..., username=john_doe, email=john@example.com, ...)
Processing user event for user: john_doe with event type: USER_CREATED
Message acknowledged successfully
```

### 3. Multiple messages test

```bash
# Send multiple events
for i in {1..5}; do
  curl -X POST http://localhost:8081/api/events/publish \
    -H "Content-Type: application/json" \
    -d "{
      \"username\": \"user_$i\",
      \"email\": \"user$i@example.com\",
      \"eventType\": \"USER_REGISTERED\"
    }"
  echo ""
  sleep 1
done
```

## 🔧 Configuration

### Kafka Topic Configuration

- **Topic name:** `user-events`
- **Partitions:** 3
- **Replication factor:** 1

### Producer Configuration

- **Port:** 8081
- **Serializer:** JSON
- **Acknowledgements:** all
- **Retries:** 3

### Consumer Configuration

- **Port:** 8082
- **Consumer group:** `user-events-consumer-group`
- **Deserializer:** JSON
- **Auto-offset-reset:** earliest
- **Acknowledgment mode:** manual
- **Concurrency:** 3 consumers

## 🐳 Docker Commands

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f

# View specific service logs
docker logs -f producer-service
docker logs -f consumer-service
docker logs -f kafka

# Rebuild and restart
docker-compose up --build --force-recreate

# Remove all containers and volumes
docker-compose down -v
```

## 🛠️ Local Development

### Running services locally (without Docker)

1. **Start Kafka:**

```bash
docker-compose up kafka -d
```

2. **Run Producer Service:**

```bash
cd producer-service
mvn spring-boot:run
```

3. **Run Consumer Service:**

```bash
cd consumer-service
mvn spring-boot:run
```

## 📊 Monitoring

### Health Endpoints

- Producer: http://localhost:8081/actuator/health
- Consumer: http://localhost:8082/actuator/health

### Kafka Topics

Check topics in Kafka:

```bash
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

View messages in topic:

```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning
```

## 🔍 Troubleshooting

### Services not starting

```bash
# Check container status
docker-compose ps

# Check logs for errors
docker-compose logs
```

### Kafka connection issues

Ensure Kafka is fully started before the services:
```bash
docker-compose up kafka -d
# Wait 30 seconds
docker-compose up producer-service consumer-service -d
```

### Port conflicts

If ports are already in use, modify the ports in `docker-compose.yml`:
```yaml
ports:
  - "8081:8081"  # Change first number to different port
```

## 🎯 Features

✅ Asynchronous message processing  
✅ Manual acknowledgment for reliability  
✅ Multiple consumer instances (concurrency: 3)  
✅ Automatic topic creation  
✅ JSON serialization/deserialization  
✅ Health check endpoints  
✅ Containerized with Docker  
✅ Production-ready configuration  
✅ **CI/CD with GitHub Actions**  
✅ **Automated dependency updates**  
✅ **Multi-platform Docker builds**  
✅ **Fixed Kafka deserialization issues**  

## 🔄 CI/CD Pipelines

This project includes comprehensive GitHub Actions workflows:

### Pull Request Checks (`ci-pr.yml`)
- ✅ Build and test both services
- ✅ Code quality verification
- ✅ Docker image building
- ✅ Integration tests with Kafka
- ✅ Matrix builds for parallel execution

### Main Branch Deployment (`cd-main.yml`)
- 🚀 Automated builds on merge to main
- 📦 Docker image publishing to GitHub Container Registry
- 🌍 Multi-platform builds (linux/amd64, linux/arm64)
- 🏥 Deployment to production with health checks
- 📢 Deployment notifications

### Dependency Updates (`dependabot.yml`)
- 🔄 Weekly automated dependency updates
- 📦 Separate updates for Maven, Docker, GitHub Actions
- 🔗 Grouped updates for related dependencies
- 🏷️ Automatic labeling and PR creation

## ⚡ Recent Upgrades

### Spring Boot 4 Migration
This project has been upgraded to **Spring Boot 4.0.2** with the following improvements:

- ✨ Latest Spring Framework 7.0
- 🔧 Fixed Kafka deserialization type mapping issues
- 📚 Updated dependencies for compatibility
- 🐳 Enhanced Docker build process
- 📖 See [SPRING_BOOT_4_MIGRATION.md](SPRING_BOOT_4_MIGRATION.md) for details  

## 📝 Next Steps

- Add error handling and Dead Letter Queue (DLQ)
- Implement database persistence
- Add monitoring with Prometheus/Grafana
- Implement message schemas with Avro
- Add integration tests
- Implement circuit breaker pattern
- Add API gateway

## 📄 License

This project is open source and available under the MIT License.
