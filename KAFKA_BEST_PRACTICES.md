# Kafka Best Practices Implementation Guide

## 🎯 Overview

This implementation follows Apache Kafka production best practices for:
- High availability and fault tolerance
- Performance optimization
- Data integrity and delivery guarantees
- Monitoring and observability
- Error handling and recovery

## 📋 Key Best Practices Implemented

### 1. Producer Best Practices

#### ✅ Idempotent Producer
```yaml
enable.idempotence: true
```
**Why**: Prevents duplicate messages if retries occur
**Result**: Exactly-once semantics within a partition

#### ✅ Acknowledgment Strategy
```yaml
acks: all
```
**Why**: Ensures message is written to all in-sync replicas
**Options**:
- `acks=0`: Fire and forget (fastest, no guarantee)
- `acks=1`: Leader acknowledges (moderate guarantee)
- `acks=all`: All ISRs acknowledge (strongest guarantee) ✅

#### ✅ Compression
```yaml
compression.type: snappy
```
**Why**: Reduces network bandwidth and storage
**Options**: none, gzip, snappy, lz4, zstd
**Recommendation**: snappy for balanced CPU/compression

#### ✅ Batching Configuration
```yaml
linger.ms: 10
batch.size: 32768
```
**Why**: Improves throughput by batching messages
**Trade-off**: Slightly higher latency for better throughput

#### ✅ Delivery Timeout
```yaml
delivery.timeout.ms: 120000
```
**Why**: Total time for send, retries, and acks
**Replaces**: retries config in modern Kafka

#### ✅ Request Timeout
```yaml
request.timeout.ms: 30000
```
**Why**: Individual request timeout
**Must be**: Less than delivery.timeout.ms

#### ✅ Buffer Memory
```yaml
buffer.memory: 67108864  # 64 MB
```
**Why**: Memory buffer for batching messages
**Tuning**: Increase for high-throughput scenarios

#### ✅ Max Block Time
```yaml
max.block.ms: 60000
```
**Why**: Time to wait when buffer is full
**Alternative**: Fail fast for time-sensitive apps

### 2. Consumer Best Practices

#### ✅ Manual Offset Commit
```java
@KafkaListener(containerFactory = "kafkaListenerContainerFactory")
public void consume(Event event, Acknowledgment ack) {
    process(event);
    ack.acknowledge(); // Manual commit after processing
}
```
**Why**: Prevents data loss if consumer crashes during processing
**Alternative**: Auto-commit has race conditions

#### ✅ Session Timeout Configuration
```yaml
session.timeout.ms: 30000
heartbeat.interval.ms: 3000
```
**Why**: Balances rebalance speed vs network hiccups
**Rule**: heartbeat.interval.ms < session.timeout.ms / 3

#### ✅ Max Poll Records
```yaml
max.poll.records: 500
```
**Why**: Controls memory usage and processing time
**Tuning**: Lower for heavy processing, higher for light processing

#### ✅ Max Poll Interval
```yaml
max.poll.interval.ms: 300000  # 5 minutes
```
**Why**: Maximum time between poll() calls
**Warning**: Must finish processing before this timeout

#### ✅ Fetch Size Configuration
```yaml
fetch.min.bytes: 1024
fetch.max.wait.ms: 500
```
**Why**: Balances latency vs throughput
**Trade-off**: Higher fetch.min.bytes = better throughput, higher latency

