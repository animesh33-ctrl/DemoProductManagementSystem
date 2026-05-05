package com.security.tokenservice;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    // Add token to blacklist with TTL = remaining expiry
    public void blacklist(String token, long remainingExpiryMs) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "revoked",
                remainingExpiryMs,
                TimeUnit.MILLISECONDS
        );
    }

    // Check if token is blacklisted
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(BLACKLIST_PREFIX + token)
        );
    }
}