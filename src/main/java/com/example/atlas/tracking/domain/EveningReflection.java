package com.example.atlas.tracking.domain;

import java.time.Instant;

public record EveningReflection(String mainResult, String mainBlocker, String tomorrowFocus, Instant createdAt) {
}
