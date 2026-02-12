# Producer Service Improvements

## 1. DTO Layer (API Contract)

### UserEventRequest.java
```java
package com.example.producer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEventRequest {
    @NotBlank(message = "Username is required")
    private String username;
    
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Event type is required")
    private String eventType;
}
```

### OrderEventRequest.java
```java
package com.example.producer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventRequest {
    @NotBlank(message = "User ID is required")
    private String userId;  // Links to UserEvent
    
    @NotBlank(message = "Product name is required")
    private String productName;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Event type is required")
    private String eventType;  // ORDER_CREATED, ORDER_COMPLETED, etc.
}
```

## 2. Domain Models (Kafka Messages)

### UserEvent.java
```java
package com.example.producer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    private String id;
    private String username;
    private String email;
    private String eventType;
    private LocalDateTime timestamp;
    private String correlationId;  // For tracing
}
```

### OrderEvent.java
```java
package com.example.producer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String id;
    private String userId;  // Foreign key to UserEvent
    private String productName;
    private BigDecimal amount;
    private String eventType;
    private LocalDateTime timestamp;
    private String correlationId;  // For tracing
}
```

## 3. Base Event Interface (Optional but Recommended)

### BaseEvent.java
```java
package com.example.producer.model;

import java.time.LocalDateTime;

public interface BaseEvent {
    String getId();
    void setId(String id);
    LocalDateTime getTimestamp();
    void setTimestamp(LocalDateTime timestamp);
    String getCorrelationId();
    void setCorrelationId(String correlationId);
}
```

Then have both events implement this interface.

## 4. Mappers (DTO to Domain)

### EventMapper.java
```java
package com.example.producer.mapper;

import com.example.producer.dto.OrderEventRequest;
import com.example.producer.dto.UserEventRequest;
import com.example.producer.model.OrderEvent;
import com.example.producer.model.UserEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class EventMapper {
    
    public UserEvent toUserEvent(UserEventRequest request) {
        return UserEvent.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .email(request.getEmail())
                .eventType(request.getEventType())
                .timestamp(LocalDateTime.now())
                .correlationId(UUID.randomUUID().toString())
                .build();
    }
    
    public OrderEvent toOrderEvent(OrderEventRequest request) {
        return OrderEvent.builder()
                .id(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .productName(request.getProductName())
                .amount(request.getAmount())
                .eventType(request.getEventType())
                .timestamp(LocalDateTime.now())
                .correlationId(UUID.randomUUID().toString())
                .build();
    }
}
```

## 5. Updated Producer Service (Generic)

### KafkaProducerService.java
```java
package com.example.producer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T> CompletableFuture<SendResult<String, Object>> sendMessage(
            String topic, String key, T event) {
        
        log.info("Sending message to topic: {} with key: {}", topic, key);
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Message sent successfully to {}: offset={}, partition={}", 
                    topic,
                    result.getRecordMetadata().offset(),
                    result.getRecordMetadata().partition());
            } else {
                log.error("Failed to send message to {}: {}", topic, event, ex);
            }
        });
        
        return future;
    }
}
```

## 6. Updated Controller

### ProducerController.java
```java
package com.example.producer.controller;

import com.example.producer.dto.OrderEventRequest;
import com.example.producer.dto.UserEventRequest;
import com.example.producer.mapper.EventMapper;
import com.example.producer.model.OrderEvent;
import com.example.producer.model.UserEvent;
import com.example.producer.service.KafkaProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/producer")
@RequiredArgsConstructor
public class ProducerController {

    private final KafkaProducerService producerService;
    private final EventMapper eventMapper;
    
    @Value("${kafka.topic.user-events}")
    private String userEventsTopic;
    
    @Value("${kafka.topic.order-events}")
    private String orderEventsTopic;

    @PostMapping("/users")
    public ResponseEntity<String> publishUserEvent(
            @Valid @RequestBody UserEventRequest request) {
        
        UserEvent event = eventMapper.toUserEvent(request);
        producerService.sendMessage(userEventsTopic, event.getId(), event);
        
        return ResponseEntity.ok("User event published with ID: " + event.getId());
    }
    
    @PostMapping("/orders")
    public ResponseEntity<String> publishOrderEvent(
            @Valid @RequestBody OrderEventRequest request) {
        
        OrderEvent event = eventMapper.toOrderEvent(request);
        producerService.sendMessage(orderEventsTopic, event.getId(), event);
        
        return ResponseEntity.ok("Order event published with ID: " + event.getId());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Producer Service is running!");
    }
}
```

## 7. Updated Kafka Configuration

### KafkaProducerConfig.java
```java
package com.example.producer.config;

import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties properties) {
        return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
```

### KafkaTopicConfig.java
```java
package com.example.producer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.user-events}")
    private String userEventsTopic;
    
    @Value("${kafka.topic.order-events}")
    private String orderEventsTopic;

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder
                .name(userEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder
                .name(orderEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
```

## 8. Updated application.yml

```yaml
server:
  port: 8081

spring:
  application:
    name: producer-service
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:29092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      properties:
        linger.ms: 1
        batch.size: 16384
        enable.idempotence: true
        # Add type mapping for polymorphic deserialization
        spring.json.type.mapping: userEvent:com.example.producer.model.UserEvent,orderEvent:com.example.producer.model.OrderEvent

kafka:
  topic:
    user-events: user-events
    order-events: order-events
```

## 9. Example Usage

### Create User Event
```bash
curl -X POST http://localhost:8081/api/producer/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "eventType": "USER_CREATED"
  }'
```

### Create Order Event (linked to user)
```bash
curl -X POST http://localhost:8081/api/producer/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-id-from-previous-response",
    "productName": "Premium Subscription",
    "amount": 99.99,
    "eventType": "ORDER_CREATED"
  }'
```
