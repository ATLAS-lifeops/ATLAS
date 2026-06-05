package com.example.atlas.profile.domain;

public record LifeProfile(
        String primaryLifeArea,
        String currentFocus,
        String planningStyle,
        boolean onboardingCompleted
) {
}
