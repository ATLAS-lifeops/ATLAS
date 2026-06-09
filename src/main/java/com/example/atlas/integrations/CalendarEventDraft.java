package com.example.atlas.integrations;

import java.time.Instant;

public record CalendarEventDraft(
        String title,
        String description,
        Instant startsAt,
        Instant endsAt
) {
}
