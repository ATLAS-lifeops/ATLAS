package com.example.atlas.runtime.service;

import com.example.atlas.llm.LlmProvider;

public record LlmRuntimeStatus(
        boolean enabled,
        boolean configured,
        LlmProvider provider,
        String model,
        String baseUrlHost,
        boolean dayPlanEnabled,
        boolean reportEnabled,
        boolean questionEnabled,
        String status
) {
}
