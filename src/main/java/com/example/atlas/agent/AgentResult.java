package com.example.atlas.agent;

import java.util.List;

public record AgentResult(
        String content,
        List<String> handledBy
) {

    public AgentResult {
        handledBy = List.copyOf(handledBy);
    }

    public static AgentResult reply(String content, String agentName) {
        return new AgentResult(content, List.of(agentName));
    }
}
