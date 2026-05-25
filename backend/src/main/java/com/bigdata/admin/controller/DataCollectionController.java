package com.bigdata.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bigdata.admin.common.Result;
import com.bigdata.admin.entity.DataCollection;
import com.bigdata.admin.service.DataCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
@Tag(name = "Data Collection Management", description = "Data collection CRUD operations")
public class DataCollectionController {

    private final DataCollectionService dataCollectionService;

    @GetMapping
    @Operation(summary = "Get all collections with pagination")
    public Result<Page<DataCollection>> getCollections(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<DataCollection> result = dataCollectionService.getCollections(page, size, keyword);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get collection by ID")
    public Result<DataCollection> getCollection(@PathVariable Long id) {
        DataCollection collection = dataCollectionService.getCollectionById(id);
        if (collection == null) {
            return Result.error("Collection not found");
        }
        return Result.success(collection);
    }

    @PostMapping
    @Operation(summary = "Create new collection")
    public Result<DataCollection> createCollection(@Validated @RequestBody DataCollection collection) {
        DataCollection created = dataCollectionService.createCollection(collection);
        return Result.success("Collection created successfully", created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update collection")
    public Result<DataCollection> updateCollection(
            @PathVariable Long id,
            @Validated @RequestBody DataCollection collection) {
        DataCollection updated = dataCollectionService.updateCollection(id, collection);
        if (updated == null) {
            return Result.error("Collection not found");
        }
        return Result.success("Collection updated successfully", updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete collection")
    public Result<Void> deleteCollection(@PathVariable Long id) {
        dataCollectionService.deleteCollection(id);
        return Result.success("Collection deleted successfully", null);
    }

    @PostMapping("/{id}/stats")
    @Operation(summary = "Update collection statistics")
    public Result<Long> updateStats(@PathVariable Long id) {
        Long count = dataCollectionService.updateRecordCount(id);
        return Result.success("Statistics updated", count);
    }
}
