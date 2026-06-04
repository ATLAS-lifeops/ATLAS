package com.example.atlas.llm;

public record LlmUsage(Integer inputTokens, Integer outputTokens, Integer totalTokens) {

    public static LlmUsage empty() {
        return new LlmUsage(null, null, null);
    }
}
