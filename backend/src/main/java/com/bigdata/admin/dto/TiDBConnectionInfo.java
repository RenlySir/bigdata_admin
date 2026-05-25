package com.bigdata.admin.dto;

/**
 * TiDB Connection Information
 * Used for testing and storing TiDB connection details
 */
public class TiDBConnectionInfo {

    /**
     * Host address
     */
    private String host;

    /**
     * Port number (default: 4000 for TiDB)
     */
    private int port = 4000;

    /**
     * Username
     */
    private String username;

    /**
     * Password
     */
    private String password;

    /**
     * Database name
     */
    private String database;

    /**
     * Enable SSL
     */
    private boolean ssl = false;

    /**
     * Server timezone
     */
    private String timezone = "Asia/Shanghai";

    /**
     * Connection status
     */
    private boolean connected;

    /**
     * TiDB version
     */
    private String version;

    /**
     * Status message
     */
    private String message;

    // Constructors
    public TiDBConnectionInfo() {}

    private TiDBConnectionInfo(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.username = builder.username;
        this.password = builder.password;
        this.database = builder.database;
        this.ssl = builder.ssl;
        this.timezone = builder.timezone;
        this.connected = builder.connected;
        this.version = builder.version;
        this.message = builder.message;
    }

    public static Builder builder() {
        return new Builder();
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

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
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

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Validate connection info
     */
    public boolean isValid() {
        return host != null && !host.trim().isEmpty()
            && port >= 1 && port <= 65535
            && username != null && !username.trim().isEmpty();
    }

    /**
     * Get JDBC URL
     */
    public String getJdbcUrl() {
        return String.format(
            "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=%s&serverTimezone=%s&allowPublicKeyRetrieval=true",
            host, port, database != null ? database : "mysql", ssl, timezone
        );
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.host = this.host;
        builder.port = this.port;
        builder.username = this.username;
        builder.password = this.password;
        builder.database = this.database;
        builder.ssl = this.ssl;
        builder.timezone = this.timezone;
        builder.connected = this.connected;
        builder.version = this.version;
        builder.message = this.message;
        return builder;
    }

    public static class Builder {
        private String host;
        private int port = 4000;
        private String username;
        private String password;
        private String database;
        private boolean ssl = false;
        private String timezone = "Asia/Shanghai";
        private boolean connected;
        private String version;
        private String message;

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Builder ssl(boolean ssl) {
            this.ssl = ssl;
            return this;
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Builder connected(boolean connected) {
            this.connected = connected;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public TiDBConnectionInfo build() {
            return new TiDBConnectionInfo(this);
        }
    }
}
