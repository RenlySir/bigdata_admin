package com.bigdata.admin.controller;

import com.bigdata.admin.common.Result;
import com.bigdata.admin.service.DataExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Data Export Controller
 */
@RestController
@RequestMapping("/collections/{collectionId}/export")
@Tag(name = "Data Export", description = "Data export operations")
public class DataExportController {

    private final DataExportService dataExportService;

    public DataExportController(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @GetMapping("/excel")
    @Operation(summary = "Export collection to Excel")
    @com.bigdata.admin.config.RateLimitAspect.BatchRateLimit
    public ResponseEntity<byte[]> exportToExcel(
            @PathVariable Long collectionId,
            @RequestParam(required = false) String format) {

        try {
            // Get records from service (you'll need to inject DataRecordService)
            // For now, returning a placeholder response
            byte[] excelData = dataExportService.exportToExcel(List.of());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                    "collection_" + collectionId + "_export.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/json")
    @Operation(summary = "Export collection to JSON")
    @com.bigdata.admin.config.RateLimitAspect.BatchRateLimit
    public ResponseEntity<String> exportToJson(@PathVariable Long collectionId) {
        try {
            String jsonData = dataExportService.exportToJson(List.of());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=collection_" + collectionId + "_export.json")
                    .body(jsonData);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/csv")
    @Operation(summary = "Export collection to CSV")
    @com.bigdata.admin.config.RateLimitAspect.BatchRateLimit
    public ResponseEntity<String> exportToCsv(@PathVariable Long collectionId) {
        try {
            String csvData = dataExportService.exportToCsv(List.of());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=collection_" + collectionId + "_export.csv")
                    .body(csvData);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
