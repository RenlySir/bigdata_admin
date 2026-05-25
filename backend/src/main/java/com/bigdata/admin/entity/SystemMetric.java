package com.bigdata.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * System Metric Entity
 * Stores system monitoring metrics
 */
@TableName("system_metric")
public class SystemMetric extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Metric name: cpu_usage, memory_usage, disk_usage, request_count, etc.
     */
    private String metricName;

    /**
     * Metric value
     */
    private Double metricValue;

    /**
     * Metric unit: %, MB, GB, count, etc.
     */
    private String metricUnit;

    /**
     * Source: system, application, database
     */
    private String metricSource;

    /**
     * Tags for additional metadata (JSON format)
     */
    private String tags;

    /**
     * Recorded at
     */
    private LocalDateTime recordedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Double getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(Double metricValue) {
        this.metricValue = metricValue;
    }

    public String getMetricUnit() {
        return metricUnit;
    }

    public void setMetricUnit(String metricUnit) {
        this.metricUnit = metricUnit;
    }

    public String getMetricSource() {
        return metricSource;
    }

    public void setMetricSource(String metricSource) {
        this.metricSource = metricSource;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
