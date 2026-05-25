package com.bigdata.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bigdata.admin.dto.EtlExecutionDto;
import com.bigdata.admin.dto.EtlTransformationDto;
import com.bigdata.admin.entity.DataCollection;
import com.bigdata.admin.entity.DataRecord;
import com.bigdata.admin.entity.EtlExecution;
import com.bigdata.admin.entity.EtlTransformation;
import com.bigdata.admin.exception.ResourceNotFoundException;
import com.bigdata.admin.mapper.DataCollectionMapper;
import com.bigdata.admin.mapper.DataRecordMapper;
import com.bigdata.admin.mapper.EtlExecutionMapper;
import com.bigdata.admin.mapper.EtlTransformationMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ETL Service
 * Provides data transformation, loading, and ETL process management
 */
@Service
public class EtlService extends ServiceImpl<EtlTransformationMapper, EtlTransformation> {

    private static final Logger log = LoggerFactory.getLogger(EtlService.class);

    private final EtlTransformationMapper transformationMapper;
    private final EtlExecutionMapper executionMapper;
    private final DataCollectionMapper collectionMapper;
    private final DataRecordMapper recordMapper;
    private final ObjectMapper objectMapper;

    private static final int MAX_PAGE_SIZE = 100;

    public EtlService(EtlTransformationMapper transformationMapper,
                      EtlExecutionMapper executionMapper,
                      DataCollectionMapper collectionMapper,
                      DataRecordMapper recordMapper,
                      ObjectMapper objectMapper) {
        this.transformationMapper = transformationMapper;
        this.executionMapper = executionMapper;
        this.collectionMapper = collectionMapper;
        this.recordMapper = recordMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Get paginated list of transformations
     */
    public Page<EtlTransformationDto> getTransformations(int page, int size, String keyword) {
        if (page < 1) {
            throw new IllegalArgumentException("Page number must be >= 1");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }

        log.debug("Fetching ETL transformations: page={}, size={}, keyword={}", page, size, keyword);

        Page<EtlTransformation> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<EtlTransformation> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(EtlTransformation::getName, keyword)
                   .or()
                   .like(EtlTransformation::getDescription, keyword);
        }

        wrapper.orderByDesc(EtlTransformation::getCreatedAt);
        Page<EtlTransformation> result = transformationMapper.selectPage(pageParam, wrapper);

        Page<EtlTransformationDto> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<EtlTransformationDto> dtoList = result.getRecords().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    /**
     * Get transformation by ID
     */
    public EtlTransformationDto getTransformationById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid transformation ID");
        }

        log.debug("Fetching ETL transformation by id: {}", id);
        EtlTransformation transformation = transformationMapper.selectById(id);

        if (transformation == null) {
            throw new ResourceNotFoundException("Transformation not found with id: " + id);
        }

