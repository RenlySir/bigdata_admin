package com.bigdata.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * TiDB Configuration Properties
 * Maps configuration from application.yml for TiDB connections
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tidb")
public class TiDBConfig {

    /**
     * TiDB host address
     */
    private String host = "localhost";

    /**
     * TiDB port (default: 4000 for TiDB)
     */
    private int port = 4000;

    /**
     * Database name
     */
    private String database = "bigdata_admin";

    /**
     * Username for authentication
     */
    private String username = "root";

    /**
     * Password for authentication
     */
    private String password = "";

    /**
     * Enable SSL connection
     */
    private boolean ssl = false;

    /**
     * Server timezone
     */
    private String timezone = "Asia/Shanghai";

    /**
     * Connection timeout in seconds
     */
    private int connectTimeout = 10;

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
