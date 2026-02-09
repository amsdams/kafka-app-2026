# Recommended Project Structure

## Option 1: Monorepo with Shared Module (RECOMMENDED)

```
kafka-microservices/
├── pom.xml (parent POM)
├── docker-compose.yml
├── shared-models/          # NEW: Common models shared across services
│   ├── pom.xml
│   └── src/main/java/com/example/common/
│       ├── model/
│       │   ├── UserEvent.java
│       │   ├── OrderEvent.java
│       │   └── BaseEvent.java (interface)
│       ├── dto/
│       │   ├── EventStatus.java
│       │   └── EventMetadata.java
│       └── constants/
│           └── Topics.java
│
├── producer-service/
│   ├── pom.xml (depends on shared-models)
│   └── src/main/java/com/example/producer/
│       ├── ProducerServiceApplication.java
│       ├── config/
│       │   ├── KafkaProducerConfig.java
│       │   ├── KafkaTopicConfig.java
│       │   └── OpenApiConfig.java
│       ├── controller/
│       │   ├── UserEventController.java
│       │   ├── OrderEventController.java
│       │   └── HealthController.java
│       ├── dto/
│       │   ├── UserEventRequest.java
│       │   ├── OrderEventRequest.java
│       │   └── EventResponse.java
│       ├── mapper/
│       │   ├── UserEventMapper.java
│       │   ├── OrderEventMapper.java
│       │   └── EventMapperConfig.java
│       ├── service/
│       │   ├── KafkaProducerService.java
│       │   ├── UserEventService.java
│       │   └── OrderEventService.java
│       └── exception/
│           ├── EventPublishException.java
│           └── GlobalExceptionHandler.java
│
└── consumer-service/
    ├── pom.xml (depends on shared-models)
    └── src/main/java/com/example/consumer/
        ├── ConsumerServiceApplication.java
        ├── config/
        │   ├── KafkaConsumerConfig.java
        │   ├── DLQConfig.java
        │   └── MetricsConfig.java
        ├── handler/
        │   ├── EventHandler.java (interface)
        │   ├── UserEventHandler.java
        │   ├── OrderEventHandler.java
        │   └── DLQHandler.java
        ├── service/
        │   ├── KafkaConsumerService.java
        │   ├── UserEventProcessor.java
        │   ├── OrderEventProcessor.java
        │   └── EventEnrichmentService.java
        ├── repository/
        │   ├── UserEventRepository.java
        │   └── OrderEventRepository.java
        ├── entity/
        │   ├── ProcessedUserEvent.java
        │   └── ProcessedOrderEvent.java
        └── metrics/
            └── ConsumerMetrics.java
```

## Option 2: Separate Repositories (Microservices Style)

```
kafka-shared-models/        # Separate repository
├── pom.xml
└── src/main/java/com/example/common/
    └── model/
        ├── UserEvent.java
        └── OrderEvent.java

producer-service/           # Separate repository
├── pom.xml (depends on kafka-shared-models artifact)
└── ... (same structure as above)

consumer-service/           # Separate repository
├── pom.xml (depends on kafka-shared-models artifact)
└── ... (same structure as above)
```

## Parent POM Configuration

### pom.xml (Root)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>kafka-microservices-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>shared-models</module>
        <module>producer-service</module>
        <module>consumer-service</module>
    </modules>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.2</version>
    </parent>

    <properties>
        <java.version>25</java.version>
        <spring-kafka.version>3.3.1</spring-kafka.version>
        <lombok.version>1.18.36</lombok.version>
        <mapstruct.version>1.6.3</mapstruct.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Shared Models -->
            <dependency>
                <groupId>com.example</groupId>
                <artifactId>shared-models</artifactId>
                <version>${project.version}</version>
            </dependency>

            <!-- Spring Kafka -->
            <dependency>
                <groupId>org.springframework.kafka</groupId>
                <artifactId>spring-kafka</artifactId>
                <version>${spring-kafka.version}</version>
            </dependency>

            <!-- Lombok -->
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </dependency>

            <!-- MapStruct for mapping -->
            <dependency>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct</artifactId>
                <version>${mapstruct.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

## Shared Models Module

### shared-models/pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example</groupId>
        <artifactId>kafka-microservices-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>shared-models</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
        </dependency>
        
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
        </dependency>
    </dependencies>
</project>
```

## Producer Service Module

### producer-service/pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example</groupId>
        <artifactId>kafka-microservices-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>producer-service</artifactId>

    <dependencies>
        <!-- Shared Models -->
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>shared-models</artifactId>
        </dependency>

        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- MapStruct -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## Benefits of This Structure

### 1. **Shared Models Module**
- ✅ Single source of truth for event models
- ✅ Version control for schemas
- ✅ No code duplication
- ✅ Easier to maintain consistency

### 2. **Clean Separation**
- ✅ DTOs separate from domain models
- ✅ API contracts (DTOs) can evolve independently
- ✅ Internal models stay stable

### 3. **Scalability**
- ✅ Easy to add new event types
- ✅ Easy to add new consumers for same events
- ✅ Services can be deployed independently

### 4. **Testability**
- ✅ Each layer can be tested independently
- ✅ Mappers are testable
- ✅ Handlers are testable

### 5. **Maintainability**
- ✅ Clear responsibility boundaries
- ✅ Easier to onboard new developers
- ✅ Better code organization

## Migration Path from Current Structure

1. **Phase 1**: Create shared-models module
   - Extract UserEvent to shared-models
   - Update producer and consumer to depend on it
   
2. **Phase 2**: Add DTOs in producer
   - Create UserEventRequest/Response
   - Add mappers
   - Update controller to use DTOs

3. **Phase 3**: Add OrderEvent
   - Add OrderEvent to shared-models
   - Add OrderEventRequest DTO
   - Create new topic and handlers

4. **Phase 4**: Refactor consumer
   - Add handler pattern
   - Separate concerns (handler vs processor)

5. **Phase 5**: Add production features
   - DLQ
   - Metrics
   - Retry logic
```
