package com.example.producer.dto;

import com.example.common.model.UserEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserEventRequest Unit Tests")
class UserEventRequestTest {

    // Constants for testing
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final UserEventType TEST_EVENT_TYPE = UserEventType.USER_CREATED;

    @Test
    @DisplayName("should create UserEventRequest with all parameters using constructor")
    void shouldCreateUserEventRequestWithAllParameters() {
        // Given
        UserEventRequest request = new UserEventRequest(
                TEST_USERNAME,
                TEST_EMAIL,
                TEST_EVENT_TYPE
        );

        // Then
        assertEquals(TEST_USERNAME, request.getUsername());
        assertEquals(TEST_EMAIL, request.getEmail());
        assertEquals(TEST_EVENT_TYPE, request.getEventType());
    }

    @Test
    @DisplayName("should create UserEventRequest with default constructor and setters")
    void shouldCreateUserEventRequestWithSetters() {
        // Given
        UserEventRequest request = new UserEventRequest();

        // When
        request.setUsername(TEST_USERNAME);
        request.setEmail(TEST_EMAIL);
        request.setEventType(TEST_EVENT_TYPE);

        // Then
        assertEquals(TEST_USERNAME, request.getUsername());
        assertEquals(TEST_EMAIL, request.getEmail());
        assertEquals(TEST_EVENT_TYPE, request.getEventType());
    }

    @Test
    @DisplayName("should create UserEventRequest using constructor with all parameters")
    void shouldCreateUserEventRequestWithConstructor() {
        // Given
        UserEventRequest request = new UserEventRequest(
                TEST_USERNAME,
                TEST_EMAIL,
                TEST_EVENT_TYPE
        );

        // Then
        assertEquals(TEST_USERNAME, request.getUsername());
        assertEquals(TEST_EMAIL, request.getEmail());
        assertEquals(TEST_EVENT_TYPE, request.getEventType());
    }

    @Test
    @DisplayName("should have correct equals and hashCode behavior")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        UserEventRequest request1 = new UserEventRequest(
                TEST_USERNAME,
                TEST_EMAIL,
                TEST_EVENT_TYPE
        );

        UserEventRequest request2 = new UserEventRequest(
                TEST_USERNAME,
                TEST_EMAIL,
                TEST_EVENT_TYPE
        );

        UserEventRequest request3 = new UserEventRequest(
                "different-user",
                TEST_EMAIL,
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
        UserEventRequest request = new UserEventRequest(
                TEST_USERNAME,
                TEST_EMAIL,
                TEST_EVENT_TYPE
        );

        // When
        String toString = request.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("UserEventRequest"));
        assertTrue(toString.contains(TEST_USERNAME));
        assertTrue(toString.contains(TEST_EMAIL));
    }

    @Test
    @DisplayName("should allow null values for all fields")
    void shouldAllowNullValues() {
        // Given
        UserEventRequest request = new UserEventRequest(
                null, null, null
        );

        // Then
        assertNull(request.getUsername());
        assertNull(request.getEmail());
        assertNull(request.getEventType());
    }

    @Test
    @DisplayName("should handle different UserEventType values")
    void shouldHandleDifferentUserEventTypes() {
        // Given
        for (UserEventType eventType : UserEventType.values()) {
            UserEventRequest request = new UserEventRequest(
                    TEST_USERNAME,
                    TEST_EMAIL,
                    eventType
            );

            // Then
            assertEquals(eventType, request.getEventType());
        }
    }
}