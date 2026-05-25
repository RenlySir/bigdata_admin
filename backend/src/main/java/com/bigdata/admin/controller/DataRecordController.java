package com.bigdata.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bigdata.admin.common.Result;
import com.bigdata.admin.entity.DataRecord;
import com.bigdata.admin.service.DataRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collections/{collectionId}/records")
@RequiredArgsConstructor
@Tag(name = "Data Record Management", description = "Data record CRUD operations")
public class DataRecordController {

    private final DataRecordService dataRecordService;

    @GetMapping
    @Operation(summary = "Get records by collection with pagination")
    public Result<Page<DataRecord>> getRecords(
            @PathVariable Long collectionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<DataRecord> result = dataRecordService.getRecords(collectionId, page, size, keyword);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get record by ID")
    public Result<DataRecord> getRecord(@PathVariable Long collectionId, @PathVariable Long id) {
        DataRecord record = dataRecordService.getRecordById(id);
        if (record == null || !record.getCollectionId().equals(collectionId)) {
            return Result.error("Record not found");
        }
        return Result.success(record);
    }

    @PostMapping
    @Operation(summary = "Create new record")
    public Result<DataRecord> createRecord(
            @PathVariable Long collectionId,
            @Validated @RequestBody DataRecord record) {
        record.setCollectionId(collectionId);
        DataRecord created = dataRecordService.createRecord(record);
        return Result.success("Record created successfully", created);
    }

    @PostMapping("/batch")
    @Operation(summary = "Batch insert records")
    public Result<Void> batchInsertRecords(
            @PathVariable Long collectionId,
            @RequestBody List<DataRecord> records) {
        records.forEach(r -> r.setCollectionId(collectionId));
        dataRecordService.batchInsertRecords(records);
        return Result.success("Records inserted successfully", null);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update record")
    public Result<DataRecord> updateRecord(
            @PathVariable Long collectionId,
            @PathVariable Long id,
            @Validated @RequestBody DataRecord record) {
        DataRecord existing = dataRecordService.getRecordById(id);
        if (existing == null || !existing.getCollectionId().equals(collectionId)) {
            return Result.error("Record not found");
        }
        DataRecord updated = dataRecordService.updateRecord(id, record);
        return Result.success("Record updated successfully", updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete record")
    public Result<Void> deleteRecord(@PathVariable Long collectionId, @PathVariable Long id) {
        DataRecord existing = dataRecordService.getRecordById(id);
        if (existing == null || !existing.getCollectionId().equals(collectionId)) {
            return Result.error("Record not found");
        }
        dataRecordService.deleteRecord(id);
        return Result.success("Record deleted successfully", null);
    }
}
