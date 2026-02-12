package com.example.producer.mapper;

import com.example.common.model.OrderEvent;
import com.example.common.model.UserEvent;
import com.example.producer.dto.EventResponse;
import com.example.producer.dto.OrderEventRequest;
import com.example.producer.dto.UserEventRequest;
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

    public EventResponse toResponse(String eventId, String correlationId, String eventType) {
        return EventResponse.builder()
                .eventId(eventId)
                .correlationId(correlationId)
                .message(eventType + " event published successfully")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}
