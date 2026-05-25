package com.bigdata.admin.controller;

import com.bigdata.admin.common.Result;
import com.bigdata.admin.service.DataSourceConnectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Data Source Connection Controller
 * Provides REST API for multi-datasource connections
 */
@RestController
@RequestMapping("/api/datasources")
@Tag(name = "Data Source Connections", description = "Multi-datasource connection management")
public class DataSourceConnectionController {

    private final DataSourceConnectionService connectionService;

    public DataSourceConnectionController(DataSourceConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    /**
     * Get supported data source types
     */
    @GetMapping("/types")
    @Operation(summary = "Get supported data source types", description = "Get all supported data source types")
    public Result<List<Map<String, Object>>> getSupportedTypes() {
        List<Map<String, Object>> types = connectionService.getSupportedDataSourceTypes();
        return Result.success(types);
    }

    /**
     * Test connection for specific data source type
     */
    @PostMapping("/{type}/test")
    @Operation(summary = "Test data source connection", description = "Test connection to a specific data source type")
    public Result<Map<String, Object>> testConnection(
            @Parameter(description = "Data source type (tidb, postgresql, mongodb, csv)") @PathVariable String type,
            @RequestBody Map<String, Object> config) {
        Map<String, Object> result = connectionService.testConnection(type, config);
        return Result.success(result);
    }

    /**
     * Get tables/collections for a data source
     */
    @GetMapping("/{id}/tables")
    @Operation(summary = "Get data source tables", description = "Get tables or collections from a data source")
    public Result<List<Map<String, Object>>> getTables(
            @Parameter(description = "Data source ID") @PathVariable Long id,
            @Parameter(description = "Data source type") @RequestParam String type) {
        List<Map<String, Object>> tables = connectionService.getTables(type, id);
        return Result.success(tables);
    }

    /**
     * Execute query on data source
     */
    @PostMapping("/{id}/query")
    @Operation(summary = "Execute query", description = "Execute a query on the data source")
    public Result<List<Map<String, Object>>> executeQuery(
            @Parameter(description = "Data source ID") @PathVariable Long id,
            @Parameter(description = "Data source type") @RequestParam String type,
            @RequestBody Map<String, String> request) {
        String query = request.get("query");
        List<Map<String, Object>> result = connectionService.executeQuery(type, id, query);
        return Result.success(result);
    }
}
