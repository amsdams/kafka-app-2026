package com.example.consumer.handler;

public interface EventHandler<T, E extends Enum<E>>  {
    void handle(T event);
    boolean supports(E eventType);
}
