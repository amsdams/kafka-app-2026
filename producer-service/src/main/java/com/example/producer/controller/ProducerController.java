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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/producer")
@RequiredArgsConstructor
public class ProducerController {

    private final KafkaProducerService producerService;
    private final EventMapper eventMapper;

    @PostMapping("/users")
    public ResponseEntity<EventResponse> publishUserEvent(
            @Valid @RequestBody UserEventRequest request) {
        
        UserEvent event = eventMapper.toUserEvent(request);
        producerService.sendMessage(Topics.USER_EVENTS, event.getId(), event);
        
        EventResponse response = eventMapper.toResponse(
            event.getId(), 
            event.getCorrelationId(), 
            request.getEventType().toString()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/orders")
    public ResponseEntity<EventResponse> publishOrderEvent(
            @Valid @RequestBody OrderEventRequest request) {
        
        OrderEvent event = eventMapper.toOrderEvent(request);
        producerService.sendMessage(Topics.ORDER_EVENTS, event.getId(), event);
        
        EventResponse response = eventMapper.toResponse(
            event.getId(), 
            event.getCorrelationId(), 
            request.getEventType().toString()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Producer Service is running!");
    }
}
