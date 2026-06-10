package com.example.atlas.hosted;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class HostedRateLimiterTest {

    @Test
    void blocksCallsAfterLimit() {
        HostedRateLimiter limiter = new HostedRateLimiter();

        assertThat(limiter.allow(1L, "question", 2, Duration.ofMinutes(1))).isTrue();
        assertThat(limiter.allow(1L, "question", 2, Duration.ofMinutes(1))).isTrue();
        assertThat(limiter.allow(1L, "question", 2, Duration.ofMinutes(1))).isFalse();
        assertThat(limiter.allow(2L, "question", 2, Duration.ofMinutes(1))).isTrue();
    }
}
