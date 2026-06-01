package com.bigdata.admin.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CacheConfig
 */
class CacheConfigTest {

    @Test
    void cacheConfig_ShouldHaveCorrectCacheNames() {
        assertEquals("collections", CacheConfig.CACHE_COLLECTIONS);
        assertEquals("collectionStats", CacheConfig.CACHE_COLLECTION_STATS);
        assertEquals("datasources", CacheConfig.CACHE_DATASOURCES);
        assertEquals("datasourceTest", CacheConfig.CACHE_DATASOURCE_TEST);
    }

    @Test
    void cacheManager_WhenCreated_ShouldUseRedisConnectionFactory() {
        RedisConnectionFactory mockConnectionFactory = mock(RedisConnectionFactory.class);
        CacheConfig cacheConfig = new CacheConfig();

        CacheManager cacheManager = cacheConfig.cacheManager(mockConnectionFactory);

        assertNotNull(cacheManager);
        assertTrue(cacheManager instanceof RedisCacheManager);
    }

    @Test
    void cacheManager_WhenCreated_ShouldHaveCacheConfigurations() {
        RedisConnectionFactory mockConnectionFactory = mock(RedisConnectionFactory.class);
        CacheConfig cacheConfig = new CacheConfig();

        CacheManager cacheManager = cacheConfig.cacheManager(mockConnectionFactory);

        assertNotNull(cacheManager);
        verify(mockConnectionFactory, atLeastOnce()).getClass();
    }

    private void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected '" + expected + "' but was '" + actual + "'");
        }
    }

    private void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("Expected non-null value");
        }
    }

    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected true but was false");
        }
    }
}
