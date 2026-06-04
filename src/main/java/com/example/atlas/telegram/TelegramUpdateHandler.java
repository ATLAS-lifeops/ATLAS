package com.example.atlas.telegram;

import com.example.atlas.conversation.service.TelegramLifeFlowService;
import com.example.atlas.message.service.TelegramMessagePersistenceService;
import com.example.atlas.orchestrator.OrchestratorService;
import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.safety.SafetyGuard;
import com.example.atlas.user.UserLanguage;
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
    private final ObjectProvider<TelegramLifeFlowService> lifeFlowService;
    private final TelegramActionRouter actionRouter;
    private final TelegramKeyboardFactory keyboardFactory;

    @Autowired
    public TelegramUpdateHandler(
            OrchestratorService orchestratorService,
            TelegramMessageSender messageSender,
            SafetyGuard safetyGuard,
            ObjectProvider<TelegramUserService> userService,
            ObjectProvider<TelegramMessagePersistenceService> messagePersistenceService,
            ObjectProvider<TelegramLifeFlowService> lifeFlowService,
            TelegramActionRouter actionRouter,
            TelegramKeyboardFactory keyboardFactory
    ) {
        this.orchestratorService = orchestratorService;
        this.messageSender = messageSender;
        this.safetyGuard = safetyGuard;
        this.userService = userService;
        this.messagePersistenceService = messagePersistenceService;
        this.lifeFlowService = lifeFlowService;
        this.actionRouter = actionRouter;
        this.keyboardFactory = keyboardFactory;
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
        this.lifeFlowService = null;
        this.actionRouter = new TelegramActionRouter();
        this.keyboardFactory = new TelegramKeyboardFactory();
    }

    public boolean handleUpdate(TelegramUpdate update) {
        if (update != null && update.callbackQuery() != null) {
            return handleCallbackQuery(update);
        }

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

        if (requestType == RequestType.CLEAR) {
            handleClear(message, user);
            log.info(
                    "Telegram update handled: update_id={}, chat_id={}, handled={}, request_type={}",
                    updateId(update),
                    message.chat().id(),
                    true,
                    requestType
            );
            return true;
        }

        if (shouldAskLanguage(user, message.text())) {
            RoutedResponse language = languageSelection();
            messageSender.sendPanel(message.chat().id(), language.content(), language.replyMarkup());
            recordOutgoing(user, message.chat().id(), language.requestType(), language.content());
            log.info(
                    "Telegram update handled: update_id={}, chat_id={}, handled={}, request_type={}, language_required={}",
                    updateId(update),
                    message.chat().id(),
                    true,
                    language.requestType(),
                    true
            );
            return true;
        }

        RoutedResponse routedResponse = requestType == RequestType.START && hasActiveFlow(user)
                ? activeFlowContinuation(user)
                : requestType == RequestType.HELP ? help(user) : handleTextMessage(user, message.text(), requestType);
        String response = routedResponse.content();
        if (requestType == RequestType.START || requestType == RequestType.HELP) {
            messageSender.sendPanel(message.chat().id(), response, routedResponse.replyMarkup());
        } else {
            messageSender.sendText(message.chat().id(), response, routedResponse.replyMarkup());
        }
        recordOutgoing(user, message.chat().id(), routedResponse.requestType(), response);
        log.info(
                "Telegram update handled: update_id={}, chat_id={}, handled={}, request_type={}",
                updateId(update),
                message.chat().id(),
                true,
                routedResponse.requestType()
        );
        return true;
    }

    private boolean handleCallbackQuery(TelegramUpdate update) {
        TelegramUpdate.TelegramCallbackQuery callbackQuery = update.callbackQuery();
        Long chatId = callbackChatId(callbackQuery);
        String callbackData = callbackQuery.data();
        if (callbackQuery.id() == null || callbackQuery.id().isBlank() || chatId == null
                || callbackData == null || callbackData.isBlank()) {
            log.info(
                    "Telegram callback handled: update_id={}, telegram_user_id={}, action={}, handled={}, reason={}",
                    updateId(update),
                    callbackUserId(callbackQuery),
                    null,
                    false,
                    "invalid_callback"
            );
            answerCallback(callbackQuery.id(), null);
            return false;
        }

        TelegramUserEntity user = upsertUser(callbackQuery);
        RoutedResponse response = routeCallback(user, callbackData);
        if (response.editPanel() && callbackQuery.message().messageId() != null) {
            boolean edited = messageSender.editPanel(chatId, callbackQuery.message().messageId(), response.content(), response.replyMarkup());
            if (!edited) {
                messageSender.sendPanel(chatId, response.content(), response.replyMarkup());
            }
        } else {
            messageSender.sendText(chatId, response.content(), response.replyMarkup());
        }
        recordOutgoing(user, chatId, response.requestType(), response.content());
        answerCallback(callbackQuery.id(), callbackAnswer(callbackData));
        log.info(
                "Telegram callback handled: update_id={}, telegram_user_id={}, action={}, handled={}",
                updateId(update),
                callbackUserId(callbackQuery),
                safeActionName(callbackData),
                true
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

    private RoutedResponse handleTextMessage(TelegramUserEntity user, String text, RequestType requestType) {
        TelegramLifeFlowService service = lifeFlowService == null ? null : lifeFlowService.getIfAvailable();
        if (service != null) {
            return service.handle(user, text, requestType)
                    .map(result -> new RoutedResponse(
                            result.content(),
                            result.requestType(),
                            result.replyMarkup() == null ? keyboardFactory.forRequest(result.requestType()) : result.replyMarkup()
                    ))
                    .orElseGet(() -> fallbackResponse(text, requestType));
        }
        return fallbackResponse(text, requestType);
    }

    private RoutedResponse fallbackResponse(String text, RequestType requestType) {
        return new RoutedResponse(handleTextMessage(text, requestType), requestType, keyboardFactory.forRequest(requestType));
    }

    private RoutedResponse routeCallback(TelegramUserEntity user, String callbackData) {
        TelegramLifeFlowService service = lifeFlowService == null ? null : lifeFlowService.getIfAvailable();
        if (!actionRouter.isSupportedCallback(callbackData)) {
            UserLanguage language = language(user).orElse(UserLanguage.RU);
            return new RoutedResponse(
                    language == UserLanguage.EN
                            ? "Could not process this button. Open the menu and choose an action again."
                            : "Не получилось обработать кнопку. Открой меню и выбери действие ещё раз.",
                    RequestType.GENERAL,
                    keyboardFactory.mainMenu(language),
                    true
            );
        }

        if (user != null && language(user).isEmpty() && !isLanguageCallback(callbackData)) {
            return languageSelection();
        }

        return actionRouter.flowInputForCallback(callbackData)
                .map(input -> routeFlowInput(user, input))
                .orElseGet(() -> actionRouter.actionForCallback(callbackData)
                        .map(action -> routeAction(user, action, service))
                        .orElseGet(() -> new RoutedResponse(
                                language(user).orElse(UserLanguage.RU) == UserLanguage.EN
                                        ? "Could not process this button. Open the menu and choose an action again."
                                        : "Не получилось обработать кнопку. Открой меню и выбери действие ещё раз.",
                                RequestType.GENERAL,
                                keyboardFactory.mainMenu(language(user).orElse(UserLanguage.RU))
                        )));
    }

    private RoutedResponse routeFlowInput(TelegramUserEntity user, String input) {
        return handleTextMessage(user, input, RequestType.GENERAL);
    }

    private RoutedResponse routeAction(TelegramUserEntity user, TelegramAction action, TelegramLifeFlowService service) {
        if (action == TelegramAction.OPEN_MAIN_MENU) {
            return mainMenu(user);
        }
        if (action == TelegramAction.GO_BACK && service != null) {
            TelegramLifeFlowService.FlowResult result = service.back(user);
            return new RoutedResponse(result.content(), result.requestType(), result.replyMarkup(), true);
        }
        if (action == TelegramAction.CONTINUE_FLOW && service != null) {
            TelegramLifeFlowService.FlowResult result = service.continueActiveFlow(user);
            return new RoutedResponse(result.content(), result.requestType(), result.replyMarkup(), true);
        }
        if (action == TelegramAction.RESTART_FLOW && service != null) {
            TelegramLifeFlowService.FlowResult result = service.restartActiveFlowConfirmation(user);
            return new RoutedResponse(result.content(), result.requestType(), result.replyMarkup(), true);
        }
        if (action == TelegramAction.CONFIRM_ACTION && service != null) {
            TelegramLifeFlowService.FlowResult result = service.restartActiveFlow(user);
            return new RoutedResponse(result.content(), result.requestType(), result.replyMarkup(), true);
        }
        if (action == TelegramAction.DECLINE_ACTION && service != null) {
            TelegramLifeFlowService.FlowResult result = service.activeFlowContinuation(user);
            return new RoutedResponse(result.content(), result.requestType(), result.replyMarkup(), true);
        }
        if (action == TelegramAction.SELECT_LANGUAGE_RU) {
            return selectLanguage(user, UserLanguage.RU);
        }
        if (action == TelegramAction.SELECT_LANGUAGE_EN) {
            return selectLanguage(user, UserLanguage.EN);
        }
        if (action == TelegramAction.CHANGE_LANGUAGE) {
            return languageSelection();
        }
        if (action == TelegramAction.START_QUESTION) {
            return questionEntry(user);
        }
        if (action == TelegramAction.OPEN_SETTINGS) {
            return settings(user, service);
        }
        if (action == TelegramAction.OPEN_PROFILE && service != null) {
            TelegramLifeFlowService.FlowResult result = service.profile(user);
            return new RoutedResponse(result.content(), result.requestType(), result.replyMarkup(), true);
        }
        if (action == TelegramAction.SHOW_HELP) {
            return help(user);
        }
        if (action == TelegramAction.CONFIRM_RESTART_ONBOARDING) {
            UserLanguage language = language(user).orElse(UserLanguage.RU);
            return new RoutedResponse(
                    language == UserLanguage.EN
                            ? """
                            Restart onboarding?

                            Your profile will be updated after the new onboarding. Check-ins, habits and reports will not be deleted.
                            """
                            : """
                            Перезапустить onboarding?

                            Текущий профиль будет обновлён после нового прохождения. История check-in, привычек и отчётов не удалится.
                            """,
                    RequestType.GENERAL,
                    keyboardFactory.restartConfirmation(language),
                    true
            );
        }
        if (action == TelegramAction.RESTART_ONBOARDING && service != null && user != null) {
            TelegramLifeFlowService.FlowResult result = service.restartOnboarding(user);
            return new RoutedResponse(result.content(), result.requestType(), result.replyMarkup());
        }
        RequestType requestType = actionRouter.requestType(action);
        String command = actionRouter.commandForAction(action);
        RoutedResponse response = handleTextMessage(user, command, requestType);
        return new RoutedResponse(response.content(), response.requestType(), response.replyMarkup(), true);
    }

    private boolean hasActiveFlow(TelegramUserEntity user) {
        TelegramLifeFlowService service = lifeFlowService == null ? null : lifeFlowService.getIfAvailable();
        return service != null && service.hasActiveFlow(user);
    }

    private RoutedResponse activeFlowContinuation(TelegramUserEntity user) {
        TelegramLifeFlowService service = lifeFlowService == null ? null : lifeFlowService.getIfAvailable();
        if (service == null) {
            return mainMenu(user);
        }
        TelegramLifeFlowService.FlowResult result = service.activeFlowContinuation(user);
        return new RoutedResponse(result.content(), result.requestType(), result.replyMarkup(), true);
    }

    private RoutedResponse help(TelegramUserEntity user) {
        UserLanguage language = language(user).orElse(UserLanguage.RU);
        if (language == UserLanguage.EN) {
            return new RoutedResponse(
                    """
                    ATLAS Help

                    Use the buttons for the main flows:
                    check-in, day plan, habits, evening reflection, report, minimal plan and settings.

                    Commands also work: /start, /checkin, /day, /habits, /evening, /report, /cancel, /clear, /emergency.
                    """,
                    RequestType.HELP,
                    keyboardFactory.help(language),
                    true
            );
        }
        return new RoutedResponse(
                """
                Помощь ATLAS

                Основной путь - кнопки: check-in, план дня, привычки, вечерняя рефлексия, отчёт, минимальный план и настройки.

                Команды тоже работают: /start, /checkin, /day, /habits, /evening, /report, /cancel, /clear, /emergency.
                """,
                RequestType.HELP,
                keyboardFactory.help(language),
                true
        );
    }

    private RoutedResponse mainMenu(TelegramUserEntity user) {
        UserLanguage language = language(user).orElse(UserLanguage.RU);
        return new RoutedResponse(
                mainMenuCaption(language),
                RequestType.GENERAL,
                keyboardFactory.mainMenu(language),
                true
        );
    }

    private RoutedResponse questionEntry(TelegramUserEntity user) {
        if (language(user).orElse(UserLanguage.RU) == UserLanguage.EN) {
            return new RoutedResponse(
                    """
                    For now, I work best through structured flows: check-in, day plan, habits, evening reflection and report.

                    Choose a section below or write a question - I will try to route it to the right flow.
                    """,
                    RequestType.GENERAL,
                    keyboardFactory.questionActions(UserLanguage.EN)
            );
        }
        return new RoutedResponse(
                """
                Пока я лучше всего работаю через сценарии: check-in, план дня, привычки, вечерняя рефлексия и отчёт.

                Выбери раздел ниже или напиши вопрос — я постараюсь направить его в подходящий сценарий.
                """,
                RequestType.GENERAL,
                keyboardFactory.questionActions(UserLanguage.RU)
        );
    }

    private RoutedResponse settings(TelegramUserEntity user, TelegramLifeFlowService service) {
        UserLanguage language = language(user).orElse(UserLanguage.RU);
        String content = service == null || user == null
                ? (language == UserLanguage.EN
                ? "ATLAS Settings\n\nProfile is available after Telegram persistence starts."
                : "Настройки ATLAS\n\nПрофиль доступен после запуска Telegram persistence.")
                : service.settings(user);
        return new RoutedResponse(content, RequestType.GENERAL, keyboardFactory.settingsActions(language), true);
    }

    private RoutedResponse languageSelection() {
        return new RoutedResponse(
                "ATLAS\n\nChoose your language / Выберите язык",
                RequestType.GENERAL,
                keyboardFactory.languageSelection(),
                true
        );
    }

    private RoutedResponse selectLanguage(TelegramUserEntity user, UserLanguage language) {
        TelegramUserService service = userService == null ? null : userService.getIfAvailable();
        TelegramUserEntity updated = service == null ? user : service.updateLanguage(user, language);
        return mainMenu(updated == null ? user : updated);
    }

    private void handleClear(TelegramUpdate.TelegramMessage message, TelegramUserEntity user) {
        if (message.messageId() != null) {
            messageSender.deleteMessage(message.chat().id(), message.messageId());
        }
        TelegramLifeFlowService service = lifeFlowService == null ? null : lifeFlowService.getIfAvailable();
        if (service != null && user != null) {
            service.handle(user, "/cancel", RequestType.CANCEL);
        }
        RoutedResponse response = user != null && language(user).isEmpty() ? languageSelection() : mainMenu(user);
        messageSender.sendPanel(message.chat().id(), response.content(), response.replyMarkup());
        recordOutgoing(user, message.chat().id(), response.requestType(), response.content());
    }

    private TelegramUserEntity upsertUser(TelegramUpdate.TelegramMessage message) {
        TelegramUserService service = userService == null ? null : userService.getIfAvailable();
        return service == null ? null : service.upsertFromMessage(message);
    }

    private TelegramUserEntity upsertUser(TelegramUpdate.TelegramCallbackQuery callbackQuery) {
        TelegramUserService service = userService == null ? null : userService.getIfAvailable();
        return service == null ? null : service.upsertFromCallbackQuery(callbackQuery);
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

    private Long updateId(TelegramUpdate update) {
        return update == null ? null : update.updateId();
    }

    private Long callbackChatId(TelegramUpdate.TelegramCallbackQuery callbackQuery) {
        if (callbackQuery == null || callbackQuery.message() == null || callbackQuery.message().chat() == null) {
            return null;
        }
        return callbackQuery.message().chat().id();
    }

    private Long callbackUserId(TelegramUpdate.TelegramCallbackQuery callbackQuery) {
        return callbackQuery == null || callbackQuery.from() == null ? null : callbackQuery.from().id();
    }

    private String safeActionName(String callbackData) {
        return actionRouter.actionForCallback(callbackData)
                .map(Enum::name)
                .orElseGet(() -> actionRouter.flowInputForCallback(callbackData).isPresent() ? "FLOW_INPUT" : "UNSUPPORTED");
    }

    private boolean shouldAskLanguage(TelegramUserEntity user, String text) {
        return user != null && language(user).isEmpty() && isCommand(text);
    }

    private boolean isCommand(String text) {
        return text != null && text.strip().startsWith("/");
    }

    private boolean isLanguageCallback(String callbackData) {
        return TelegramActionRouter.LANGUAGE_RU.equals(callbackData) || TelegramActionRouter.LANGUAGE_EN.equals(callbackData);
    }

    private String callbackAnswer(String callbackData) {
        if (TelegramActionRouter.LANGUAGE_RU.equals(callbackData)) {
            return "Язык сохранён";
        }
        if (TelegramActionRouter.LANGUAGE_EN.equals(callbackData)) {
            return "Language saved";
        }
        return null;
    }

    private java.util.Optional<UserLanguage> language(TelegramUserEntity user) {
        return user == null ? java.util.Optional.empty() : user.getLanguage();
    }

    private String mainMenuCaption(UserLanguage language) {
        return language == UserLanguage.EN
                ? "ATLAS\n\nWhat would you like to do now?"
                : "ATLAS\n\nЧто хочешь сделать сейчас?";
    }

    private void answerCallback(String callbackQueryId, String text) {
        if (callbackQueryId == null || callbackQueryId.isBlank()) {
            return;
        }
        try {
            messageSender.answerCallbackQuery(callbackQueryId, text);
        } catch (RuntimeException exception) {
            log.warn(
                    "Telegram answerCallbackQuery failed: callback_query_id={}, error_type={}",
                    callbackQueryId,
                    exception.getClass().getSimpleName()
            );
        }
    }

    private record RoutedResponse(String content, RequestType requestType, InlineKeyboardMarkup replyMarkup, boolean editPanel) {
        private RoutedResponse(String content, RequestType requestType, InlineKeyboardMarkup replyMarkup) {
            this(content, requestType, replyMarkup, false);
        }
    }
}
