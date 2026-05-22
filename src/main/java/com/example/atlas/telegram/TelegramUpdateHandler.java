package com.example.atlas.telegram;

import com.example.atlas.checkin.service.CheckInPersistenceService;
import com.example.atlas.message.service.TelegramMessagePersistenceService;
import com.example.atlas.orchestrator.OrchestratorService;
import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.safety.SafetyGuard;
import com.example.atlas.user.entity.TelegramUserEntity;
import com.example.atlas.user.service.TelegramUserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TelegramUpdateHandler {

    private static final Logger log = LoggerFactory.getLogger(TelegramUpdateHandler.class);

    private final OrchestratorService orchestratorService;
    private final TelegramMessageSender messageSender;
    private final SafetyGuard safetyGuard;
    private final ObjectProvider<TelegramUserService> userService;
    private final ObjectProvider<TelegramMessagePersistenceService> messagePersistenceService;
    private final ObjectProvider<CheckInPersistenceService> checkInPersistenceService;

    @Autowired
    public TelegramUpdateHandler(
            OrchestratorService orchestratorService,
            TelegramMessageSender messageSender,
            SafetyGuard safetyGuard,
            ObjectProvider<TelegramUserService> userService,
            ObjectProvider<TelegramMessagePersistenceService> messagePersistenceService,
            ObjectProvider<CheckInPersistenceService> checkInPersistenceService
    ) {
        this.orchestratorService = orchestratorService;
        this.messageSender = messageSender;
        this.safetyGuard = safetyGuard;
        this.userService = userService;
        this.messagePersistenceService = messagePersistenceService;
        this.checkInPersistenceService = checkInPersistenceService;
    }

    TelegramUpdateHandler(
            OrchestratorService orchestratorService,
            TelegramMessageSender messageSender,
            SafetyGuard safetyGuard
    ) {
        this.orchestratorService = orchestratorService;
        this.messageSender = messageSender;
        this.safetyGuard = safetyGuard;
        this.userService = null;
        this.messagePersistenceService = null;
        this.checkInPersistenceService = null;
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
        TelegramUserEntity user = upsertUser(message);
        recordIncoming(user, message.chat().id(), requestType, message.text());
        if (requestType == RequestType.CHECKIN) {
            recordCheckIn(user, message.text());
        }

        String response = handleTextMessage(message.text(), requestType);
        messageSender.sendText(message.chat().id(), response);
        recordOutgoing(user, message.chat().id(), requestType, response);
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

    private TelegramUserEntity upsertUser(TelegramUpdate.TelegramMessage message) {
        TelegramUserService service = userService == null ? null : userService.getIfAvailable();
        return service == null ? null : service.upsertFromMessage(message);
    }

    private void recordIncoming(TelegramUserEntity user, Long chatId, RequestType requestType, String text) {
        TelegramMessagePersistenceService service = messagePersistenceService == null ? null : messagePersistenceService.getIfAvailable();
        if (service != null) {
            service.recordIncoming(user, chatId, requestType, text);
        }
    }

    private void recordOutgoing(TelegramUserEntity user, Long chatId, RequestType requestType, String text) {
        TelegramMessagePersistenceService service = messagePersistenceService == null ? null : messagePersistenceService.getIfAvailable();
        if (service != null) {
            service.recordOutgoing(user, chatId, requestType, text);
        }
    }

    private void recordCheckIn(TelegramUserEntity user, String text) {
        CheckInPersistenceService service = checkInPersistenceService == null ? null : checkInPersistenceService.getIfAvailable();
        if (service != null) {
            service.record(user, text);
        }
    }

    private Long updateId(TelegramUpdate update) {
        return update == null ? null : update.updateId();
    }
}
