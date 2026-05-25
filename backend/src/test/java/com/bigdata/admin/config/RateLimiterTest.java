package com.bigdata.admin.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimiterTest {

    @Test
    void isAllowed_WhenRedisFails_ShouldEnforceLocalFallbackLimit() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenThrow(new RuntimeException("redis down"));

        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setTimeWindowSeconds(60);

        RateLimiter rateLimiter = new RateLimiter(redisTemplate, properties);

        assertTrue(rateLimiter.isAllowed("login:127.0.0.1", 2));
        assertTrue(rateLimiter.isAllowed("login:127.0.0.1", 2));
        assertFalse(rateLimiter.isAllowed("login:127.0.0.1", 2));
    }

    @Test
    void isAllowed_WhenRedisDisabled_ShouldUseLocalLimitAndRemainingCount() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setUseRedis(false);
        properties.setCapacity(3);
        properties.setTimeWindowSeconds(60);

        RateLimiter rateLimiter = new RateLimiter(redisTemplate, properties);

        assertEquals(3, rateLimiter.getRemaining("api:127.0.0.1"));
        assertTrue(rateLimiter.isAllowed("api:127.0.0.1"));
        assertEquals(2, rateLimiter.getRemaining("api:127.0.0.1"));
    }
}
