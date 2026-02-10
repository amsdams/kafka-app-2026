package com.example.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String id;
    private String userId;
    private String productName;
    private BigDecimal amount;
    private OrderEventType eventType;
    private LocalDateTime timestamp;
    private String correlationId;
}
