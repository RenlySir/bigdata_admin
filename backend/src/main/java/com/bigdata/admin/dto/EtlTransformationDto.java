package com.bigdata.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * ETL Transformation DTO
 */
public class EtlTransformationDto {

    private Long id;

    @NotBlank(message = "转换名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "源数据集合ID不能为空")
    private Long sourceCollectionId;

    private Long targetCollectionId;

    @NotBlank(message = "转换类型不能为空")
    private String transformationType;

    private String transformationRules;

    private String status;

    private String scheduleExpression;

    private String lastExecutedAt;

    private String nextExecutedAt;

    private Integer totalExecutions;

    private Integer successExecutions;

    private Integer failureExecutions;

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

    public String getLastExecutedAt() {
        return lastExecutedAt;
    }

    public void setLastExecutedAt(String lastExecutedAt) {
        this.lastExecutedAt = lastExecutedAt;
    }

    public String getNextExecutedAt() {
        return nextExecutedAt;
    }

    public void setNextExecutedAt(String nextExecutedAt) {
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
}
