package com.example.consumer.config;

import com.example.common.model.OrderEvent;
import com.example.common.model.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.Map;

/**
 * Kafka Consumer Configuration with Best Practices
 * <p>
 * Best Practices Implemented:
 * 1. Type-safe consumer factories for each event type
 * 2. Manual offset commit for reliability
 * 3. Dead Letter Queue (DLQ) for poison messages
 * 4. Exponential backoff retry strategy
 * 5. Proper error handling
 * 6. Consumer concurrency for parallelism
 */
@EnableKafka
@Configuration
@Slf4j
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConsumerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    // ===== BEST PRACTICE: Consumer Factory for UserEvent =====
    @Bean
    public ConsumerFactory<String, UserEvent> userEventConsumerFactory() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * BEST PRACTICE: Container Factory with Error Handler and DLQ
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEvent>
    userEventKafkaListenerContainerFactory(
            ConsumerFactory<String, UserEvent> userEventConsumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, UserEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(userEventConsumerFactory);

        // BEST PRACTICE: Consumer Concurrency
        // Number of concurrent consumers (threads) per topic
        // Rule: Should be <= number of partitions
        factory.setConcurrency(3);

        // BEST PRACTICE: Manual Acknowledgment Mode
        // Prevents data loss if consumer crashes during processing
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        // BEST PRACTICE: Error Handler with DLQ and Exponential Backoff
        factory.setCommonErrorHandler(createErrorHandler(kafkaTemplate));

        return factory;
    }

    // ===== BEST PRACTICE: Consumer Factory for OrderEvent =====
    @Bean
    public ConsumerFactory<String, OrderEvent> orderEventConsumerFactory() {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent>
    orderEventKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderEvent> orderEventConsumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(orderEventConsumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(createErrorHandler(kafkaTemplate));

        return factory;
    }

    /**
     * BEST PRACTICE: Create Error Handler with DLQ and Retry Logic
     * <p>
     * Strategy:
     * 1. Retry with exponential backoff (1s, 2s, 4s)
     * 2. After max retries, send to Dead Letter Queue
     * 3. Log all failures with full context
     */
    private DefaultErrorHandler createErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {

        // BEST PRACTICE: Dead Letter Publishing Recoverer
        // Publishes failed messages to DLQ topic
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (consumerRecord, exception) -> {
                    // BEST PRACTICE: DLQ Topic Naming Convention
                    // Original topic: user-events -> DLQ: user-events.DLQ
                    String dlqTopic = consumerRecord.topic() + ".DLQ";

                    log.error("Sending message to DLQ: topic={}, partition={}, offset={}, key={}, exception={}",
                            consumerRecord.topic(),
                            consumerRecord.partition(),
                            consumerRecord.offset(),
                            consumerRecord.key(),
                            exception.getMessage());

                    return new org.apache.kafka.common.TopicPartition(
                            dlqTopic,
                            consumerRecord.partition()
                    );
                }
        );

        // BEST PRACTICE: Exponential Backoff with Max Retries
        // Retry: 1s -> 2s -> 4s -> DLQ
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000L);    // 1 second
        backOff.setMultiplier(2.0);            // Double each time
        backOff.setMaxInterval(10000L);        // Max 10 seconds

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // BEST PRACTICE: Add listener for all errors
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.warn("Retry attempt {} for message: topic={}, partition={}, offset={}, exception={}",
                    deliveryAttempt,
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    ex.getMessage());
        });

        return errorHandler;
    }

    /**
     * BEST PRACTICE: Optional - Batch Listener Configuration
     * For high-throughput scenarios where you want to process messages in batches
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEvent>
    batchUserEventKafkaListenerContainerFactory(
            ConsumerFactory<String, UserEvent> userEventConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, UserEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(userEventConsumerFactory);
        factory.setConcurrency(3);

        // Enable batch processing
        factory.setBatchListener(true);

        // Manual acknowledgment after entire batch
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }
}
