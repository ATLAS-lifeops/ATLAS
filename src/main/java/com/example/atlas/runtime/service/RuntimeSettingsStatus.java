package com.example.atlas.runtime.service;

import com.example.atlas.runtime.entity.TelegramLaunchMode;

public record RuntimeSettingsStatus(
        boolean setupCompleted,
        TelegramLaunchMode telegramMode,
        String botUsername,
        boolean tokenConfigured,
        boolean webhookConfigured,
        String maskedToken,
        String maskedWebhookSecret
) {
}
