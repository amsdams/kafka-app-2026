# Multi-stage build for optimized image size
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-25-alpine AS builder

WORKDIR /build

# Copy parent pom and shared-models first (for layer caching)
COPY pom.xml .
COPY shared-models/pom.xml shared-models/
COPY shared-models/src shared-models/src

# Build shared-models
RUN mvn install -N
RUN mvn -X -f shared-models/pom.xml clean install -DskipTests

# Copy consumer-service
COPY consumer-service/pom.xml consumer-service/
COPY consumer-service/src consumer-service/src

# Build consumer-service
RUN mvn -f consumer-service/pom.xml clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine

# Add metadata
LABEL maintainer="your-team@example.com"
LABEL description="Kafka Consumer Service with Best Practices"
LABEL version="1.0.0"

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/consumer-service/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# JVM options for production
ENV JAVA_OPTS="-Xms1024m -Xmx2048m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/app/logs/heap-dump.hprof \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
