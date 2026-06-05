package com.example.atlas.shared.events;

public interface EventPublisher {

    void publish(DomainEvent event);
}
