package com.example.atlas.llm;

import com.example.atlas.user.UserLanguage;

import java.util.UUID;

public record PromptContext(
        PromptPurpose purpose,
        UUID userScopeId,
        UserLanguage language,
        String structuredContext,
        String currentRequest,
        boolean safetyRisk
) {
}
