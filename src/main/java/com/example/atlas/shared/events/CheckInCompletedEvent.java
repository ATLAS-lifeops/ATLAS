package com.example.atlas.shared.events;

import com.example.atlas.shared.domain.TelegramUserId;

import java.time.Instant;

public record CheckInCompletedEvent(
        TelegramUserId telegramUserId,
        Instant occurredAt
) implements DomainEvent {
}
