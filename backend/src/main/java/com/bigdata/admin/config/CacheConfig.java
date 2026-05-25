package com.bigdata.admin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache Configuration
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    /**
     * Cache names for different data types
     */
    public static final String CACHE_COLLECTIONS = "collections";
    public static final String CACHE_COLLECTION_STATS = "collectionStats";
    public static final String CACHE_DATASOURCES = "datasources";
    public static final String CACHE_DATASOURCE_TEST = "datasourceTest";

    /**
     * Cache TTL values
     */
    private static final Duration TTL_SHORT = Duration.ofMinutes(5);
    private static final Duration TTL_MEDIUM = Duration.ofMinutes(15);
    private static final Duration TTL_LONG = Duration.ofHours(1);

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("Initializing Redis cache manager...");

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(TTL_MEDIUM)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer())
                )
                .disableCachingNullValues();

        // Cache-specific configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Collections cache - longer TTL as they don't change frequently
        cacheConfigurations.put(CACHE_COLLECTIONS,
                defaultConfig.entryTtl(TTL_LONG));

        // Collection statistics cache - short TTL as they change frequently
        cacheConfigurations.put(CACHE_COLLECTION_STATS,
                defaultConfig.entryTtl(TTL_SHORT));

        // Data sources cache - longer TTL
        cacheConfigurations.put(CACHE_DATASOURCES,
                defaultConfig.entryTtl(TTL_LONG));

        // Data source test results cache - short TTL
        cacheConfigurations.put(CACHE_DATASOURCE_TEST,
                defaultConfig.entryTtl(TTL_SHORT));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
