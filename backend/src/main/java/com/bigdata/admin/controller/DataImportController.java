package com.bigdata.admin.controller;

import com.bigdata.admin.common.Result;
import com.bigdata.admin.config.FileValidationUtil;
import com.bigdata.admin.entity.ImportTask;
import com.bigdata.admin.service.DataImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Data Import Controller
 */
@RestController
@RequestMapping("/collections/{collectionId}/import")
@Tag(name = "Data Import", description = "Data import operations")
public class DataImportController {

    private final DataImportService dataImportService;

    public DataImportController(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @PostMapping
    @Operation(summary = "Create and start import task")
    @com.bigdata.admin.config.RateLimitAspect.BatchRateLimit
    public Result<ImportTask> createImportTask(
            @PathVariable Long collectionId,
            @RequestParam("sourceType") String sourceType,
            @RequestParam("file") MultipartFile file) {

        try {
            // Validate file type and content
            FileValidationUtil.validateImportFile(file);

            // Validate source type matches file extension
            String fileExtension = file.getOriginalFilename();
            if (fileExtension != null) {
                int lastDot = fileExtension.lastIndexOf('.');
                if (lastDot > 0) {
                    fileExtension = fileExtension.substring(lastDot + 1).toLowerCase();
                    if (!isSourceTypeValid(sourceType, fileExtension)) {
                        return Result.error("Source type '" + sourceType + "' does not match file extension '" + fileExtension + "'");
                    }
                }
            }

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

        } catch (IllegalArgumentException e) {
            return Result.error("Validation error: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("Failed to create import task: " + e.getMessage());
        }
    }

    /**
     * Validate that source type matches file extension
     */
    private boolean isSourceTypeValid(String sourceType, String fileExtension) {
        return switch (sourceType.toLowerCase()) {
            case "csv" -> fileExtension.equals("csv");
            case "excel", "xlsx" -> fileExtension.equals("xlsx") || fileExtension.equals("xls");
            case "json" -> fileExtension.equals("json");
            default -> false;
        };
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
