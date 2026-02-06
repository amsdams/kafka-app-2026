# Spring Boot 4 Migration Guide

This document outlines the changes made to upgrade the Kafka application from Spring Boot 3.5.10 to Spring Boot 4.0.2.

## What's Changed

### Version Updates

| Component | Old Version | New Version         |
|-----------|-------------|---------------------|
| Spring Boot | 3.5.10 | 4.0.2               |
| SpringDoc OpenAPI | 2.3.0 | 2.8.4               |
| Java | 21 | 25                  |
| Maven Compiler Plugin | 3.15.0 | 3.15.0 (unchanged)  |
| Lombok | 1.18.42 | 1.18.42 (unchanged) |

### Breaking Changes & Compatibility

#### 1. Spring Boot 4.0 Changes
Spring Boot 4.0 is a major release with several important changes:

- **Jakarta EE 11**: Requires Jakarta EE 11 (previously Jakarta EE 10)
- **Minimum Java Version**: Java 25 (we're already using this)
- **Spring Framework 7**: Updated to Spring Framework 7.0
- **Spring Kafka**: Updated to latest compatible version

#### 2. Configuration Changes
The Kafka deserialization configuration has been fixed to handle the type mapping issue:

```yaml
spring:
  kafka:
    consumer:
      properties:
        spring.json.trusted.packages: "*"
        spring.json.type.mapping: com.example.producer.model.UserEvent:com.example.consumer.model.UserEvent
```

This ensures proper deserialization when producer and consumer have different package structures.

### New Features Added

#### GitHub Actions Workflows

1. **CI - Pull Request** (`.github/workflows/ci-pr.yml`)
   - Runs on every pull request to `main` or `develop`
   - Matrix build for both services
   - Runs tests and code quality checks
   - Builds Docker images
   - Integration tests with Kafka

2. **CD - Main** (`.github/workflows/cd-main.yml`)
   - Runs on push to `main` branch
   - Builds and tests both services
   - Builds and pushes Docker images to GitHub Container Registry
   - Deploys to production environment
   - Multi-platform Docker builds (linux/amd64, linux/arm64)

3. **Dependabot** (`.github/dependabot.yml`)
   - Weekly dependency updates
   - Separate updates for Maven, Docker, and GitHub Actions
   - Grouped updates for related dependencies
   - Automatic PR creation

## Migration Steps

### 1. Prerequisites
- Java 25 installed
- Maven 3.9+
- Docker and Docker Compose
- Git

### 2. Update Your Local Environment

```bash
# Pull the latest changes
git pull origin main

# Clean previous builds
./mvnw clean

# Build with new Spring Boot 4
./mvnw clean install
```

### 3. Test Locally

```bash
# Start infrastructure
docker-compose up -d kafka

# Build and start services
./start.sh

# Test the services
./test-messages.sh
```

### 4. Verify Tests Pass

```bash
# Run tests for consumer
cd consumer-service
mvn test

# Run tests for producer
cd ../producer-service
mvn test
```

## Known Issues & Solutions

### Issue 1: Kafka Deserialization Error
**Error**: `java.lang.ClassNotFoundException: com.example.producer.model.UserEvent`

**Solution**: Already fixed in `consumer-service/src/main/resources/application.yml` with proper type mapping:
```yaml
spring.json.type.mapping: com.example.producer.model.UserEvent:com.example.consumer.model.UserEvent
```

### Issue 2: Docker Build Issues
If you encounter Docker build issues, ensure you're using a recent Docker version:
```bash
docker --version  # Should be 20.10+
docker buildx version  # Should be 0.11+
```

## GitHub Actions Setup

### Required Secrets
Add these secrets in your GitHub repository settings:

1. `GITHUB_TOKEN` - Automatically provided by GitHub Actions
2. (Optional) `SLACK_WEBHOOK` - For deployment notifications

### Environment Configuration
Configure the `production` environment in GitHub:
1. Go to Settings → Environments
2. Create `production` environment
3. Add protection rules as needed
4. Add environment secrets if required

## Deployment

### GitHub Container Registry
Docker images are automatically published to:
```
ghcr.io/<your-org>/<repo>/consumer-service:latest
ghcr.io/<your-org>/<repo>/producer-service:latest
```

### Pull and Run Images
```bash
# Login to GitHub Container Registry
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Pull images
docker pull ghcr.io/<your-org>/<repo>/consumer-service:latest
docker pull ghcr.io/<your-org>/<repo>/producer-service:latest

# Update docker-compose.yml to use these images
```

## Monitoring & Health Checks

Both services expose actuator endpoints:
- Health: `http://localhost:8081/actuator/health` (producer)
- Health: `http://localhost:8082/actuator/health` (consumer)
- Prometheus: `http://localhost:808x/actuator/prometheus`

## Rollback Strategy

If issues occur in production:

1. **Quick Rollback**:
   ```bash
   # Revert to previous version
   docker-compose down
   git checkout <previous-version>
   docker-compose up -d
   ```

2. **GitHub Actions**:
   - Re-run previous successful deployment
   - Or manually deploy specific SHA

## Next Steps

1. **Review Workflows**: Customize the GitHub Actions workflows for your specific needs
2. **Add Tests**: Expand test coverage for integration scenarios
3. **Configure Environments**: Set up staging/production environments
4. **Add Monitoring**: Integrate with monitoring tools (Prometheus, Grafana)
5. **Security Scanning**: Add security scanning to CI pipeline

## Resources

- [Spring Boot 4.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes)
- [Spring Framework 7.0 What's New](https://github.com/spring-projects/spring-framework/wiki/What%27s-New-in-Spring-Framework-7.x)
- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/reference/)

## Support

For issues or questions:
1. Check the GitHub Issues
2. Review the ARCHITECTURE.md documentation
3. Consult Spring Boot migration guides
