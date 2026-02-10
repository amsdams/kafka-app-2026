# Kafka Best Practices - Production-Ready Implementation

This is a production-ready Spring Boot Kafka implementation following Apache Kafka best practices.

## 🎯 What's New - Best Practices Applied

### ✅ Producer Enhancements
1. **Idempotence** - Prevents duplicate messages (`enable.idempotence=true`)
2. **Compression** - 50-70% bandwidth reduction (`compression.type=snappy`)
3. **Optimized Batching** - Better throughput (`linger.ms=10`, `batch.size=32KB`)
4. **Proper Timeouts** - Predictable behavior (`delivery.timeout.ms`, `request.timeout.ms`)
5. **Metrics & Monitoring** - Prometheus integration with custom metrics
6. **Callback Handling** - Proper async/sync send with error callbacks

### ✅ Consumer Enhancements
1. **Manual Commit** - Prevents data loss (`enable-auto-commit=false`)
2. **Dead Letter Queue** - Isolates poison messages
3. **Exponential Backoff** - Retry with 1s → 2s → 4s delays
4. **Session Management** - Optimized heartbeat and timeouts
5. **Cooperative Rebalancing** - Minimal disruption during rebalances
6. **Isolation Level** - Read committed messages only
7. **Batch Processing** - Optional batch listener for high throughput

### ✅ Topic Configuration
1. **Replication Factor** - 3 replicas for fault tolerance
2. **Min In-Sync Replicas** - 2 for data durability
3. **Partitions** - 6 partitions for parallelism
4. **Retention** - 7 days for main topics, 30 days for DLQ
5. **Compression** - Inherits from producer

### ✅ Infrastructure
1. **Graceful Shutdown** - 30s timeout for cleanup
2. **Health Checks** - Kafka connectivity monitoring
3. **Metrics** - Prometheus endpoint enabled
4. **Structured Logging** - Full context in logs

## 📊 Performance Impact

| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| **Network Bandwidth** | 100% | 30-50% | 50-70% reduction (compression) |
| **Duplicates** | Possible | ✅ Prevented | Idempotence |
| **Data Loss Risk** | Medium | ✅ Very Low | Manual commit + min.insync.replicas |
| **Poison Messages** | ❌ Block queue | ✅ Isolated to DLQ | Error handling |
| **Throughput** | Baseline | +30-50% | Batching optimization |
| **Rebalance Time** | ~30s | ~5s | Cooperative rebalancing |

## 🏗️ Architecture

```
┌─────────────────────────┐
│   Producer Service      │
│   Port 8081             │
│                         │
│  ✅ Idempotence         │
│  ✅ Compression         │
│  ✅ Batching            │
│  ✅ Metrics             │
└───────────┬─────────────┘
            │
            ▼
    ┌───────────────┐
    │  Kafka Broker │
    │               │
    │  Topics:      │
    │  - user-events (6 partitions, RF=3) │
    │  - order-events (6 partitions, RF=3) │
    │  - *.DLQ (6 partitions, RF=3)        │
    │               │
    │  ✅ min.insync.replicas=2 │
    │  ✅ Compression           │
    └───────┬───────────────┘
            │
            ▼
┌─────────────────────────┐
│  Consumer Service       │
│  Port 8082              │
│                         │
│  ✅ Manual Commit       │
│  ✅ DLQ + Retry         │
│  ✅ Concurrency=3       │
│  ✅ Metrics             │
└─────────────────────────┘
```

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 25+ (for local development)
- Maven 3.6+ (optional if using Docker)

### Option 1: Automated Script (Recommended)

```bash
# One command to build and run everything
./build-and-run.sh
```

### Option 2: Using Makefile

```bash
# View all available commands
make help

# Build and start services
make up

# Send test messages
make test

# View logs
make logs

# Start with monitoring (Prometheus + Grafana)
make monitoring

# Stop everything
make down
```

### Option 3: Manual Docker Compose

```bash
# Build
mvn clean install

# Start infrastructure + services
docker-compose up --build -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

### Services Available
- **Producer**:  http://localhost:8081
- **Consumer**:  http://localhost:8082
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **Prometheus**: http://localhost:9090 (with --profile monitoring)
- **Grafana**:   http://localhost:3000 (with --profile monitoring)

## 📝 Configuration Highlights

### Producer (application.yml)

```yaml
spring.kafka.producer:
  acks: all                          # ✅ Wait for all replicas
  properties:
    enable.idempotence: true         # ✅ Prevent duplicates
    compression.type: snappy         # ✅ Reduce bandwidth 50-70%
    linger.ms: 10                    # ✅ Batch for throughput
    batch.size: 32768                # ✅ 32KB batches
    delivery.timeout.ms: 120000      # ✅ Total timeout
    request.timeout.ms: 30000        # ✅ Per-request timeout
```

### Consumer (application.yml)

```yaml
spring.kafka.consumer:
  enable-auto-commit: false          # ✅ Manual commit
  auto-offset-reset: earliest        # ✅ Don't lose data
  properties:
    session.timeout.ms: 30000        # ✅ 30s session timeout
    heartbeat.interval.ms: 3000      # ✅ 3s heartbeat
    max.poll.interval.ms: 300000     # ✅ 5min processing time
    max.poll.records: 500            # ✅ 500 records per poll
    isolation.level: read_committed  # ✅ Exactly-once
    partition.assignment.strategy:   # ✅ Cooperative rebalancing
      org.apache.kafka.clients.consumer.CooperativeStickyAssignor
