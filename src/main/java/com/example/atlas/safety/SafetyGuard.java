package com.example.atlas.safety;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class SafetyGuard {

    private static final List<String> RISK_KEYWORDS = List.of(
            "боль",
            "болит",
            "pain",
            "травма",
            "injury",
            "боль в груди",
            "chest pain",
            "проблемы с дыханием",
            "трудно дышать",
            "breathing problems",
            "breathing issues",
            "головокружение",
            "dizziness",
            "обморок",
            "fainting",
            "давление",
            "blood pressure",
            "сердце",
            "heart",
            "heart problems",
            "сильный перегруз",
            "перегруз",
            "severe overload",
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
                + "Не игнорируй сильные или повторяющиеся сигналы и обратись к квалифицированному специалисту, "
                + "если есть боль в груди, проблемы с дыханием, давлением, сердцем, головокружением, обмороком или выраженным перегрузом.";
    }
}
