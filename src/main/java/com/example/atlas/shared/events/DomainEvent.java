package com.example.atlas.shared.events;

import java.time.Instant;

public interface DomainEvent {

    Instant occurredAt();
}
