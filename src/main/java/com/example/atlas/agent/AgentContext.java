package com.example.atlas.agent;

import com.example.atlas.orchestrator.RequestType;

import java.time.Instant;

public record AgentContext(
        Long userId,
        String message,
        RequestType requestType,
        Instant receivedAt
) {

    public static AgentContext anonymous(String message, RequestType requestType) {
        return new AgentContext(null, message, requestType, Instant.now());
    }
}
