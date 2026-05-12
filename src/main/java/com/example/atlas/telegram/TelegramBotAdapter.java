package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "atlas.telegram", name = "enabled", havingValue = "true")
public class TelegramBotAdapter {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotAdapter.class);

    private final AtlasProperties properties;
    private final TelegramUpdateHandler updateHandler;

    public TelegramBotAdapter(AtlasProperties properties, TelegramUpdateHandler updateHandler) {
        this.properties = properties;
        this.updateHandler = updateHandler;
    }

    public String botUsername() {
        return properties.telegram().botUsername();
    }

    public boolean handleUpdate(TelegramUpdate update) {
        return updateHandler.handleUpdate(update);
    }

    public String handleTextMessage(String text) {
        return updateHandler.handleTextMessage(text);
    }

    @PostConstruct
    void logStartup() {
        log.info("Telegram integration is enabled for bot username '{}'", botUsername());
    }
}
