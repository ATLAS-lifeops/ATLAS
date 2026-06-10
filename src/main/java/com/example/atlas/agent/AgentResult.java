package com.example.atlas.agent;

import com.example.atlas.memory.MemoryWrite;

import java.util.List;
import java.util.Map;

public record AgentResult(
        String content,
        List<String> handledBy,
        Map<String, String> metadata,
        boolean fallback,
        boolean safety,
        List<MemoryWrite> memoryWrites
) {

    public AgentResult {
        handledBy = List.copyOf(handledBy);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        memoryWrites = memoryWrites == null ? List.of() : List.copyOf(memoryWrites);
    }

    public AgentResult(String content, List<String> handledBy) {
        this(content, handledBy, Map.of(), false, false, List.of());
    }

    public static AgentResult reply(String content, String agentName) {
        return new AgentResult(content, List.of(agentName));
    }

    public static AgentResult fallback(String content, String agentName) {
        return new AgentResult(content, List.of(agentName), Map.of(), true, false, List.of());
    }

    public AgentResult withMemoryWrites(List<MemoryWrite> writes) {
        return new AgentResult(content, handledBy, metadata, fallback, safety, writes);
    }
}