#### ✅ Auto Offset Reset
```yaml
auto.offset.reset: earliest
```
**Why**: Behavior when no offset exists
**Options**:
- `earliest`: Read from beginning (don't lose data)
- `latest`: Read only new messages (lose historical data)
- `none`: Throw exception

#### ✅ Isolation Level
```yaml
isolation.level: read_committed
```
**Why**: Only read committed messages (for exactly-once)
**Alternative**: `read_uncommitted` for higher throughput

### 3. Topic Configuration Best Practices

#### ✅ Replication Factor
```java
.replicas(3)  // For production
```
**Why**: Fault tolerance - survives broker failures
**Minimum**: 2 for production, 3 recommended
**Rule**: replication-factor <= number of brokers

#### ✅ Partition Count
```java
.partitions(6)  // Based on expected throughput
```
**Why**: Enables parallelism and scalability
**Calculation**: (target throughput MB/s) / (single partition throughput MB/s)
**Consideration**: More partitions = more overhead

#### ✅ Min In-Sync Replicas
```java
min.insync.replicas: 2
```
**Why**: Minimum replicas that must acknowledge writes
**Recommendation**: Set to replication-factor - 1
**Requirement**: Must use with acks=all

#### ✅ Retention Configuration
```java
retention.ms: 604800000  // 7 days
retention.bytes: -1      // Unlimited
```
**Why**: Balance between storage cost and replay needs
**Options**: Time-based, size-based, or both

#### ✅ Cleanup Policy
```java
cleanup.policy: delete
```
**Why**: Remove old data automatically
**Options**:
- `delete`: Time/size-based deletion
- `compact`: Keep latest value per key
- `delete,compact`: Both strategies

#### ✅ Compression at Topic Level
```java
compression.type: producer  // Inherit from producer
```
**Why**: Let producers choose compression
**Alternative**: Force specific compression at broker

### 4. Error Handling Best Practices

#### ✅ Dead Letter Queue (DLQ)
```java
@KafkaListener(...)
public void consume(Event event) {
    try {
        process(event);
    } catch (RecoverableException e) {
        // Retry logic
        retry(event);
    } catch (Exception e) {
        // Send to DLQ
        sendToDLQ(event, e);
    }
}
```
**Why**: Isolate poison messages
**Pattern**: topic-name.DLQ

#### ✅ Retry Topics with Backoff
```
topic-name
topic-name.retry-1  (1 second delay)
topic-name.retry-2  (10 second delay)
topic-name.retry-3  (1 minute delay)
topic-name.DLQ      (final failure)
```
**Why**: Exponential backoff for transient errors
**Implementation**: Spring Kafka RetryTemplate

#### ✅ Error Handlers
```java
factory.setCommonErrorHandler(
    new DefaultErrorHandler(
        new DeadLetterPublishingRecoverer(kafkaTemplate),
        new FixedBackOff(1000L, 3L)
    )
);
```
**Why**: Centralized error handling
**Options**: DefaultErrorHandler, SeekToCurrentErrorHandler

#### ✅ Deserialization Error Handler
```java
factory.setCommonErrorHandler(
    new DeadLetterPublishingRecoverer(
        kafkaTemplate,
        (record, ex) -> new TopicPartition(
            record.topic() + ".DLQ",
            record.partition()
        )
    )
);
```
**Why**: Handle malformed messages
**Alternative**: Log and skip

### 5. Monitoring and Observability

#### ✅ Metrics Exposure
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    export:
      prometheus:
        enabled: true
```
**Why**: Monitor Kafka performance
**Metrics**: 
- Producer: record-send-rate, record-error-rate
- Consumer: records-consumed-rate, commit-latency

#### ✅ Health Checks
```java
@Component
public class KafkaHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Check Kafka connectivity
        return Health.up().build();
    }
}
```
**Why**: Liveness/readiness probes
**Use**: Kubernetes health checks

#### ✅ Logging Best Practices
```java
log.info("Message sent: topic={}, partition={}, offset={}, key={}", 
    topic, partition, offset, key);
```
**Why**: Traceability and debugging
**Include**: Topic, partition, offset, key, correlation-id

#### ✅ Distributed Tracing
```java
@Data
public class Event {
    private String correlationId;  // For tracing
    private String causationId;    // Event that caused this
    private LocalDateTime timestamp;
}
```
**Why**: Track message flow across services
**Tools**: Zipkin, Jaeger, OpenTelemetry

### 6. Security Best Practices

#### ✅ SSL/TLS Encryption
```yaml
spring.kafka:
  security:
    protocol: SSL
  ssl:
    key-store-location: classpath:kafka.keystore.jks
    key-store-password: ${KEYSTORE_PASSWORD}
    trust-store-location: classpath:kafka.truststore.jks
    trust-store-password: ${TRUSTSTORE_PASSWORD}
```
**Why**: Encrypt data in transit
**Requirement**: Production environments

#### ✅ SASL Authentication
```yaml
spring.kafka:
  properties:
    sasl.mechanism: SCRAM-SHA-512
    sasl.jaas.config: |
      org.apache.kafka.common.security.scram.ScramLoginModule required
      username="${KAFKA_USERNAME}"
      password="${KAFKA_PASSWORD}";
```
**Why**: Authenticate clients
**Options**: PLAIN, SCRAM, GSSAPI (Kerberos)

#### ✅ ACLs (Access Control Lists)
```bash
# Create ACL for producer
kafka-acls --add --allow-principal User:producer \
  --operation Write --topic user-events

# Create ACL for consumer  
kafka-acls --add --allow-principal User:consumer \
  --operation Read --topic user-events --group events-group
```
**Why**: Fine-grained access control
**Principle**: Least privilege

### 7. Performance Optimization

#### ✅ Consumer Concurrency
```java
factory.setConcurrency(3);  // 3 consumer threads per partition
```
**Why**: Parallel processing
**Rule**: concurrency <= partition count

#### ✅ Producer Connection Pooling
```java
@Bean
public ProducerFactory<String, Object> producerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 180000);
    return new DefaultKafkaProducerFactory<>(config);
}
```
**Why**: Reuse TCP connections
**Tuning**: Balance between resource usage and latency

#### ✅ Consumer Connection Pooling
```yaml
connections.max.idle.ms: 540000  # 9 minutes
```
**Why**: Avoid connection churn
**Consideration**: Higher than producer (consumers are long-lived)

#### ✅ Partition Assignment Strategy
```yaml
partition.assignment.strategy: org.apache.kafka.clients.consumer.CooperativeStickyAssignor
```
**Why**: Minimize rebalance overhead
**Options**:
- RangeAssignor (default)
- RoundRobinAssignor
- StickyAssignor
- CooperativeStickyAssignor (best) ✅

### 8. Data Quality Best Practices

#### ✅ Schema Validation
```java
@Valid @RequestBody UserEventRequest request
```
**Why**: Validate data at API boundary
**Tools**: JSON Schema, Avro, Protobuf

#### ✅ Schema Registry (Optional)
```yaml
spring.kafka:
  properties:
    schema.registry.url: http://schema-registry:8081
