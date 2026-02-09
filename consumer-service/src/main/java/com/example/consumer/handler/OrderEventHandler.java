package com.example.consumer.handler;

import com.example.common.model.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventHandler implements EventHandler<OrderEvent> {

    @Override
    public void handle(OrderEvent event) {
        log.info("Processing OrderEvent: {}", event);
        
        switch (event.getEventType()) {
            case "ORDER_CREATED":
                handleOrderCreated(event);
                break;
            case "ORDER_COMPLETED":
                handleOrderCompleted(event);
                break;
            case "ORDER_CANCELLED":
                handleOrderCancelled(event);
                break;
            default:
                log.warn("Unknown order event type: {}", event.getEventType());
        }
    }

    @Override
    public boolean supports(String eventType) {
        return eventType != null && eventType.startsWith("ORDER_");
    }
    
    private void handleOrderCreated(OrderEvent event) {
        log.info("Order created for user: {} - Product: {} - Amount: {}", 
            event.getUserId(), event.getProductName(), event.getAmount());
        // TODO: Save order to database, send confirmation email
    }
    
    private void handleOrderCompleted(OrderEvent event) {
        log.info("Order completed: {}", event.getId());
        // TODO: Update order status, trigger fulfillment
    }
    
    private void handleOrderCancelled(OrderEvent event) {
        log.info("Order cancelled: {}", event.getId());
        // TODO: Process refund, update inventory
    }
}
