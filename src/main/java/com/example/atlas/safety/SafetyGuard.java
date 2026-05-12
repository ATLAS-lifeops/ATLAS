package com.example.atlas.safety;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class SafetyGuard {

    private static final List<String> RISK_KEYWORDS = List.of(
            "pain",
            "injury",
            "chest pain",
            "breathing problems",
            "dizziness",
            "fainting",
            "heart problems",
            "blood pressure",
            "eating disorder",
            "extreme weight loss"
    );

    public boolean requiresSafetyResponse(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }

        String normalized = message.toLowerCase(Locale.ROOT);
        return RISK_KEYWORDS.stream().anyMatch(normalized::contains);
    }

    public String safetyResponse() {
        return "Похоже, сообщение затрагивает симптомы или риски для здоровья. "
                + "Снизь интенсивность, не тренируйся через боль и обратись к медицинскому специалисту, "
                + "если симптомы сильные, повторяются или связаны с дыханием, сердцем, давлением, головокружением или обмороком.";
    }
}
