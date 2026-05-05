package com.security.ratelimit;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isAllowed(String key) {

        Attempt attempt = attempts.computeIfAbsent(key, k -> new Attempt());

        long now = Instant.now().getEpochSecond();

        if (now - attempt.timestamp > WINDOW_SECONDS) {
            attempt.count = 0;
            attempt.timestamp = now;
        }

        attempt.count++;

        return attempt.count <= MAX_ATTEMPTS;
    }

    private static class Attempt {
        int count = 0;
        long timestamp = Instant.now().getEpochSecond();
    }
}