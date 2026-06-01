package com.bigdata.admin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CacheService
 */
@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache mockCache;

    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new CacheService(cacheManager);
    }

    @Test
    void evict_WhenCacheExists_ShouldEvictEntry() {
        when(cacheManager.getCache("testCache")).thenReturn(mockCache);

        cacheService.evict("testCache", "testKey");

        verify(mockCache).evict("testKey");
    }

    @Test
    void evict_WhenCacheNotExists_ShouldDoNothing() {
        when(cacheManager.getCache("nonExistentCache")).thenReturn(null);

        cacheService.evict("nonExistentCache", "testKey");

        verify(mockCache, never()).evict(any());
    }

    @Test
    void evictAll_WhenCacheExists_ShouldClearCache() {
        when(cacheManager.getCache("testCache")).thenReturn(mockCache);

        cacheService.evictAll("testCache");

        verify(mockCache).clear();
    }

    @Test
    void evictAll_WhenCacheNotExists_ShouldDoNothing() {
        when(cacheManager.getCache("nonExistentCache")).thenReturn(null);

        cacheService.evictAll("nonExistentCache");

        verify(mockCache, never()).clear();
    }

    @Test
    void evictCollectionCaches_ShouldEvictMultipleCaches() {
        when(cacheManager.getCache("collections")).thenReturn(mockCache);
        when(cacheManager.getCache("collectionStats")).thenReturn(mockCache);

        cacheService.evictCollectionCaches(1L);

        verify(mockCache, atLeastOnce()).evict(1L);
    }

    @Test
    void evictDataSourceCaches_ShouldEvictMultipleCaches() {
        when(cacheManager.getCache("datasources")).thenReturn(mockCache);
        when(cacheManager.getCache("datasourceTest")).thenReturn(mockCache);

        cacheService.evictDataSourceCaches(1L);

        verify(mockCache, atLeastOnce()).evict(1L);
    }

    @Test
    void getCacheSize_WhenCacheExists_ShouldReturnSize() {
        when(cacheManager.getCache("testCache")).thenReturn(mockCache);
        when(mockCache.getNativeCache()).thenReturn("someCacheImplementation");

        long size = cacheService.getCacheSize("testCache");

        assertTrue(size >= 0);
    }

    @Test
    void getCacheSize_WhenCacheNotExists_ShouldReturnZero() {
        when(cacheManager.getCache("nonExistentCache")).thenReturn(null);

        long size = cacheService.getCacheSize("nonExistentCache");

        assertEquals(0, size);
    }

    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected true but was false");
        }
    }

    private void assertEquals(long expected, long actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }
}
