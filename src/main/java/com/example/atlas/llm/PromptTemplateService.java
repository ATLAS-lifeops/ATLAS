package com.example.atlas.llm;

import com.example.atlas.config.AtlasProperties;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class PromptTemplateService {

    private static final String CORE_SYSTEM_INSTRUCTION = """
            You are ATLAS, a Telegram-based life operating system. Help the user understand state, focus, habits, planning, reflection and progress. Do not act as a doctor, therapist, dietitian or medical specialist. Do not diagnose or prescribe treatment. If serious symptoms, pain, breathing issues, heart concerns, fainting or blood pressure concerns appear, recommend contacting a qualified professional. Do not invent user data. Use only the provided context.
            """.strip();

    private final AtlasProperties properties;

    public PromptTemplateService(AtlasProperties properties) {
        this.properties = properties;
    }

    public LlmRequest request(PromptContext context) {
        PromptTemplate template = template(context.purpose());
        String language = context.language() == com.example.atlas.user.UserLanguage.EN ? "English" : "Russian";
        String userPrompt = template.user()
                .replace("{{language}}", language)
                .replace("{{context}}", context.structuredContext() == null ? "" : context.structuredContext())
                .replace("{{currentRequest}}", context.currentRequest() == null ? "" : context.currentRequest())
                .replace("{{safetyNotes}}", context.safetyRisk()
                        ? "Safety risk is present. Keep the response conservative and recommend qualified help for serious symptoms."
                        : "No explicit safety risk was detected.");
        AtlasProperties.Llm llm = properties.llm();
        return new LlmRequest(
                llm.model(),
                List.of(
                        new LlmMessage(LlmRole.SYSTEM, template.system()),
                        new LlmMessage(LlmRole.USER, userPrompt)
                ),
                llm.temperature(),
                llm.maxOutputTokens(),
                context.purpose().name(),
                context.userScopeId(),
                Duration.ofSeconds(llm.timeoutSeconds())
        );
    }

    public PromptTemplate template(PromptPurpose purpose) {
        return switch (purpose) {
            case DAY_PLAN -> new PromptTemplate(
                    purpose,
                    CORE_SYSTEM_INSTRUCTION,
                    """
                            Answer in {{language}}.
                            Build a realistic ATLAS day plan from the context. Include: main focus, short action list, state support, minimal habit, and a fallback if the day falls apart. Adapt to energy, focus, stress and sleep when present. Do not make movement central. Do not invent missing data.

                            {{safetyNotes}}

                            Context:
                            {{context}}
                            """
            );
            case WEEKLY_REPORT -> new PromptTemplate(
                    purpose,
                    CORE_SYSTEM_INSTRUCTION,
                    """
                            Answer in {{language}}.
                            Summarize the last 7 days from the deterministic metrics and context only. Mention missing data. Identify a simple pattern only if supported. Suggest one next-week focus. Keep it short and do not invent metrics.

                            {{safetyNotes}}

                            Context:
                            {{context}}
                            """
            );
            case QUESTION_ROUTING -> new PromptTemplate(
                    purpose,
                    CORE_SYSTEM_INSTRUCTION,
                    """
                            Answer in {{language}}.
                            The user asked: {{currentRequest}}

                            Answer only inside ATLAS scope: day planning, focus, habits, state tracking, reflection and progress. If the question is outside scope, politely route back to supported ATLAS flows. If it asks for medical, legal or financial high-stakes advice, avoid direct advice and recommend a qualified professional or safe general guidance.

                            {{safetyNotes}}

                            Context:
                            {{context}}
                            """
            );
        };
    }
}
