# Troubleshooting Guide

## Build Issues

### Maven Build Fails

#### Issue: "cannot find symbol" errors
**Symptoms:**
```
[ERROR] cannot find symbol: method sendMessage(String,String,OrderEvent)
```

**Solution:**
Build in the correct order:
```bash
# Clean everything first
mvn clean

# Build shared-models first
mvn -f shared-models/pom.xml install

# Then build services
mvn -f producer-service/pom.xml package
mvn -f consumer-service/pom.xml package

# Or build everything at once
mvn clean install
```

#### Issue: Java version mismatch
**Symptoms:**
```
[ERROR] Source option 25 is no longer supported. Use 21 or later.
```

**Solution:**
Ensure you have Java 25 installed:
```bash
java -version  # Should show Java 25

# If not, install Java 25 or update pom.xml to use your version
# Change <java.version>25</java.version> to your version
```

#### Issue: Maven not found
**Symptoms:**
```
mvn: command not found
```

**Solution:**
Install Maven:
```bash
# macOS
brew install maven

# Ubuntu/Debian
sudo apt-get install maven

# Or use Maven wrapper (included)
./mvnw clean install
```

---

## Docker Issues

### Docker Build Fails

#### Issue: Cannot connect to Docker daemon
**Symptoms:**
```
Cannot connect to the Docker daemon
```

**Solution:**
Start Docker Desktop:
```bash
# macOS: Start Docker Desktop app
# Linux: 
sudo systemctl start docker
```

#### Issue: Multi-stage build fails
**Symptoms:**
```
ERROR [builder 3/5] RUN mvn -f shared-models/pom.xml clean install
```

**Solution:**
Build locally first to verify Maven configuration:
```bash
./verify-build.sh
```

#### Issue: Port already in use
**Symptoms:**
```
Bind for 0.0.0.0:8081 failed: port is already allocated
```

**Solution:**
```bash
# Find what's using the port
lsof -i :8081

# Kill the process or change port in docker-compose.yml
ports:
  - "8181:8081"  # Change external port
```

---

## Runtime Issues

### Kafka Not Starting

#### Issue: Kafka health check failing
**Symptoms:**
```
kafka | [error] health check failed
```

**Solution:**
```bash
# Wait longer for Kafka to start (30-60 seconds)
docker logs kafka

# If still failing, increase memory
docker-compose down -v
# Edit docker-compose.yml, add:
# deploy:
#   resources:
#     limits:
#       memory: 2G
docker-compose up -d
```

### Services Not Starting

#### Issue: Producer/Consumer failing to start
**Symptoms:**
```
producer-service | Connection to node -1 could not be established
```

**Solution:**
Services are trying to connect before Kafka is ready:
```bash
# Stop everything
docker-compose down -v

# Start Kafka first
docker-compose up -d kafka

# Wait 30 seconds
sleep 30

# Start services
docker-compose up -d producer-service consumer-service
```

### Health Check Fails

#### Issue: Service shows as unhealthy
**Symptoms:**
```bash
curl http://localhost:8081/actuator/health
# Returns error or "DOWN"
```

**Solution:**
```bash
# Check logs
docker logs producer-service

# Common issues:
# 1. Kafka not connected
# 2. Port conflict
# 3. Out of memory

# Check if port is accessible
netstat -an | grep 8081

# Restart the service
docker-compose restart producer-service
```

---

## Testing Issues

### Test Messages Not Being Consumed

#### Issue: Messages sent but not consumed
**Symptoms:**
Producer sends successfully, but consumer doesn't log anything

**Solution:**
```bash
# Check if consumer is running
docker ps | grep consumer

# Check consumer logs
docker logs -f consumer-service

# Verify consumer group
docker exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe --group events-consumer-group

# Check if messages are in topic
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning
```

---

## Performance Issues

### High Memory Usage

**Symptoms:**
Docker consuming too much memory

**Solution:**
Adjust JVM settings in docker-compose.yml:
```yaml
environment:
  JAVA_OPTS: >
    -Xms256m -Xmx512m  # Reduce from defaults
```

### Slow Builds

**Symptoms:**
Docker builds taking very long

**Solution:**
```bash
# Use build cache
docker-compose build --parallel

# Or build images separately
docker build -t producer:latest -f producer-service/Dockerfile .

# Clean up old images
docker system prune -a
```

---

## Common Commands

### Reset Everything
```bash
# Stop and remove all containers, volumes, and images
docker-compose down -v
docker system prune -a -f
mvn clean
```

### View All Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker logs -f producer-service
docker logs -f consumer-service
docker logs -f kafka
```

### Check Service Status
```bash
# Docker Compose
docker-compose ps

# Health checks
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health

# Kafka
docker exec kafka kafka-broker-api-versions \
  --bootstrap-server localhost:9092
```

### Rebuild Specific Service
```bash
# Rebuild just producer
docker-compose up -d --build --force-recreate producer-service

# Rebuild just consumer
docker-compose up -d --build --force-recreate consumer-service
```

---

## Still Having Issues?

### Debug Checklist

- [ ] Java 25+ installed?
- [ ] Maven 3.6+ installed?
- [ ] Docker running?
- [ ] Ports 8081, 8082, 9092 available?
- [ ] Enough disk space? (2GB+)
- [ ] Enough memory? (4GB+ recommended)
- [ ] Built in correct order? (shared-models first)
- [ ] All dependencies downloaded?

### Get Help

1. **Check logs**: Always check Docker logs first
   ```bash
   docker-compose logs -f
   ```

2. **Run verification script**:
   ```bash
   ./verify-build.sh
   ```

3. **Clean rebuild**:
   ```bash
   make clean
   make up
   ```

4. **Enable debug logging**:
   Edit `application.yml`:
   ```yaml
   logging:
     level:
       root: DEBUG
   ```
