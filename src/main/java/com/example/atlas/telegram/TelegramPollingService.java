package com.example.atlas.telegram;

import com.example.atlas.runtime.entity.TelegramLaunchMode;
import com.example.atlas.runtime.service.AtlasRuntimeSettingsService;
import com.example.atlas.runtime.service.EffectiveTelegramConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnBean(AtlasRuntimeSettingsService.class)
public class TelegramPollingService {

    private static final Logger log = LoggerFactory.getLogger(TelegramPollingService.class);
    private static final int LONG_POLL_TIMEOUT_SECONDS = 25;

    private final AtlasRuntimeSettingsService runtimeSettingsService;
    private final TelegramApiClient telegramApiClient;
    private final TelegramBotAdapter botAdapter;
    private final AtomicBoolean pollInProgress = new AtomicBoolean(false);

    private String webhookDeletedForToken;

    public TelegramPollingService(
            AtlasRuntimeSettingsService runtimeSettingsService,
            TelegramApiClient telegramApiClient,
            TelegramBotAdapter botAdapter
    ) {
        this.runtimeSettingsService = runtimeSettingsService;
        this.telegramApiClient = telegramApiClient;
        this.botAdapter = botAdapter;
    }

    @Scheduled(
            initialDelayString = "${atlas.telegram.polling-initial-delay-ms:2000}",
            fixedDelayString = "${atlas.telegram.polling-delay-ms:2000}"
    )
    public void pollIfConfigured() {
        if (!pollInProgress.compareAndSet(false, true)) {
            log.debug("Telegram polling skipped because a previous poll is still running");
            return;
        }

        try {
            pollOnce();
        } finally {
            pollInProgress.set(false);
        }
    }

    void pollOnce() {
        EffectiveTelegramConfig config = runtimeSettingsService.effectiveTelegramConfig();
        if (!config.configured() || config.mode() != TelegramLaunchMode.POLLING || !config.hasBotToken()) {
            webhookDeletedForToken = null;
            return;
        }

        ensureWebhookDeleted(config);
        List<TelegramUpdate> updates = telegramApiClient.getUpdates(config.pollingOffset(), LONG_POLL_TIMEOUT_SECONDS);
        for (TelegramUpdate update : updates) {
            botAdapter.handleUpdate(update);
            if (update.updateId() != null) {
                runtimeSettingsService.updatePollingOffset(update.updateId() + 1);
            }
        }
    }

    private void ensureWebhookDeleted(EffectiveTelegramConfig config) {
        if (config.botToken().equals(webhookDeletedForToken)) {
            return;
        }

        telegramApiClient.deleteWebhook(config.dropPendingUpdatesOnWebhookRegistration());
        webhookDeletedForToken = config.botToken();
        log.info("Telegram webhook removed before polling mode startup");
    }
}
