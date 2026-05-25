package com.bigdata.admin.controller;

import com.bigdata.admin.common.Result;
import com.bigdata.admin.entity.ImportTask;
import com.bigdata.admin.service.DataImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Data Import Controller
 */
@RestController
@RequestMapping("/collections/{collectionId}/import")
@RequiredArgsConstructor
@Tag(name = "Data Import", description = "Data import operations")
public class DataImportController {

    private final DataImportService dataImportService;

    @PostMapping
    @Operation(summary = "Create and start import task")
    @com.bigdata.admin.config.RateLimitAspect.BatchRateLimit
    public Result<ImportTask> createImportTask(
            @PathVariable Long collectionId,
            @RequestParam("sourceType") String sourceType,
            @RequestParam("file") MultipartFile file) {

        try {
            // Create import task
            Long taskId = dataImportService.createImportTask(
                    collectionId,
                    sourceType,
                    file.getOriginalFilename()
            );

            // Process import asynchronously
            dataImportService.processImport(taskId, file);

            ImportTask task = dataImportService.getTask(taskId);
            return Result.success("Import task created successfully", task);

        } catch (Exception e) {
            return Result.error("Failed to create import task: " + e.getMessage());
        }
    }

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "Get import task status")
    public Result<ImportTask> getImportTask(
            @PathVariable Long collectionId,
            @PathVariable Long taskId) {

        ImportTask task = dataImportService.getTask(taskId);
        if (task == null || !task.getCollectionId().equals(collectionId)) {
            return Result.error("Import task not found");
        }
        return Result.success(task);
    }

    @GetMapping("/tasks")
    @Operation(summary = "Get all import tasks for collection")
    public Result<List<ImportTask>> getImportTasks(@PathVariable Long collectionId) {
        List<ImportTask> tasks = dataImportService.getTasksByCollection(collectionId);
        return Result.success(tasks);
    }
}
