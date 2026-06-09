package com.example.atlas.memory;

import com.example.atlas.agent.AgentType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MemoryWrite(
        UUID userId,
        AgentType ownerAgent,
        MemoryType type,
        MemoryScope scope,
        String title,
        String content,
        MemoryConfidence confidence,
        List<MemoryTag> tags,
        MemorySource source,
        Instant expiresAt
) {
    public MemoryWrite {
        title = title == null ? "" : title.strip();
        content = content == null ? "" : content.strip();
        confidence = confidence == null ? MemoryConfidence.MEDIUM : confidence;
        tags = tags == null ? List.of() : List.copyOf(tags);
        source = source == null ? MemorySource.SYSTEM : source;
    }
}
