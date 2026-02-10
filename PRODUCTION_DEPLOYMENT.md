# Production Deployment Guide

## 🎯 Prerequisites

### Infrastructure Requirements
- **Kafka Cluster**: 3+ brokers minimum
- **ZooKeeper**: 3+ nodes (or KRaft mode in Kafka 3.x+)
- **Resources per service**:
  - Producer: 512MB-1GB RAM, 0.5-1 CPU
  - Consumer: 1GB-2GB RAM, 1-2 CPU
  - Kafka broker: 8GB+ RAM, 4+ CPU, SSD storage

### Environment Setup
- Java 25+
- Maven 3.6+
- Docker (for containerization)
- Kubernetes (recommended for orchestration)

## 📋 Pre-Deployment Checklist

### Configuration Review
- [ ] Update bootstrap servers to production Kafka cluster
- [ ] Configure SSL/TLS certificates
- [ ] Set up SASL authentication
- [ ] Configure environment-specific properties
- [ ] Review partition count based on expected load
- [ ] Set appropriate retention periods
- [ ] Configure resource limits

### Security
- [ ] SSL certificates generated and deployed
- [ ] SASL credentials configured
- [ ] ACLs defined for each service
- [ ] Secrets stored in vault/secrets manager
- [ ] Network policies configured

### Monitoring
- [ ] Prometheus configured
- [ ] Grafana dashboards imported
- [ ] Alerting rules defined
- [ ] Log aggregation configured
- [ ] Distributed tracing enabled

## 🚀 Deployment Steps

### 1. Build Artifacts

```bash
# Build all services
mvn clean install -DskipTests

# Build Docker images
cd producer-service
docker build -t your-registry/producer-service:1.0.0 .

cd ../consumer-service
docker build -t your-registry/consumer-service:1.0.0 .

# Push to registry
docker push your-registry/producer-service:1.0.0
docker push your-registry/consumer-service:1.0.0
```

### 2. Deploy to Kubernetes

#### Create Namespace
```bash
kubectl create namespace kafka-app-prod
```

#### Deploy Secrets
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: kafka-credentials
  namespace: kafka-app-prod
type: Opaque
stringData:
  username: ${KAFKA_USERNAME}
  password: ${KAFKA_PASSWORD}
  ssl-keystore-password: ${KEYSTORE_PASSWORD}
  ssl-truststore-password: ${TRUSTSTORE_PASSWORD}
```

#### Deploy Producer
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: producer-service
  namespace: kafka-app-prod
spec:
  replicas: 3
  selector:
    matchLabels:
      app: producer-service
  template:
    metadata:
      labels:
        app: producer-service
    spec:
      containers:
      - name: producer
        image: your-registry/producer-service:1.0.0
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-1:9092,kafka-2:9092,kafka-3:9092"
        - name: ENVIRONMENT
          value: "prod"
        - name: KAFKA_USERNAME
          valueFrom:
            secretKeyRef:
              name: kafka-credentials
              key: username
        - name: KAFKA_PASSWORD
          valueFrom:
            secretKeyRef:
              name: kafka-credentials
              key: password
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 20
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: producer-service
  namespace: kafka-app-prod
spec:
  selector:
    app: producer-service
  ports:
  - port: 8081
    targetPort: 8081
  type: LoadBalancer
```

#### Deploy Consumer
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: consumer-service
  namespace: kafka-app-prod
spec:
  replicas: 3  # Should match or be less than partition count
  selector:
    matchLabels:
      app: consumer-service
  template:
    metadata:
      labels:
        app: consumer-service
    spec:
      containers:
      - name: consumer
        image: your-registry/consumer-service:1.0.0
        ports:
        - containerPort: 8082
        env:
        - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-1:9092,kafka-2:9092,kafka-3:9092"
        - name: ENVIRONMENT
          value: "prod"
        resources:
          requests:
            memory: "1Gi"
            cpu: "1000m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8082
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8082
          initialDelaySeconds: 20
          periodSeconds: 5
```

### 3. Configure Monitoring

#### Prometheus ServiceMonitor
```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: kafka-app-metrics
  namespace: kafka-app-prod
spec:
  selector:
    matchLabels:
      app: producer-service
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
```

#### Alerting Rules
```yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: kafka-app-alerts
  namespace: kafka-app-prod
spec:
  groups:
  - name: kafka-app
    interval: 30s
    rules:
    - alert: HighProducerFailureRate
      expr: rate(kafka_producer_messages_failed_total[5m]) > 0.01
      for: 5m
      labels:
        severity: critical
      annotations:
        summary: "High producer failure rate"
        description: "Producer failure rate > 1% for 5 minutes"
    
    - alert: ConsumerLagHigh
      expr: kafka_consumer_lag > 10000
      for: 10m
      labels:
        severity: warning
      annotations:
        summary: "Consumer lag is high"
        description: "Consumer lag > 10000 messages"
    
    - alert: ServiceDown
      expr: up{job="kafka-app"} == 0
      for: 2m
      labels:
        severity: critical
      annotations:
        summary: "Service is down"
```

## 🔧 Configuration Management

### Environment-Specific Configs

#### Production (application-prod.yml)
```yaml
spring:
  kafka:
    bootstrap-servers: kafka-1:9092,kafka-2:9092,kafka-3:9092
    security:
      protocol: SASL_SSL
    ssl:
      key-store-location: file:/etc/kafka/certs/kafka.keystore.jks
      trust-store-location: file:/etc/kafka/certs/kafka.truststore.jks
    properties:
      sasl.mechanism: SCRAM-SHA-512
      
logging:
  level:
    root: INFO
    com.example: INFO
    org.apache.kafka: WARN
```

#### Staging (application-staging.yml)
```yaml
spring:
  kafka:
    bootstrap-servers: kafka-staging:9092
    
