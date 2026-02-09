# Spring Boot Kafka Microservices - Improved Version

This is an improved version of the Spring Boot Kafka application with:
- ✅ **DTOs** separate from domain models
- ✅ **Shared models** module to avoid duplication
- ✅ **Multiple event types** (UserEvent and OrderEvent)
- ✅ **Handler pattern** for event processing
- ✅ **Input validation** with Jakarta Validation
- ✅ **Proper separation of concerns**

## 🏗️ Architecture

```
┌─────────────────────┐      ┌──────────────┐      ┌─────────────────────┐
│  Producer Service   │─────▶│    Kafka     │─────▶│  Consumer Service   │
│    (Port 8081)      │      │   Broker     │      │    (Port 8082)      │
│                     │      │              │      │                     │
│ REST API → DTOs     │      │ user-events  │      │ Handlers Pattern    │
│ DTOs → Models       │      │ order-events │      │ Event Processing    │
│ Models → Kafka      │      │              │      │                     │
└─────────────────────┘      └──────────────┘      └─────────────────────┘
         ↓                                                   ↓
    ┌─────────────────────────────────────────────────────────┐
    │              Shared Models Module                        │
    │   UserEvent, OrderEvent, Topics Constants                │
    └─────────────────────────────────────────────────────────┘
```

## 📁 Project Structure

```
kafka-app-improved/
├── pom.xml                           # Parent POM
├── docker-compose.yml                # Kafka infrastructure
│
├── shared-models/                    # Common models shared across services
│   ├── pom.xml
│   └── src/main/java/com/example/common/
│       ├── model/
│       │   ├── UserEvent.java        # Kafka message for user events
│       │   └── OrderEvent.java       # Kafka message for order events
│       └── constants/
│           └── Topics.java           # Topic name constants
│
├── producer-service/                 # Event producer
│   ├── pom.xml
│   └── src/main/java/com/example/producer/
│       ├── ProducerServiceApplication.java
│       ├── config/
│       │   ├── KafkaProducerConfig.java
│       │   └── KafkaTopicConfig.java
│       ├── controller/
│       │   └── ProducerController.java
│       ├── dto/                      # API layer DTOs
│       │   ├── UserEventRequest.java
│       │   ├── OrderEventRequest.java
│       │   └── EventResponse.java
│       ├── mapper/
│       │   └── EventMapper.java      # DTO to Model mapping
│       └── service/
│           └── KafkaProducerService.java
│
└── consumer-service/                 # Event consumer
    ├── pom.xml
    └── src/main/java/com/example/consumer/
        ├── ConsumerServiceApplication.java
        ├── config/
        │   └── KafkaConsumerConfig.java
        ├── handler/                  # Handler pattern
        │   ├── EventHandler.java     # Interface
        │   ├── UserEventHandler.java
        │   └── OrderEventHandler.java
        └── service/
            └── KafkaConsumerService.java
```

## 🚀 Key Improvements

### 1. DTOs for API Layer
**Before:**
```java
@PostMapping("/publish")
public ResponseEntity<String> publishEvent(@RequestBody UserEvent event) {
    event.setId(UUID.randomUUID().toString());  // ❌ Controller shouldn't do this
    producerService.sendMessage(event);
    return ResponseEntity.ok("Event published");
}
```

**After:**
```java
@PostMapping("/users")
public ResponseEntity<EventResponse> publishUserEvent(
        @Valid @RequestBody UserEventRequest request) {  // ✅ Validation
    
    UserEvent event = eventMapper.toUserEvent(request);  // ✅ Mapping
    producerService.sendMessage(Topics.USER_EVENTS, event.getId(), event);
    
    EventResponse response = eventMapper.toResponse(
        event.getId(), event.getCorrelationId(), request.getEventType()
    );
    return ResponseEntity.ok(response);
}
```

### 2. Shared Models Module
- ✅ Single source of truth for event models
- ✅ No code duplication between producer and consumer
- ✅ Version control for schemas
- ✅ Constants for topic names

### 3. Multiple Event Types
- ✅ UserEvent for user-related operations
- ✅ OrderEvent for order-related operations
- ✅ Separate Kafka topics (user-events, order-events)
- ✅ OrderEvent links to UserEvent via userId

### 4. Handler Pattern
- ✅ Strategy pattern for event processing
- ✅ Easy to add new event types
- ✅ Testable handlers
- ✅ Separation of concerns

## 🛠️ Technologies

- **Spring Boot 4.0.2**
- **Spring Kafka**
- **Apache Kafka 7.5.0**
- **Java 25**
- **Lombok**
- **Jakarta Validation**
- **Docker & Docker Compose**

## 🚀 Getting Started

### Prerequisites
- Docker and Docker Compose
- Java 25+ (for local development)
- Maven 3.6+

### Build the Project

```bash
# Build all modules
mvn clean install
```

### Run with Docker Compose

```bash
# Start all services
docker-compose up --build
```

This will start:
- Kafka broker (ports 9092, 29092)
- Producer service (port 8081)
- Consumer service (port 8082)

