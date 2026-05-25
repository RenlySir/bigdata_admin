package com.bigdata.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TiDB Connection Information
 * Used for testing and storing TiDB connection details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TiDBConnectionInfo {

    /**
     * Host address
     */
    private String host;

    /**
     * Port number (default: 4000 for TiDB)
     */
    @Builder.Default
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
    @Builder.Default
    private boolean ssl = false;

    /**
     * Server timezone
     */
    @Builder.Default
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
}
