package com.bigdata.admin.config;

import com.bigdata.admin.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rate Limiting Aspect
 */
@Aspect
@Component
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RateLimiter rateLimiter;

    public RateLimitAspect(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    /**
     * Rate limit annotation for controller methods
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RateLimit {
        int capacity() default 100;
        String key() default "";
    }

    /**
     * Rate limit for batch operations
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BatchRateLimit {
        int capacity() default 10;
    }

    /**
     * Rate limit for sensitive operations
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SensitiveRateLimit {
        int capacity() default 5;
    }

    @Around("@annotation(rateLimit)")
    public Object aroundRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = getClientKey(rateLimit.key());

        if (!rateLimiter.isAllowed(key, rateLimit.capacity())) {
            log.warn("Rate limit exceeded for key: {}", key);
            return Result.error(429, "Too many requests. Please try again later.");
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(batchRateLimit)")
    public Object aroundBatchRateLimit(ProceedingJoinPoint joinPoint, BatchRateLimit batchRateLimit) throws Throwable {
        String key = getClientKey("");

        if (!rateLimiter.isBatchAllowed(key)) {
            log.warn("Batch rate limit exceeded for key: {}", key);
            return Result.error(429, "Too many batch operations. Please wait before trying again.");
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(sensitiveRateLimit)")
    public Object aroundSensitiveRateLimit(ProceedingJoinPoint joinPoint, SensitiveRateLimit sensitiveRateLimit) throws Throwable {
        String key = getClientKey("");

        if (!rateLimiter.isSensitiveAllowed(key)) {
            log.warn("Sensitive operation rate limit exceeded for key: {}", key);
            return Result.error(429, "Too many sensitive operations. Please wait before trying again.");
        }

        return joinPoint.proceed();
    }

    /**
     * Get client key for rate limiting
     * Uses IP address or user ID if authenticated
     */
    private String getClientKey(String customKey) {
        if (!customKey.isEmpty()) {
            return customKey;
        }

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // Try to get user ID from security context
            Object userId = request.getAttribute("userId");
            if (userId != null) {
                return "user:" + userId;
            }

            // Fall back to IP address
            String ip = getClientIp(request);
            return "ip:" + ip;
        }

        return "global";
    }

    /**
     * Get client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Handle multiple IPs in X-Forwarded-For
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
