package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.runtime.entity.TelegramLaunchMode;
import com.example.atlas.runtime.service.AtlasRuntimeSettingsService;
import com.example.atlas.runtime.service.EffectiveTelegramConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class TelegramWebhookRegistrationService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookRegistrationService.class);

    private final AtlasProperties properties;
    private final ObjectProvider<AtlasRuntimeSettingsService> runtimeSettingsService;
    private final TelegramWebhookClient webhookClient;

    @Autowired
    public TelegramWebhookRegistrationService(
            AtlasProperties properties,
            ObjectProvider<AtlasRuntimeSettingsService> runtimeSettingsService,
            TelegramWebhookClient webhookClient
    ) {
        this.properties = properties;
        this.runtimeSettingsService = runtimeSettingsService;
        this.webhookClient = webhookClient;
    }

    TelegramWebhookRegistrationService(
            AtlasProperties properties,
            TelegramWebhookClient webhookClient
    ) {
        this.properties = properties;
        this.runtimeSettingsService = null;
        this.webhookClient = webhookClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        registerWebhookIfEnabled();
    }

    void registerWebhookIfEnabled() {
        EffectiveTelegramConfig config = effectiveTelegramConfig();
        if (!config.registerWebhookOnStartup()) {
            log.info("Telegram webhook registration on startup is disabled");
            return;
        }

        registerWebhook(config);
    }

    public void registerConfiguredWebhook() {
        registerWebhook(effectiveTelegramConfig());
    }

    private void registerWebhook(EffectiveTelegramConfig config) {
        if (!config.isWebhookMode()) {
            log.info("Telegram webhook registration skipped because webhook mode is not configured");
            return;
        }

        validateConfiguration(config);

        String webhookUrl = buildWebhookUrl(config.publicBaseUrl(), config.webhookPath());
        webhookClient.setWebhook(new TelegramWebhookClient.TelegramWebhookRequest(
                webhookUrl,
                config.webhookSecret(),
                config.dropPendingUpdatesOnWebhookRegistration()
        ));

        log.info(
                "Telegram webhook registered: url='{}', dropPendingUpdates={}",
                webhookUrl,
                config.dropPendingUpdatesOnWebhookRegistration()
        );
    }

    private void validateConfiguration(EffectiveTelegramConfig config) {
        if (!config.hasBotToken()) {
            throw new IllegalStateException(
                    "Telegram webhook registration is enabled, but atlas.telegram.bot-token is missing. "
                            + "Set ATLAS_TELEGRAM_BOT_TOKEN."
            );
        }

        if (!config.hasPublicBaseUrl()) {
            throw new IllegalStateException(
                    "Telegram webhook registration is enabled, but atlas.telegram.public-base-url is missing. "
                            + "Set ATLAS_PUBLIC_BASE_URL."
            );
        }

        if (!config.publicBaseUrl().startsWith("https://")) {
            throw new IllegalStateException(
                    "Telegram webhook registration requires atlas.telegram.public-base-url to start with https://."
            );
        }

        if (config.webhookPath() == null || config.webhookPath().isBlank()) {
            throw new IllegalStateException(
                    "Telegram webhook registration requires atlas.telegram.webhook-path to be configured."
            );
        }

        if (!config.webhookPath().startsWith("/")) {
            throw new IllegalStateException(
                    "Telegram webhook registration requires atlas.telegram.webhook-path to start with /."
            );
        }
    }

    private EffectiveTelegramConfig effectiveTelegramConfig() {
        AtlasRuntimeSettingsService service = runtimeSettingsService == null ? null : runtimeSettingsService.getIfAvailable();
        if (service != null) {
            return service.effectiveTelegramConfig();
        }

        AtlasProperties.Telegram telegram = properties.telegram();
        TelegramLaunchMode mode = telegram.mode();
        return new EffectiveTelegramConfig(
                telegram.enabled() || telegram.hasBotToken(),
                telegram.hasBotToken(),
                telegram.botToken(),
                telegram.botUsername(),
                mode,
                telegram.webhookPath(),
                telegram.effectiveWebhookUrl(),
                telegram.webhookSecret(),
                telegram.registerWebhookOnStartup(),
                telegram.dropPendingUpdatesOnWebhookRegistration(),
                0L
        );
    }

    private String buildWebhookUrl(String publicBaseUrl, String webhookPath) {
        return publicBaseUrl.replaceAll("/+$", "") + webhookPath;
    }
}
