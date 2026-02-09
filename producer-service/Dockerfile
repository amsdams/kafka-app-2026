# Stage 1: Build
FROM maven:3-eclipse-temurin-25-alpine AS build
WORKDIR /app

# 1. Copy the Parent POM
COPY pom.xml .

# 2. Copy ALL module POMs (to satisfy the Parent's <modules> list)
COPY shared-models/pom.xml shared-models/
COPY producer-service/pom.xml producer-service/
COPY consumer-service/pom.xml consumer-service/

# 3. Copy the Shared Models SOURCE code
# IMPORTANT: This must happen BEFORE building the consumer
COPY shared-models/src shared-models/src

# 4. Install the Parent and the Shared Models module
# -N installs parent; then we install shared-models so its JAR is in the local repo
RUN mvn install -N
RUN mvn install -pl shared-models -DskipTests

# 5. Copy Consumer Service SOURCE code
COPY producer-service/src producer-service/src

# 6. Build the Consumer Service
WORKDIR /app/producer-service
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR from build stage
COPY --from=build /app/producer-service/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
