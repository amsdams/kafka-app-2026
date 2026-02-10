package com.example.producer.controller;

import com.example.common.constants.Topics;
import com.example.common.model.OrderEvent;
import com.example.common.model.UserEvent;
import com.example.producer.dto.EventResponse;
import com.example.producer.dto.OrderEventRequest;
import com.example.producer.dto.UserEventRequest;
import com.example.producer.mapper.EventMapper;
import com.example.producer.service.KafkaProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/producer")
@RequiredArgsConstructor
public class ProducerController {

    private final KafkaProducerService producerService;
    private final EventMapper eventMapper;

    @PostMapping("/users")
    public ResponseEntity<@NonNull EventResponse> publishUserEvent(
            @Valid @NonNull @RequestBody UserEventRequest request) {

        UserEvent event = eventMapper.toUserEvent(request);
        producerService.sendMessage(Topics.USER_EVENTS, event.getId(), event);

        // Added null check for event type to prevent NPE
        String eventTypeString = request.getEventType() != null ? request.getEventType().toString() : "UNKNOWN";
        EventResponse response = eventMapper.toResponse(
            event.getId(),
            event.getCorrelationId(),
            eventTypeString
        );

        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/orders")
    public ResponseEntity<@NonNull EventResponse> publishOrderEvent(
            @Valid @NonNull @RequestBody OrderEventRequest request) {

        OrderEvent event = eventMapper.toOrderEvent(request);
        producerService.sendMessage(Topics.ORDER_EVENTS, event.getId(), event);

        // Added null check for event type to prevent NPE
        String eventTypeString = request.getEventType() != null ? request.getEventType().toString() : "UNKNOWN";
        EventResponse response = eventMapper.toResponse(
            event.getId(),
            event.getCorrelationId(),
            eventTypeString
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Producer Service is running!");
    }
}
