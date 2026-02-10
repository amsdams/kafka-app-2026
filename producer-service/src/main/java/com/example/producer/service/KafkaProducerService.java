package com.example.producer.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Kafka Producer Service with Best Practices
 * <p>
 * Best Practices Implemented:
 * 1. Proper error handling with callbacks
 * 2. Metrics collection for monitoring
 * 3. Structured logging with context
 * 4. Async/sync send options
 * 5. Correlation ID tracking
 */
@Service
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Counter messagesSentCounter;
    private final Counter messagesFailedCounter;
    private final Timer sendTimer;

    public KafkaProducerService(
            KafkaTemplate<String, Object> kafkaTemplate,
            MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;

        // BEST PRACTICE: Initialize metrics for monitoring
        this.messagesSentCounter = Counter.builder("kafka.producer.messages.sent")
                .description("Total number of messages sent successfully")
                .tag("service", "producer")
                .register(meterRegistry);

        this.messagesFailedCounter = Counter.builder("kafka.producer.messages.failed")
                .description("Total number of messages that failed to send")
                .tag("service", "producer")
                .register(meterRegistry);

        this.sendTimer = Timer.builder("kafka.producer.send.duration")
                .description("Time taken to send messages")
                .tag("service", "producer")
                .register(meterRegistry);
    }

    /**
     * Convenience method - delegates to sendMessageAsync
     * Use this for simpler code when you don't need the Future
     */
    public <T> void sendMessage(String topic, String key, T event) {
        sendMessageAsync(topic, key, event);
    }

    /**
     * BEST PRACTICE: Async send with proper callback handling
     * Returns CompletableFuture for non-blocking operations
     */
    public <T> CompletableFuture<SendResult<String, Object>> sendMessageAsync(
            String topic, String key, T event) {

        long startTime = System.nanoTime();

        log.debug("Sending message asynchronously: topic={}, key={}", topic, key);

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, event);

        // BEST PRACTICE: Proper callback handling with metrics
        future.whenComplete((result, ex) -> {
            long duration = System.nanoTime() - startTime;
            sendTimer.record(duration, TimeUnit.NANOSECONDS);

            if (ex == null) {
                // Success
                messagesSentCounter.increment();

                // BEST PRACTICE: Structured logging with all context
                log.info("Message sent successfully: topic={}, partition={}, offset={}, key={}, timestamp={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        key,
                        result.getRecordMetadata().timestamp());
            } else {
                // Failure
                messagesFailedCounter.increment();

                // BEST PRACTICE: Log failure with full context for debugging
                log.error("Failed to send message: topic={}, key={}, error={}",
                        topic, key, ex.getMessage(), ex);

                // BEST PRACTICE: You could trigger alerts here
                // alertService.sendAlert("Kafka send failed", ex);
            }
        });

        return future;
    }

    /**
     * BEST PRACTICE: Synchronous send for critical operations
     * Blocks until send completes or fails
     * Use sparingly - async is preferred for performance
     */
    public <T> SendResult<String, Object> sendMessageSync(
            String topic, String key, T event) throws Exception {

        long startTime = System.nanoTime();

        log.debug("Sending message synchronously: topic={}, key={}", topic, key);

        try {
            // BEST PRACTICE: Use get() to block and wait for result
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(topic, key, event);

            // Wait for completion with timeout
            SendResult<String, Object> result = future.get(30, TimeUnit.SECONDS);

            long duration = System.nanoTime() - startTime;
            sendTimer.record(duration, TimeUnit.NANOSECONDS);
            messagesSentCounter.increment();

            log.info("Message sent synchronously: topic={}, partition={}, offset={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

            return result;

        } catch (Exception e) {
            messagesFailedCounter.increment();
            log.error("Synchronous send failed: topic={}, key={}", topic, key, e);
            throw e;
        }
    }

    /**
     * BEST PRACTICE: Send with custom headers for metadata
     */
    public <T> CompletableFuture<SendResult<String, Object>> sendMessageWithHeaders(
            String topic, String key, T event, java.util.Map<String, String> headers) {

        log.debug("Sending message with headers: topic={}, key={}, headers={}",
                topic, key, headers);

        org.springframework.messaging.Message<T> message =
                org.springframework.messaging.support.MessageBuilder
                        .withPayload(event)
                        .setHeader(org.springframework.kafka.support.KafkaHeaders.TOPIC, topic)
                        .setHeader(org.springframework.kafka.support.KafkaHeaders.KEY, key)
                        .copyHeaders(headers)
                        .build();

        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                messagesSentCounter.increment();
                log.info("Message with headers sent: topic={}, partition={}, offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                messagesFailedCounter.increment();
                log.error("Failed to send message with headers: topic={}, key={}",
                        topic, key, ex);
            }
        });

        return future;
    }

    /**
     * BEST PRACTICE: Flush and close for graceful shutdown
     * Should be called during application shutdown
     */
    public void flush() {
        log.info("Flushing pending messages...");
        kafkaTemplate.flush();
        log.info("All pending messages flushed");
    }
}
