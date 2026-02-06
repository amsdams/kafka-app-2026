package com.example.consumer.service;

import com.example.consumer.model.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {

    @KafkaListener(
        topics = "${kafka.topic.name}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload UserEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received message from partition: {}, offset: {}", partition, offset);
            log.info("Processing event: {}", event);
            
            // Process the event (business logic goes here)
            processEvent(event);
            
            // Manually acknowledge the message
            acknowledgment.acknowledge();
            log.info("Message acknowledged successfully");
            
        } catch (Exception e) {
            log.error("Error processing message: {}", event, e);
            // In production, you might want to send to a DLQ (Dead Letter Queue)
        }
    }

    private void processEvent(UserEvent event) {
        // Simulate business logic processing
        log.info("Processing user event for user: {} with event type: {}", 
            event.getUsername(), 
            event.getEventType());
        
        // Add your business logic here
        // e.g., save to database, send notifications, etc.
    }
}
