package com.example.consumer.handler;

import com.example.common.model.UserEvent;
import com.example.common.model.UserEventType;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventHandler implements EventHandler<UserEvent, UserEventType> {

    @Override
    public void handle(@NonNull UserEvent event) {
        log.info("Processing UserEvent: {}", event);

        switch (event.getEventType()) {
            case USER_CREATED:
                handleUserCreated(event);
                break;
            case USER_UPDATED:
                handleUserUpdated(event);
                break;
            case USER_DELETED:
                handleUserDeleted(event);
                break;
            default:
                log.warn("Unknown user event type: {}", event.getEventType());
        }
    }


    @Override
    public boolean supports(UserEventType eventType) {
        return eventType != null && eventType.toString().startsWith("USER_");
    }

    private void handleUserCreated(UserEvent event) {
        log.info("User created: {} with email: {}", event.getUsername(), event.getEmail());
        // TODO: Save to database, send welcome email, etc.
    }

    private void handleUserUpdated(UserEvent event) {
        log.info("User updated: {}", event.getUsername());
        // TODO: Update user in database
    }

    private void handleUserDeleted(UserEvent event) {
        log.info("User deleted: {}", event.getUsername());
        // TODO: Soft delete or archive user
    }
}
