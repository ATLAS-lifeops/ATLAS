package com.example.atlas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "atlas")
public record AtlasProperties(Telegram telegram) {

    public AtlasProperties {
        if (telegram == null) {
            telegram = new Telegram(false, "", "", "/telegram/webhook", "", "", false, true);
        }
    }

    public record Telegram(
            boolean enabled,
            String botToken,
            String botUsername,
            String webhookPath,
            String webhookSecret,
            String publicBaseUrl,
            boolean registerWebhookOnStartup,
            boolean dropPendingUpdatesOnWebhookRegistration
    ) {
        public Telegram {
            botToken = defaultString(botToken);
            botUsername = defaultString(botUsername);
            webhookPath = defaultString(webhookPath, "/telegram/webhook");
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

        private static String defaultString(String value) {
            return defaultString(value, "");
        }

        private static String defaultString(String value, String fallback) {
            return value == null ? fallback : value;
        }
    }
}
