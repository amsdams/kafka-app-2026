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
