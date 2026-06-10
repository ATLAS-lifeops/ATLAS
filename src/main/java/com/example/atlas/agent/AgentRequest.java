package com.example.atlas.agent;

import com.example.atlas.orchestrator.RequestType;

public record AgentRequest(
        Long userId,
        String message,
        RequestType requestType
) {
}