logging:
  level:
    root: INFO
    com.example: DEBUG
```

## 📊 Post-Deployment Verification

### 1. Health Checks
```bash
# Producer health
curl https://producer.example.com/actuator/health

# Consumer health
curl https://consumer.example.com/actuator/health
```

### 2. Test Message Flow
```bash
# Send test message
curl -X POST https://producer.example.com/api/events/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test-user",
    "email": "test@example.com",
    "eventType": "USER_CREATED"
  }'

# Check consumer logs
kubectl logs -n kafka-app-prod -l app=consumer-service --tail=100
```

### 3. Monitor Metrics
```bash
# Check Prometheus targets
kubectl port-forward -n monitoring prometheus-0 9090:9090
# Open http://localhost:9090/targets

# Check Grafana dashboards
kubectl port-forward -n monitoring grafana-0 3000:3000
# Open http://localhost:3000
```

### 4. Verify Kafka Topics
```bash
# List topics
kafka-topics --bootstrap-server kafka-1:9092 --list

# Check topic config
kafka-topics --bootstrap-server kafka-1:9092 --describe --topic user-events

# Check consumer groups
kafka-consumer-groups --bootstrap-server kafka-1:9092 --list
```

## 🔄 Rolling Updates

### Zero-Downtime Deployment
```bash
# Update producer with rolling restart
kubectl set image deployment/producer-service \
  producer=your-registry/producer-service:1.1.0 \
  -n kafka-app-prod

# Monitor rollout
kubectl rollout status deployment/producer-service -n kafka-app-prod

# If issues, rollback
kubectl rollout undo deployment/producer-service -n kafka-app-prod
```

### Consumer Updates
```bash
# Drain consumers gracefully
kubectl scale deployment/consumer-service --replicas=0 -n kafka-app-prod

# Wait for processing to complete (check lag)
kafka-consumer-groups --bootstrap-server kafka-1:9092 \
  --describe --group consumer-group

# Deploy new version
kubectl set image deployment/consumer-service \
  consumer=your-registry/consumer-service:1.1.0 \
  -n kafka-app-prod

# Scale up
kubectl scale deployment/consumer-service --replicas=3 -n kafka-app-prod
```

## 🚨 Disaster Recovery

### Backup Strategy
1. **Topics backup**: Use Kafka Mirror Maker 2
2. **Database backup**: Regular snapshots
3. **Configuration backup**: Store in Git

### Recovery Procedures

#### Kafka Cluster Failure
```bash
# 1. Check cluster status
kafka-broker-api-versions --bootstrap-server kafka-1:9092

# 2. Identify failed brokers
kubectl get pods -n kafka

# 3. Restart failed brokers
kubectl delete pod kafka-1 -n kafka

# 4. Verify recovery
kafka-topics --bootstrap-server kafka-1:9092 --describe
```

#### Producer/Consumer Failure
```bash
# 1. Check pod status
kubectl get pods -n kafka-app-prod

# 2. Check logs
kubectl logs -n kafka-app-prod pod-name

# 3. Restart deployment
kubectl rollout restart deployment/producer-service -n kafka-app-prod

# 4. Verify recovery
curl https://producer.example.com/actuator/health
```

## 📈 Scaling Guidelines

### Horizontal Scaling

#### When to Scale Up
- Consumer lag consistently > 10,000 messages
- CPU > 70% for 15 minutes
- Memory > 80%

#### Producer Scaling
```bash
# Scale producers based on request volume
kubectl scale deployment/producer-service --replicas=5 -n kafka-app-prod
```

#### Consumer Scaling
```bash
# Scale consumers (max = partition count)
kubectl scale deployment/consumer-service --replicas=6 -n kafka-app-prod
```

### Vertical Scaling
```yaml
resources:
  requests:
    memory: "2Gi"  # Increased from 1Gi
    cpu: "2000m"   # Increased from 1000m
  limits:
    memory: "4Gi"
    cpu: "4000m"
```

## 🔍 Troubleshooting

### Common Issues

#### High Consumer Lag
**Symptoms**: Lag metric increasing
**Diagnosis**:
```bash
kafka-consumer-groups --bootstrap-server kafka-1:9092 \
  --describe --group consumer-group
```
**Solutions**:
1. Scale consumers horizontally
2. Increase partition count
3. Optimize processing code
4. Increase `max.poll.records`

#### Rebalancing Loops
**Symptoms**: Frequent rebalances in logs
**Diagnosis**:
```bash
kubectl logs -n kafka-app-prod consumer-pod | grep rebalanc
```
**Solutions**:
1. Increase `session.timeout.ms`
2. Decrease `max.poll.records`
3. Optimize processing time
4. Check network stability

#### OOM Errors
**Symptoms**: Pod restarts, OOMKilled
**Diagnosis**:
```bash
kubectl describe pod consumer-pod -n kafka-app-prod
```
**Solutions**:
1. Increase memory limits
2. Decrease `max.poll.records`
3. Reduce batch size
4. Add JVM heap tuning

## 📝 Maintenance Tasks

### Weekly
- [ ] Review consumer lag metrics
- [ ] Check DLQ topics for messages
- [ ] Review error logs

### Monthly
- [ ] Review and update retention policies
- [ ] Optimize partition count based on usage
- [ ] Review and update resource limits
- [ ] Security patches and updates

### Quarterly
- [ ] Capacity planning review
- [ ] Disaster recovery drill
- [ ] Performance benchmarking
- [ ] Security audit

## 📞 Support Contacts

- **On-Call**: on-call@example.com
- **Platform Team**: platform@example.com
- **Documentation**: https://wiki.example.com/kafka-app
- **Runbook**: https://runbook.example.com/kafka-app