```
**Why**: Centralized schema management
**Tool**: Confluent Schema Registry

#### ✅ Message Versioning
```java
public class UserEventV2 {
    private String version = "2.0";
    // ... fields
}
```
**Why**: Handle schema evolution
**Pattern**: Versioned topic or message field

#### ✅ Serialization Best Practices
```java
// Use JSON for flexibility
@Bean
public ProducerFactory<String, Object> producerFactory() {
    JsonSerializer<Object> serializer = new JsonSerializer<>();
    serializer.setAddTypeInfo(false);  // Don't add __type
    // Configure serializer
}
```
**Why**: Control serialization behavior
**Alternative**: Avro for performance

### 9. Deployment Best Practices

#### ✅ Graceful Shutdown
```yaml
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```
**Why**: Finish processing before shutdown
**Implementation**: @PreDestroy hooks

#### ✅ Consumer Group Management
```yaml
spring.kafka.consumer.group-id: ${HOSTNAME}-consumer-group
```
**Why**: Unique consumer groups per application
**Pattern**: app-name-env-consumer-group

#### ✅ Environment-Specific Configuration
```yaml
spring:
  config:
    import: optional:configserver:
  profiles:
    active: ${ENVIRONMENT:dev}
```
**Why**: Different configs per environment
**Environments**: dev, test, staging, prod

#### ✅ Resource Limits
```yaml
# Kubernetes
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m"
```
**Why**: Prevent resource exhaustion
**Consideration**: Based on message size and throughput

### 10. Testing Best Practices

#### ✅ Embedded Kafka for Tests
```java
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"test-topic"})
class KafkaTest {
    // Test consumer and producer
}
```
**Why**: Integration tests without external Kafka
**Tools**: spring-kafka-test

#### ✅ Consumer Test with Timeout
```java
@Test
void testConsumer() {
    // Send message
    producer.send(message);
    
    // Wait for consumption
    boolean consumed = latch.await(10, TimeUnit.SECONDS);
    assertThat(consumed).isTrue();
}
```
**Why**: Async nature of Kafka
**Pattern**: Use CountDownLatch

## 📊 Comparison: Before vs After

| Practice | Before | After | Impact |
|----------|--------|-------|--------|
| **Idempotence** | ❌ Not enabled | ✅ Enabled | Prevents duplicates |
| **Compression** | ❌ None | ✅ Snappy | 50-70% bandwidth savings |
| **DLQ** | ❌ No DLQ | ✅ DLQ + Retry topics | Poison message handling |
| **Error Handler** | ❌ Basic try-catch | ✅ DefaultErrorHandler | Centralized errors |
| **Monitoring** | ⚠️ Basic | ✅ Prometheus + tracing | Full observability |
| **Security** | ❌ None | ✅ SSL + SASL | Production-ready |
| **Min ISR** | ❌ Default (1) | ✅ 2 | Data durability |
| **Partitions** | 3 | 6 | 2x parallelism |
| **Batching** | linger.ms=1 | linger.ms=10 | Better throughput |
| **Timeouts** | ⚠️ Defaults | ✅ Tuned | Predictable behavior |

## 🎯 Implementation Priority

### Phase 1: Reliability (P0)
1. ✅ Enable idempotence
2. ✅ Set acks=all
3. ✅ Configure min.insync.replicas=2
4. ✅ Manual offset commit
5. ✅ Dead Letter Queue

### Phase 2: Performance (P1)
1. ✅ Enable compression (snappy)
2. ✅ Tune batching (linger.ms, batch.size)
3. ✅ Optimize partitions
4. ✅ Consumer concurrency
5. ✅ Connection pooling

### Phase 3: Observability (P2)
1. ✅ Prometheus metrics
2. ✅ Distributed tracing
3. ✅ Health checks
4. ✅ Structured logging
5. ✅ Alerting rules

### Phase 4: Security (P3)
1. ✅ SSL/TLS encryption
2. ✅ SASL authentication
3. ✅ ACLs
4. ✅ Secrets management
5. ✅ Network policies

## 📝 Checklist for Production

- [ ] Idempotent producer enabled
- [ ] acks=all for durability
- [ ] min.insync.replicas >= 2
- [ ] Replication factor >= 3
- [ ] Dead Letter Queue configured
- [ ] Retry mechanism implemented
- [ ] Manual offset commit
- [ ] Compression enabled
- [ ] Monitoring/metrics enabled
- [ ] Health checks configured
- [ ] SSL/TLS enabled
- [ ] Authentication configured
- [ ] ACLs defined
- [ ] Graceful shutdown implemented
- [ ] Resource limits defined
- [ ] Integration tests passing
- [ ] Load testing completed
- [ ] Disaster recovery plan
- [ ] Runbook created
- [ ] Alerting configured
