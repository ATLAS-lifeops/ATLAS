package com.example.atlas.setup.service;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.runtime.entity.TelegramLaunchMode;
import com.example.atlas.runtime.service.AtlasRuntimeSettingsService;
import com.example.atlas.runtime.service.RuntimeSettingsValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class PreconfiguredTelegramStartupService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PreconfiguredTelegramStartupService.class);
    private static final String SETUP_URL = "http://localhost:8080/setup";

    private final AtlasProperties properties;
    private final AtlasRuntimeSettingsService runtimeSettingsService;
    private final TelegramBotTokenValidator tokenValidator;

    public PreconfiguredTelegramStartupService(
            AtlasProperties properties,
            AtlasRuntimeSettingsService runtimeSettingsService,
            TelegramBotTokenValidator tokenValidator
    ) {
        this.properties = properties;
        this.runtimeSettingsService = runtimeSettingsService;
        this.tokenValidator = tokenValidator;
    }

    @Override
    public void run(ApplicationArguments args) {
        AtlasProperties.Telegram telegram = properties.telegram();
        if (!telegram.hasBotToken()) {
            return;
        }

        if (!runtimeSettingsService.hasPersistence()) {
            runtimeSettingsService.markEnvironmentTelegramConfigAccepted();
            return;
        }

        try {
            TelegramBotIdentity identity = tokenValidator.validate(telegram.botToken());
            String botUsername = firstNonBlank(telegram.botUsername(), identity.username());
            TelegramLaunchMode mode = telegram.mode() == null ? TelegramLaunchMode.POLLING : telegram.mode();
            runtimeSettingsService.saveTelegramSetup(
                    telegram.botToken(),
                    botUsername,
                    mode,
                    telegram.effectiveWebhookUrl(),
                    telegram.webhookSecret()
            );
            log.info("Telegram bot configuration validated. ATLAS Telegram mode: {}", mode);
        } catch (RuntimeSettingsValidationException | TelegramBotTokenValidationException exception) {
            runtimeSettingsService.markSetupError("Telegram token could not be validated. Replace it through setup.");
            log.warn("Telegram bot configuration could not be validated. ATLAS setup is available at {}", SETUP_URL);
        }
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.strip();
        }
        return second == null || second.isBlank() ? null : second.strip();
    }
}
