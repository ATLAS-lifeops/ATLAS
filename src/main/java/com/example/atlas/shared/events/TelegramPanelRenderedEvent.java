package com.example.atlas.shared.events;

import java.time.Instant;

public record TelegramPanelRenderedEvent(
        Long chatId,
        String panel,
        Instant occurredAt
) implements DomainEvent {
}
