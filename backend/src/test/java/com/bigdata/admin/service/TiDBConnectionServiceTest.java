package com.bigdata.admin.service;

import com.bigdata.admin.config.TiDBConfig;
import com.bigdata.admin.dto.TiDBConnectionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TiDBConnectionService
 */
class TiDBConnectionServiceTest {

    private TiDBConfig defaultConfig;
    private TiDBConnectionService tiDBConnectionService;

    @BeforeEach
    void setUp() {
        defaultConfig = new TiDBConfig();
        defaultConfig.setHost("localhost");
        defaultConfig.setPort(4000);
        defaultConfig.setDatabase("test_db");
        defaultConfig.setUsername("root");
        defaultConfig.setPassword("");
        defaultConfig.setSsl(false);
        defaultConfig.setTimezone("Asia/Shanghai");
        tiDBConnectionService = new TiDBConnectionService(defaultConfig);
    }

    @Test
    void testConnection_WhenValidCredentials_ShouldReturnSuccess() {
        // This test requires a mock or test database
        // For now, we'll test the validation logic

        TiDBConnectionInfo connectionInfo = TiDBConnectionInfo.builder()
            .host("localhost")
            .port(4000)
            .username("root")
            .database("test_db")
            .ssl(false)
            .timezone("Asia/Shanghai")
            .build();

        assertTrue(connectionInfo.isValid());
        assertEquals("localhost", connectionInfo.getHost());
        assertEquals(4000, connectionInfo.getPort());
    }

    @Test
    void testConnection_WhenInvalidHost_ShouldFailValidation() {
        TiDBConnectionInfo connectionInfo = TiDBConnectionInfo.builder()
            .host("")  // Empty host
            .port(4000)
            .username("root")
            .build();

        assertFalse(connectionInfo.isValid());
    }

    @Test
    void testConnection_WhenInvalidPort_ShouldFailValidation() {
        TiDBConnectionInfo connectionInfo = TiDBConnectionInfo.builder()
            .host("localhost")
            .port(70000)  // Invalid port
            .username("root")
            .build();

        assertFalse(connectionInfo.isValid());
    }

    @Test
    void testGetJdbcUrl_WhenValidParams_ShouldReturnCorrectUrl() {
        TiDBConnectionInfo connectionInfo = TiDBConnectionInfo.builder()
            .host("127.0.0.1")
            .port(4000)
            .database("mydb")
            .ssl(true)
            .timezone("UTC")
            .build();

        String url = connectionInfo.getJdbcUrl();

        assertTrue(url.contains("jdbc:mysql://"));
        assertTrue(url.contains("127.0.0.1"));
        assertTrue(url.contains(":4000"));
        assertTrue(url.contains("mydb"));
        assertTrue(url.contains("useSSL=true"));
        assertTrue(url.contains("serverTimezone=UTC"));
    }

    @Test
    void testTiDBConfig_WhenDefaultValues_ShouldBeValid() {
        assertTrue(defaultConfig.isValid());
        assertEquals("localhost", defaultConfig.getHost());
        assertEquals(4000, defaultConfig.getPort());
        assertEquals("root", defaultConfig.getUsername());
    }

    @Test
    void testTiDBConfig_GetJdbcUrl_ShouldReturnValidUrl() {
        String url = defaultConfig.getJdbcUrl();

        assertNotNull(url);
        assertTrue(url.startsWith("jdbc:mysql://"));
        assertTrue(url.contains("localhost"));
        assertTrue(url.contains(":4000"));
        assertTrue(url.contains("test_db"));
    }

    @Test
    void testTiDBConfig_GetJdbcUrl_WithCustomDatabase() {
        String url = defaultConfig.getJdbcUrl("custom_db");

        assertNotNull(url);
        assertTrue(url.contains("custom_db"));
    }
    @Test
    void validateDatabaseName_WhenUnsafeIdentifier_ShouldRejectSqlInjection() {
        assertDoesNotThrow(() -> tiDBConnectionService.validateDatabaseName("analytics_2026"));
        assertThrows(IllegalArgumentException.class,
                () -> tiDBConnectionService.validateDatabaseName("analytics`; DROP TABLE sys_user; --"));
    }

    @Test
    void validateReadOnlyQuery_WhenMutationOrMultiStatement_ShouldRejectSqlInjection() {
        assertDoesNotThrow(() -> tiDBConnectionService.validateReadOnlyQuery("SELECT id, name FROM orders LIMIT 10"));
        assertDoesNotThrow(() -> tiDBConnectionService.validateReadOnlyQuery("SHOW TABLES"));
        assertThrows(IllegalArgumentException.class,
                () -> tiDBConnectionService.validateReadOnlyQuery("SELECT * FROM orders; DROP TABLE orders"));
        assertThrows(IllegalArgumentException.class,
                () -> tiDBConnectionService.validateReadOnlyQuery("UPDATE orders SET amount = 0"));
    }

}
