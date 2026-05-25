package com.bigdata.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * ETL Execution Entity
 * Tracks execution history of ETL transformations
 */
@TableName("etl_execution")
public class EtlExecution extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Transformation ID
     */
    private Long transformationId;

    /**
     * Execution status: running, completed, failed, cancelled
     */
    private String status;

    /**
     * Records processed count
     */
    private Long recordsProcessed;

    /**
     * Records success count
     */
    private Long recordsSuccess;

    /**
     * Records failed count
     */
    private Long recordsFailed;

    /**
     * Started at
     */
    private LocalDateTime startedAt;

    /**
     * Completed at
     */
    private LocalDateTime completedAt;

    /**
     * Execution duration in milliseconds
     */
    private Long durationMs;

    /**
     * Error message if failed
     */
    private String errorMessage;

    /**
     * Execution log
     */
    private String executionLog;

    /**
     * Triggered by: system, manual, schedule
     */
    private String triggeredBy;

    /**
     * Triggered by user ID (for manual triggers)
     */
    private Long triggeredByUserId;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTransformationId() {
        return transformationId;
    }

    public void setTransformationId(Long transformationId) {
        this.transformationId = transformationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getRecordsProcessed() {
        return recordsProcessed;
    }

    public void setRecordsProcessed(Long recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    public Long getRecordsSuccess() {
        return recordsSuccess;
    }

    public void setRecordsSuccess(Long recordsSuccess) {
        this.recordsSuccess = recordsSuccess;
    }

    public Long getRecordsFailed() {
        return recordsFailed;
    }

    public void setRecordsFailed(Long recordsFailed) {
        this.recordsFailed = recordsFailed;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getExecutionLog() {
        return executionLog;
    }

    public void setExecutionLog(String executionLog) {
        this.executionLog = executionLog;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public Long getTriggeredByUserId() {
        return triggeredByUserId;
    }

    public void setTriggeredByUserId(Long triggeredByUserId) {
        this.triggeredByUserId = triggeredByUserId;
    }
}
