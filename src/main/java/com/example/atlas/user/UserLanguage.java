package com.example.atlas.user;

import java.util.Locale;
import java.util.Optional;

public enum UserLanguage {
    RU("ru"),
    EN("en");

    private final String code;

    UserLanguage(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static Optional<UserLanguage> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.strip().toLowerCase(Locale.ROOT);
        for (UserLanguage language : values()) {
            if (language.code.equals(normalized)) {
                return Optional.of(language);
            }
        }
        return Optional.empty();
    }
}
