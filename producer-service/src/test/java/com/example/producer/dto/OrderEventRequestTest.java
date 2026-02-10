package com.example.producer.dto;

import com.example.common.model.OrderEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

@DisplayName("OrderEventRequest Unit Tests")
class OrderEventRequestTest {

    // Constants for testing
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_PRODUCT_NAME = "Test Product";
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("99.99");
    private static final OrderEventType TEST_EVENT_TYPE = OrderEventType.ORDER_CREATED;

    @Test
    @DisplayName("should create OrderEventRequest with all parameters using constructor")
    void shouldCreateOrderEventRequestWithAllParameters() {
        // Given
        OrderEventRequest request = new OrderEventRequest(
                TEST_USER_ID,
                TEST_PRODUCT_NAME,
                TEST_AMOUNT,
                TEST_EVENT_TYPE
        );

        // Then
        assertEquals(TEST_USER_ID, request.getUserId());
        assertEquals(TEST_PRODUCT_NAME, request.getProductName());
        assertEquals(TEST_AMOUNT, request.getAmount());
        assertEquals(TEST_EVENT_TYPE, request.getEventType());
    }

    @Test
    @DisplayName("should create OrderEventRequest with default constructor and setters")
    void shouldCreateOrderEventRequestWithSetters() {
        // Given
        OrderEventRequest request = new OrderEventRequest();

        // When
        request.setUserId(TEST_USER_ID);
        request.setProductName(TEST_PRODUCT_NAME);
        request.setAmount(TEST_AMOUNT);
        request.setEventType(TEST_EVENT_TYPE);

        // Then
        assertEquals(TEST_USER_ID, request.getUserId());
        assertEquals(TEST_PRODUCT_NAME, request.getProductName());
        assertEquals(TEST_AMOUNT, request.getAmount());
        assertEquals(TEST_EVENT_TYPE, request.getEventType());
    }

    @Test
    @DisplayName("should create OrderEventRequest using constructor with all parameters")
    void shouldCreateOrderEventRequestWithConstructor() {
        // Given
        OrderEventRequest request = new OrderEventRequest(
                TEST_USER_ID,
                TEST_PRODUCT_NAME,
                TEST_AMOUNT,
                TEST_EVENT_TYPE
        );

        // Then
        assertEquals(TEST_USER_ID, request.getUserId());
        assertEquals(TEST_PRODUCT_NAME, request.getProductName());
        assertEquals(TEST_AMOUNT, request.getAmount());
        assertEquals(TEST_EVENT_TYPE, request.getEventType());
    }

    @Test
    @DisplayName("should have correct equals and hashCode behavior")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        OrderEventRequest request1 = new OrderEventRequest(
                TEST_USER_ID,
                TEST_PRODUCT_NAME,
                TEST_AMOUNT,
                TEST_EVENT_TYPE
        );

        OrderEventRequest request2 = new OrderEventRequest(
                TEST_USER_ID,
                TEST_PRODUCT_NAME,
                TEST_AMOUNT,
                TEST_EVENT_TYPE
        );

        OrderEventRequest request3 = new OrderEventRequest(
                "different-id",
                TEST_PRODUCT_NAME,
                TEST_AMOUNT,
                TEST_EVENT_TYPE
        );

        // Then
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1, request3);
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }

    @Test
    @DisplayName("should have correct toString representation")
    void shouldHaveCorrectToString() {
        // Given
        OrderEventRequest request = new OrderEventRequest(
                TEST_USER_ID,
                TEST_PRODUCT_NAME,
                TEST_AMOUNT,
                TEST_EVENT_TYPE
        );

        // When
        String toString = request.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("OrderEventRequest"));
        assertTrue(toString.contains(TEST_USER_ID));
        assertTrue(toString.contains(TEST_PRODUCT_NAME));
    }

    @Test
    @DisplayName("should allow null values for all fields")
    void shouldAllowNullValues() {
        // Given
        OrderEventRequest request = new OrderEventRequest(
                null, null, null, null
        );

        // Then
        assertNull(request.getUserId());
        assertNull(request.getProductName());
        assertNull(request.getAmount());
        assertNull(request.getEventType());
    }

    @Test
    @DisplayName("should handle different OrderEventType values")
    void shouldHandleDifferentOrderEventTypes() {
        // Given
        for (OrderEventType eventType : OrderEventType.values()) {
            OrderEventRequest request = new OrderEventRequest(
                    TEST_USER_ID,
                    TEST_PRODUCT_NAME,
                    TEST_AMOUNT,
                    eventType
            );

            // Then
            assertEquals(eventType, request.getEventType());
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
            OrderEventRequest request = new OrderEventRequest(
                    TEST_USER_ID,
                    TEST_PRODUCT_NAME,
                    amount,
                    TEST_EVENT_TYPE
            );

            assertEquals(amount, request.getAmount());
        }
    }
}