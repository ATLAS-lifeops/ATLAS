package com.example.atlas.tracking.domain;

import java.time.Instant;

public record CheckIn(
        Integer energy,
        Integer focus,
        Integer stress,
        Integer sleepQuality,
        Integer mood,
        String mainPriority,
        Instant createdAt
) {
}
