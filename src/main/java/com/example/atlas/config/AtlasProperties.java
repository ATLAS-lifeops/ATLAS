package com.example.atlas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "atlas")
public record AtlasProperties(Telegram telegram) {

    public AtlasProperties {
        if (telegram == null) {
            telegram = new Telegram("", "");
        }
    }

    public record Telegram(
            String botToken,
            String botUsername
    ) {
    }
}
