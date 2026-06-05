package com.example.atlas.shared.events;

import com.example.atlas.shared.domain.TelegramUserId;

import java.time.Instant;

public record HabitTrackedEvent(
        TelegramUserId telegramUserId,
        String habitName,
        boolean completed,
        Instant occurredAt
) implements DomainEvent {
}
