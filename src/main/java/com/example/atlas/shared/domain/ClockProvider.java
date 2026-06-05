package com.example.atlas.shared.domain;

import java.time.Instant;

@FunctionalInterface
public interface ClockProvider {

    Instant now();
}
