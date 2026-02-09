# Consumer Service Improvements

## 1. Domain Models (Same as Producer)

Copy the UserEvent and OrderEvent from the producer, or better yet, create a **shared-models** module.

## 2. Event Handlers Strategy Pattern

### EventHandler.java (Interface)
```java
package com.example.consumer.handler;

public interface EventHandler<T> {
    void handle(T event);
    boolean supports(String eventType);
}
```

### UserEventHandler.java
```java
package com.example.consumer.handler;

import com.example.consumer.model.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventHandler implements EventHandler<UserEvent> {

    @Override
    public void handle(UserEvent event) {
        log.info("Processing UserEvent: {}", event);
        
        switch (event.getEventType()) {
            case "USER_CREATED":
                handleUserCreated(event);
                break;
            case "USER_UPDATED":
                handleUserUpdated(event);
                break;
            case "USER_DELETED":
                handleUserDeleted(event);
                break;
            default:
                log.warn("Unknown user event type: {}", event.getEventType());
        }
    }

    @Override
    public boolean supports(String eventType) {
        return eventType != null && eventType.startsWith("USER_");
    }
    
    private void handleUserCreated(UserEvent event) {
        log.info("User created: {} with email: {}", event.getUsername(), event.getEmail());
        // Save to database, send welcome email, etc.
    }
    
    private void handleUserUpdated(UserEvent event) {
        log.info("User updated: {}", event.getUsername());
        // Update user in database
    }
    
    private void handleUserDeleted(UserEvent event) {
        log.info("User deleted: {}", event.getUsername());
        // Soft delete or archive user
    }
}
```

### OrderEventHandler.java
```java
package com.example.consumer.handler;

import com.example.consumer.model.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventHandler implements EventHandler<OrderEvent> {

    @Override
    public void handle(OrderEvent event) {
        log.info("Processing OrderEvent: {}", event);
        
        switch (event.getEventType()) {
            case "ORDER_CREATED":
                handleOrderCreated(event);
                break;
            case "ORDER_COMPLETED":
                handleOrderCompleted(event);
                break;
            case "ORDER_CANCELLED":
                handleOrderCancelled(event);
                break;
            default:
                log.warn("Unknown order event type: {}", event.getEventType());
        }
    }

    @Override
    public boolean supports(String eventType) {
        return eventType != null && eventType.startsWith("ORDER_");
    }
    
    private void handleOrderCreated(OrderEvent event) {
        log.info("Order created for user: {} - Product: {} - Amount: {}", 
            event.getUserId(), event.getProductName(), event.getAmount());
        // Save order to database, send confirmation email
    }
    
    private void handleOrderCompleted(OrderEvent event) {
        log.info("Order completed: {}", event.getId());
        // Update order status, trigger fulfillment
    }
    
    private void handleOrderCancelled(OrderEvent event) {
        log.info("Order cancelled: {}", event.getId());
        // Process refund, update inventory
    }
}
```

## 3. Updated Consumer Service

### KafkaConsumerService.java
```java
package com.example.consumer.service;

import com.example.consumer.handler.EventHandler;
import com.example.consumer.model.OrderEvent;
import com.example.consumer.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final List<EventHandler<UserEvent>> userEventHandlers;
    private final List<EventHandler<OrderEvent>> orderEventHandlers;

    @KafkaListener(
        topics = "${kafka.topic.user-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "userEventKafkaListenerContainerFactory"
    )
    public void consumeUserEvent(
            @Payload UserEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received UserEvent from partition: {}, offset: {}", partition, offset);
            log.info("UserEvent details: {}", event);
            
            // Find appropriate handler
            userEventHandlers.stream()
                .filter(handler -> handler.supports(event.getEventType()))
                .findFirst()
                .ifPresentOrElse(
                    handler -> handler.handle(event),
                    () -> log.warn("No handler found for event type: {}", event.getEventType())
                );
            
            acknowledgment.acknowledge();
            log.info("UserEvent acknowledged successfully");
            
        } catch (Exception e) {
            log.error("Error processing UserEvent: {}", event, e);
            // Send to DLQ or retry logic here
        }
    }

    @KafkaListener(
        topics = "${kafka.topic.order-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "orderEventKafkaListenerContainerFactory"
    )
    public void consumeOrderEvent(
            @Payload OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received OrderEvent from partition: {}, offset: {}", partition, offset);
            log.info("OrderEvent details: {}", event);
            
            // Find appropriate handler
            orderEventHandlers.stream()
                .filter(handler -> handler.supports(event.getEventType()))
                .findFirst()
                .ifPresentOrElse(
                    handler -> handler.handle(event),
                    () -> log.warn("No handler found for event type: {}", event.getEventType())
                );
            
            acknowledgment.acknowledge();
            log.info("OrderEvent acknowledged successfully");
            
        } catch (Exception e) {
            log.error("Error processing OrderEvent: {}", event, e);
            // Send to DLQ or retry logic here
        }
    }
}
```

