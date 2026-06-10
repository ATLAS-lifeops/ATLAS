package com.example.atlas.memory;

import java.util.UUID;

public record MemoryWriteResult(boolean stored, UUID memoryId, String reason) {
    public static MemoryWriteResult stored(UUID memoryId) {
        return new MemoryWriteResult(true, memoryId, "stored");
    }

    public static MemoryWriteResult rejected(String reason) {
        return new MemoryWriteResult(false, null, reason);
    }
}
