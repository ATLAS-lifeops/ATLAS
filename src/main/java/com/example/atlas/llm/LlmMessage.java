package com.example.atlas.llm;

public record LlmMessage(LlmRole role, String content) {

    public LlmMessage {
        if (role == null) {
            throw new IllegalArgumentException("role is required");
        }
        content = content == null ? "" : content;
    }
}
