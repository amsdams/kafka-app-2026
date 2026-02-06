package com.example.producer.service;

import com.example.producer.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Value("${kafka.topic.name}")
    private String topicName;

    public void sendMessage(UserEvent event) {
        log.info("Sending message to topic: {} with key: {}", topicName, event.getId());
        
        CompletableFuture<SendResult<String, UserEvent>> future = 
            kafkaTemplate.send(topicName, event.getId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Message sent successfully: {} with offset: {}", 
                    event, 
                    result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send message: {}", event, ex);
            }
        });
    }
}
