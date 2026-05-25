package com.bigdata.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Rate Limiting Configuration Properties
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ratelimit")
public class RateLimitProperties {

    /**
     * Enable or disable rate limiting
     */
    private boolean enabled = true;

    /**
     * Maximum requests per time window
     */
    private int capacity = 100;

    /**
     * Time window in seconds
     */
    private int timeWindowSeconds = 60;

    /**
     * Rate limit for batch operations
     */
    private int batchCapacity = 10;

    /**
     * Rate limit for sensitive operations (delete, import)
     */
    private int sensitiveCapacity = 5;

    /**
     * Whether to use Redis for distributed rate limiting
     */
    private boolean useRedis = true;
}
