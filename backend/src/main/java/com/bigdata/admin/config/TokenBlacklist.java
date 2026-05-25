package com.bigdata.admin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token Blacklist Service
 * Manages revoked tokens using Redis for distributed support
 */
@Service
public class TokenBlacklist {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklist.class);

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final long DEFAULT_TTL_DAYS = 7;

    public TokenBlacklist(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Add a token to the blacklist
     * @param token JWT token to blacklist
     */
    public void blacklist(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        String key = BLACKLIST_PREFIX + token;
        // Store for 7 days by default
        redisTemplate.opsForValue().set(key, "revoked", DEFAULT_TTL_DAYS, TimeUnit.DAYS);
        log.debug("Token added to blacklist: {}", token.substring(0, Math.min(20, token.length())) + "...");
    }

    /**
     * Add a token to the blacklist with custom TTL
     * @param token JWT token to blacklist
     * @param ttlSeconds Time to live in seconds
     */
    public void blacklist(String token, long ttlSeconds) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "revoked", ttlSeconds, TimeUnit.SECONDS);
        log.debug("Token added to blacklist with TTL: {}",
            token.substring(0, Math.min(20, token.length())) + "...");
    }

    /**
     * Check if a token is blacklisted
     * @param token JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Remove a token from the blacklist (e.g., for testing)
     * @param token JWT token to remove
     */
    public void remove(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        String key = BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
        log.debug("Token removed from blacklist: {}",
            token.substring(0, Math.min(20, token.length())) + "...");
    }
}
