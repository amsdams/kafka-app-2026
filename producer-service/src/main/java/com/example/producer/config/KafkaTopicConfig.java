package com.example.producer.config;

import com.example.common.constants.Topics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka Topic Configuration with Best Practices
 * <p>
 * Best Practices Implemented:
 * 1. Appropriate replication factor for fault tolerance
 * 2. Optimal partition count for parallelism
 * 3. Min in-sync replicas for durability
 * 4. Compression configuration
 * 5. Retention policies
 * 6. Dead Letter Queue topics
 */
@Configuration
@Slf4j
public class KafkaTopicConfig {

    /**
     * BEST PRACTICE: User Events Topic
     * <p>
     * Configuration:
     * - Partitions: 6 (allows 6 parallel consumers)
     * - Replication: 3 (survives 2 broker failures)
     * - Min ISR: 2 (ensures data durability)
     * - Retention: 7 days (configurable based on requirements)
     */
    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder
                .name(Topics.USER_EVENTS)
                // BEST PRACTICE: Partition count based on expected throughput
                // Formula: (target_throughput_MB/s) / (single_partition_throughput_MB/s)
                .partitions(6)
                // BEST PRACTICE: Replication factor for production
                // Minimum: 2, Recommended: 3
                .replicas(3)
                // BEST PRACTICE: Min in-sync replicas for durability
                // Should be: replication_factor - 1 = 2
                // Used with acks=all to ensure writes to multiple replicas
                .config("min.insync.replicas", "2")
                // BEST PRACTICE: Compression
                // "producer" = inherit from producer config
                .config("compression.type", "producer")
                // BEST PRACTICE: Retention policy
                // Time-based: 7 days
                .config("retention.ms", "604800000")
                // Size-based: Unlimited (-1)
                .config("retention.bytes", "-1")
                // BEST PRACTICE: Cleanup policy
                .config("cleanup.policy", "delete")
                // BEST PRACTICE: Segment configuration
                .config("segment.ms", "86400000")  // 1 day
                .config("segment.bytes", "1073741824")  // 1 GB
                .build();
    }

    /**
     * BEST PRACTICE: Order Events Topic
     */
    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder
                .name(Topics.ORDER_EVENTS)
                .partitions(6)
                .replicas(3)
                .config("min.insync.replicas", "2")
                .config("compression.type", "producer")
                .config("retention.ms", "604800000")  // 7 days
                .config("retention.bytes", "-1")
                .config("cleanup.policy", "delete")
                .build();
    }

    /**
     * BEST PRACTICE: Dead Letter Queue for User Events
     * <p>
     * Configuration:
     * - Same partitions as main topic for ordering
     * - Longer retention for investigation
     * - Lower replication (optional, depending on criticality)
     */
    @Bean
    public NewTopic userEventsDLQTopic() {
        return TopicBuilder
                .name(Topics.USER_EVENTS_DLQ)
                .partitions(6)  // Same as main topic
                .replicas(3)
                .config("min.insync.replicas", "2")
                .config("compression.type", "producer")
                // BEST PRACTICE: Longer retention for DLQ (30 days)
                .config("retention.ms", "2592000000")
                .config("retention.bytes", "-1")
                .config("cleanup.policy", "delete")
                .build();
    }

    /**
     * BEST PRACTICE: Dead Letter Queue for Order Events
     */
    @Bean
    public NewTopic orderEventsDLQTopic() {
        return TopicBuilder
                .name(Topics.ORDER_EVENTS_DLQ)
                .partitions(6)
                .replicas(3)
                .config("min.insync.replicas", "2")
                .config("compression.type", "producer")
                .config("retention.ms", "2592000000")  // 30 days
                .config("retention.bytes", "-1")
                .config("cleanup.policy", "delete")
                .build();
    }

    /**
     * BEST PRACTICE: Optional - Compacted Topic Example
     * Use for topics where you only care about latest value per key
     * (e.g., user profile updates, configuration changes)
     */
    // @Bean
    // public NewTopic userProfilesTopic() {
    //     return TopicBuilder
    //             .name("user-profiles")
    //             .partitions(6)
    //             .replicas(3)
    //             .config("min.insync.replicas", "2")
    //             .config("cleanup.policy", "compact")
    //             .config("min.cleanable.dirty.ratio", "0.5")
    //             .config("delete.retention.ms", "86400000")  // 1 day
    //             .build();
    // }
}
