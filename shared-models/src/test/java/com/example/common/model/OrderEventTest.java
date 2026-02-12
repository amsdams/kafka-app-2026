package com.example.common.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@DisplayName("OrderEvent Unit Tests")
class OrderEventTest {

    // Constants for testing
    private static final String TEST_ID = "test-id-123";
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_PRODUCT_NAME = "Test Product";
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("99.99");
    private static final OrderEventType TEST_EVENT_TYPE = OrderEventType.ORDER_CREATED;
    private static final LocalDateTime TEST_TIMESTAMP = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
    private static final String TEST_CORRELATION_ID = "corr-123";

    @Test
    @DisplayName("should create OrderEvent with all parameters using constructor")
    void shouldCreateOrderEventWithAllParameters() {
        // Given
        OrderEvent orderEvent = new OrderEvent(
                TEST_ID,
                TEST_USER_ID,
                TEST_PRODUCT_NAME,
                TEST_AMOUNT,
                TEST_EVENT_TYPE,
                TEST_TIMESTAMP,
                TEST_CORRELATION_ID
        );

        // Then
        assertEquals(TEST_ID, orderEvent.getId());
        assertEquals(TEST_USER_ID, orderEvent.getUserId());
        assertEquals(TEST_PRODUCT_NAME, orderEvent.getProductName());
        assertEquals(TEST_AMOUNT, orderEvent.getAmount());
        assertEquals(TEST_EVENT_TYPE, orderEvent.getEventType());
        assertEquals(TEST_TIMESTAMP, orderEvent.getTimestamp());
        assertEquals(TEST_CORRELATION_ID, orderEvent.getCorrelationId());
    }

    @Test
    @DisplayName("should create OrderEvent with default constructor and setters")
    void shouldCreateOrderEventWithSetters() {
        // Given
        OrderEvent orderEvent = new OrderEvent();

        // When
        orderEvent.setId(TEST_ID);
        orderEvent.setUserId(TEST_USER_ID);
        orderEvent.setProductName(TEST_PRODUCT_NAME);
        orderEvent.setAmount(TEST_AMOUNT);
        orderEvent.setEventType(TEST_EVENT_TYPE);
        orderEvent.setTimestamp(TEST_TIMESTAMP);
        orderEvent.setCorrelationId(TEST_CORRELATION_ID);

        // Then
        assertEquals(TEST_ID, orderEvent.getId());
        assertEquals(TEST_USER_ID, orderEvent.getUserId());
        assertEquals(TEST_PRODUCT_NAME, orderEvent.getProductName());
        assertEquals(TEST_AMOUNT, orderEvent.getAmount());
        assertEquals(TEST_EVENT_TYPE, orderEvent.getEventType());
        assertEquals(TEST_TIMESTAMP, orderEvent.getTimestamp());
        assertEquals(TEST_CORRELATION_ID, orderEvent.getCorrelationId());
    }

    @Test
    @DisplayName("should create OrderEvent using builder pattern")
    void shouldCreateOrderEventWithBuilder() {
        // Given
        OrderEvent orderEvent = OrderEvent.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .productName(TEST_PRODUCT_NAME)
                .amount(TEST_AMOUNT)
                .eventType(TEST_EVENT_TYPE)
                .timestamp(TEST_TIMESTAMP)
                .correlationId(TEST_CORRELATION_ID)
                .build();

        // Then
        assertEquals(TEST_ID, orderEvent.getId());
        assertEquals(TEST_USER_ID, orderEvent.getUserId());
        assertEquals(TEST_PRODUCT_NAME, orderEvent.getProductName());
        assertEquals(TEST_AMOUNT, orderEvent.getAmount());
        assertEquals(TEST_EVENT_TYPE, orderEvent.getEventType());
        assertEquals(TEST_TIMESTAMP, orderEvent.getTimestamp());
        assertEquals(TEST_CORRELATION_ID, orderEvent.getCorrelationId());
    }

