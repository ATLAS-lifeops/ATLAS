package com.example.atlas.config;

import com.example.atlas.runtime.entity.TelegramLaunchMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "atlas")
public record AtlasProperties(Telegram telegram, Setup setup) {

    public AtlasProperties(Telegram telegram) {
        this(telegram, null);
    }

    @ConstructorBinding
    public AtlasProperties {
        if (telegram == null) {
            telegram = new Telegram(false, "", "", TelegramLaunchMode.POLLING, "/telegram/webhook", "", "", "", false, true);
        }
        if (setup == null) {
            setup = new Setup(true);
        }
    }

    public record Telegram(
            boolean enabled,
            String botToken,
            String botUsername,
            TelegramLaunchMode mode,
            String webhookPath,
            String webhookUrl,
            String webhookSecret,
            String publicBaseUrl,
            boolean registerWebhookOnStartup,
            boolean dropPendingUpdatesOnWebhookRegistration
    ) {
        public Telegram {
            botToken = defaultString(botToken);
            botUsername = defaultString(botUsername);
            mode = mode == null ? TelegramLaunchMode.POLLING : mode;
            webhookPath = defaultString(webhookPath, "/telegram/webhook");
            webhookUrl = defaultString(webhookUrl);
            webhookSecret = defaultString(webhookSecret);
            publicBaseUrl = defaultString(publicBaseUrl);
        }

        public boolean hasBotToken() {
            return botToken != null && !botToken.isBlank();
        }

        public boolean hasWebhookSecret() {
            return webhookSecret != null && !webhookSecret.isBlank();
        }

        public boolean hasPublicBaseUrl() {
            return publicBaseUrl != null && !publicBaseUrl.isBlank();
        }

        public String effectiveWebhookUrl() {
            String strippedWebhookUrl = stripToNull(webhookUrl);
            return strippedWebhookUrl == null ? stripToNull(publicBaseUrl) : strippedWebhookUrl;
        }

        private static String defaultString(String value) {
            return defaultString(value, "");
        }

        private static String defaultString(String value, String fallback) {
            return value == null ? fallback : value;
        }

        private static String stripToNull(String value) {
            return value == null || value.isBlank() ? null : value.strip();
        }
    }

    public record Setup(boolean enabled) {
    }
}
