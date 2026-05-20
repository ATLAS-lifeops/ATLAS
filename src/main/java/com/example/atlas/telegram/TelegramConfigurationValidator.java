package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "atlas.telegram", name = "enabled", havingValue = "true")
public class TelegramConfigurationValidator {

    private final AtlasProperties properties;

    public TelegramConfigurationValidator(AtlasProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void validate() {
        if (!properties.setup().enabled() && !properties.telegram().hasBotToken()) {
            throw new IllegalStateException(
                    "ATLAS Telegram integration is enabled, but atlas.telegram.bot-token is missing. "
                            + "Set ATLAS_TELEGRAM_BOT_TOKEN, enable setup with ATLAS_SETUP_ENABLED=true, "
                            + "or disable Telegram with ATLAS_TELEGRAM_ENABLED=false."
            );
        }
    }
}
