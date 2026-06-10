package com.example.atlas.privacy;

public record PrivacyPanel(
        long profileCount,
        long checkInCount,
        long habitCount,
        long reflectionCount,
        long memoryCount,
        boolean telegramIdentifiersStored
) {
}
