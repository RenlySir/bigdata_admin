package com.bigdata.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bigdata.admin.config.CacheConfig;
import com.bigdata.admin.entity.DataCollection;
import com.bigdata.admin.entity.DataRecord;
import com.bigdata.admin.exception.ResourceNotFoundException;
import com.bigdata.admin.mapper.DataCollectionMapper;
import com.bigdata.admin.mapper.DataRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for managing data collections
 * Provides CRUD operations with caching support
 */
@Service
public class DataCollectionService extends ServiceImpl<DataCollectionMapper, DataCollection> {

    private static final Logger log = LoggerFactory.getLogger(DataCollectionService.class);

    private final DataCollectionMapper dataCollectionMapper;
    private final DataRecordMapper dataRecordMapper;
    private final ObjectMapper objectMapper;

    public DataCollectionService(DataCollectionMapper dataCollectionMapper,
                                  DataRecordMapper dataRecordMapper,
                                  ObjectMapper objectMapper) {
        this.dataCollectionMapper = dataCollectionMapper;
        this.dataRecordMapper = dataRecordMapper;
        this.objectMapper = objectMapper;
    }

    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Get paginated list of collections with optional keyword search
     */
    public Page<DataCollection> getCollections(int page, int size, String keyword) {
        // Validate pagination parameters
        if (page < 1) {
            throw new IllegalArgumentException("Page number must be >= 1");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }

        log.debug("Fetching collections: page={}, size={}, keyword={}", page, size, keyword);

        Page<DataCollection> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DataCollection> wrapper = new LambdaQueryWrapper<>();

        // Apply keyword filter if provided
        if (StringUtils.hasText(keyword)) {
            wrapper.like(DataCollection::getName, keyword)
                   .or()
                   .like(DataCollection::getDescription, keyword);
        }

        wrapper.orderByDesc(DataCollection::getCreatedAt);
        Page<DataCollection> result = dataCollectionMapper.selectPage(pageParam, wrapper);

        log.debug("Found {} collections", result.getRecords().size());
        return result;
    }

    /**
     * Get collection by ID with caching
     */
    @Cacheable(value = CacheConfig.CACHE_COLLECTIONS, key = "#id", unless = "#result == null")
    public DataCollection getCollectionById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid collection ID");
        }

        log.debug("Fetching collection by id: {}", id);
        DataCollection collection = dataCollectionMapper.selectById(id);

        if (collection == null) {
            throw new ResourceNotFoundException("Collection not found with id: " + id);
        }

        return collection;
    }

    /**
     * Create new collection
     */
    @CacheEvict(value = CacheConfig.CACHE_COLLECTIONS, allEntries = true)
    @Transactional
    public DataCollection createCollection(DataCollection collection) {
        validateCollection(collection);

        log.info("Creating new collection: {}", collection.getName());

        collection.setRecordCount(0L);
        collection.setSizeInBytes(0L);
        collection.setStatus(1);

        dataCollectionMapper.insert(collection);
        log.info("Collection created successfully with id: {}", collection.getId());

        return collection;
    }

    /**
     * Update existing collection
     */
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_COLLECTIONS, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_COLLECTION_STATS, key = "#id")
    })
    @Transactional
    public DataCollection updateCollection(Long id, DataCollection collection) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid collection ID");
        }

        validateCollection(collection);

        log.info("Updating collection: {}", id);

        // Verify collection exists
        DataCollection existing = getCollectionById(id);

        collection.setId(id);
        collection.setCreatedAt(existing.getCreatedAt()); // Preserve creation time

        dataCollectionMapper.updateById(collection);
        log.info("Collection updated successfully: {}", id);

        return getCollectionById(id);
    }

    /**
     * Delete collection and all its records
     */
    @CacheEvict(value = CacheConfig.CACHE_COLLECTIONS, key = "#id")
    @Transactional
    public void deleteCollection(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid collection ID");
        }

        log.info("Deleting collection: {}", id);

        // Delete collection
        int deleted = dataCollectionMapper.deleteById(id);

        // Delete associated records
        LambdaQueryWrapper<DataRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataRecord::getCollectionId, id);
        long recordsDeleted = dataRecordMapper.delete(wrapper);

        log.info("Collection deleted: {} records removed: {}", deleted > 0, recordsDeleted);
    }

    /**
     * Update record count for a collection
     */
    @CacheEvict(value = CacheConfig.CACHE_COLLECTION_STATS, key = "#collectionId")
    public Long updateRecordCount(Long collectionId) {
        if (collectionId == null || collectionId <= 0) {
            throw new IllegalArgumentException("Invalid collection ID");
        }

        log.debug("Updating record count for collection: {}", collectionId);

        LambdaQueryWrapper<DataRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataRecord::getCollectionId, collectionId);
        Long count = dataRecordMapper.selectCount(wrapper);

        DataCollection collection = new DataCollection();
        collection.setId(collectionId);
        collection.setRecordCount(count);
        dataCollectionMapper.updateById(collection);

        return count;
    }

    /**
     * Validate collection data
     */
    private void validateCollection(DataCollection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }

        if (!StringUtils.hasText(collection.getName())) {
            throw new IllegalArgumentException("Collection name is required");
        }

        if (collection.getName().length() > 100) {
            throw new IllegalArgumentException("Collection name cannot exceed 100 characters");
        }

        if (StringUtils.hasText(collection.getDescription()) &&
            collection.getDescription().length() > 500) {
            throw new IllegalArgumentException("Description cannot exceed 500 characters");
        }
    }
}
