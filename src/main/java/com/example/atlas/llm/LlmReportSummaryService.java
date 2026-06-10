package com.example.atlas.llm;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.hosted.LlmQuotaService;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@ConditionalOnBean(LlmContextAssembler.class)
public class LlmReportSummaryService {

    private final AtlasProperties properties;
    private final LlmClient llmClient;
    private final LlmContextAssembler contextAssembler;
    private final PromptTemplateService promptTemplateService;
    private final LlmSafetyService safetyService;
    private final ObjectProvider<LlmQuotaService> quotaService;

    @Autowired
    public LlmReportSummaryService(
            AtlasProperties properties,
            LlmClient llmClient,
            LlmContextAssembler contextAssembler,
            PromptTemplateService promptTemplateService,
            LlmSafetyService safetyService,
            ObjectProvider<LlmQuotaService> quotaService
    ) {
        this.properties = properties;
        this.llmClient = llmClient;
        this.contextAssembler = contextAssembler;
        this.promptTemplateService = promptTemplateService;
        this.safetyService = safetyService;
        this.quotaService = quotaService;
    }

    public LlmReportSummaryService(
            AtlasProperties properties,
            LlmClient llmClient,
            LlmContextAssembler contextAssembler,
            PromptTemplateService promptTemplateService,
            LlmSafetyService safetyService
    ) {
        this(properties, llmClient, contextAssembler, promptTemplateService, safetyService, null);
    }

    public Optional<String> summary(TelegramUserEntity user, String deterministicMetrics) {
        if (!properties.llm().reportAvailable() || !llmClient.available() || !quotaAllows(user)) {
            return Optional.empty();
        }
        try {
            PromptContext context = contextAssembler.assemble(
                    user,
                    PromptPurpose.WEEKLY_REPORT,
                    "Deterministic weekly report metrics:\n" + deterministicMetrics
            );
            LlmResponse response = llmClient.chat(promptTemplateService.request(context));
            return safetyService.safeOutput(response.text(), context.language())
                    .map(text -> deterministicMetrics.strip() + "\n\nКраткое объяснение\n" + text);
        } catch (LlmClientException exception) {
            return Optional.empty();
        }
    }

    private boolean quotaAllows(TelegramUserEntity user) {
        LlmQuotaService service = quotaService == null ? null : quotaService.getIfAvailable();
        return service == null || user == null || service.allowLlmCall(user.getTelegramUserId());
    }
}
