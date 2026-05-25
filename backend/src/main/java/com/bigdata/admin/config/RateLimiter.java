package com.bigdata.admin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiter using Token Bucket algorithm
 */
@Component
public class RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiter.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final RateLimitProperties properties;
    private final Clock clock;
    private final ConcurrentHashMap<String, LocalBucket> localBuckets = new ConcurrentHashMap<>();

    public RateLimiter(RedisTemplate<String, Object> redisTemplate,
                       RateLimitProperties properties) {
        this(redisTemplate, properties, Clock.systemUTC());
    }

    RateLimiter(RedisTemplate<String, Object> redisTemplate,
                RateLimitProperties properties,
                Clock clock) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.clock = clock;
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

        if (!properties.isUseRedis()) {
            return isAllowedLocally(key, capacity);
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
            log.warn("Redis rate limiting failed for key: {}; using local fallback: {}", key, e.getMessage());
            return isAllowedLocally(key, capacity);
        }
    }

    private boolean isAllowedLocally(String key, int capacity) {
        long now = clock.millis();
        long windowMillis = TimeUnit.SECONDS.toMillis(properties.getTimeWindowSeconds());
        LocalBucket bucket = localBuckets.compute(key, (ignored, existing) -> {
            if (existing == null || now >= existing.windowExpiresAtMillis) {
                return new LocalBucket(now + windowMillis);
            }
            return existing;
        });

        int current = bucket.count.incrementAndGet();
        if (current > capacity) {
            log.warn("Local fallback rate limit exceeded for key: {}, count: {}, limit: {}",
                    key, current, capacity);
            return false;
        }
        return true;
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
        localBuckets.remove(key);
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
        if (!properties.isUseRedis()) {
            return getLocalRemaining(key, properties.getCapacity());
        }
        try {
            Long current = (Long) redisTemplate.opsForValue().get("ratelimit:" + key);
            if (current == null) {
                return properties.getCapacity();
            }
            return Math.max(0, properties.getCapacity() - current);
        } catch (Exception e) {
            log.warn("Redis remaining lookup failed for key: {}; using local fallback: {}", key, e.getMessage());
            return getLocalRemaining(key, properties.getCapacity());
        }
    }

    private long getLocalRemaining(String key, int capacity) {
        LocalBucket bucket = localBuckets.get(key);
        if (bucket == null || clock.millis() >= bucket.windowExpiresAtMillis) {
            return capacity;
        }
        return Math.max(0, capacity - bucket.count.get());
    }

    private static final class LocalBucket {
        private final long windowExpiresAtMillis;
        private final AtomicInteger count = new AtomicInteger(0);

        private LocalBucket(long windowExpiresAtMillis) {
            this.windowExpiresAtMillis = windowExpiresAtMillis;
        }
    }
}
