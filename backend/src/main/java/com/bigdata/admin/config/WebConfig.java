package com.bigdata.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600)
                .exposedHeaders(HttpHeaders.AUTHORIZATION);
    }

    /**
     * Security Headers Configuration
     * These headers provide defense-in-depth security measures
     *
     * Note: For production, consider using a reverse proxy (nginx, apache)
     * or Spring Security's HeadersConfigurer for more comprehensive header management
     *
     * Recommended headers:
     * - X-Content-Type-Options: nosniff - Prevent MIME type sniffing
     * - X-Frame-Options: DENY - Prevent clickjacking
     * - X-XSS-Protection: 1; mode=block - Enable XSS filtering
     * - Strict-Transport-Security: max-age=31536000 - Enforce HTTPS
     * - Content-Security-Policy: Restrict resource sources
     * - Referrer-Policy: no-referrer - Control referrer information
     * - Permissions-Policy: Restrict browser features
     */
    public static class SecurityHeaders {
        public static final String X_CONTENT_TYPE_OPTIONS = "nosniff";
        public static final String X_FRAME_OPTIONS = "DENY";
        public static final String X_XSS_PROTECTION = "1; mode=block";
        public static final String STRICT_TRANSPORT_SECURITY = "max-age=31536000; includeSubDomains";
        public static final String CONTENT_SECURITY_POLICY =
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "font-src 'self'; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none'; " +
            "base-uri 'self'; " +
            "form-action 'self';";
        public static final String REFERRER_POLICY = "no-referrer";
        public static final String PERMISSIONS_POLICY =
            "geolocation=(), " +
            "microphone=(), " +
            "camera=(), " +
            "payment=(), " +
            "usb=(), " +
            "magnetometer=(), " +
            "gyroscope=(), " +
            "accelerometer=()";
    }
}
