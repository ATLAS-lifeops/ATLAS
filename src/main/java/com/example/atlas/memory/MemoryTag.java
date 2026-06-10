package com.example.atlas.memory;

public record MemoryTag(String value) {
    public MemoryTag {
        value = value == null ? "" : value.strip().toLowerCase();
    }
}
