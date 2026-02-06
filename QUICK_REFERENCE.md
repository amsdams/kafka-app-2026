# Quick Reference Guide

## 🚀 Common Commands

### Local Development

```bash
# Build both services
cd consumer-service && mvn clean install && cd ..
cd producer-service && mvn clean install && cd ..

# Run tests
cd consumer-service && mvn test && cd ..
cd producer-service && mvn test && cd ..

# Start infrastructure only
docker-compose up -d kafka zookeeper

# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# Clean rebuild everything
docker-compose down -v
docker-compose up --build -d

# View logs
docker-compose logs -f consumer-service
docker-compose logs -f producer-service
```

### Testing Endpoints

```bash
# Health checks
curl http://localhost:8081/actuator/health  # Producer
curl http://localhost:8082/actuator/health  # Consumer

# Send test message
curl -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "email": "test@example.com",
    "eventType": "TEST_EVENT"
  }'

# Check Swagger UI
open http://localhost:8081/swagger-ui.html  # Producer
open http://localhost:8082/swagger-ui.html  # Consumer

# Prometheus metrics
curl http://localhost:8081/actuator/prometheus
curl http://localhost:8082/actuator/prometheus
```

### Kafka Commands

```bash
# List topics
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

# Describe topic
docker exec -it kafka kafka-topics --describe --topic user-events --bootstrap-server localhost:9092

# Consume messages from beginning
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning

# Check consumer group
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe --group user-events-consumer-group
```

---

## 📋 GitHub Actions Workflows

### Trigger CI on Pull Request

```bash
# Create feature branch
git checkout -b feature/my-feature

# Make changes and commit
git add .
git commit -m "feat: add new feature"

# Push to GitHub
git push origin feature/my-feature

# Create PR on GitHub
# CI pipeline will automatically run
```

### Monitor Workflow Status

```bash
# View workflow runs
# Go to: https://github.com/<org>/<repo>/actions

# Download logs
gh run download <run-id>

# Re-run failed workflow
gh run rerun <run-id>

# Watch workflow in terminal
gh run watch
```

### Manual Workflow Dispatch

```bash
# Trigger workflow manually (if configured)
gh workflow run "CD - Deploy to Production"

# Check status
gh run list --workflow="CD - Deploy to Production"
```

---

## 🐳 Docker Image Management

### Pull Images from GHCR

```bash
# Login to GitHub Container Registry
echo $GITHUB_TOKEN | docker login ghcr.io -u $GITHUB_USERNAME --password-stdin

# Pull latest images
docker pull ghcr.io/<org>/<repo>/consumer-service:latest
docker pull ghcr.io/<org>/<repo>/producer-service:latest

# Pull specific version
docker pull ghcr.io/<org>/<repo>/consumer-service:main-abc1234
```

### Build and Push Manually

```bash
# Build multi-platform images
docker buildx build --platform linux/amd64,linux/arm64 \
  -t ghcr.io/<org>/<repo>/consumer-service:latest \
  --push \
  ./consumer-service

docker buildx build --platform linux/amd64,linux/arm64 \
  -t ghcr.io/<org>/<repo>/producer-service:latest \
  --push \
  ./producer-service
```

### Clean Up Images

```bash
# Remove old local images
docker image prune -a

# List all tags for an image (using GitHub API)
curl -H "Authorization: Bearer $GITHUB_TOKEN" \
  https://api.github.com/orgs/<org>/packages/container/<repo>%2Fconsumer-service/versions
```

---

## 🔧 Maven Commands

### Build & Package

```bash
# Clean build
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Run specific test
mvn test -Dtest=KafkaConsumerServiceTest

# Package without tests
mvn package -DskipTests

# Verify (runs tests + quality checks)
mvn verify
```

### Dependency Management

```bash
# Check for updates
mvn versions:display-dependency-updates

# Display dependency tree
mvn dependency:tree

# Update Spring Boot version
mvn versions:update-parent

# Check for security vulnerabilities
mvn dependency:analyze
```

---

## 🔍 Troubleshooting

### Check Service Health

```bash
# Consumer service
curl http://localhost:8082/actuator/health | jq

# Producer service
curl http://localhost:8081/actuator/health | jq

# Detailed health
curl http://localhost:8082/actuator/health/liveness | jq
curl http://localhost:8082/actuator/health/readiness | jq
```

### Debug Kafka Issues

```bash
# Check if Kafka is running
docker ps | grep kafka

# Check Kafka logs
docker logs kafka

# Test Kafka connectivity
docker exec -it kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# Reset consumer group offset
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group user-events-consumer-group \
  --reset-offsets --to-earliest --topic user-events --execute
```

### Debug Application Issues

```bash
# View consumer logs
docker logs -f consumer-service --tail 100

# View producer logs
docker logs -f producer-service --tail 100

# Enter container shell
docker exec -it consumer-service /bin/sh

# Check Java process
docker exec consumer-service ps aux | grep java

# Check environment variables
docker exec consumer-service env | grep SPRING
```

### Fix Common Issues

```bash
# Port already in use
lsof -ti:8081 | xargs kill -9
lsof -ti:8082 | xargs kill -9

# Clean Maven cache
rm -rf ~/.m2/repository

# Clean Docker volumes
docker-compose down -v
docker volume prune

# Restart everything
docker-compose down
docker-compose up --build -d
```

