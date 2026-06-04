package com.example.atlas.telegram;

import com.example.atlas.agent.coach.CoachAgent;
import com.example.atlas.agent.core.CoreAgent;
import com.example.atlas.agent.fuel.FuelAgent;
import com.example.atlas.agent.habits.HabitsAgent;
import com.example.atlas.agent.planner.PlannerAgent;
import com.example.atlas.agent.recovery.RecoveryAgent;
import com.example.atlas.agent.report.ReportAgent;
import com.example.atlas.conversation.service.TelegramLifeFlowService;
import com.example.atlas.orchestrator.OrchestratorService;
import com.example.atlas.safety.SafetyGuard;
import com.example.atlas.user.UserLanguage;
import com.example.atlas.user.entity.TelegramUserEntity;
import com.example.atlas.user.service.TelegramUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramUpdateHandlerTest {

    private final RecordingTelegramApiClient apiClient = new RecordingTelegramApiClient();
    private final TelegramUpdateHandler handler = new TelegramUpdateHandler(
            orchestratorService(),
            new TelegramMessageSender(apiClient),
            new SafetyGuard()
    );

    @Test
    void supportedCommandTextReachesOrchestrator() {
        boolean handled = handler.handleUpdate(textUpdate("/workout"));

        assertThat(handled).isTrue();
        assertThat(apiClient.sentTexts()).singleElement().asString()
                .contains("Движение на сегодня");
    }

    @Test
    void dayCommandReturnsPlannerResponse() {
        boolean handled = handler.handleUpdate(textUpdate("/day"));

        assertThat(handled).isTrue();
        assertThat(apiClient.sentTexts()).singleElement().asString()
                .contains("План дня");
    }

    @Test
    void simpleCommandReturnsExpectedResult() {
        assertThat(handler.handleTextMessage("/start"))
                .contains("ATLAS на связи");
    }

    @Test
    void safetyKeywordReturnsSafeRecommendation() {
        assertThat(handler.handleTextMessage("I have chest pain during workout"))
                .contains("квалифицированному специалисту")
                .contains("боль в груди");
    }

    @Test
    void unsupportedUpdateWithoutMessageIsIgnored() {
        boolean handled = handler.handleUpdate(new TelegramUpdate(
                100L,
                null,
                new TelegramUpdate.TelegramMessage(
                        10L,
                        new TelegramUpdate.TelegramChat(42L),
                        null,
                        "edited"
                ),
                null
        ));

        assertThat(handled).isFalse();
        assertThat(apiClient.sentTexts()).isEmpty();
    }

    @Test
    void blankTextMessageIsIgnored() {
        boolean handled = handler.handleUpdate(new TelegramUpdate(
                100L,
                new TelegramUpdate.TelegramMessage(
                        10L,
                        new TelegramUpdate.TelegramChat(42L),
                        new TelegramUpdate.TelegramUser(7L, "user", "User"),
                        "  "
                ),
                null,
                null
        ));

        assertThat(handled).isFalse();
        assertThat(apiClient.sentTexts()).isEmpty();
    }

    @Test
    void callbackQueryOpensMainMenuAndIsAnswered() {
        boolean handled = handler.handleUpdate(callbackUpdate("atlas:menu"));

        assertThat(handled).isTrue();
        assertThat(apiClient.answeredCallbackIds()).containsExactly("callback-1");
        assertThat(apiClient.editedCaptions()).singleElement().asString()
                .contains("ATLAS")
                .contains("Что хочешь сделать сейчас");
        assertThat(apiClient.editedMarkups()).singleElement().isNotNull();
    }

    @Test
    void menuCallbackWithActiveFlowOpensMenuWithoutCancellingFlow() {
        RecordingTelegramApiClient client = new RecordingTelegramApiClient();
        FakeTelegramUserService userService = new FakeTelegramUserService();
        userService.user().updateLanguage(UserLanguage.RU);
        FakeActiveLifeFlowService lifeFlowService = new FakeActiveLifeFlowService();
        TelegramUpdateHandler activeFlowHandler = handler(client, userService, lifeFlowService);

        boolean handled = activeFlowHandler.handleUpdate(callbackUpdate("atlas:menu"));

        assertThat(handled).isTrue();
        assertThat(client.editedCaptions()).singleElement().asString()
                .contains("ATLAS")
                .contains("Что хочешь сделать сейчас");
        assertThat(lifeFlowService.active).isTrue();
        assertThat(client.answeredCallbackIds()).containsExactly("callback-1");
    }

    @Test
    void unsupportedCallbackReturnsSafeFallback() {
        boolean handled = handler.handleUpdate(callbackUpdate("atlas:bad"));

        assertThat(handled).isTrue();
        assertThat(apiClient.answeredCallbackIds()).containsExactly("callback-1");
        assertThat(apiClient.editedCaptions()).singleElement().asString()
                .contains("Не получилось обработать кнопку");
    }

    @Test
    void helpCommandSendsProductPanel() {
        boolean handled = handler.handleUpdate(textUpdate("/help"));

        assertThat(handled).isTrue();
        assertThat(apiClient.sentPhotoCaptions()).singleElement().asString()
                .contains("Помощь ATLAS");
        assertThat(apiClient.sentPhotoMarkups()).singleElement().isNotNull();
    }

    @Test
    void clearCommandDeletesCommandMessageAndShowsFreshPanel() {
        boolean handled = handler.handleUpdate(textUpdate("/clear"));

        assertThat(handled).isTrue();
        assertThat(apiClient.deletedMessages()).containsExactly("42:10");
        assertThat(apiClient.sentPhotoCaptions()).singleElement().asString()
                .contains("Что хочешь сделать сейчас");
    }

    @Test
    void menuCallbackFallsBackToNewPanelWhenCaptionEditFails() {
        RecordingTelegramApiClient client = new RecordingTelegramApiClient();
        client.failEdits();
        TelegramUpdateHandler fallbackHandler = new TelegramUpdateHandler(
                orchestratorService(),
                new TelegramMessageSender(client),
                new SafetyGuard()
        );

        boolean handled = fallbackHandler.handleUpdate(callbackUpdate("atlas:menu"));

        assertThat(handled).isTrue();
        assertThat(client.editedCaptions()).isEmpty();
        assertThat(client.sentPhotoCaptions()).singleElement().asString()
                .contains("Что хочешь сделать сейчас");
        assertThat(client.answeredCallbackIds()).containsExactly("callback-1");
    }

    @Test
    void startWithoutSavedLanguageSendsLanguagePanel() {
        RecordingTelegramApiClient client = new RecordingTelegramApiClient();
        FakeTelegramUserService userService = new FakeTelegramUserService();
        TelegramUpdateHandler languageHandler = handler(client, userService);

        boolean handled = languageHandler.handleUpdate(textUpdate("/start"));

        assertThat(handled).isTrue();
        assertThat(client.sentPhotoCaptions()).singleElement().asString()
                .contains("Choose your language / Выберите язык");
        assertThat(client.sentPhotoMarkups()).singleElement()
                .extracting(markup -> markup.inlineKeyboard().get(0).get(0).callbackData())
                .isEqualTo("atlas:language:ru");
    }

    @Test
    void languageCallbackSavesLanguageAndEditsPanelToEnglishMenu() {
        RecordingTelegramApiClient client = new RecordingTelegramApiClient();
        FakeTelegramUserService userService = new FakeTelegramUserService();
        TelegramUpdateHandler languageHandler = handler(client, userService);

        boolean handled = languageHandler.handleUpdate(callbackUpdate("atlas:language:en"));

        assertThat(handled).isTrue();
        assertThat(userService.user().getLanguage()).contains(UserLanguage.EN);
        assertThat(client.editedCaptions()).singleElement().asString()
                .contains("What would you like to do now?");
        assertThat(client.answeredCallbackIds()).containsExactly("callback-1");
        assertThat(client.answeredCallbackTexts()).containsExactly("Language saved");
    }

    @Test
    void settingsLanguageButtonEditsPanelToLanguageSelection() {
        RecordingTelegramApiClient client = new RecordingTelegramApiClient();
        FakeTelegramUserService userService = new FakeTelegramUserService();
        userService.user().updateLanguage(UserLanguage.EN);
        TelegramUpdateHandler languageHandler = handler(client, userService);

        boolean handled = languageHandler.handleUpdate(callbackUpdate("atlas:settings:language"));

        assertThat(handled).isTrue();
        assertThat(client.editedCaptions()).singleElement().asString()
                .contains("Choose your language / Выберите язык");
    }

    private TelegramUpdate textUpdate(String text) {
        return new TelegramUpdate(
                100L,
                new TelegramUpdate.TelegramMessage(
                        10L,
                        new TelegramUpdate.TelegramChat(42L),
                        new TelegramUpdate.TelegramUser(7L, "user", "User"),
                        text
                ),
                null,
                null
        );
    }

    private TelegramUpdate callbackUpdate(String callbackData) {
        return new TelegramUpdate(
                100L,
                null,
                null,
                new TelegramUpdate.TelegramCallbackQuery(
                        "callback-1",
                        new TelegramUpdate.TelegramUser(7L, "user", "User"),
                        new TelegramUpdate.TelegramMessage(
                                10L,
                                new TelegramUpdate.TelegramChat(42L),
                                new TelegramUpdate.TelegramUser(7L, "user", "User"),
                                null
                        ),
                        callbackData
                )
        );
    }

    private OrchestratorService orchestratorService() {
        return new OrchestratorService(List.of(
                new CoreAgent(),
                new CoachAgent(),
                new PlannerAgent(),
                new RecoveryAgent(),
                new HabitsAgent(),
                new FuelAgent(),
                new ReportAgent()
        ));
    }

    private TelegramUpdateHandler handler(RecordingTelegramApiClient client, TelegramUserService userService) {
        return handler(client, userService, null);
    }

    private TelegramUpdateHandler handler(
            RecordingTelegramApiClient client,
            TelegramUserService userService,
            TelegramLifeFlowService lifeFlowService
    ) {
        return new TelegramUpdateHandler(
                orchestratorService(),
                new TelegramMessageSender(client),
                new SafetyGuard(),
                provider(userService),
                provider(null),
                provider(lifeFlowService),
                new TelegramActionRouter(),
                new TelegramKeyboardFactory()
        );
    }

    private <T> ObjectProvider<T> provider(T value) {
        return new ObjectProvider<>() {
            @Override
            public T getObject(Object... args) {
                return value;
            }

            @Override
            public T getIfAvailable() {
                return value;
            }

            @Override
            public T getIfUnique() {
                return value;
            }

            @Override
            public T getObject() {
                return value;
            }
        };
    }

    private static class RecordingTelegramApiClient implements TelegramApiClient {

        private final List<String> sentTexts = new ArrayList<>();
        private final List<InlineKeyboardMarkup> sentMarkups = new ArrayList<>();
        private final List<String> sentPhotoCaptions = new ArrayList<>();
        private final List<InlineKeyboardMarkup> sentPhotoMarkups = new ArrayList<>();
        private final List<String> editedCaptions = new ArrayList<>();
        private final List<InlineKeyboardMarkup> editedMarkups = new ArrayList<>();
        private final List<String> answeredCallbackIds = new ArrayList<>();
        private final List<String> answeredCallbackTexts = new ArrayList<>();
        private final List<String> deletedMessages = new ArrayList<>();
        private boolean failEdits;

        @Override
        public void sendMessage(long chatId, String text) {
            sentTexts.add(text);
            sentMarkups.add(null);
        }

        @Override
        public void sendMessage(long chatId, String text, InlineKeyboardMarkup replyMarkup) {
            sentTexts.add(text);
            sentMarkups.add(replyMarkup);
        }

        @Override
        public void sendPhoto(long chatId, String photo, String caption, InlineKeyboardMarkup replyMarkup) {
            sentPhotoCaptions.add(caption);
            sentPhotoMarkups.add(replyMarkup);
        }

        @Override
        public void editMessageCaption(long chatId, long messageId, String caption, InlineKeyboardMarkup replyMarkup) {
            if (failEdits) {
                throw new IllegalStateException("caption edit failed");
            }
            editedCaptions.add(caption);
            editedMarkups.add(replyMarkup);
        }

        @Override
        public void answerCallbackQuery(String callbackQueryId, String text) {
            answeredCallbackIds.add(callbackQueryId);
            answeredCallbackTexts.add(text);
        }

        @Override
        public void deleteMessage(long chatId, long messageId) {
            deletedMessages.add(chatId + ":" + messageId);
        }

        List<String> sentTexts() {
            return sentTexts;
        }

        List<InlineKeyboardMarkup> sentMarkups() {
            return sentMarkups;
        }

        List<String> sentPhotoCaptions() {
            return sentPhotoCaptions;
        }

        List<InlineKeyboardMarkup> sentPhotoMarkups() {
            return sentPhotoMarkups;
        }

        List<String> editedCaptions() {
            return editedCaptions;
        }

        List<InlineKeyboardMarkup> editedMarkups() {
            return editedMarkups;
        }

        List<String> answeredCallbackIds() {
            return answeredCallbackIds;
        }

        List<String> answeredCallbackTexts() {
            return answeredCallbackTexts;
        }

        List<String> deletedMessages() {
            return deletedMessages;
        }

        void failEdits() {
            failEdits = true;
        }
    }

    private static class FakeTelegramUserService extends TelegramUserService {

        private final TelegramUserEntity user = TelegramUserEntity.create(
                7L,
                42L,
                "user",
                "User",
                Instant.parse("2026-06-04T00:00:00Z")
        );

        FakeTelegramUserService() {
            super(null);
        }

        @Override
        public TelegramUserEntity upsertFromMessage(TelegramUpdate.TelegramMessage message) {
            return user;
        }

        @Override
        public TelegramUserEntity upsertFromCallbackQuery(TelegramUpdate.TelegramCallbackQuery callbackQuery) {
            return user;
        }

        @Override
        public TelegramUserEntity updateLanguage(TelegramUserEntity user, UserLanguage language) {
            this.user.updateLanguage(language);
            return this.user;
        }

        TelegramUserEntity user() {
            return user;
        }
    }

    private static class FakeActiveLifeFlowService extends TelegramLifeFlowService {

        private boolean active = true;

        FakeActiveLifeFlowService() {
            super(null, null, null, null, null, null, null, null);
        }

        @Override
        public boolean hasActiveFlow(TelegramUserEntity user) {
            return active;
        }
    }
}
