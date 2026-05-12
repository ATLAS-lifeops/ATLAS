package com.example.atlas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "atlas")
public record AtlasProperties(Telegram telegram) {

    public AtlasProperties {
        if (telegram == null) {
            telegram = new Telegram(false, "", "");
        }
    }

    public record Telegram(
            boolean enabled,
            String botToken,
            String botUsername
    ) {
        public boolean hasBotToken() {
            return botToken != null && !botToken.isBlank();
        }
    }
}
