package com.bigdata.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bigdata.admin.common.Result;
import com.bigdata.admin.dto.EtlExecutionDto;
import com.bigdata.admin.dto.EtlTransformationDto;
import com.bigdata.admin.service.EtlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ETL Transformation Controller
 * Provides REST API for ETL operations
 */
@RestController
@RequestMapping("/api/etl")
@Tag(name = "ETL Transformations", description = "ETL transformation management and execution")
public class EtlController {

    private final EtlService etlService;

    public EtlController(EtlService etlService) {
        this.etlService = etlService;
    }

    /**
     * Get paginated list of transformations
     */
    @GetMapping("/transformations")
    @Operation(summary = "Get ETL transformations", description = "Get paginated list of ETL transformations")
    public Result<Page<EtlTransformationDto>> getTransformations(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword) {
        Page<EtlTransformationDto> result = etlService.getTransformations(page, size, keyword);
        return Result.success(result);
    }

    /**
     * Get transformation by ID
     */
    @GetMapping("/transformations/{id}")
    @Operation(summary = "Get transformation by ID", description = "Get a specific ETL transformation by ID")
    public Result<EtlTransformationDto> getTransformationById(
            @Parameter(description = "Transformation ID") @PathVariable Long id) {
        EtlTransformationDto result = etlService.getTransformationById(id);
        return Result.success(result);
    }

    /**
     * Create new transformation
     */
    @PostMapping("/transformations")
    @Operation(summary = "Create transformation", description = "Create a new ETL transformation")
    public Result<EtlTransformationDto> createTransformation(@RequestBody EtlTransformationDto dto) {
        EtlTransformationDto result = etlService.createTransformation(dto);
        return Result.success(result);
    }

    /**
     * Update transformation
     */
    @PutMapping("/transformations/{id}")
    @Operation(summary = "Update transformation", description = "Update an existing ETL transformation")
    public Result<EtlTransformationDto> updateTransformation(
            @Parameter(description = "Transformation ID") @PathVariable Long id,
            @RequestBody EtlTransformationDto dto) {
        EtlTransformationDto result = etlService.updateTransformation(id, dto);
        return Result.success(result);
    }

    /**
     * Delete transformation
     */
    @DeleteMapping("/transformations/{id}")
    @Operation(summary = "Delete transformation", description = "Delete an ETL transformation")
    public Result<Void> deleteTransformation(
            @Parameter(description = "Transformation ID") @PathVariable Long id) {
        etlService.deleteTransformation(id);
        return Result.success();
    }

    /**
     * Execute transformation
     */
    @PostMapping("/transformations/{id}/execute")
    @Operation(summary = "Execute transformation", description = "Execute an ETL transformation")
    public Result<EtlExecutionDto> executeTransformation(
            @Parameter(description = "Transformation ID") @PathVariable Long id,
            @Parameter(description = "Triggered by") @RequestParam(defaultValue = "manual") String triggeredBy) {
        EtlExecutionDto result = etlService.executeTransformation(id, triggeredBy, null);
        return Result.success(result);
    }

    /**
     * Get execution history
     */
    @GetMapping("/executions")
    @Operation(summary = "Get execution history", description = "Get execution history for transformations")
    public Result<Page<EtlExecutionDto>> getExecutions(
            @Parameter(description = "Transformation ID") @RequestParam(required = false) Long transformationId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Page<EtlExecutionDto> result = etlService.getExecutions(transformationId, page, size);
        return Result.success(result);
    }
}
