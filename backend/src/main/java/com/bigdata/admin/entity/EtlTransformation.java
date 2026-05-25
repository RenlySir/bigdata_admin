package com.bigdata.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * ETL Transformation Entity
 * Defines data transformation rules for ETL processes
 */
@TableName("etl_transformation")
public class EtlTransformation extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Transformation name
     */
    private String name;

    /**
     * Transformation description
     */
    private String description;

    /**
     * Source data collection ID
     */
    private Long sourceCollectionId;

    /**
     * Target data collection ID (null for export)
     */
    private Long targetCollectionId;

    /**
     * Transformation type: mapping, filter, aggregate, join, lookup, custom
     */
    private String transformationType;

    /**
     * Transformation rules in JSON format
     */
    private String transformationRules;

    /**
     * Transformation status: draft, active, paused, archived
     */
    private String status;

    /**
     * Schedule expression (cron)
     */
    private String scheduleExpression;

    /**
     * Last execution time
     */
    private LocalDateTime lastExecutedAt;

    /**
     * Next execution time
     */
    private LocalDateTime nextExecutedAt;

    /**
     * Total executions count
     */
    private Integer totalExecutions;

    /**
     * Success executions count
     */
    private Integer successExecutions;

    /**
     * Failure executions count
     */
    private Integer failureExecutions;

    /**
     * Created by user ID
     */
    private Long createdBy;

    /**
     * Updated by user ID
     */
    private Long updatedBy;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getSourceCollectionId() {
        return sourceCollectionId;
    }

    public void setSourceCollectionId(Long sourceCollectionId) {
        this.sourceCollectionId = sourceCollectionId;
    }

    public Long getTargetCollectionId() {
        return targetCollectionId;
    }

    public void setTargetCollectionId(Long targetCollectionId) {
        this.targetCollectionId = targetCollectionId;
    }

    public String getTransformationType() {
        return transformationType;
    }

    public void setTransformationType(String transformationType) {
        this.transformationType = transformationType;
    }

    public String getTransformationRules() {
        return transformationRules;
    }

    public void setTransformationRules(String transformationRules) {
        this.transformationRules = transformationRules;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScheduleExpression() {
        return scheduleExpression;
    }

    public void setScheduleExpression(String scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
    }

    public LocalDateTime getLastExecutedAt() {
        return lastExecutedAt;
    }

    public void setLastExecutedAt(LocalDateTime lastExecutedAt) {
        this.lastExecutedAt = lastExecutedAt;
    }

    public LocalDateTime getNextExecutedAt() {
        return nextExecutedAt;
    }

    public void setNextExecutedAt(LocalDateTime nextExecutedAt) {
        this.nextExecutedAt = nextExecutedAt;
    }

    public Integer getTotalExecutions() {
        return totalExecutions;
    }

    public void setTotalExecutions(Integer totalExecutions) {
        this.totalExecutions = totalExecutions;
    }

    public Integer getSuccessExecutions() {
        return successExecutions;
    }

    public void setSuccessExecutions(Integer successExecutions) {
        this.successExecutions = successExecutions;
    }

    public Integer getFailureExecutions() {
        return failureExecutions;
    }

    public void setFailureExecutions(Integer failureExecutions) {
        this.failureExecutions = failureExecutions;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
}
