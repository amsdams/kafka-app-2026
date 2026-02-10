package com.example.consumer.service;

import com.example.common.constants.Topics;
import com.example.common.model.OrderEvent;
import com.example.common.model.OrderEventType;
import com.example.common.model.UserEvent;
import com.example.common.model.UserEventType;
import com.example.consumer.handler.EventHandler;
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

    private final List<EventHandler<UserEvent, UserEventType>> userEventHandlers;
    private final List<EventHandler<OrderEvent, OrderEventType>> orderEventHandlers;

    @KafkaListener(
        topics = Topics.USER_EVENTS,
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
            // TODO: Send to DLQ or implement retry logic
            acknowledgment.acknowledge(); // Still acknowledge to avoid infinite reprocessing
        }
    }

    @KafkaListener(
        topics = Topics.ORDER_EVENTS,
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
            // TODO: Send to DLQ or implement retry logic
            acknowledgment.acknowledge(); // Still acknowledge to avoid infinite reprocessing
        }
    }
}
