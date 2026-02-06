package com.example.consumer.config;

import org.springframework.boot.kafka.autoconfigure.KafkaProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    /**
     * By injecting KafkaProperties, we pull in everything from application.yml
     * (bootstrap-servers, type mappings, trusted packages, etc.) automatically.
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory(KafkaProperties properties) {
        // properties.buildConsumerProperties(null) merges YAML settings into a Map
        return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        // Concurrency is great with Java 21 Virtual Threads
        factory.setConcurrency(3);

        // Manual Ack Mode is required for your @KafkaListener to use Acknowledgment
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }
}
