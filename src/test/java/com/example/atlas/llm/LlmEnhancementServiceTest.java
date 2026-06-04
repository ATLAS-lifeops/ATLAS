package com.example.atlas.llm;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.safety.SafetyGuard;
import com.example.atlas.user.UserLanguage;
import com.example.atlas.user.entity.TelegramUserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LlmEnhancementServiceTest {

    @Test
    void dayPlanUsesLlmWhenEnabled() {
        LlmDayPlanService service = new LlmDayPlanService(
                properties(true),
                new FixedClient("LLM day plan"),
                contextAssembler("How should I plan today?", false),
                new PromptTemplateService(properties(true)),
                new LlmSafetyService(new SafetyGuard())
        );

        assertThat(service.dayPlan(user(), "Deterministic plan")).contains("LLM day plan");
    }

    @Test
    void dayPlanFallsBackWhenClientFails() {
        LlmDayPlanService service = new LlmDayPlanService(
                properties(true),
                new FailingClient(),
                contextAssembler("How should I plan today?", false),
                new PromptTemplateService(properties(true)),
                new LlmSafetyService(new SafetyGuard())
        );

        assertThat(service.dayPlan(user(), "Deterministic plan")).isEmpty();
    }

    @Test
    void reportKeepsDeterministicMetricsAsSourceOfTruth() {
        LlmReportSummaryService service = new LlmReportSummaryService(
                properties(true),
                new FixedClient("Supported pattern summary"),
                contextAssembler("report", false),
                new PromptTemplateService(properties(true)),
                new LlmSafetyService(new SafetyGuard())
        );

        assertThat(service.summary(user(), "Check-ins: 3 из 7"))
                .hasValueSatisfying(text -> assertThat(text)
                        .contains("Check-ins: 3 из 7")
                        .contains("Supported pattern summary"));
    }

    @Test
    void questionRoutesOutOfScopeBackToAtlasFlows() {
        LlmQuestionAnswerService service = new LlmQuestionAnswerService(
                properties(true),
                new FixedClient("unused"),
                contextAssembler("unused", false),
                new PromptTemplateService(properties(true)),
                new LlmSafetyService(new SafetyGuard())
        );

        assertThat(service.answer(user(), "What is the capital of France?"))
                .hasValueSatisfying(text -> assertThat(text).contains("ATLAS"));
    }

    @Test
    void safetyRiskQuestionUsesDeterministicSafetyResponse() {
        TelegramUserEntity user = user();
        user.updateLanguage(UserLanguage.EN);
        LlmQuestionAnswerService service = new LlmQuestionAnswerService(
                properties(true),
                new FixedClient("push through pain"),
                contextAssembler("chest pain", true),
                new PromptTemplateService(properties(true)),
                new LlmSafetyService(new SafetyGuard())
        );

        assertThat(service.answer(user, "I have chest pain, what should I do?"))
                .hasValueSatisfying(text -> assertThat(text).contains("qualified professional"));
    }

    @Test
    void outputSanitizationBlocksUnsafeAdvice() {
        LlmSafetyService safetyService = new LlmSafetyService(new SafetyGuard());

        assertThat(safetyService.safeOutput("You should push through pain.", UserLanguage.EN)).isEmpty();
    }

    private LlmContextAssembler contextAssembler(String currentRequest, boolean safetyRisk) {
        return new FixedContextAssembler(currentRequest, safetyRisk);
    }

    private AtlasProperties properties(boolean enabled) {
        return new AtlasProperties(
                null,
                null,
                new AtlasProperties.Llm(
                        enabled,
                        LlmProvider.OPENAI_COMPATIBLE,
                        "http://localhost:1",
                        "test-key",
                        "test-model",
                        20,
                        700,
                        0.3,
                        1,
                        false,
                        0,
                        true,
                        true,
                        true
                )
        );
    }

    private TelegramUserEntity user() {
        return TelegramUserEntity.create(7L, 42L, "user", "User", Instant.parse("2026-06-01T08:00:00Z"));
    }

    private static class FixedClient implements LlmClient {
        private final String text;

        FixedClient(String text) {
            this.text = text;
        }

        @Override
        public LlmResponse chat(LlmRequest request) {
            return new LlmResponse(text, request.model(), LlmProvider.OPENAI_COMPATIBLE, LlmUsage.empty(), "stop", "req");
        }

        @Override
        public boolean available() {
            return true;
        }

        @Override
        public LlmProvider provider() {
            return LlmProvider.OPENAI_COMPATIBLE;
        }
    }

    private static class FailingClient extends FixedClient {
        FailingClient() {
            super("");
        }

        @Override
        public LlmResponse chat(LlmRequest request) {
            throw new LlmUnavailableException("unavailable");
        }
    }

    private static class FixedContextAssembler extends LlmContextAssembler {
        private final String currentRequest;
        private final boolean safetyRisk;

        FixedContextAssembler(String currentRequest, boolean safetyRisk) {
            super(null, null, null, null, new SafetyGuard());
            this.currentRequest = currentRequest;
            this.safetyRisk = safetyRisk;
        }

        @Override
        public PromptContext assemble(TelegramUserEntity user, PromptPurpose purpose, String currentRequest) {
            return new PromptContext(
                    purpose,
                    UUID.randomUUID(),
                    user.getLanguage().orElse(UserLanguage.RU),
                    "User profile\ncurrent_focus=Work",
                    this.currentRequest,
                    safetyRisk
            );
        }
    }
}
