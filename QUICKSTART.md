# Quick Start Guide - Spring Boot Kafka Microservices

## 🎯 What You Have

A complete microservices setup with:
- **Producer Service** - Publishes messages to Kafka
- **Consumer Service** - Consumes and processes messages from Kafka
- **Kafka Broker** - Message broker
- **Zookeeper** - Kafka coordinator
- **Docker Compose** - Orchestrates all services

## ⚡ Quick Start (3 Steps)

### 1. Start Everything
```bash
./start.sh
```
Or manually:
```bash
docker-compose up --build -d
```

### 2. Send a Test Message
```bash
curl -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","eventType":"USER_CREATED"}'
```

### 3. Watch Consumer Process It
```bash
docker logs -f consumer-service
```

## 🧪 Run Automated Tests
```bash
./test-messages.sh
```
This sends 10 random events and you can watch them being processed.

## 📊 Service Ports

- Producer API: http://localhost:8081
- Consumer API: http://localhost:8082
- Kafka: localhost:29092 (external), kafka:9092 (internal)
- Zookeeper: localhost:2181

## 🔍 Useful Commands

**Check if services are running:**
```bash
docker-compose ps
```

**View all logs:**
```bash
docker-compose logs -f
```

**View producer logs only:**
```bash
docker logs -f producer-service
```

**View consumer logs only:**
```bash
docker logs -f consumer-service
```

**Check Kafka topics:**
```bash
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

**View messages in Kafka topic:**
```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning
```

**Stop everything:**
```bash
docker-compose down
```

**Stop and remove all data:**
```bash
docker-compose down -v
```

## 🏗️ Project Structure

```
spring-kafka-microservices/
├── docker-compose.yml          # Orchestration file
├── start.sh                    # Easy startup script
├── test-messages.sh            # Test script
├── README.md                   # Full documentation
├── producer-service/           # Producer microservice
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           └── resources/
└── consumer-service/           # Consumer microservice
    ├── Dockerfile
    ├── pom.xml
    └── src/
        └── main/
            ├── java/
            └── resources/
```

## 🎨 Example Payloads

**User Created:**
```json
{
  "username": "alice",
  "email": "alice@example.com",
  "eventType": "USER_CREATED"
}
```

**User Updated:**
```json
{
  "username": "bob",
  "email": "bob@example.com",
  "eventType": "USER_UPDATED"
}
```

**Login Event:**
```json
{
  "username": "charlie",
  "email": "charlie@example.com",
  "eventType": "USER_LOGGED_IN"
}
```

## 🐛 Troubleshooting

**Services won't start?**
- Make sure Docker is running
- Check if ports 8081, 8082, 9092, 29092, 2181 are free
- Run: `docker-compose down -v` then start again

**Can't connect to Kafka?**
- Wait 30 seconds after starting (Kafka takes time to initialize)
- Check logs: `docker logs kafka`

**Consumer not receiving messages?**
- Verify producer sent message successfully
- Check consumer logs: `docker logs -f consumer-service`
- Verify Kafka is running: `docker ps | grep kafka`

## 📚 Learn More

See `README.md` for detailed documentation including:
- Architecture details
- Configuration options
- Local development setup
- Advanced features
- Production considerations

## ✨ What's Next?

1. Modify `KafkaConsumerService.java` to add your business logic
2. Add database persistence (PostgreSQL, MongoDB, etc.)
3. Implement error handling and DLQ
4. Add more microservices
5. Implement API Gateway
6. Add monitoring and metrics

Happy coding! 🚀
