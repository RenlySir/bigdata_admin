package com.bigdata.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Rate Limiter using Token Bucket algorithm
 */
@Slf4j
@Component
public class RateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RateLimitProperties properties;

    public RateLimiter(RedisTemplate<String, Object> redisTemplate,
                       RateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    /**
     * Check if request is allowed for given key
     * @param key Unique identifier (IP, user ID, etc.)
     * @return true if allowed, false otherwise
     */
    public boolean isAllowed(String key) {
        return isAllowed(key, properties.getCapacity());
    }

    /**
     * Check if request is allowed with custom capacity
     * @param key Unique identifier
     * @param capacity Maximum requests
     * @return true if allowed, false otherwise
     */
    public boolean isAllowed(String key, int capacity) {
        if (!properties.isEnabled()) {
            return true;
        }

        String redisKey = "ratelimit:" + key;

        try {
            // Get current count
            Long current = redisTemplate.opsForValue().increment(redisKey);

            if (current == null) {
                current = 1L;
            }

            // Set expiration on first request
            if (current == 1) {
                redisTemplate.expire(redisKey, properties.getTimeWindowSeconds(), TimeUnit.SECONDS);
            }

            // Check if within limit
            if (current > capacity) {
                log.warn("Rate limit exceeded for key: {}, count: {}, limit: {}",
                        key, current, capacity);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            // Fail open - allow request if Redis is unavailable
            return true;
        }
    }

    /**
     * Check batch operation rate limit
     * @param key Unique identifier
     * @return true if allowed, false otherwise
     */
    public boolean isBatchAllowed(String key) {
        return isAllowed("batch:" + key, properties.getBatchCapacity());
    }

    /**
     * Check sensitive operation rate limit
     * @param key Unique identifier
     * @return true if allowed, false otherwise
     */
    public boolean isSensitiveAllowed(String key) {
        return isAllowed("sensitive:" + key, properties.getSensitiveCapacity());
    }

    /**
     * Reset rate limit for a key
     * @param key Unique identifier
     */
    public void reset(String key) {
        try {
            redisTemplate.delete("ratelimit:" + key);
        } catch (Exception e) {
            log.error("Error resetting rate limit for key: {}", key, e);
        }
    }

    /**
     * Get remaining requests for a key
     * @param key Unique identifier
     * @return Remaining requests
     */
    public long getRemaining(String key) {
        try {
            Long current = (Long) redisTemplate.opsForValue().get("ratelimit:" + key);
            if (current == null) {
                return properties.getCapacity();
            }
            return Math.max(0, properties.getCapacity() - current);
        } catch (Exception e) {
            log.error("Error getting remaining rate limit for key: {}", key, e);
            return 0;
        }
    }
}
