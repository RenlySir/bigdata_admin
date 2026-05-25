package com.bigdata.admin.dto;

/**
 * PostgreSQL Connection Information
 */
public class PostgreSQLConnectionInfo {

    /**
     * Host address
     */
    private String host;

    /**
     * Port number (default: 5432 for PostgreSQL)
     */
    private int port = 5432;

    /**
     * Database name
     */
    private String database;

    /**
     * Username
     */
    private String username;

    /**
     * Password
     */
    private String password;

    /**
     * Schema name (default: public)
     */
    private String schema = "public";

    /**
     * SSL mode
     */
    private boolean ssl = false;

    /**
     * Connection timeout in seconds
     */
    private int connectTimeout = 10;

    /**
     * Get JDBC URL for PostgreSQL
     */
    public String getJdbcUrl() {
        return String.format(
            "jdbc:postgresql://%s:%d/%s?currentSchema=%s&ssl=%s",
            host, port, database, schema, ssl
        );
    }

    /**
     * Validate connection info
     */
    public boolean isValid() {
        return host != null && !host.trim().isEmpty()
            && port >= 1 && port <= 65535
            && database != null && !database.trim().isEmpty()
            && username != null && !username.trim().isEmpty();
    }

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

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}
