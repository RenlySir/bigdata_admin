package com.bigdata.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Rate Limiting Configuration Properties
 */
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getTimeWindowSeconds() {
        return timeWindowSeconds;
    }

    public void setTimeWindowSeconds(int timeWindowSeconds) {
        this.timeWindowSeconds = timeWindowSeconds;
    }

    public int getBatchCapacity() {
        return batchCapacity;
    }

    public void setBatchCapacity(int batchCapacity) {
        this.batchCapacity = batchCapacity;
    }

    public int getSensitiveCapacity() {
        return sensitiveCapacity;
    }

    public void setSensitiveCapacity(int sensitiveCapacity) {
        this.sensitiveCapacity = sensitiveCapacity;
    }

    public boolean isUseRedis() {
        return useRedis;
    }

    public void setUseRedis(boolean useRedis) {
        this.useRedis = useRedis;
    }
}
