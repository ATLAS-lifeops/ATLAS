package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "atlas.telegram", name = "enabled", havingValue = "true")
public class TelegramWebhookRegistrationService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookRegistrationService.class);

    private final AtlasProperties properties;
    private final TelegramWebhookClient webhookClient;

    public TelegramWebhookRegistrationService(
            AtlasProperties properties,
            TelegramWebhookClient webhookClient
    ) {
        this.properties = properties;
        this.webhookClient = webhookClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        registerWebhookIfEnabled();
    }

    void registerWebhookIfEnabled() {
        AtlasProperties.Telegram telegram = properties.telegram();
        if (!telegram.registerWebhookOnStartup()) {
            log.info("Telegram webhook registration on startup is disabled");
            return;
        }

        validateConfiguration(telegram);

        String webhookUrl = buildWebhookUrl(telegram.publicBaseUrl(), telegram.webhookPath());
        webhookClient.setWebhook(new TelegramWebhookClient.TelegramWebhookRequest(
                webhookUrl,
                telegram.webhookSecret(),
                telegram.dropPendingUpdatesOnWebhookRegistration()
        ));

        log.info(
                "Telegram webhook registered: url='{}', dropPendingUpdates={}",
                webhookUrl,
                telegram.dropPendingUpdatesOnWebhookRegistration()
        );
    }

    private void validateConfiguration(AtlasProperties.Telegram telegram) {
        if (!telegram.hasBotToken()) {
            throw new IllegalStateException(
                    "Telegram webhook registration is enabled, but atlas.telegram.bot-token is missing. "
                            + "Set ATLAS_TELEGRAM_BOT_TOKEN."
            );
        }

        if (!telegram.hasPublicBaseUrl()) {
            throw new IllegalStateException(
                    "Telegram webhook registration is enabled, but atlas.telegram.public-base-url is missing. "
                            + "Set ATLAS_PUBLIC_BASE_URL."
            );
        }

        if (!telegram.publicBaseUrl().startsWith("https://")) {
            throw new IllegalStateException(
                    "Telegram webhook registration requires atlas.telegram.public-base-url to start with https://."
            );
        }

        if (telegram.webhookPath() == null || telegram.webhookPath().isBlank()) {
            throw new IllegalStateException(
                    "Telegram webhook registration requires atlas.telegram.webhook-path to be configured."
            );
        }

        if (!telegram.webhookPath().startsWith("/")) {
            throw new IllegalStateException(
                    "Telegram webhook registration requires atlas.telegram.webhook-path to start with /."
            );
        }
    }

    private String buildWebhookUrl(String publicBaseUrl, String webhookPath) {
        return publicBaseUrl.replaceAll("/+$", "") + webhookPath;
    }
}
