package com.example.atlas.llm;

public record LlmResponse(
        String text,
        String model,
        LlmProvider provider,
        LlmUsage usage,
        String finishReason,
        String rawProviderRequestId
) {

    public LlmResponse {
        text = text == null ? "" : text;
        model = model == null ? "" : model;
        usage = usage == null ? LlmUsage.empty() : usage;
        finishReason = finishReason == null ? "" : finishReason;
        rawProviderRequestId = rawProviderRequestId == null ? "" : rawProviderRequestId;
    }
}
