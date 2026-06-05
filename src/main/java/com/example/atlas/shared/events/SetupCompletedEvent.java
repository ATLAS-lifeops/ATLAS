package com.example.atlas.shared.events;

import java.time.Instant;

public record SetupCompletedEvent(Instant occurredAt) implements DomainEvent {
}
