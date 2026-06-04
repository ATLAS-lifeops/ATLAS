package com.example.atlas.runtime.service;

import com.example.atlas.runtime.entity.TelegramLaunchMode;

public record RuntimeSettingsStatus(
        boolean setupRequired,
        boolean setupCompleted,
        boolean telegramConfigured,
        TelegramLaunchMode telegramMode,
        String botUsername,
        LocalLaunchState state,
        String adapterStatus,
        boolean tokenConfigured,
        boolean webhookConfigured,
        String setupError
) {
}
