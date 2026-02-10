package com.example.consumer.handler;

public interface EventHandler<T> {
    void handle(T event);
    boolean supports(T eventType);
}
