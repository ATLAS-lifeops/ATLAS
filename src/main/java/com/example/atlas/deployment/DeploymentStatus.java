package com.example.atlas.deployment;

public record DeploymentStatus(
        DeploymentMode mode,
        boolean setupEnabled,
        boolean telegramEnabled,
        boolean webhookMode,
        boolean safe
) {
}
