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
    private volatile boolean environmentTelegramConfigAccepted;
    private volatile String setupError;

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
        this.environmentTelegramConfigAccepted = !properties.telegram().hasBotToken();
    }

    @Transactional(readOnly = true)
    public boolean isSetupCompleted() {
        AtlasRuntimeSettingsEntity settings = currentSettingsOrNull();
        return settings != null && settings.isSetupCompleted();
    }

    @Transactional(readOnly = true)
    public RuntimeSettingsStatus status() {
        EffectiveTelegramConfig config = effectiveTelegramConfig();
        boolean setupCompleted = isSetupCompleted();
        boolean telegramConfigured = config.configured() && config.hasBotToken();
        LocalLaunchState state = state(setupCompleted, telegramConfigured, config);
        return new RuntimeSettingsStatus(
                !setupCompleted && !telegramConfigured,
                setupCompleted,
                telegramConfigured,
                config.mode(),
                blankToNull(config.botUsername()),
                state,
                adapterStatus(state),
                config.hasBotToken(),
                config.isWebhookMode() && config.hasPublicBaseUrl() && config.hasWebhookSecret(),
                setupError
        );
    }

    @Transactional(readOnly = true)
    public EffectiveTelegramConfig effectiveTelegramConfig() {
        AtlasRuntimeSettingsEntity settings = currentSettingsOrNull();
        AtlasProperties.Telegram telegram = properties.telegram();

        boolean setupCompleted = settings != null && settings.isSetupCompleted();
        boolean allowEnvironmentConfig = telegram.hasBotToken() && environmentTelegramConfigAccepted && setupError == null;

        String botToken = firstNonBlank(
                setupCompleted ? settings.getTelegramBotToken() : null,
                allowEnvironmentConfig ? telegram.botToken() : null
        );
        String botUsername = firstNonBlank(
                setupCompleted ? settings.getTelegramBotUsername() : null,
                allowEnvironmentConfig ? telegram.botUsername() : null
        );
        String publicBaseUrl = firstNonBlank(
                setupCompleted ? settings.getTelegramPublicBaseUrl() : null,
                allowEnvironmentConfig ? telegram.effectiveWebhookUrl() : null
        );
        String webhookSecret = firstNonBlank(
                setupCompleted ? settings.getTelegramWebhookSecret() : null,
                allowEnvironmentConfig ? telegram.webhookSecret() : null
        );
        TelegramLaunchMode mode = resolveMode(telegram, settings);
        boolean enabled = telegram.enabled() || setupCompleted || allowEnvironmentConfig;
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
                normalizeWebhookUrl(stripToNull(publicBaseUrl), properties.telegram().webhookPath()),
                stripToNull(webhookSecret),
                Instant.now(clock)
        );
        this.setupError = null;
        this.environmentTelegramConfigAccepted = true;
        return requireRepository().save(settings);
    }

    public boolean hasPersistence() {
        return repository.getIfAvailable() != null;
    }

    public void markEnvironmentTelegramConfigAccepted() {
        this.environmentTelegramConfigAccepted = true;
        this.setupError = null;
    }

    public void markSetupError(String setupError) {
        this.setupError = setupError == null || setupError.isBlank()
                ? "Setup error."
                : setupError.strip();
        this.environmentTelegramConfigAccepted = false;
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
        if (settings != null && settings.isSetupCompleted() && settings.getTelegramMode() != null) {
            return settings.getTelegramMode();
        }
        if (telegram.hasBotToken() && environmentTelegramConfigAccepted && setupError == null) {
            return telegram.mode();
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

    private String normalizeWebhookUrl(String value, String webhookPath) {
        if (value == null || webhookPath == null || webhookPath.isBlank()) {
            return value;
        }
        String normalizedPath = webhookPath.startsWith("/") ? webhookPath : "/" + webhookPath;
        if (value.endsWith(normalizedPath)) {
            String base = value.substring(0, value.length() - normalizedPath.length());
            return base.isBlank() ? value : base;
        }
        return value;
    }

    private LocalLaunchState state(boolean setupCompleted, boolean telegramConfigured, EffectiveTelegramConfig config) {
        if (setupError != null) {
            return LocalLaunchState.SETUP_ERROR;
        }
        if (!telegramConfigured) {
            return setupCompleted ? LocalLaunchState.TELEGRAM_DISABLED : LocalLaunchState.SETUP_REQUIRED;
        }
        if (config.isPollingMode()) {
            return LocalLaunchState.TELEGRAM_POLLING_ACTIVE;
        }
        if (config.isWebhookMode()) {
            return LocalLaunchState.TELEGRAM_WEBHOOK_ACTIVE;
        }
        return LocalLaunchState.READY;
    }

    private String adapterStatus(LocalLaunchState state) {
        return switch (state) {
            case TELEGRAM_POLLING_ACTIVE, TELEGRAM_WEBHOOK_ACTIVE -> "Active";
            case SETUP_ERROR -> "Error";
            case SETUP_REQUIRED -> "Setup required";
            case READY -> "Ready";
            case TELEGRAM_DISABLED -> "Disabled";
        };
    }
}
