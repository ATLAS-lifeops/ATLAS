package com.example.atlas.llm;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@ConditionalOnBean(LlmContextAssembler.class)
public class LlmDayPlanService {

    private final AtlasProperties properties;
    private final LlmClient llmClient;
    private final LlmContextAssembler contextAssembler;
    private final PromptTemplateService promptTemplateService;
    private final LlmSafetyService safetyService;

    public LlmDayPlanService(
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

    public Optional<String> dayPlan(TelegramUserEntity user, String deterministicFallback) {
        if (!properties.llm().dayPlanAvailable() || !llmClient.available()) {
            return Optional.empty();
        }
        try {
            PromptContext context = contextAssembler.assemble(user, PromptPurpose.DAY_PLAN, "/day");
            LlmResponse response = llmClient.chat(promptTemplateService.request(context));
            return safetyService.safeOutput(response.text(), context.language());
        } catch (LlmClientException exception) {
            return Optional.empty();
        }
    }
}
