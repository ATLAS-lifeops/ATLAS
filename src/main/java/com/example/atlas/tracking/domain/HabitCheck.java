package com.example.atlas.tracking.domain;

import java.time.Instant;

public record HabitCheck(String habitName, String minimumVersion, boolean completed, Instant createdAt) {
}
