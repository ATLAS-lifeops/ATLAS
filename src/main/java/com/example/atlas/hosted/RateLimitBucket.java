package com.example.atlas.hosted;

import java.time.Instant;

record RateLimitBucket(int used, Instant resetAt, boolean allowed) {
}
