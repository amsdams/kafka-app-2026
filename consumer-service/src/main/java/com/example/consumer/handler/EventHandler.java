package com.example.consumer.handler;

import org.jspecify.annotations.NonNull;

public interface EventHandler<T, E extends Enum<E>>  {
    void handle(@NonNull T event);
    boolean supports(E eventType);
}