## 📤 Testing the Services

### 1. Create a User Event

```bash
curl -X POST http://localhost:8081/api/events/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "eventType": "USER_CREATED"
  }'
```

**Response:**
```json
{
  "eventId": "123e4567-e89b-12d3-a456-426614174000",
  "correlationId": "123e4567-e89b-12d3-a456-426614174001",
  "message": "USER_CREATED event published successfully",
  "timestamp": "2024-02-09T10:30:00"
}
```

### 2. Create an Order Event (linked to user)

```bash
curl -X POST http://localhost:8081/api/events/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "productName": "Premium Subscription",
    "amount": 99.99,
    "eventType": "ORDER_CREATED"
  }'
```

**Response:**
```json
{
  "eventId": "123e4567-e89b-12d3-a456-426614174002",
  "correlationId": "123e4567-e89b-12d3-a456-426614174003",
  "message": "ORDER_CREATED event published successfully",
  "timestamp": "2024-02-09T10:31:00"
}
```

### 3. Check Consumer Logs

```bash
docker logs -f consumer-service
```

You should see:
```
Received UserEvent from partition: 0, offset: 0
UserEvent details: UserEvent(id=..., username=john_doe, ...)
Processing UserEvent: ...
User created: john_doe with email: john@example.com
UserEvent acknowledged successfully

Received OrderEvent from partition: 0, offset: 0
OrderEvent details: OrderEvent(id=..., userId=..., productName=Premium Subscription, ...)
Processing OrderEvent: ...
Order created for user: 123e4567... - Product: Premium Subscription - Amount: 99.99
OrderEvent acknowledged successfully
```

## 🔄 How Events Are Related

The OrderEvent contains a `userId` field that references the UserEvent:

```java
// UserEvent
{
  "id": "user-123",
  "username": "john_doe",
  "email": "john@example.com",
  "eventType": "USER_CREATED"
}

// OrderEvent (references the user)
{
  "id": "order-456",
  "userId": "user-123",  // ← References UserEvent.id
  "productName": "Premium Subscription",
  "amount": 99.99,
  "eventType": "ORDER_CREATED"
}
```

## 🎯 Benefits of This Architecture

### DTOs vs Domain Models
| Aspect | DTOs | Domain Models |
|--------|------|---------------|
| Purpose | API contract | Kafka messages |
| Validation | ✅ Input validation | No validation |
| Evolution | Can change independently | Schema versioning |
| Security | Control input fields | Internal structure |

### Shared Models
- ✅ No code duplication
- ✅ Version consistency
- ✅ Single dependency for both services
- ✅ Easy schema evolution

### Handler Pattern
- ✅ Single Responsibility Principle
- ✅ Easy to unit test
- ✅ Extensible for new event types
- ✅ Clean separation of concerns

## 📊 API Endpoints

### Producer Service (Port 8081)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/events/users` | Publish a user event |
| POST | `/api/events/orders` | Publish an order event |
| GET | `/api/events/health` | Health check |
| GET | `/actuator/health` | Actuator health |

### Consumer Service (Port 8082)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Actuator health |

## 🔧 Configuration

### Kafka Topics
- **user-events** - 3 partitions, 1 replica
- **order-events** - 3 partitions, 1 replica

### Consumer Configuration
- **Group ID:** events-consumer-group
- **Concurrency:** 3 consumers per topic
- **Acknowledgment:** Manual
- **Auto-offset-reset:** earliest

## 🧪 Testing Multiple Events

```bash
# Create a user
USER_RESPONSE=$(curl -s -X POST http://localhost:8081/api/events/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "eventType": "USER_CREATED"
  }')

# Extract user ID from response
USER_ID=$(echo $USER_RESPONSE | jq -r '.eventId')

# Create an order for that user
curl -X POST http://localhost:8081/api/events/orders \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": \"$USER_ID\",
    \"productName\": \"Premium Plan\",
    \"amount\": 149.99,
    \"eventType\": \"ORDER_CREATED\"
  }"
```

## 🔍 Monitoring

### View Kafka Topics

```bash
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

### View Messages in Topic

```bash
# User events
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning

# Order events
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning
```

## 📝 What's Next?

Potential future improvements:
- [ ] Dead Letter Queue (DLQ) implementation
- [ ] Retry logic with exponential backoff
- [ ] Idempotency checks
- [ ] Database persistence
- [ ] Integration tests
- [ ] Metrics and monitoring (Prometheus/Grafana)
- [ ] Circuit breaker pattern
- [ ] Schema registry with Avro
- [ ] Event versioning (V1, V2)
- [ ] Distributed tracing with correlation IDs

## 📄 Migration from Original

If you're migrating from the original version:

1. **Phase 1:** Build and test the new structure
2. **Phase 2:** Deploy shared-models module
3. **Phase 3:** Update producer service
4. **Phase 4:** Update consumer service
5. **Phase 5:** Test end-to-end

The improved version is backward compatible with the original topics.

## 📄 License

This project is open source and available under the MIT License.
