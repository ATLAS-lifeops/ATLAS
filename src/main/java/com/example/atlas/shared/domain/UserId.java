package com.example.atlas.shared.domain;

import java.util.Objects;

public record UserId(Long value) {

    public UserId {
        Objects.requireNonNull(value, "value must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException("value must be positive");
        }
    }
}
