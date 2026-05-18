package com.example.atlas.runtime.service;

import com.example.atlas.runtime.entity.TelegramLaunchMode;

public record EffectiveTelegramConfig(
        boolean enabled,
        boolean configured,
        String botToken,
        String botUsername,
        TelegramLaunchMode mode,
        String webhookPath,
        String publicBaseUrl,
        String webhookSecret,
        boolean registerWebhookOnStartup,
        boolean dropPendingUpdatesOnWebhookRegistration,
        long pollingOffset
) {

    public boolean hasBotToken() {
        return botToken != null && !botToken.isBlank();
    }

    public boolean hasWebhookSecret() {
        return webhookSecret != null && !webhookSecret.isBlank();
    }

    public boolean hasPublicBaseUrl() {
        return publicBaseUrl != null && !publicBaseUrl.isBlank();
    }

    public boolean isPollingMode() {
        return configured && mode == TelegramLaunchMode.POLLING;
    }

    public boolean isWebhookMode() {
        return configured && mode == TelegramLaunchMode.WEBHOOK;
    }
}
