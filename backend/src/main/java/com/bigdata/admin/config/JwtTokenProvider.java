package com.bigdata.admin.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token Provider for token generation and validation
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // Validate configuration on startup
        jwtProperties.validate();
        // Create signing key from secret
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        log.info("JWT Token Provider initialized with issuer: {}", jwtProperties.getIssuer());
    }

    /**
     * Generate JWT token for user
     * @param userId User ID
     * @param username Username
     * @return JWT token string
     */
    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, null);
    }

    /**
     * Generate JWT token for user with version tracking
     * @param userId User ID
     * @param username Username
     * @param tokenVersion Token version for rotation (null for new tokens)
     * @return JWT token string
     */
    public String generateToken(Long userId, String username, Long tokenVersion) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());
        Date absoluteExpiryDate = new Date(now.getTime() + jwtProperties.getAbsoluteExpiration());

        // Generate new token version if not provided
        long version = (tokenVersion != null) ? tokenVersion : System.currentTimeMillis();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("version", version)
                .claim("absoluteExpiry", absoluteExpiryDate.getTime())
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(jwtProperties.getIssuer())
                .signWith(key)
                .compact();
    }

    /**
     * Get user ID from token
     * @param token JWT token
     * @return User ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Get username from token
     * @param token JWT token
     * @return Username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("username", String.class);
    }

    /**
     * Validate token
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    /**
     * Get token expiration time
     * @return Expiration time in milliseconds
     */
    public Long getExpirationTime() {
        return jwtProperties.getExpiration();
    }

    /**
     * Get token version from JWT token
     * @param token JWT token
     * @return Token version or 0 if not present
     */
    public Long getTokenVersion(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("version", Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get absolute expiration time from JWT token
     * @param token JWT token
     * @return Absolute expiration timestamp or 0 if not present
     */
    public Long getAbsoluteExpiration(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("absoluteExpiry", Long.class);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Check if token can be refreshed
     * A token can be refreshed if it hasn't passed the absolute expiration time
     * @param token JWT token
     * @return true if refresh is allowed, false otherwise
     */
    public boolean canRefreshToken(String token) {
        try {
            Long absoluteExpiry = getAbsoluteExpiration(token);
            if (absoluteExpiry == null || absoluteExpiry == 0) {
                // Legacy token without absolute expiration - deny refresh
                return false;
            }
            // Check if we've passed the absolute expiration time
            return System.currentTimeMillis() < absoluteExpiry;
        } catch (Exception e) {
            log.error("Error checking token refresh eligibility", e);
            return false;
        }
    }
}