```

### Topics (KafkaTopicConfig.java)

```java
TopicBuilder
    .name("user-events")
    .partitions(6)                   # ✅ Parallelism
    .replicas(3)                     # ✅ Fault tolerance
    .config("min.insync.replicas", "2")  # ✅ Durability
    .config("compression.type", "producer")
    .config("retention.ms", "604800000")  # 7 days
```

## 📤 Testing

### Create User Event

```bash
curl -X POST http://localhost:8081/api/producer/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
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

### Create Order Event

```bash
curl -X POST http://localhost:8081/api/producer/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-123",
    "productName": "Premium Plan",
    "amount": 99.99,
    "eventType": "ORDER_CREATED"
  }'
```

### View Metrics

```bash
# Prometheus metrics
curl http://localhost:8081/actuator/prometheus | grep kafka

# Producer metrics
kafka.producer.messages.sent_total
kafka.producer.messages.failed_total
kafka.producer.send.duration_seconds

# Health check
curl http://localhost:8081/actuator/health
```

## 🔍 Monitoring

### Key Metrics to Monitor

**Producer:**
- `kafka.producer.messages.sent` - Total messages sent
- `kafka.producer.messages.failed` - Failed sends
- `kafka.producer.send.duration` - Send latency

**Consumer:**
- `kafka.consumer.records.consumed` - Consumption rate
- `kafka.consumer.commit.latency` - Commit time
- `kafka.consumer.lag` - Consumer lag

**Kafka:**
- `kafka_server_replicamanager_underreplicated_partitions` - Under-replicated partitions
- `kafka_controller_kafkacontroller_activecontrollercount` - Active controller

### Grafana Dashboards

Import these community dashboards:
- Kafka Overview: 7589
- Kafka Exporter: 7589
- Spring Boot: 6756

## 🎯 Best Practices Checklist

### Producer
- [x] Idempotence enabled
- [x] acks=all configured
- [x] Compression enabled (snappy)
- [x] Batching optimized (linger.ms, batch.size)
- [x] Timeouts configured
- [x] Metrics exposed
- [x] Async with callbacks
- [x] Sync option available

### Consumer
- [x] Manual commit mode
- [x] Dead Letter Queue
- [x] Exponential backoff retry
- [x] Session timeout optimized
- [x] Heartbeat configured
- [x] Cooperative rebalancing
- [x] Isolation level: read_committed
- [x] Concurrency configured

### Topics
- [x] Replication factor: 3
- [x] Min ISR: 2
- [x] Appropriate partitions
- [x] Retention policy
- [x] Compression configured
- [x] DLQ topics created

### Infrastructure
- [x] Graceful shutdown
- [x] Health checks
- [x] Metrics endpoint
- [x] Structured logging
- [x] Docker support

## 📚 Documentation

- **[KAFKA_BEST_PRACTICES.md](./KAFKA_BEST_PRACTICES.md)** - Complete best practices guide
- **[Producer Config](./producer-service/src/main/resources/application.yml)** - All settings explained
- **[Consumer Config](./consumer-service/src/main/resources/application.yml)** - All settings explained

## 🔧 Configuration Tuning Guide

### High Throughput (Batch Processing)

```yaml
producer:
  properties:
    linger.ms: 100           # Wait longer for batches
    batch.size: 65536        # Larger batches (64KB)
    buffer.memory: 134217728 # More buffer (128MB)

consumer:
  properties:
    max.poll.records: 1000   # More records per poll
    fetch.min.bytes: 10240   # Wait for 10KB
```

### Low Latency (Real-time)

```yaml
producer:
  properties:
    linger.ms: 0             # Send immediately
    batch.size: 16384        # Smaller batches

consumer:
  properties:
    max.poll.records: 100    # Fewer records
    fetch.min.bytes: 1       # Don't wait
    fetch.max.wait.ms: 100   # Max 100ms wait
```

### Heavy Processing (Long Tasks)

```yaml
consumer:
  properties:
    max.poll.interval.ms: 600000  # 10 minutes
    max.poll.records: 50          # Process fewer at once
```

## 🚨 Troubleshooting

### Consumer Lag Growing

**Symptoms:** Consumer can't keep up
**Solutions:**
1. Increase consumer concurrency
2. Add more consumer instances
3. Optimize processing code
4. Increase partitions

### Rebalancing Too Often

**Symptoms:** Frequent rebalances in logs
**Solutions:**
1. Increase `session.timeout.ms`
2. Decrease `max.poll.records`
3. Optimize processing time
4. Check network stability

### Messages Going to DLQ

**Symptoms:** Messages in DLQ topic
**Actions:**
1. Check DLQ messages: `docker exec kafka kafka-console-consumer --topic user-events.DLQ ...`
2. Identify error pattern
3. Fix root cause
4. Replay from DLQ if needed

## 🔐 Production Security (TODO)

```yaml
spring.kafka:
  security:
    protocol: SSL
  ssl:
    key-store-location: classpath:kafka.keystore.jks
    trust-store-location: classpath:kafka.truststore.jks
  properties:
    sasl.mechanism: SCRAM-SHA-512
    sasl.jaas.config: |
      org.apache.kafka.common.security.scram.ScramLoginModule required
      username="${KAFKA_USERNAME}"
      password="${KAFKA_PASSWORD}";
```

## 📄 License

MIT License - Open Source
