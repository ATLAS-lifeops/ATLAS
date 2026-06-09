package com.example.atlas.agent;

import com.example.atlas.memory.MemoryWrite;

import java.util.List;
import java.util.Map;

public record AgentResponse(
        String text,
        Map<String, String> metadata,
        boolean fallback,
        boolean safety,
        AgentFailureReason failureReason,
        List<MemoryWrite> memoryWrites
) {
    public AgentResponse {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        memoryWrites = memoryWrites == null ? List.of() : List.copyOf(memoryWrites);
        failureReason = failureReason == null ? AgentFailureReason.NONE : failureReason;
    }
}
