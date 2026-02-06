package com.example.producer.controller;

import com.example.producer.model.UserEvent;
import com.example.producer.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class ProducerController {

    private final KafkaProducerService producerService;

    @PostMapping("/publish")
    public ResponseEntity<String> publishEvent(@RequestBody UserEvent event) {
        event.setId(UUID.randomUUID().toString());
        event.setTimestamp(LocalDateTime.now());
        
        producerService.sendMessage(event);
        
        return ResponseEntity.ok("Event published successfully with ID: " + event.getId());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Producer Service is running!");
    }
}
