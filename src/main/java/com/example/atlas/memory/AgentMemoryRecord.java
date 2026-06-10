package com.example.atlas.memory;

import com.example.atlas.agent.AgentType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AgentMemoryRecord(
        UUID id,
        UUID userId,
        AgentType agentType,
        MemoryType type,
        MemoryScope scope,
        String title,
        String content,
        MemoryConfidence confidence,
        List<MemoryTag> tags,
        MemorySource source,
        Instant createdAt,
        Instant updatedAt,
        Instant expiresAt,
        boolean archived
) {
    public AgentMemoryRecord {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