        return toDto(transformation);
    }

    /**
     * Create new transformation
     */
    @Transactional
    public EtlTransformationDto createTransformation(EtlTransformationDto dto) {
        validateTransformation(dto);

        log.info("Creating new ETL transformation: {}", dto.getName());

        EtlTransformation transformation = toEntity(dto);
        transformation.setStatus("draft");
        transformation.setTotalExecutions(0);
        transformation.setSuccessExecutions(0);
        transformation.setFailureExecutions(0);

        transformationMapper.insert(transformation);
        log.info("ETL transformation created successfully with id: {}", transformation.getId());

        return toDto(transformation);
    }

    /**
     * Update transformation
     */
    @Transactional
    public EtlTransformationDto updateTransformation(Long id, EtlTransformationDto dto) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid transformation ID");
        }

        validateTransformation(dto);

        log.info("Updating ETL transformation: {}", id);

        EtlTransformation existing = transformationMapper.selectById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Transformation not found with id: " + id);
        }

        EtlTransformation transformation = toEntity(dto);
        transformation.setId(id);
        transformation.setCreatedAt(existing.getCreatedAt());

        transformationMapper.updateById(transformation);
        log.info("ETL transformation updated successfully: {}", id);

        return toDto(transformation);
    }

    /**
     * Delete transformation
     */
    @Transactional
    public void deleteTransformation(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid transformation ID");
        }

        log.info("Deleting ETL transformation: {}", id);

        // Delete associated executions
        LambdaQueryWrapper<EtlExecution> execWrapper = new LambdaQueryWrapper<>();
        execWrapper.eq(EtlExecution::getTransformationId, id);
        executionMapper.delete(execWrapper);

        // Delete transformation
        int deleted = transformationMapper.deleteById(id);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Transformation not found with id: " + id);
        }

        log.info("ETL transformation deleted successfully: {}", id);
    }

    /**
     * Execute transformation
     */
    @Transactional
    public EtlExecutionDto executeTransformation(Long id, String triggeredBy, Long userId) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid transformation ID");
        }

        log.info("Executing ETL transformation: {}, triggered by: {}", id, triggeredBy);

        EtlTransformation transformation = transformationMapper.selectById(id);
        if (transformation == null) {
            throw new ResourceNotFoundException("Transformation not found with id: " + id);
        }

        // Create execution record
        EtlExecution execution = new EtlExecution();
        execution.setTransformationId(id);
        execution.setStatus("running");
        execution.setStartedAt(LocalDateTime.now());
        execution.setTriggeredBy(triggeredBy);
        execution.setTriggeredByUserId(userId);
        execution.setRecordsProcessed(0L);
        execution.setRecordsSuccess(0L);
        execution.setRecordsFailed(0L);
        executionMapper.insert(execution);

        try {
            // Execute transformation based on type
            ExecutionResult result = executeTransformationLogic(transformation);

            // Update execution record
            execution.setStatus("completed");
            execution.setCompletedAt(LocalDateTime.now());
            execution.setDurationMs(java.time.Duration.between(execution.getStartedAt(), execution.getCompletedAt()).toMillis());
            execution.setRecordsProcessed(result.getProcessed());
            execution.setRecordsSuccess(result.getSuccess());
            execution.setRecordsFailed(result.getFailed());
            execution.setExecutionLog(result.getLog());
            executionMapper.updateById(execution);

            // Update transformation stats
            transformation.setLastExecutedAt(execution.getStartedAt());
            transformation.setTotalExecutions(transformation.getTotalExecutions() + 1);
            transformation.setSuccessExecutions(transformation.getSuccessExecutions() + 1);
            transformationMapper.updateById(transformation);

            log.info("ETL transformation executed successfully: {}, processed: {}", id, result.getProcessed());

            return toExecutionDto(execution, transformation.getName());

        } catch (Exception e) {
            log.error("ETL transformation execution failed: {}", id, e);

            execution.setStatus("failed");
            execution.setCompletedAt(LocalDateTime.now());
            execution.setDurationMs(java.time.Duration.between(execution.getStartedAt(), execution.getCompletedAt()).toMillis());
            execution.setErrorMessage(e.getMessage());
            executionMapper.updateById(execution);

            // Update transformation stats
            transformation.setTotalExecutions(transformation.getTotalExecutions() + 1);
            transformation.setFailureExecutions(transformation.getFailureExecutions() + 1);
            transformationMapper.updateById(transformation);

            return toExecutionDto(execution, transformation.getName());
        }
    }

    /**
     * Get execution history
     */
    public Page<EtlExecutionDto> getExecutions(Long transformationId, int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("Page number must be >= 1");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }

        log.debug("Fetching ETL executions: transformationId={}, page={}, size={}", transformationId, page, size);

        Page<EtlExecution> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<EtlExecution> wrapper = new LambdaQueryWrapper<>();

        if (transformationId != null) {
            wrapper.eq(EtlExecution::getTransformationId, transformationId);
        }

        wrapper.orderByDesc(EtlExecution::getStartedAt);
        Page<EtlExecution> result = executionMapper.selectPage(pageParam, wrapper);

        Page<EtlExecutionDto> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());

        // Fetch transformation names
        Map<Long, String> transformationNames = new HashMap<>();
        if (!result.getRecords().isEmpty()) {
            Set<Long> ids = result.getRecords().stream()
                    .map(EtlExecution::getTransformationId)
                    .collect(Collectors.toSet());
            if (!ids.isEmpty()) {
                List<EtlTransformation> transforms = transformationMapper.selectBatchIds(ids);
                transformationNames = transforms.stream()
                        .collect(Collectors.toMap(EtlTransformation::getId, EtlTransformation::getName));
            }
        }

        Map<Long, String> finalTransformationNames = transformationNames;
        List<EtlExecutionDto> dtoList = result.getRecords().stream()
                .map(e -> toExecutionDto(e, finalTransformationNames.get(e.getTransformationId())))
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    /**
     * Execute transformation logic based on type
     */
    private ExecutionResult executeTransformationLogic(EtlTransformation transformation) throws Exception {
        ExecutionResult result = new ExecutionResult();
        StringBuilder log = new StringBuilder();

        switch (transformation.getTransformationType()) {
            case "mapping":
                return executeMappingTransformation(transformation, result, log);
            case "filter":
                return executeFilterTransformation(transformation, result, log);
            case "aggregate":
                return executeAggregateTransformation(transformation, result, log);
            case "export":
                return executeExportTransformation(transformation, result, log);
            default:
                throw new IllegalArgumentException("Unsupported transformation type: " + transformation.getTransformationType());
        }
    }

    /**
     * Execute mapping transformation
     */
    private ExecutionResult executeMappingTransformation(EtlTransformation transformation, ExecutionResult result, StringBuilder log) throws Exception {
        log.append("Starting mapping transformation...\n");

        // Get source records
        LambdaQueryWrapper<DataRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataRecord::getCollectionId, transformation.getSourceCollectionId());
        List<DataRecord> sourceRecords = recordMapper.selectList(wrapper);

        log.append("Found ").append(sourceRecords.size()).append(" source records\n");

        // Process mapping
        Map<String, Object> rules = objectMapper.readValue(transformation.getTransformationRules(), Map.class);
        Map<String, String> fieldMapping = (Map<String, String>) rules.get("fieldMapping");

        int processed = 0;
        int success = 0;

        for (DataRecord sourceRecord : sourceRecords) {
            try {
                // Apply mapping
                Map<String, Object> sourceData = objectMapper.readValue(sourceRecord.getJsonData(), Map.class);
                Map<String, Object> targetData = new HashMap<>();

                if (fieldMapping != null) {
                    for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
                        String sourceField = entry.getKey();
                        String targetField = entry.getValue();
                        if (sourceData.containsKey(sourceField)) {
                            targetData.put(targetField, sourceData.get(sourceField));
                        }
                    }
                }

                // Create target record
                if (transformation.getTargetCollectionId() != null) {
                    DataRecord targetRecord = new DataRecord();
                    targetRecord.setCollectionId(transformation.getTargetCollectionId());
                    targetRecord.setJsonData(objectMapper.writeValueAsString(targetData));
                    targetRecord.setDataType(sourceRecord.getDataType());
                    recordMapper.insert(targetRecord);
                }

                success++;
            } catch (Exception e) {
                log.append("Error processing record: ").append(e.getMessage()).append("\n");
            }
            processed++;
        }

        result.setProcessed((long) processed);
        result.setSuccess((long) success);
        result.setFailed((long) (processed - success));
        result.setLog(log.toString());

        return result;
    }

    /**
     * Execute filter transformation
     */
    private ExecutionResult executeFilterTransformation(EtlTransformation transformation, ExecutionResult result, StringBuilder log) throws Exception {
        log.append("Starting filter transformation...\n");

        LambdaQueryWrapper<DataRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataRecord::getCollectionId, transformation.getSourceCollectionId());
        List<DataRecord> sourceRecords = recordMapper.selectList(wrapper);

        log.append("Found ").append(sourceRecords.size()).append(" source records\n");

        Map<String, Object> rules = objectMapper.readValue(transformation.getTransformationRules(), Map.class);
        Map<String, Object> filterConditions = (Map<String, Object>) rules.get("conditions");

        int processed = 0;
        int success = 0;

        for (DataRecord sourceRecord : sourceRecords) {
            try {
                Map<String, Object> sourceData = objectMapper.readValue(sourceRecord.getJsonData(), Map.class);

                // Apply filter
                boolean passesFilter = true;
                if (filterConditions != null) {
                    for (Map.Entry<String, Object> condition : filterConditions.entrySet()) {
                        String field = condition.getKey();
                        Object expectedValue = condition.getValue();
                        if (!expectedValue.equals(sourceData.get(field))) {
                            passesFilter = false;
                            break;
                        }
                    }
                }

                if (passesFilter && transformation.getTargetCollectionId() != null) {
                    DataRecord targetRecord = new DataRecord();
                    targetRecord.setCollectionId(transformation.getTargetCollectionId());
                    targetRecord.setJsonData(sourceRecord.getJsonData());
                    targetRecord.setDataType(sourceRecord.getDataType());
                    recordMapper.insert(targetRecord);
                    success++;
                }
            } catch (Exception e) {
                log.append("Error processing record: ").append(e.getMessage()).append("\n");
            }
            processed++;
        }

        result.setProcessed((long) processed);
        result.setSuccess((long) success);
        result.setFailed((long) (processed - success));
        result.setLog(log.toString());

        return result;
    }

    /**
     * Execute aggregate transformation
     */
    private ExecutionResult executeAggregateTransformation(EtlTransformation transformation, ExecutionResult result, StringBuilder log) throws Exception {
        log.append("Starting aggregate transformation...\n");

        LambdaQueryWrapper<DataRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataRecord::getCollectionId, transformation.getSourceCollectionId());
        List<DataRecord> sourceRecords = recordMapper.selectList(wrapper);

        log.append("Found ").append(sourceRecords.size()).append(" source records\n");

        Map<String, Object> rules = objectMapper.readValue(transformation.getTransformationRules(), Map.class);
        String aggregateField = (String) rules.get("aggregateField");
        String operation = (String) rules.get("operation"); // sum, avg, count, min, max

        // Perform aggregation
        double aggregateValue = 0;
        switch (operation) {
            case "sum":
                for (DataRecord record : sourceRecords) {
                    Map<String, Object> data = objectMapper.readValue(record.getJsonData(), Map.class);
                    Object value = data.get(aggregateField);
                    if (value instanceof Number) {
                        aggregateValue += ((Number) value).doubleValue();
                    }
                }
                break;
            case "avg":
                double sum = 0;
                int count = 0;
                for (DataRecord record : sourceRecords) {
                    Map<String, Object> data = objectMapper.readValue(record.getJsonData(), Map.class);
                    Object value = data.get(aggregateField);
                    if (value instanceof Number) {
                        sum += ((Number) value).doubleValue();
                        count++;
                    }
                }
                aggregateValue = count > 0 ? sum / count : 0;
                break;
            case "count":
                aggregateValue = sourceRecords.size();
                break;
        }

        // Create aggregated record
        if (transformation.getTargetCollectionId() != null) {
            Map<String, Object> aggregatedData = new HashMap<>();
            aggregatedData.put(aggregateField, aggregateValue);
            aggregatedData.put("_operation", operation);
            aggregatedData.put("_timestamp", LocalDateTime.now().toString());

            DataRecord targetRecord = new DataRecord();
            targetRecord.setCollectionId(transformation.getTargetCollectionId());
            targetRecord.setJsonData(objectMapper.writeValueAsString(aggregatedData));
            targetRecord.setDataType("aggregate");
            recordMapper.insert(targetRecord);
        }

        log.append("Aggregation completed: ").append(operation).append("(").append(aggregateField).append(") = ").append(aggregateValue).append("\n");

        result.setProcessed((long) sourceRecords.size());
        result.setSuccess(1L);
        result.setFailed(0L);
        result.setLog(log.toString());

        return result;
    }

    /**
     * Execute export transformation
     */
    private ExecutionResult executeExportTransformation(EtlTransformation transformation, ExecutionResult result, StringBuilder log) throws Exception {
        log.append("Starting export transformation...\n");

        LambdaQueryWrapper<DataRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataRecord::getCollectionId, transformation.getSourceCollectionId());
        List<DataRecord> sourceRecords = recordMapper.selectList(wrapper);

        log.append("Found ").append(sourceRecords.size()).append(" records to export\n");

        // For now, just count the records
        // In a real implementation, this would export to file or external system
        result.setProcessed((long) sourceRecords.size());
        result.setSuccess((long) sourceRecords.size());
        result.setFailed(0L);
        result.setLog(log.append("Export completed successfully\n").toString());

        return result;
    }

    /**
     * Validate transformation data
     */
    private void validateTransformation(EtlTransformationDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Transformation cannot be null");
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Transformation name is required");
        }

        if (dto.getName().length() > 100) {
            throw new IllegalArgumentException("Transformation name cannot exceed 100 characters");
        }

        if (dto.getSourceCollectionId() == null) {
            throw new IllegalArgumentException("Source collection ID is required");
        }

        // Verify source collection exists
        DataCollection sourceCollection = collectionMapper.selectById(dto.getSourceCollectionId());
        if (sourceCollection == null) {
            throw new IllegalArgumentException("Source collection not found");
        }

        // Verify target collection exists if specified
        if (dto.getTargetCollectionId() != null) {
            DataCollection targetCollection = collectionMapper.selectById(dto.getTargetCollectionId());
            if (targetCollection == null) {
                throw new IllegalArgumentException("Target collection not found");
            }
        }

        // Validate transformation type
        if (dto.getTransformationType() == null) {
            throw new IllegalArgumentException("Transformation type is required");
        }

        if (!Arrays.asList("mapping", "filter", "aggregate", "export").contains(dto.getTransformationType())) {
            throw new IllegalArgumentException("Invalid transformation type. Must be one of: mapping, filter, aggregate, export");
        }
    }

    private EtlTransformationDto toDto(EtlTransformation entity) {
        EtlTransformationDto dto = new EtlTransformationDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setSourceCollectionId(entity.getSourceCollectionId());
        dto.setTargetCollectionId(entity.getTargetCollectionId());
        dto.setTransformationType(entity.getTransformationType());
        dto.setTransformationRules(entity.getTransformationRules());
        dto.setStatus(entity.getStatus());
        dto.setScheduleExpression(entity.getScheduleExpression());
        dto.setTotalExecutions(entity.getTotalExecutions());
        dto.setSuccessExecutions(entity.getSuccessExecutions());
        dto.setFailureExecutions(entity.getFailureExecutions());

        if (entity.getLastExecutedAt() != null) {
            dto.setLastExecutedAt(entity.getLastExecutedAt().toString());
        }
        if (entity.getNextExecutedAt() != null) {
            dto.setNextExecutedAt(entity.getNextExecutedAt().toString());
        }

        return dto;
    }

    private EtlTransformation toEntity(EtlTransformationDto dto) {
        EtlTransformation entity = new EtlTransformation();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setSourceCollectionId(dto.getSourceCollectionId());
        entity.setTargetCollectionId(dto.getTargetCollectionId());
        entity.setTransformationType(dto.getTransformationType());
        entity.setTransformationRules(dto.getTransformationRules());
        entity.setStatus(dto.getStatus());
        entity.setScheduleExpression(dto.getScheduleExpression());
        return entity;
    }

    private EtlExecutionDto toExecutionDto(EtlExecution entity, String transformationName) {
        EtlExecutionDto dto = new EtlExecutionDto();
        dto.setId(entity.getId());
        dto.setTransformationId(entity.getTransformationId());
        dto.setTransformationName(transformationName);
        dto.setStatus(entity.getStatus());
        dto.setRecordsProcessed(entity.getRecordsProcessed());
        dto.setRecordsSuccess(entity.getRecordsSuccess());
        dto.setRecordsFailed(entity.getRecordsFailed());
        dto.setStartedAt(entity.getStartedAt());
        dto.setCompletedAt(entity.getCompletedAt());
        dto.setDurationMs(entity.getDurationMs());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setTriggeredBy(entity.getTriggeredBy());
        return dto;
    }

    private static class ExecutionResult {
        private long processed;
        private long success;
        private long failed;
        private String log;

        public long getProcessed() {
            return processed;
        }

        public void setProcessed(long processed) {
            this.processed = processed;
        }

        public long getSuccess() {
            return success;
        }

        public void setSuccess(long success) {
            this.success = success;
        }

        public long getFailed() {
            return failed;
        }

        public void setFailed(long failed) {
            this.failed = failed;
        }

        public String getLog() {
            return log;
        }

        public void setLog(String log) {
            this.log = log;
        }
    }
}
