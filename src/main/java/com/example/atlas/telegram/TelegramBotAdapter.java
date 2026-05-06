package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import org.springframework.stereotype.Component;

@Component
public class TelegramBotAdapter {

    private final AtlasProperties properties;
    private final TelegramUpdateHandler updateHandler;

    public TelegramBotAdapter(AtlasProperties properties, TelegramUpdateHandler updateHandler) {
        this.properties = properties;
        this.updateHandler = updateHandler;
    }

    public String botUsername() {
        return properties.telegram().botUsername();
    }

    public String handleTextMessage(String text) {
        return updateHandler.handleTextMessage(text);
    }
}
