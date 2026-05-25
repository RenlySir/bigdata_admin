package com.bigdata.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * TiDB Configuration Properties
 * Maps configuration from application.yml for TiDB connections
 */
@Component
public class TiDBConfig {

    /**
     * TiDB host address
     */
    @Value("${tidb.host:localhost}")
    private String host;

    /**
     * TiDB port (default: 4000 for TiDB)
     */
    @Value("${tidb.port:4000}")
    private int port;

    /**
     * Database name
     */
    @Value("${tidb.database:bigdata_admin}")
    private String database;

    /**
     * Username for authentication
     */
    @Value("${tidb.username:root}")
    private String username;

    /**
     * Password for authentication
     */
    @Value("${tidb.password:}")
    private String password;

    /**
     * Enable SSL connection
     */
    @Value("${tidb.ssl:false}")
    private boolean ssl;

    /**
     * Server timezone
     */
    @Value("${tidb.timezone:Asia/Shanghai}")
    private String timezone;

    /**
     * Connection timeout in seconds
     */
    @Value("${tidb.connectTimeout:10}")
    private int connectTimeout;

    // Getters and Setters
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Build JDBC URL for TiDB connection
     */
    public String getJdbcUrl() {
        return String.format(
            "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=%s&serverTimezone=%s&allowPublicKeyRetrieval=true",
            host, port, database, ssl, timezone
        );
    }

    /**
     * Build JDBC URL for specific database
     */
    public String getJdbcUrl(String dbName) {
        return String.format(
            "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=%s&serverTimezone=%s&allowPublicKeyRetrieval=true",
            host, port, dbName, ssl, timezone
        );
    }

    /**
     * Validate TiDB configuration
     */
    public boolean isValid() {
        return host != null && !host.trim().isEmpty()
            && port >= 1 && port <= 65535
            && username != null && !username.trim().isEmpty();
    }
}
