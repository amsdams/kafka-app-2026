package com.example.producer.config;

import com.example.producer.model.UserEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, UserEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Pass the actual serializer instances here to avoid InstantiationException
        return new DefaultKafkaProducerFactory<>(
                configProps,
                new StringSerializer(),
                new JacksonJsonSerializer<>()
        );
    }

    @Bean
    public KafkaTemplate<String, UserEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
