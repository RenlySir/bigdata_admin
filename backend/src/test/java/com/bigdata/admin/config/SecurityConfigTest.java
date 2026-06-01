package com.bigdata.admin.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SecurityConfig
 */
class SecurityConfigTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = mock(JwtTokenProvider.class);
        jwtProperties = mock(JwtProperties.class);
        jwtAuthenticationFilter = mock(JwtAuthenticationFilter.class);

        securityConfig = new SecurityConfig(jwtTokenProvider, jwtProperties, jwtAuthenticationFilter);
    }

    @Test
    void securityConfig_WhenCreated_ShouldNotBeNull() {
        assertNotNull(securityConfig);
    }

    @Test
    void passwordEncoder_WhenCreated_ShouldReturnBCryptPasswordEncoder() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder.matches("test", passwordEncoder.encode("test")));
    }

    @Test
    void passwordEncoder_ShouldHashPasswords() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void passwordEncoder_ShouldNotMatchDifferentPasswords() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String password1 = "password1";
        String password2 = "password2";

        String encodedPassword1 = passwordEncoder.encode(password1);

        assertFalse(passwordEncoder.matches(password2, encodedPassword1));
    }

    private void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("Expected non-null value");
        }
    }

    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected true but was false");
        }
    }

    private void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("Expected false but was true");
        }
    }

    private void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected '" + expected + "' but was '" + actual + "'");
        }
    }

    private void assertNotEquals(String expected, String actual) {
        if (expected.equals(actual)) {
            throw new AssertionError("Expected different values but both were '" + expected + "'");
        }
    }
}
