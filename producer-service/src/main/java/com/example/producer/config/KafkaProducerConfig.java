package com.example.producer.config;

import com.example.producer.model.UserEvent;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

    /**
     * Injects KafkaProperties (from org.springframework.boot.kafka.autoconfigure)
     * which contains everything from your application.yml.
     */
    @Bean
    public ProducerFactory<String, UserEvent> producerFactory(KafkaProperties properties) {
        // This automatically picks up:
        // - key-serializer: StringSerializer
        // - value-serializer: JsonSerializer
        // - acks: all
        // - retries: 3
        return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
    }

    @Bean
    public KafkaTemplate<String, UserEvent> kafkaTemplate(ProducerFactory<String, UserEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}