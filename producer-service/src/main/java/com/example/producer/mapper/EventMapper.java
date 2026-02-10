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
        // Added null check for request to prevent NPE
        if (request == null) {
            throw new IllegalArgumentException("UserEventRequest cannot be null");
        }
        
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
        // Added null check for request to prevent NPE
        if (request == null) {
            throw new IllegalArgumentException("OrderEventRequest cannot be null");
        }
        
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
        // Added null checks for parameters to prevent NPE
        String eventTypeSafe = eventType != null ? eventType : "UNKNOWN";
        String eventIdSafe = eventId != null ? eventId : "NO_ID";
        String correlationIdSafe = correlationId != null ? correlationId : "NO_CORRELATION";
        
        return EventResponse.builder()
                .eventId(eventIdSafe)
                .correlationId(correlationIdSafe)
                .message(eventTypeSafe + " event published successfully")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}