## 4. Updated Kafka Consumer Configuration

### KafkaConsumerConfig.java
```java
package com.example.consumer.config;

import com.example.consumer.model.OrderEvent;
import com.example.consumer.model.UserEvent;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConsumerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    // Consumer Factory for UserEvent
    @Bean
    public ConsumerFactory<String, UserEvent> userEventConsumerFactory() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEvent> 
            userEventKafkaListenerContainerFactory(
                ConsumerFactory<String, UserEvent> userEventConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, UserEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(userEventConsumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }

    // Consumer Factory for OrderEvent
    @Bean
    public ConsumerFactory<String, OrderEvent> orderEventConsumerFactory() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> 
            orderEventKafkaListenerContainerFactory(
                ConsumerFactory<String, OrderEvent> orderEventConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(orderEventConsumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }
}
```

## 5. Updated application.yml

```yaml
server:
  port: 8082

spring:
  application:
    name: consumer-service
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:29092}
    consumer:
      group-id: user-events-consumer-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.example.consumer.model,com.example.producer.model"
        # Type mapping for different event types
        spring.json.type.mapping: userEvent:com.example.consumer.model.UserEvent,orderEvent:com.example.consumer.model.OrderEvent
        spring.json.value.default.type: com.example.consumer.model.UserEvent

kafka:
  topic:
    user-events: user-events
    order-events: order-events
```

## 6. Alternative: Single Topic with Event Wrapper

If you want ALL events in one topic with routing:

### EventWrapper.java
```java
package com.example.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventWrapper {
    private String eventType;  // "USER_EVENT" or "ORDER_EVENT"
    private Object payload;    // The actual event
    private String correlationId;
}
```

### Single Consumer with Routing
```java
@KafkaListener(topics = "all-events", groupId = "consumer-group")
public void consume(@Payload EventWrapper wrapper, Acknowledgment ack) {
    try {
        switch (wrapper.getEventType()) {
            case "USER_EVENT":
                UserEvent userEvent = objectMapper.convertValue(
                    wrapper.getPayload(), UserEvent.class);
                handleUserEvent(userEvent);
                break;
            case "ORDER_EVENT":
                OrderEvent orderEvent = objectMapper.convertValue(
                    wrapper.getPayload(), OrderEvent.class);
                handleOrderEvent(orderEvent);
                break;
            default:
                log.warn("Unknown event type: {}", wrapper.getEventType());
        }
        ack.acknowledge();
    } catch (Exception e) {
        log.error("Error processing event", e);
    }
}
```

## 7. Dead Letter Queue (DLQ) Implementation

### DLQConfig.java
```java
package com.example.consumer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class DLQConfig {

    @Bean
    public NewTopic userEventsDLQ() {
        return TopicBuilder
                .name("user-events-dlq")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderEventsDLQ() {
        return TopicBuilder
                .name("order-events-dlq")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
```

### Updated Consumer with DLQ
```java
@KafkaListener(...)
public void consumeUserEvent(...) {
    try {
        // Process event
        processUserEvent(event);
        acknowledgment.acknowledge();
    } catch (Exception e) {
        log.error("Failed to process event, sending to DLQ", e);
        sendToDLQ("user-events-dlq", event, e);
        acknowledgment.acknowledge(); // Still ack to avoid reprocessing
    }
}

private void sendToDLQ(String dlqTopic, Object event, Exception error) {
    // Create DLQ message with error details
    DLQMessage dlqMessage = DLQMessage.builder()
        .originalEvent(event)
        .errorMessage(error.getMessage())
        .timestamp(LocalDateTime.now())
        .build();
    
    kafkaTemplate.send(dlqTopic, dlqMessage);
}
```
