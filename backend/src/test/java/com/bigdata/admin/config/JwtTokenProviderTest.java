package com.bigdata.admin.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    @Test
    void generateToken_WhenRefreshing_ShouldPreserveOriginalAbsoluteExpiration() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-for-review-minimum-32-chars");
        properties.setExpiration(60_000L);
        properties.setAbsoluteExpiration(604_800_000L);
        properties.setIssuer("test-issuer");
        JwtTokenProvider provider = new JwtTokenProvider(properties);
        long originalAbsoluteExpiry = System.currentTimeMillis() + 300_000L;

        String refreshedToken = provider.generateToken(1L, "alice", 2L, originalAbsoluteExpiry);

        assertEquals(originalAbsoluteExpiry, provider.getAbsoluteExpiration(refreshedToken));
        assertTrue(provider.canRefreshToken(refreshedToken));
    }
}