---

## 📊 Monitoring & Metrics

### Actuator Endpoints

```bash
# All endpoints
curl http://localhost:8082/actuator | jq

# Application info
curl http://localhost:8082/actuator/info | jq

# Environment variables
curl http://localhost:8082/actuator/env | jq

# Configuration properties
curl http://localhost:8082/actuator/configprops | jq

# Thread dump
curl http://localhost:8082/actuator/threaddump | jq

# Heap dump (large file)
curl http://localhost:8082/actuator/heapdump -o heapdump.bin
```

### Kafka Metrics

```bash
# Consumer lag
docker exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe --group user-events-consumer-group

# Topic stats
docker exec kafka kafka-run-class kafka.tools.GetOffsetShell \
  --broker-list localhost:9092 \
  --topic user-events
```

---

## 🔐 Security

### Scan for Vulnerabilities

```bash
# Maven dependency check
mvn dependency-check:check

# Docker image scanning (if Trivy installed)
trivy image consumer-service:latest
trivy image producer-service:latest

# Check for secrets in code
git secrets --scan
```

### Update Dependencies

```bash
# Check for outdated dependencies
mvn versions:display-dependency-updates

# Update all dependencies to latest
mvn versions:use-latest-releases

# Update Spring Boot version
mvn versions:update-parent -DparentVersion=4.0.2
```

---

## 📦 Deployment

### Production Deployment

```bash
# SSH to production server
ssh user@production-server

# Pull latest images
cd /app/kafka-app
docker-compose pull

# Update services (zero-downtime)
docker-compose up -d

# Check service health
docker-compose ps
curl http://localhost:8082/actuator/health

# View recent logs
docker-compose logs --tail 50 -f
```

### Rollback

```bash
# Rollback to previous version
docker-compose down
git checkout <previous-commit>
docker-compose up -d

# Or rollback specific service
docker-compose up -d --no-deps consumer-service
```

---

## 🎯 Useful Aliases

Add to your `~/.bashrc` or `~/.zshrc`:

```bash
# Kafka app aliases
alias kafka-up='docker-compose up -d'
alias kafka-down='docker-compose down'
alias kafka-logs='docker-compose logs -f'
alias kafka-rebuild='docker-compose down -v && docker-compose up --build -d'

# Consumer service
alias consumer-logs='docker logs -f consumer-service'
alias consumer-health='curl http://localhost:8082/actuator/health | jq'
alias consumer-shell='docker exec -it consumer-service /bin/sh'

# Producer service
alias producer-logs='docker logs -f producer-service'
alias producer-health='curl http://localhost:8081/actuator/health | jq'
alias producer-shell='docker exec -it producer-service /bin/sh'

# Testing
alias send-test='curl -X POST http://localhost:8081/api/events/publish -H "Content-Type: application/json" -d '"'"'{"username":"test","email":"test@example.com","eventType":"TEST"}'"'"

# Kafka commands
alias kafka-topics='docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092'
alias kafka-console='docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic user-events --from-beginning'
```

---

## 📚 Quick Links

### Documentation
- [README.md](../README.md) - Project overview
- [SPRING_BOOT_4_MIGRATION.md](../SPRING_BOOT_4_MIGRATION.md) - Migration guide
- [WORKFLOWS.md](.github/WORKFLOWS.md) - CI/CD documentation
- [ARCHITECTURE.md](../ARCHITECTURE.md) - System architecture

### External Resources
- [Spring Boot 4 Docs](https://docs.spring.io/spring-boot/docs/4.0.x/reference/)
- [Spring Kafka Docs](https://docs.spring.io/spring-kafka/reference/)
- [Docker Compose Docs](https://docs.docker.com/compose/)
- [GitHub Actions Docs](https://docs.github.com/en/actions)

### Endpoints
- Producer Swagger: http://localhost:8081/swagger-ui.html
- Consumer Swagger: http://localhost:8082/swagger-ui.html
- Producer Health: http://localhost:8081/actuator/health
- Consumer Health: http://localhost:8082/actuator/health
- Prometheus Metrics: http://localhost:808x/actuator/prometheus

---

## 🆘 Getting Help

1. **Check logs first**: `docker-compose logs -f`
2. **Review documentation**: See links above
3. **Check GitHub Actions**: Failed builds have detailed logs
4. **Search issues**: Check GitHub repository issues
5. **Ask team**: Reach out to team members
6. **Create issue**: Document problem with logs and steps to reproduce

---

## ✅ Pre-Deployment Checklist

```
□ All tests pass locally
□ Docker compose works locally
□ No secrets in code
□ Dependencies up to date
□ Documentation updated
□ GitHub Actions configured
□ Environment variables set
□ Health checks working
□ Monitoring configured
□ Rollback plan documented
□ Team notified
```

---

## 🎓 Learning Resources

### Spring Boot 4
- What's new in Spring Boot 4
- Spring Framework 7 features
- Jakarta EE 11 migration

### Kafka
- Kafka fundamentals
- Consumer groups
- Message serialization
- Partition strategies

### DevOps
- GitHub Actions workflows
- Docker multi-stage builds
- Container orchestration
- CI/CD best practices
