package com.example.atlas.hosted;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HostedRateLimiter {

    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();
    private final Clock clock;

    public HostedRateLimiter() {
        this(Clock.systemUTC());
    }

    HostedRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public boolean allow(Long userId, String operation, int limit, Duration window) {
        String key = userId + ":" + operation;
        Instant now = Instant.now(clock);
        RateLimitBucket updated = buckets.compute(key, (ignored, current) -> {
            if (current == null || !current.resetAt().isAfter(now)) {
                return new RateLimitBucket(1, now.plus(window), true);
            }
            if (current.used() >= limit) {
                return new RateLimitBucket(current.used(), current.resetAt(), false);
            }
            return new RateLimitBucket(current.used() + 1, current.resetAt(), true);
        });
        return updated.allowed();
    }
}
