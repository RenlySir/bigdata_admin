package com.bigdata.admin.service;

import com.bigdata.admin.config.TiDBConfig;
import com.bigdata.admin.dto.TiDBConnectionInfo;
import com.bigdata.admin.dto.TiDBDatabaseInfo;
import com.bigdata.admin.dto.TiDBTableInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

/**
 * Service for managing TiDB connections and operations
 * Provides connection pooling, database discovery, and metadata access
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TiDBConnectionService {

    private final TiDBConfig defaultConfig;

    // Connection cache for active data sources
    private final Map<Long, ConnectionInfo> connectionCache = new HashMap<>();

    /**
     * Test connection to TiDB using provided connection info
     */
    public TiDBConnectionInfo testConnection(TiDBConnectionInfo connectionInfo) {
        log.info("Testing TiDB connection: {}:{}", connectionInfo.getHost(), connectionInfo.getPort());

        Connection connection = null;
        try {
            connection = createConnection(connectionInfo);

            // Test query
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT VERSION() as version, DATABASE() as current_db")) {

                if (rs.next()) {
                    String version = rs.getString("version");
                    log.info("TiDB connection successful. Version: {}", version);

                    return TiDBConnectionInfo.builder()
                        .host(connectionInfo.getHost())
                        .port(connectionInfo.getPort())
                        .username(connectionInfo.getUsername())
                        .database(connectionInfo.getDatabase())
                        .connected(true)
                        .version(version)
                        .message("Connection successful")
                        .build();
                }
            }

            return connectionInfo.toBuilder()
                .connected(true)
                .message("Connection successful")
                .build();

        } catch (SQLException e) {
            log.error("TiDB connection failed", e);
            return connectionInfo.toBuilder()
                .connected(false)
                .message("Connection failed: " + e.getMessage())
                .build();
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Get list of databases from TiDB
     */
    public List<TiDBDatabaseInfo> getDatabases(Long dataSourceId) {
        log.debug("Fetching databases for data source: {}", dataSourceId);

        Connection connection = null;
        try {
            connection = getConnection(dataSourceId);

            List<TiDBDatabaseInfo> databases = new ArrayList<>();

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {

                while (rs.next()) {
                    String dbName = rs.getString("Database");

                    // Skip system databases
                    if (!isSystemDatabase(dbName)) {
                        databases.add(TiDBDatabaseInfo.builder()
                            .name(dbName)
                            .dataSourceId(dataSourceId)
                            .build());
                    }
                }
            }

            log.debug("Found {} databases", databases.size());
            return databases;

        } catch (SQLException e) {
            log.error("Failed to fetch databases", e);
            throw new RuntimeException("Failed to fetch databases: " + e.getMessage(), e);
        }
    }

    /**
     * Get list of tables from a database
     */
    public List<TiDBTableInfo> getTables(Long dataSourceId, String database) {
        log.debug("Fetching tables for database: {}", database);

        Connection connection = null;
        try {
            connection = getConnection(dataSourceId);

            // Set the database context
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("USE `" + database + "`");
            }

            List<TiDBTableInfo> tables = new ArrayList<>();

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW TABLES")) {

                while (rs.next()) {
                    String tableName = rs.getString(1);

                    tables.add(TiDBTableInfo.builder()
                        .name(tableName)
                        .database(database)
                        .dataSourceId(dataSourceId)
                        .build());
                }
            }

            log.debug("Found {} tables in database {}", tables.size(), database);
            return tables;

        } catch (SQLException e) {
            log.error("Failed to fetch tables", e);
            throw new RuntimeException("Failed to fetch tables: " + e.getMessage(), e);
        }
    }

    /**
     * Execute a query and return results
     */
    public List<Map<String, Object>> executeQuery(Long dataSourceId, String database, String query) {
        log.debug("Executing query on database: {}", database);

        Connection connection = null;
        try {
            connection = getConnection(dataSourceId);

            // Set the database context
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("USE `" + database + "`");
            }

            List<Map<String, Object>> results = new ArrayList<>();

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    results.add(row);
                }
            }

            log.debug("Query returned {} rows", results.size());
            return results;

        } catch (SQLException e) {
            log.error("Query execution failed", e);
            throw new RuntimeException("Query failed: " + e.getMessage(), e);
        }
    }

    /**
     * Create connection from connection info
     */
    private Connection createConnection(TiDBConnectionInfo info) throws SQLException {
        String url = String.format(
            "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=%s&serverTimezone=%s&allowPublicKeyRetrieval=true",
            info.getHost(),
            info.getPort(),
            info.getDatabase() != null ? info.getDatabase() : "mysql",
            info.isSsl(),
            info.getTimezone() != null ? info.getTimezone() : "Asia/Shanghai"
        );

        return DriverManager.getConnection(url, info.getUsername(), info.getPassword());
    }

    /**
     * Get connection from cache or create new one
     */
    private Connection getConnection(Long dataSourceId) throws SQLException {
        ConnectionInfo info = connectionCache.get(dataSourceId);
        if (info != null && info.isValid()) {
            return info.connection;
        }

        // For now, use default config
        // In production, fetch from database by dataSourceId
        String url = defaultConfig.getJdbcUrl();
        Connection conn = DriverManager.getConnection(url,
            defaultConfig.getUsername(),
            defaultConfig.getPassword());

        connectionCache.put(dataSourceId, new ConnectionInfo(conn, System.currentTimeMillis()));
        return conn;
    }

    /**
     * Close connection
     */
    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.warn("Failed to close connection", e);
            }
        }
    }

    /**
     * Check if database is a system database
     */
    private boolean isSystemDatabase(String dbName) {
        return dbName.equalsIgnoreCase("mysql") ||
               dbName.equalsIgnoreCase("information_schema") ||
               dbName.equalsIgnoreCase("performance_schema") ||
               dbName.equalsIgnoreCase("metrics_schema");
    }

    /**
     * Close all cached connections
     */
    public void closeAllConnections() {
        connectionCache.values().forEach(info -> closeConnection(info.connection));
        connectionCache.clear();
        log.info("All TiDB connections closed");
    }

    /**
     * Internal connection info wrapper
     */
    private static class ConnectionInfo {
        final Connection connection;
        final long createdTime;

        ConnectionInfo(Connection connection, long createdTime) {
            this.connection = connection;
            this.createdTime = createdTime;
        }

        boolean isValid() {
            try {
                return connection != null && !connection.isClosed() &&
                       (System.currentTimeMillis() - createdTime) < 3600000; // 1 hour
            } catch (SQLException e) {
                return false;
            }
        }
    }
}
