package com.example.atlas.shared.domain;

import java.util.Objects;

public record TelegramUserId(Long value) {

    public TelegramUserId {
        Objects.requireNonNull(value, "value must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException("value must be positive");
        }
    }
}
