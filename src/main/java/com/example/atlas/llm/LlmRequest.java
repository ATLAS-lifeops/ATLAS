package com.example.atlas.llm;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public record LlmRequest(
        String model,
        List<LlmMessage> messages,
        double temperature,
        int maxOutputTokens,
        String requestPurpose,
        UUID userScopeId,
        Duration timeout
) {

    public LlmRequest {
        model = model == null ? "" : model.strip();
        messages = messages == null ? List.of() : List.copyOf(messages);
        requestPurpose = requestPurpose == null ? "" : requestPurpose.strip();
        timeout = timeout == null || timeout.isNegative() || timeout.isZero() ? Duration.ofSeconds(20) : timeout;
    }
}
