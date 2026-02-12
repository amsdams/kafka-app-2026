package com.example.common.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

@DisplayName("UserEvent Unit Tests")
class UserEventTest {

    // Constants for testing
    private static final String TEST_ID = "test-id-123";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final UserEventType TEST_EVENT_TYPE = UserEventType.USER_CREATED;
    private static final LocalDateTime TEST_TIMESTAMP = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
    private static final String TEST_CORRELATION_ID = "corr-123";

    @Test
    @DisplayName("should create UserEvent with all parameters using constructor")
    void shouldCreateUserEventWithAllParameters() {
        // Given
        UserEvent userEvent = new UserEvent(
                TEST_ID,
                TEST_USERNAME,
                TEST_EMAIL,
                TEST_EVENT_TYPE,
                TEST_TIMESTAMP,
                TEST_CORRELATION_ID
        );

        // Then
        assertEquals(TEST_ID, userEvent.getId());
        assertEquals(TEST_USERNAME, userEvent.getUsername());
        assertEquals(TEST_EMAIL, userEvent.getEmail());
        assertEquals(TEST_EVENT_TYPE, userEvent.getEventType());
        assertEquals(TEST_TIMESTAMP, userEvent.getTimestamp());
        assertEquals(TEST_CORRELATION_ID, userEvent.getCorrelationId());
    }

    @Test
    @DisplayName("should create UserEvent with default constructor and setters")
    void shouldCreateUserEventWithSetters() {
        // Given
        UserEvent userEvent = new UserEvent();

        // When
        userEvent.setId(TEST_ID);
        userEvent.setUsername(TEST_USERNAME);
        userEvent.setEmail(TEST_EMAIL);
        userEvent.setEventType(TEST_EVENT_TYPE);
        userEvent.setTimestamp(TEST_TIMESTAMP);
        userEvent.setCorrelationId(TEST_CORRELATION_ID);

        // Then
        assertEquals(TEST_ID, userEvent.getId());
        assertEquals(TEST_USERNAME, userEvent.getUsername());
        assertEquals(TEST_EMAIL, userEvent.getEmail());
        assertEquals(TEST_EVENT_TYPE, userEvent.getEventType());
        assertEquals(TEST_TIMESTAMP, userEvent.getTimestamp());
        assertEquals(TEST_CORRELATION_ID, userEvent.getCorrelationId());
    }

    @Test
    @DisplayName("should create UserEvent using builder pattern")
    void shouldCreateUserEventWithBuilder() {
        // Given
        UserEvent userEvent = UserEvent.builder()
                .id(TEST_ID)
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .eventType(TEST_EVENT_TYPE)
                .timestamp(TEST_TIMESTAMP)
                .correlationId(TEST_CORRELATION_ID)
                .build();

        // Then
        assertEquals(TEST_ID, userEvent.getId());
        assertEquals(TEST_USERNAME, userEvent.getUsername());
        assertEquals(TEST_EMAIL, userEvent.getEmail());
        assertEquals(TEST_EVENT_TYPE, userEvent.getEventType());
        assertEquals(TEST_TIMESTAMP, userEvent.getTimestamp());
        assertEquals(TEST_CORRELATION_ID, userEvent.getCorrelationId());
    }

    @Test
    @DisplayName("should have correct equals and hashCode behavior")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        UserEvent event1 = UserEvent.builder()
                .id(TEST_ID)
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .eventType(TEST_EVENT_TYPE)
                .timestamp(TEST_TIMESTAMP)
                .correlationId(TEST_CORRELATION_ID)
                .build();

        UserEvent event2 = UserEvent.builder()
                .id(TEST_ID)
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .eventType(TEST_EVENT_TYPE)
                .timestamp(TEST_TIMESTAMP)
                .correlationId(TEST_CORRELATION_ID)
                .build();

        UserEvent event3 = UserEvent.builder()
                .id("different-id")
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
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
        UserEvent userEvent = UserEvent.builder()
                .id(TEST_ID)
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .eventType(TEST_EVENT_TYPE)
                .timestamp(TEST_TIMESTAMP)
                .correlationId(TEST_CORRELATION_ID)
                .build();

        // When
        String toString = userEvent.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("UserEvent"));
        assertTrue(toString.contains(TEST_ID));
        assertTrue(toString.contains(TEST_USERNAME));
        assertTrue(toString.contains(TEST_EMAIL));
    }

    @Test
    @DisplayName("should allow null values for all fields")
    void shouldAllowNullValues() {
        // Given
        UserEvent userEvent = new UserEvent(
                null, null, null, null, null, null
        );

        // Then
        assertNull(userEvent.getId());
        assertNull(userEvent.getUsername());
        assertNull(userEvent.getEmail());
        assertNull(userEvent.getEventType());
        assertNull(userEvent.getTimestamp());
        assertNull(userEvent.getCorrelationId());
    }

    @Test
    @DisplayName("should handle different UserEventType values")
    void shouldHandleDifferentUserEventTypes() {
        // Given
        for (UserEventType eventType : UserEventType.values()) {
            UserEvent userEvent = UserEvent.builder()
                    .id(TEST_ID)
                    .username(TEST_USERNAME)
                    .email(TEST_EMAIL)
                    .eventType(eventType)
                    .timestamp(TEST_TIMESTAMP)
                    .correlationId(TEST_CORRELATION_ID)
                    .build();

            // Then
            assertEquals(eventType, userEvent.getEventType());
        }
    }
}