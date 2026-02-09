package com.example.producer.config;

import com.example.common.constants.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder
                .name(Topics.USER_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder
                .name(Topics.ORDER_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
