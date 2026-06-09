package com.example.atlas.memory;

public record MemoryValidationResult(boolean valid, String reason) {
    public static MemoryValidationResult accepted() {
        return new MemoryValidationResult(true, "accepted");
    }

    public static MemoryValidationResult rejected(String reason) {
        return new MemoryValidationResult(false, reason);
    }
}
