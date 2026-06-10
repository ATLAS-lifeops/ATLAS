package com.example.atlas.hosted;

import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LlmQuotaService {

    private final HostedRateLimiter rateLimiter;

    public LlmQuotaService(HostedRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public boolean allowLlmCall(Long userId) {
        return rateLimiter.allow(userId, "llm", 40, Duration.ofDays(1));
    }
}
