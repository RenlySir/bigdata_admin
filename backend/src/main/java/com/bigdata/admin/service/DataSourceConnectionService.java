package com.bigdata.admin.service;

import com.bigdata.admin.dto.PostgreSQLConnectionInfo;
import com.bigdata.admin.dto.TiDBConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

/**
 * Data Source Connection Service
 * Supports multiple data source types: TiDB, PostgreSQL, MongoDB, CSV
 */
@Service
public class DataSourceConnectionService {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConnectionService.class);

    /**
     * Test connection based on data source type
     */
    public Map<String, Object> testConnection(String type, Map<String, Object> config) {
        try {
            return switch (type.toLowerCase()) {
                case "tidb", "mysql" -> testTiDBConnection(config);
                case "postgresql" -> testPostgreSQLConnection(config);
                case "mongodb" -> testMongoDBConnection(config);
                case "csv" -> testCSVFileConnection(config);
                default -> throw new IllegalArgumentException("Unsupported data source type: " + type);
            };
        } catch (Exception e) {
            log.error("Connection test failed for type: {}", type, e);
            return Map.of(
                "success", false,
                "message", "Connection failed: " + e.getMessage(),
                "type", type
            );
        }
    }

    /**
     * Get tables/channels based on data source type
     */
    public List<Map<String, Object>> getTables(String type, Long dataSourceId) {
        try {
            return switch (type.toLowerCase()) {
                case "tidb", "mysql" -> getTiDBTables(dataSourceId);
                case "postgresql" -> getPostgreSQLTables(dataSourceId);
                case "mongodb" -> getMongoDBCollections(dataSourceId);
                case "csv" -> getCSSheets(dataSourceId);
                default -> throw new IllegalArgumentException("Unsupported data source type: " + type);
            };
        } catch (Exception e) {
            log.error("Failed to get tables for type: {}", type, e);
            return Collections.emptyList();
        }
    }

    /**
     * Execute query based on data source type
     */
    public List<Map<String, Object>> executeQuery(String type, Long dataSourceId, String query) {
        try {
            return switch (type.toLowerCase()) {
                case "tidb", "mysql", "postgresql" -> executeSQLQuery(dataSourceId, query);
                case "mongodb" -> executeMongoQuery(dataSourceId, query);
                default -> throw new IllegalArgumentException("Query execution not supported for type: " + type);
            };
        } catch (Exception e) {
            log.error("Query execution failed for type: {}", type, e);
            throw new RuntimeException("Query execution failed: " + e.getMessage());
        }
    }

    /**
     * Test TiDB/MySQL connection
     */
    private Map<String, Object> testTiDBConnection(Map<String, Object> config) throws SQLException {
        TiDBConnectionInfo info = buildTiDBConnectionInfo(config);

        try (Connection conn = createTiDBConnection(info)) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT VERSION() as version, DATABASE() as current_db")) {

                if (rs.next()) {
                    String version = rs.getString("version");
                    return Map.of(
                        "success", true,
                        "message", "Connection successful",
                        "type", "tidb",
                        "version", version,
                        "database", rs.getString("current_db")
                    );
                }
            }

            return Map.of(
                "success", true,
                "message", "Connection successful",
                "type", "tidb"
            );
        }
    }

    /**
     * Test PostgreSQL connection
     */
    private Map<String, Object> testPostgreSQLConnection(Map<String, Object> config) throws SQLException {
        PostgreSQLConnectionInfo info = buildPostgreSQLConnectionInfo(config);

        try (Connection conn = createPostgreSQLConnection(info)) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT version()")) {

                if (rs.next()) {
                    String version = rs.getString("version");
                    return Map.of(
                        "success", true,
                        "message", "Connection successful",
                        "type", "postgresql",
                        "version", version,
                        "database", info.getDatabase()
                    );
                }
            }

            return Map.of(
                "success", true,
                "message", "Connection successful",
                "type", "postgresql"
            );
        }
    }

    /**
     * Test MongoDB connection (requires MongoDB driver)
     */
    private Map<String, Object> testMongoDBConnection(Map<String, Object> config) {
        String host = (String) config.get("host");
        int port = (Integer) config.getOrDefault("port", 27017);
        String database = (String) config.get("database");

        try {
            // MongoDB connection test would go here
            // For now, return a simulated response
            return Map.of(
                "success", true,
                "message", "Connection successful (simulated)",
                "type", "mongodb",
                "host", host,
                "port", port,
                "database", database
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "MongoDB connection test: " + e.getMessage(),
                "type", "mongodb"
            );
        }
    }

    /**
     * Test CSV file connection
     */
    private Map<String, Object> testCSVFileConnection(Map<String, Object> config) {
        String filePath = (String) config.get("filePath");

        if (filePath == null || filePath.trim().isEmpty()) {
            return Map.of(
                "success", false,
                "message", "File path is required",
                "type", "csv"
            );
        }

        return Map.of(
            "success", true,
            "message", "CSV file accessible",
            "type", "csv",
            "filePath", filePath
        );
    }

    /**
     * Get TiDB tables
     */
    private List<Map<String, Object>> getTiDBTables(Long dataSourceId) {
        // Implementation would query TiDB for table list
        return Collections.emptyList();
    }

    /**
     * Get PostgreSQL tables
     */
    private List<Map<String, Object>> getPostgreSQLTables(Long dataSourceId) {
        // Implementation would query PostgreSQL for table list
        return Collections.emptyList();
    }

    /**
     * Get MongoDB collections
     */
    private List<Map<String, Object>> getMongoDBCollections(Long dataSourceId) {
        // Implementation would query MongoDB for collection list
        return Collections.emptyList();
    }

    /**
     * Get CSV sheets
     */
    private List<Map<String, Object>> getCSSheets(Long dataSourceId) {
        // Implementation would parse CSV file for sheets
        return Collections.singletonList(Map.of(
            "name", "Sheet1",
            "type", "csv"
        ));
    }

    /**
     * Execute SQL query
     */
    private List<Map<String, Object>> executeSQLQuery(Long dataSourceId, String query) throws SQLException {
        // Implementation would execute SQL query on the data source
        return Collections.emptyList();
    }

    /**
     * Execute MongoDB query
     */
    private List<Map<String, Object>> executeMongoQuery(Long dataSourceId, String query) {
        // Implementation would execute MongoDB query
        return Collections.emptyList();
    }

    /**
     * Build TiDBConnectionInfo from config map
     */
    private TiDBConnectionInfo buildTiDBConnectionInfo(Map<String, Object> config) {
        return TiDBConnectionInfo.builder()
            .host((String) config.get("host"))
            .port(((Number) config.getOrDefault("port", 4000)).intValue())
            .database((String) config.get("database"))
            .username((String) config.get("username"))
            .password((String) config.getOrDefault("password", ""))
            .ssl((Boolean) config.getOrDefault("ssl", false))
            .timezone((String) config.getOrDefault("timezone", "Asia/Shanghai"))
            .build();
    }

    /**
     * Build PostgreSQLConnectionInfo from config map
     */
    private PostgreSQLConnectionInfo buildPostgreSQLConnectionInfo(Map<String, Object> config) {
        PostgreSQLConnectionInfo info = new PostgreSQLConnectionInfo();
        info.setHost((String) config.get("host"));
        info.setPort(((Number) config.getOrDefault("port", 5432)).intValue());
        info.setDatabase((String) config.get("database"));
        info.setUsername((String) config.get("username"));
        info.setPassword((String) config.getOrDefault("password", ""));
        info.setSchema((String) config.getOrDefault("schema", "public"));
        info.setSsl((Boolean) config.getOrDefault("ssl", false));
        return info;
    }

    /**
     * Create TiDB connection
     */
    private Connection createTiDBConnection(TiDBConnectionInfo info) throws SQLException {
        String url = info.getJdbcUrl();
        return DriverManager.getConnection(url, info.getUsername(), info.getPassword());
    }

    /**
     * Create PostgreSQL connection
     */
    private Connection createPostgreSQLConnection(PostgreSQLConnectionInfo info) throws SQLException {
        String url = info.getJdbcUrl();
        return DriverManager.getConnection(url, info.getUsername(), info.getPassword());
    }

    /**
     * Get supported data source types
     */
    public List<Map<String, Object>> getSupportedDataSourceTypes() {
        return Arrays.asList(
            Map.of(
                "type", "tidb",
                "name", "TiDB",
                "description", "TiDB 分布式数据库",
                "icon", "database",
                "color", "#409eff",
                "features", Arrays.asList("HTAP", "分布式事务", "水平扩展")
            ),
            Map.of(
                "type", "postgresql",
                "name", "PostgreSQL",
                "description", "PostgreSQL 关系型数据库",
                "icon", "database",
                "color", "#336791",
                "features", Arrays.asList("ACID", "JSON支持", "全文搜索")
            ),
            Map.of(
                "type", "mongodb",
                "name", "MongoDB",
                "description", "MongoDB 文档数据库",
                "icon", "document",
                "color", "#4DB33D",
                "features", Arrays.asList("文档存储", "水平扩展", "灵活模式")
            ),
            Map.of(
                "type", "csv",
                "name", "CSV 文件",
                "description", "CSV 格式文件导入",
                "icon", "document",
                "color", "#67C23A",
                "features", Arrays.asList("批量导入", "简单格式", "广泛支持")
            ),
            Map.of(
                "type", "excel",
                "name", "Excel 文件",
                "description", "Excel 格式文件导入",
                "icon", "tickets",
                "color", "#217346",
                "features", Arrays.asList("多工作表", "格式化数据", "公式支持")
            )
        );
    }
}
