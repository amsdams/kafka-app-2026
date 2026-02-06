package com.example.producer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    private String id;
    private String username;
    private String email;
    private String eventType;
    private LocalDateTime timestamp;
}
