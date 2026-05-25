package com.bigdata.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bigdata.admin.common.Result;
import com.bigdata.admin.dto.TiDBConnectionInfo;
import com.bigdata.admin.dto.TiDBDatabaseInfo;
import com.bigdata.admin.dto.TiDBTableInfo;
import com.bigdata.admin.entity.DataSource;
import com.bigdata.admin.service.DataSourceService;
import com.bigdata.admin.service.TiDBConnectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/datasources")
@Tag(name = "Data Source Management", description = "Data source CRUD operations")
public class DataSourceController {

    private static final Logger log = LoggerFactory.getLogger(DataSourceController.class);

    private final DataSourceService dataSourceService;
    private final TiDBConnectionService tiDBConnectionService;

    public DataSourceController(DataSourceService dataSourceService, TiDBConnectionService tiDBConnectionService) {
        this.dataSourceService = dataSourceService;
        this.tiDBConnectionService = tiDBConnectionService;
    }

    @GetMapping
    @Operation(summary = "Get all data sources with pagination")
    public Result<Page<DataSource>> getDataSources(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type) {
        Page<DataSource> result = dataSourceService.getDataSources(page, size, type);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get data source by ID")
    public Result<DataSource> getDataSource(@PathVariable Long id) {
        DataSource dataSource = dataSourceService.getDataSourceById(id);
        if (dataSource == null) {
            return Result.error("Data source not found");
        }
        return Result.success(dataSource);
    }

    @PostMapping
    @Operation(summary = "Create new data source")
    public Result<DataSource> createDataSource(@Validated @RequestBody DataSource dataSource) {
        DataSource created = dataSourceService.createDataSource(dataSource);
        return Result.success("Data source created successfully", created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update data source")
    public Result<DataSource> updateDataSource(
            @PathVariable Long id,
            @Validated @RequestBody DataSource dataSource) {
        DataSource updated = dataSourceService.updateDataSource(id, dataSource);
        if (updated == null) {
            return Result.error("Data source not found");
        }
        return Result.success("Data source updated successfully", updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete data source")
    public Result<Void> deleteDataSource(@PathVariable Long id) {
        dataSourceService.deleteDataSource(id);
        return Result.success("Data source deleted successfully", null);
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "Test data source connection")
    public Result<Boolean> testConnection(@PathVariable Long id) {
        DataSource dataSource = dataSourceService.getDataSourceById(id);
        if (dataSource == null) {
            return Result.error("Data source not found");
        }
        boolean result = dataSourceService.testConnection(dataSource);
        return Result.success(result ? "Connection successful" : "Connection failed", result);
    }

    // ========== TiDB Specific Endpoints ==========

    @PostMapping("/tidb/test")
    @Operation(summary = "Test TiDB connection")
    public Result<TiDBConnectionInfo> testTiDBConnection(@RequestBody TiDBConnectionInfo connectionInfo) {
        if (!connectionInfo.isValid()) {
            return Result.error("Invalid connection parameters");
        }

        TiDBConnectionInfo result = tiDBConnectionService.testConnection(connectionInfo);
        return Result.success(
            result.isConnected() ? "TiDB connection successful" : "TiDB connection failed",
            result
        );
    }

    @GetMapping("/{id}/tidb/databases")
    @Operation(summary = "Get TiDB databases")
    public Result<List<TiDBDatabaseInfo>> getTiDBDatabases(@PathVariable Long id) {
        try {
            List<TiDBDatabaseInfo> databases = tiDBConnectionService.getDatabases(id);
            return Result.success(databases);
        } catch (Exception e) {
            log.error("Failed to fetch TiDB databases", e);
            return Result.error("Failed to fetch databases: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/tidb/tables")
    @Operation(summary = "Get TiDB tables from database")
    public Result<List<TiDBTableInfo>> getTiDBTables(
            @PathVariable Long id,
            @RequestParam String database) {

        try {
            List<TiDBTableInfo> tables = tiDBConnectionService.getTables(id, database);
            return Result.success(tables);
        } catch (Exception e) {
            log.error("Failed to fetch TiDB tables", e);
            return Result.error("Failed to fetch tables: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/tidb/query")
    @Operation(summary = "Execute query on TiDB")
    public Result<List<Map<String, Object>>> executeQuery(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        try {
            String database = request.get("database");
            String query = request.get("query");

            if (database == null || database.trim().isEmpty()) {
                return Result.error("Database name is required");
            }

            if (query == null || query.trim().isEmpty()) {
                return Result.error("Query is required");
            }

            List<Map<String, Object>> results = tiDBConnectionService.executeQuery(id, database, query);
            return Result.success(results);
        } catch (Exception e) {
            log.error("Query execution failed", e);
            return Result.error("Query failed: " + e.getMessage());
        }
    }

    @GetMapping("/tidb/info")
    @Operation(summary = "Get default TiDB configuration info")
    public Result<TiDBConnectionInfo> getDefaultTiDBInfo() {
        TiDBConnectionInfo info = TiDBConnectionInfo.builder()
            .host("localhost")
            .port(4000)
            .database("bigdata_admin")
            .ssl(false)
            .timezone("Asia/Shanghai")
            .build();

        return Result.success(info);
    }
}
