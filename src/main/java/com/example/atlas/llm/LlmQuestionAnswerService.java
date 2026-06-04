package com.example.atlas.llm;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.user.UserLanguage;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@ConditionalOnBean(LlmContextAssembler.class)
public class LlmQuestionAnswerService {

    private static final List<String> ATLAS_SCOPE_MARKERS = List.of(
            "day",
            "plan",
            "focus",
            "habit",
            "state",
            "energy",
            "stress",
            "sleep",
            "report",
            "reflection",
            "progress",
            "день",
            "план",
            "фокус",
            "привыч",
            "состоя",
            "энерг",
            "стресс",
            "сон",
            "отч",
            "рефлекс",
            "прогресс"
    );

    private final AtlasProperties properties;
    private final LlmClient llmClient;
    private final LlmContextAssembler contextAssembler;
    private final PromptTemplateService promptTemplateService;
    private final LlmSafetyService safetyService;

    public LlmQuestionAnswerService(
            AtlasProperties properties,
            LlmClient llmClient,
            LlmContextAssembler contextAssembler,
            PromptTemplateService promptTemplateService,
            LlmSafetyService safetyService
    ) {
        this.properties = properties;
        this.llmClient = llmClient;
        this.contextAssembler = contextAssembler;
        this.promptTemplateService = promptTemplateService;
        this.safetyService = safetyService;
    }

    public Optional<String> answer(TelegramUserEntity user, String question) {
        if (safetyService.inputHasSafetyRisk(question)) {
            UserLanguage language = user.getLanguage().orElse(UserLanguage.RU);
            return Optional.of(safetyService.deterministicSafetyResponse(language));
        }
        if (!properties.llm().questionAvailable() || !llmClient.available()) {
            return Optional.empty();
        }
        if (!inAtlasScope(question)) {
            UserLanguage language = user.getLanguage().orElse(UserLanguage.RU);
            return Optional.of(scopeFallback(language));
        }
        try {
            PromptContext context = contextAssembler.assemble(user, PromptPurpose.QUESTION_ROUTING, question);
            LlmResponse response = llmClient.chat(promptTemplateService.request(context));
            return safetyService.safeOutput(response.text(), context.language());
        } catch (LlmClientException exception) {
            return Optional.empty();
        }
    }

    private boolean inAtlasScope(String question) {
        if (question == null || question.isBlank()) {
            return false;
        }
        String normalized = question.toLowerCase(Locale.ROOT);
        return ATLAS_SCOPE_MARKERS.stream().anyMatch(normalized::contains);
    }

    private String scopeFallback(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return "I can answer within ATLAS flows: state, focus, habits, day planning, reflection and weekly progress. Use Check-in, Day plan, Habits or Report.";
        }
        return "Я отвечаю в рамках ATLAS: состояние, фокус, привычки, план дня, рефлексия и недельный прогресс. Можно выбрать Check-in, План дня, Привычки или Отчёт.";
    }
}
