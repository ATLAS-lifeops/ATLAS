package com.example.atlas.telegram;

import com.example.atlas.agent.coach.CoachAgent;
import com.example.atlas.agent.core.CoreAgent;
import com.example.atlas.agent.fuel.FuelAgent;
import com.example.atlas.agent.habits.HabitsAgent;
import com.example.atlas.agent.planner.PlannerAgent;
import com.example.atlas.agent.recovery.RecoveryAgent;
import com.example.atlas.agent.report.ReportAgent;
import com.example.atlas.orchestrator.OrchestratorService;
import com.example.atlas.safety.SafetyGuard;
import org.junit.jupiter.api.Test;

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
                .contains("Тренировка на сегодня");
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
                .contains("Снизь интенсивность")
                .contains("медицинскому специалисту");
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

    private static class RecordingTelegramApiClient implements TelegramApiClient {

        private final List<String> sentTexts = new ArrayList<>();

        @Override
        public void sendMessage(long chatId, String text) {
            sentTexts.add(text);
        }

        List<String> sentTexts() {
            return sentTexts;
        }
    }
}
