package com.example.atlas.runtime.service;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.runtime.entity.AtlasRuntimeSettingsEntity;
import com.example.atlas.runtime.entity.TelegramLaunchMode;
import com.example.atlas.runtime.repository.AtlasRuntimeSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class AtlasRuntimeSettingsService {

    private final AtlasProperties properties;
    private final ObjectProvider<AtlasRuntimeSettingsRepository> repository;
    private final Clock clock;

    @Autowired
    public AtlasRuntimeSettingsService(
            AtlasProperties properties,
            ObjectProvider<AtlasRuntimeSettingsRepository> repository
    ) {
        this(properties, repository, Clock.systemUTC());
    }

    AtlasRuntimeSettingsService(
            AtlasProperties properties,
            ObjectProvider<AtlasRuntimeSettingsRepository> repository,
            Clock clock
    ) {
        this.properties = properties;
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public boolean isSetupCompleted() {
        return currentSettingsOrNull() != null && currentSettingsOrNull().isSetupCompleted();
    }

    @Transactional(readOnly = true)
    public RuntimeSettingsStatus status() {
        EffectiveTelegramConfig config = effectiveTelegramConfig();
        return new RuntimeSettingsStatus(
                isSetupCompleted(),
                config.mode(),
                blankToNull(config.botUsername()),
                config.hasBotToken(),
                config.isWebhookMode() && config.hasPublicBaseUrl() && config.hasWebhookSecret(),
                mask(config.botToken()),
                mask(config.webhookSecret())
        );
    }

    @Transactional(readOnly = true)
    public EffectiveTelegramConfig effectiveTelegramConfig() {
        AtlasRuntimeSettingsEntity settings = currentSettingsOrNull();
        AtlasProperties.Telegram telegram = properties.telegram();

        String botToken = firstNonBlank(telegram.botToken(), settings == null ? null : settings.getTelegramBotToken());
        String botUsername = firstNonBlank(telegram.botUsername(), settings == null ? null : settings.getTelegramBotUsername());
        String publicBaseUrl = firstNonBlank(telegram.publicBaseUrl(), settings == null ? null : settings.getTelegramPublicBaseUrl());
        String webhookSecret = firstNonBlank(telegram.webhookSecret(), settings == null ? null : settings.getTelegramWebhookSecret());
        TelegramLaunchMode mode = resolveMode(telegram, settings);
        boolean setupCompleted = settings != null && settings.isSetupCompleted();
        boolean enabled = telegram.enabled() || setupCompleted;
        boolean configured = enabled && botToken != null && mode != null;

        return new EffectiveTelegramConfig(
                enabled,
                configured,
                botToken,
                botUsername,
                mode,
                telegram.webhookPath(),
                publicBaseUrl,
                webhookSecret,
                telegram.registerWebhookOnStartup(),
                telegram.dropPendingUpdatesOnWebhookRegistration(),
                settings == null || settings.getTelegramPollingOffset() == null ? 0L : settings.getTelegramPollingOffset()
        );
    }

    @Transactional
    public AtlasRuntimeSettingsEntity saveTelegramSetup(
            String botToken,
            String botUsername,
            TelegramLaunchMode mode,
            String publicBaseUrl,
            String webhookSecret
    ) {
        validateTelegramSetup(botToken, mode, publicBaseUrl, webhookSecret, properties.telegram().webhookPath());

        AtlasRuntimeSettingsEntity settings = currentSettingsOrCreate();
        settings.updateTelegramSetup(
                botToken.strip(),
                stripToNull(botUsername),
                mode,
                stripToNull(publicBaseUrl),
                stripToNull(webhookSecret),
                Instant.now(clock)
        );
        return requireRepository().save(settings);
    }

    @Transactional
    public void updatePollingOffset(long offset) {
        AtlasRuntimeSettingsEntity settings = currentSettingsOrCreate();
        settings.updateTelegramPollingOffset(offset, Instant.now(clock));
        requireRepository().save(settings);
    }

    public void validateTelegramSetup(
            String botToken,
            TelegramLaunchMode mode,
            String publicBaseUrl,
            String webhookSecret,
            String webhookPath
    ) {
        if (botToken == null || botToken.isBlank()) {
            throw new RuntimeSettingsValidationException("Telegram Bot Token is required.");
        }
        if (mode == null) {
            throw new RuntimeSettingsValidationException("Launch mode is required.");
        }
        if (mode == TelegramLaunchMode.WEBHOOK) {
            if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
                throw new RuntimeSettingsValidationException("Production webhook mode requires a public HTTPS base URL.");
            }
            if (!publicBaseUrl.startsWith("https://")) {
                throw new RuntimeSettingsValidationException("Public Base URL must start with https://.");
            }
            if (webhookSecret == null || webhookSecret.isBlank()) {
                throw new RuntimeSettingsValidationException("Production webhook mode requires a webhook secret.");
            }
        }
        if (webhookPath == null || webhookPath.isBlank() || !webhookPath.startsWith("/")) {
            throw new RuntimeSettingsValidationException("Telegram webhook path must start with /.");
        }
    }

    @Transactional
    public AtlasRuntimeSettingsEntity currentSettingsOrCreate() {
        AtlasRuntimeSettingsRepository settingsRepository = requireRepository();
        return settingsRepository.findFirstByOrderByCreatedAtAsc()
                .orElseGet(() -> settingsRepository.save(AtlasRuntimeSettingsEntity.create(Instant.now(clock))));
    }

    @Transactional(readOnly = true)
    public AtlasRuntimeSettingsEntity currentSettingsOrNull() {
        AtlasRuntimeSettingsRepository settingsRepository = repository.getIfAvailable();
        return settingsRepository == null ? null : settingsRepository.findFirstByOrderByCreatedAtAsc().orElse(null);
    }

    private AtlasRuntimeSettingsRepository requireRepository() {
        AtlasRuntimeSettingsRepository settingsRepository = repository.getIfAvailable();
        if (settingsRepository == null) {
            throw new IllegalStateException("Runtime settings persistence is not available.");
        }
        return settingsRepository;
    }

    private TelegramLaunchMode resolveMode(AtlasProperties.Telegram telegram, AtlasRuntimeSettingsEntity settings) {
        if (telegram.enabled() && telegram.hasBotToken()) {
            return telegram.registerWebhookOnStartup() ? TelegramLaunchMode.WEBHOOK : TelegramLaunchMode.POLLING;
        }
        return settings == null ? null : settings.getTelegramMode();
    }

    private String firstNonBlank(String first, String second) {
        String firstValue = stripToNull(first);
        return firstValue == null ? stripToNull(second) : firstValue;
    }

    private String stripToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }

    private String blankToNull(String value) {
        return stripToNull(value);
    }

    private String mask(String value) {
        String stripped = stripToNull(value);
        if (stripped == null) {
            return null;
        }
        if (stripped.length() <= 6) {
            return "******";
        }
        return stripped.substring(0, 3) + "..." + stripped.substring(stripped.length() - 3);
    }
}
