package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.runtime.service.AtlasRuntimeSettingsService;
import com.example.atlas.runtime.service.EffectiveTelegramConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TelegramBotAdapter {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotAdapter.class);

    private final AtlasProperties properties;
    private final ObjectProvider<AtlasRuntimeSettingsService> runtimeSettingsService;
    private final TelegramUpdateHandler updateHandler;

    @Autowired
    public TelegramBotAdapter(
            AtlasProperties properties,
            ObjectProvider<AtlasRuntimeSettingsService> runtimeSettingsService,
            TelegramUpdateHandler updateHandler
    ) {
        this.properties = properties;
        this.runtimeSettingsService = runtimeSettingsService;
        this.updateHandler = updateHandler;
    }

    TelegramBotAdapter(AtlasProperties properties, TelegramUpdateHandler updateHandler) {
        this.properties = properties;
        this.runtimeSettingsService = null;
        this.updateHandler = updateHandler;
    }

    public String botUsername() {
        AtlasRuntimeSettingsService service = runtimeSettingsService == null ? null : runtimeSettingsService.getIfAvailable();
        if (service != null) {
            EffectiveTelegramConfig config = service.effectiveTelegramConfig();
            if (config.botUsername() != null && !config.botUsername().isBlank()) {
                return config.botUsername();
            }
        }
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
        log.info("Telegram integration is available for runtime configuration");
    }
}
