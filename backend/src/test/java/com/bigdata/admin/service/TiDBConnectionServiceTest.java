package com.bigdata.admin.service;

import com.bigdata.admin.config.TiDBConfig;
import com.bigdata.admin.dto.TiDBConnectionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TiDBConnectionService
 */
@ExtendWith(MockitoExtension.class)
class TiDBConnectionServiceTest {

    @Mock
    private TiDBConfig defaultConfig;

    @InjectMocks
    private TiDBConnectionService tiDBConnectionService;

    @BeforeEach
    void setUp() {
        // Configure default TiDB config
        ReflectionTestUtils.setField(defaultConfig, "host", "localhost");
        ReflectionTestUtils.setField(defaultConfig, "port", 4000);
        ReflectionTestUtils.setField(defaultConfig, "database", "test_db");
        ReflectionTestUtils.setField(defaultConfig, "username", "root");
        ReflectionTestUtils.setField(defaultConfig, "password", "");
        ReflectionTestUtils.setField(defaultConfig, "ssl", false);
        ReflectionTestUtils.setField(defaultConfig, "timezone", "Asia/Shanghai");
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
}
