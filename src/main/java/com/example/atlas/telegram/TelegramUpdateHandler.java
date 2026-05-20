package com.example.atlas.telegram;

import com.example.atlas.orchestrator.OrchestratorService;
import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.safety.SafetyGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
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
            log.info(
                    "Telegram update handled: update_id={}, chat_id={}, handled={}, reason={}",
                    updateId(update),
                    null,
                    false,
                    "unsupported_update"
            );
            return false;
        }

        TelegramUpdate.TelegramMessage message = update.message();
        if (message.chat() == null || message.chat().id() == null) {
            log.info(
                    "Telegram update handled: update_id={}, chat_id={}, handled={}, reason={}",
                    updateId(update),
                    null,
                    false,
                    "missing_chat_id"
            );
            return false;
        }

        if (message.text() == null || message.text().isBlank()) {
            log.info(
                    "Telegram update handled: update_id={}, chat_id={}, handled={}, reason={}",
                    updateId(update),
                    message.chat().id(),
                    false,
                    "blank_text"
            );
            return false;
        }

        RequestType requestType = orchestratorService.resolveRequestType(message.text());
        String response = handleTextMessage(message.text(), requestType);
        messageSender.sendText(message.chat().id(), response);
        log.info(
                "Telegram update handled: update_id={}, chat_id={}, handled={}, request_type={}",
                updateId(update),
                message.chat().id(),
                true,
                requestType
        );
        return true;
    }

    public String handleTextMessage(String text) {
        return handleTextMessage(text, orchestratorService.resolveRequestType(text));
    }

    private String handleTextMessage(String text, RequestType requestType) {
        if (safetyGuard.requiresSafetyResponse(text)) {
            return safetyGuard.safetyResponse();
        }

        return orchestratorService.route(requestType, text).content();
    }

    private Long updateId(TelegramUpdate update) {
        return update == null ? null : update.updateId();
    }
}
