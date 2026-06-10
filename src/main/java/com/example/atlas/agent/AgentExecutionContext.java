package com.example.atlas.agent;

import java.time.Instant;
import java.util.List;

public record AgentExecutionContext(
        AgentRequest request,
        List<String> contextLines,
        Instant receivedAt
) {
    public AgentExecutionContext {
        contextLines = contextLines == null ? List.of() : List.copyOf(contextLines);
        receivedAt = receivedAt == null ? Instant.now() : receivedAt;
    }
}
