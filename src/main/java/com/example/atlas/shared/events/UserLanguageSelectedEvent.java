package com.example.atlas.shared.events;

import com.example.atlas.shared.domain.TelegramUserId;

import java.time.Instant;

public record UserLanguageSelectedEvent(
        TelegramUserId telegramUserId,
        String language,
        Instant occurredAt
) implements DomainEvent {
}
