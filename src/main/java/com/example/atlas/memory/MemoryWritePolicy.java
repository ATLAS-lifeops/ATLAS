package com.example.atlas.memory;

import java.util.Locale;
import java.util.Set;

public class MemoryWritePolicy {

    private static final Set<String> SECRET_MARKERS = Set.of(
            "token", "api key", "apikey", "secret", "password", "bearer ", "telegram_bot_token"
    );
    private static final Set<String> UNSAFE_MEDICAL_MARKERS = Set.of(
            "diagnosis", "diagnose", "prescribe", "treatment plan", "blood pressure medicine",
            "диагноз", "назначить лечение", "давление лекарство"
    );

    public MemoryValidationResult validate(MemoryWrite write) {
        if (write == null) {
            return MemoryValidationResult.rejected("missing_memory");
        }
        if (write.userId() == null) {
            return MemoryValidationResult.rejected("missing_user_scope");
        }
        if (write.ownerAgent() == null || write.scope() == null || write.type() == null) {
            return MemoryValidationResult.rejected("missing_owner_or_scope");
        }
        if (write.content().isBlank() || write.content().length() < 8) {
            return MemoryValidationResult.rejected("not_useful");
        }
        String lowered = (write.title() + " " + write.content()).toLowerCase(Locale.ROOT);
        if (SECRET_MARKERS.stream().anyMatch(lowered::contains)) {
            return MemoryValidationResult.rejected("secret_like_content");
        }
        if (UNSAFE_MEDICAL_MARKERS.stream().anyMatch(lowered::contains)) {
            return MemoryValidationResult.rejected("unsafe_medical_claim");
        }
        return MemoryValidationResult.accepted();
    }

    public String deduplicationKey(MemoryWrite write) {
        String normalized = write.content().toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", " ")
                .strip();
        return write.userId() + ":" + write.ownerAgent() + ":" + write.scope() + ":" + normalized;
    }
}
