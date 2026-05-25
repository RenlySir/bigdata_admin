package com.bigdata.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT Configuration Properties
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * JWT secret key for signing tokens
     * IMPORTANT: Use a strong, randomly generated key in production
     * Minimum 256 bits (32 bytes) for HS256 algorithm
     */
    private String secret;

    /**
     * Token expiration time in milliseconds
     * Default: 24 hours (86400000 ms)
     */
    private Long expiration = 86400000L;

    /**
     * Absolute token expiration time in milliseconds
     * Tokens cannot be refreshed beyond this time
     * Default: 7 days (604800000 ms)
     */
    private Long absoluteExpiration = 604800000L;

    /**
     * Token issuer claim
     */
    private String issuer = "bigdata-admin";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public Long getAbsoluteExpiration() {
        return absoluteExpiration;
    }

    public void setAbsoluteExpiration(Long absoluteExpiration) {
        this.absoluteExpiration = absoluteExpiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * Validate the JWT secret configuration
     * @throws IllegalStateException if the secret is invalid
     */
    public void validate() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException(
                "JWT secret is not configured. Please set 'app.jwt.secret' in application.yml or environment variable 'JWT_SECRET'. " +
                "Generate a secure key using: openssl rand -base64 32"
            );
        }

        // Check minimum key length for HS256 (256 bits = 32 bytes)
        // Base64 encoded, so minimum 44 characters
        if (secret.length() < 32) {
            throw new IllegalStateException(
                "JWT secret is too weak. Minimum 32 characters required for HS256 algorithm. " +
                "Current length: " + secret.length() + " characters. " +
                "Generate a secure key using: openssl rand -base64 32"
            );
        }

        // Warn if using default/weak keys
        if (secret.contains("change") || secret.contains("example") || secret.contains("default")) {
            throw new IllegalStateException(
                "JWT secret appears to be a default/placeholder value. " +
                "Please generate a secure key using: openssl rand -base64 32"
            );
        }
    }
}