    @Test
    @DisplayName("should have correct equals and hashCode behavior")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        OrderEvent event1 = OrderEvent.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .productName(TEST_PRODUCT_NAME)
                .amount(TEST_AMOUNT)
                .eventType(TEST_EVENT_TYPE)
                .timestamp(TEST_TIMESTAMP)
                .correlationId(TEST_CORRELATION_ID)
                .build();

        OrderEvent event2 = OrderEvent.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .productName(TEST_PRODUCT_NAME)
                .amount(TEST_AMOUNT)
                .eventType(TEST_EVENT_TYPE)
                .timestamp(TEST_TIMESTAMP)
                .correlationId(TEST_CORRELATION_ID)
                .build();

        OrderEvent event3 = OrderEvent.builder()
                .id("different-id")
                .userId(TEST_USER_ID)
                .productName(TEST_PRODUCT_NAME)
                .amount(TEST_AMOUNT)
                .eventType(TEST_EVENT_TYPE)
                .timestamp(TEST_TIMESTAMP)
                .correlationId(TEST_CORRELATION_ID)
                .build();

        // Then
        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
        assertNotEquals(event1, event3);
        assertNotEquals(event1.hashCode(), event3.hashCode());
    }

    @Test
    @DisplayName("should have correct toString representation")
    void shouldHaveCorrectToString() {
        // Given
        OrderEvent orderEvent = OrderEvent.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .productName(TEST_PRODUCT_NAME)
                .amount(TEST_AMOUNT)
                .eventType(TEST_EVENT_TYPE)
                .timestamp(TEST_TIMESTAMP)
                .correlationId(TEST_CORRELATION_ID)
                .build();

        // When
        String toString = orderEvent.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("OrderEvent"));
        assertTrue(toString.contains(TEST_ID));
        assertTrue(toString.contains(TEST_USER_ID));
        assertTrue(toString.contains(TEST_PRODUCT_NAME));
    }

    @Test
    @DisplayName("should allow null values for all fields")
    void shouldAllowNullValues() {
        // Given
        OrderEvent orderEvent = new OrderEvent(
                null, null, null, null, null, null, null
        );

        // Then
        assertNull(orderEvent.getId());
        assertNull(orderEvent.getUserId());
        assertNull(orderEvent.getProductName());
        assertNull(orderEvent.getAmount());
        assertNull(orderEvent.getEventType());
        assertNull(orderEvent.getTimestamp());
        assertNull(orderEvent.getCorrelationId());
    }

    @Test
    @DisplayName("should handle different OrderEventType values")
    void shouldHandleDifferentOrderEventTypes() {
        // Given
        for (OrderEventType eventType : OrderEventType.values()) {
            OrderEvent orderEvent = OrderEvent.builder()
                    .id(TEST_ID)
                    .userId(TEST_USER_ID)
                    .productName(TEST_PRODUCT_NAME)
                    .amount(TEST_AMOUNT)
                    .eventType(eventType)
                    .timestamp(TEST_TIMESTAMP)
                    .correlationId(TEST_CORRELATION_ID)
                    .build();

            // Then
            assertEquals(eventType, orderEvent.getEventType());
        }
    }

    @Test
    @DisplayName("should handle different BigDecimal amounts")
    void shouldHandleDifferentAmounts() {
        // Given
        BigDecimal[] amounts = {
                BigDecimal.ZERO,
                new BigDecimal("0.01"),
                new BigDecimal("100.00"),
                new BigDecimal("999999.99")
        };

        // When & Then
        for (BigDecimal amount : amounts) {
            OrderEvent orderEvent = OrderEvent.builder()
                    .id(TEST_ID)
                    .userId(TEST_USER_ID)
                    .productName(TEST_PRODUCT_NAME)
                    .amount(amount)
                    .eventType(TEST_EVENT_TYPE)
                    .timestamp(TEST_TIMESTAMP)
                    .correlationId(TEST_CORRELATION_ID)
                    .build();

            assertEquals(amount, orderEvent.getAmount());
        }
    }
}