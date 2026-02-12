package com.example.producer.dto;

import com.example.common.model.OrderEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Product name is required")
    private String productName;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Event type is required")
    private OrderEventType eventType;
}
