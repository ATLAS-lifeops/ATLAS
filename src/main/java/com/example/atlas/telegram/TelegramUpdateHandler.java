package com.example.atlas.telegram;

import com.example.atlas.orchestrator.OrchestratorService;
import com.example.atlas.safety.SafetyGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "atlas.telegram", name = "enabled", havingValue = "true")
public class TelegramUpdateHandler {

    private static final Logger log = LoggerFactory.getLogger(TelegramUpdateHandler.class);

    private final OrchestratorService orchestratorService;
    private final TelegramMessageSender messageSender;
    private final SafetyGuard safetyGuard;

    public TelegramUpdateHandler(
            OrchestratorService orchestratorService,
            TelegramMessageSender messageSender,
            SafetyGuard safetyGuard
    ) {
        this.orchestratorService = orchestratorService;
        this.messageSender = messageSender;
        this.safetyGuard = safetyGuard;
    }

    public boolean handleUpdate(TelegramUpdate update) {
        if (update == null || update.message() == null) {
            log.debug("Ignoring unsupported Telegram update without a text message");
            return false;
        }

        TelegramUpdate.TelegramMessage message = update.message();
        if (message.chat() == null || message.chat().id() == null) {
            log.debug("Ignoring Telegram message without chat id");
            return false;
        }

        if (message.text() == null || message.text().isBlank()) {
            log.debug("Ignoring Telegram non-text or blank message");
            return false;
        }

        String response = handleTextMessage(message.text());
        messageSender.sendText(message.chat().id(), response);
        return true;
    }

    public String handleTextMessage(String text) {
        if (safetyGuard.requiresSafetyResponse(text)) {
            return safetyGuard.safetyResponse();
        }

        return orchestratorService.route(text).content();
    }
}
