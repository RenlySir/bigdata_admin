package com.bigdata.admin.service;

import com.bigdata.admin.config.CacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Cache Management Service
 */
@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private final CacheManager cacheManager;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Evict a specific cache entry
     * @param cacheName Cache name
     * @param key Cache key
     */
    public void evict(String cacheName, Object key) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.debug("Evicted cache entry: {} -> {}", cacheName, key);
        }
    }

    /**
     * Evict all entries in a cache
     * @param cacheName Cache name
     */
    public void evictAll(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Evicted all entries in cache: {}", cacheName);
        }
    }

    /**
     * Evict collection related caches
     * @param collectionId Collection ID
     */
    public void evictCollectionCaches(Long collectionId) {
        evict(CacheConfig.CACHE_COLLECTIONS, collectionId);
        evict(CacheConfig.CACHE_COLLECTION_STATS, collectionId);
        log.info("Evicted all caches for collection: {}", collectionId);
    }

    /**
     * Evict data source related caches
     * @param dataSourceId Data source ID
     */
    public void evictDataSourceCaches(Long dataSourceId) {
        evict(CacheConfig.CACHE_DATASOURCES, dataSourceId);
        evict(CacheConfig.CACHE_DATASOURCE_TEST, dataSourceId);
        log.info("Evicted all caches for data source: {}", dataSourceId);
    }

    /**
     * Get cache size estimate
     * @param cacheName Cache name
     * @return Estimated size (may not be accurate for all cache implementations)
     */
    public long getCacheSize(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            var nativeCache = cache.getNativeCache();
            // This is implementation-specific and may not work for all cache providers
            return nativeCache.toString().length(); // Rough estimate
        }
        return 0;
    }
}
