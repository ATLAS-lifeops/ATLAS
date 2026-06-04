package com.example.atlas.llm;

import com.example.atlas.safety.SafetyGuard;
import com.example.atlas.user.UserLanguage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class LlmSafetyService {

    private static final List<String> UNSAFE_OUTPUT_MARKERS = List.of(
            "push through pain",
            "ignore the pain",
            "diagnosis",
            "i diagnose",
            "treatment plan",
            "prescribe",
            "лечебный план",
            "назначаю лечение",
            "диагноз",
            "преодолевай боль",
            "игнорируй боль"
    );

    private final SafetyGuard safetyGuard;

    public LlmSafetyService(SafetyGuard safetyGuard) {
        this.safetyGuard = safetyGuard;
    }

    public boolean inputHasSafetyRisk(String text) {
        return safetyGuard.requiresSafetyResponse(text);
    }

    public Optional<String> safeOutput(String output, UserLanguage language) {
        if (output == null || output.isBlank()) {
            return Optional.empty();
        }
        String normalized = output.toLowerCase(Locale.ROOT);
        boolean unsafe = UNSAFE_OUTPUT_MARKERS.stream().anyMatch(normalized::contains);
        if (unsafe) {
            return Optional.empty();
        }
        return Optional.of(output.strip());
    }

    public String deterministicSafetyResponse(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return "This may involve symptoms or health risk. Do not ignore chest pain, breathing issues, blood pressure concerns, heart concerns, fainting, dizziness or severe overload. Contact a qualified professional if these signs are present or recurring.";
        }
        return safetyGuard.safetyResponse();
    }
}
