package com.bigdata.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bigdata.admin.common.Result;
import com.bigdata.admin.entity.DataSource;
import com.bigdata.admin.service.DataSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/datasources")
@RequiredArgsConstructor
@Tag(name = "Data Source Management", description = "Data source CRUD operations")
public class DataSourceController {

    private final DataSourceService dataSourceService;

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
}
