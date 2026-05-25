package com.bigdata.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * Alert Rule Entity
 * Defines alert rules for monitoring
 */
@TableName("alert_rule")
public class AlertRule extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Rule name
     */
    private String name;

    /**
     * Rule description
     */
    private String description;

    /**
     * Metric name to monitor
     */
    private String metricName;

    /**
     * Condition: gt (greater than), lt (less than), eq (equals)
     */
    private String condition;

    /**
     * Threshold value
     */
    private Double threshold;

    /**
     * Duration in minutes (how long condition must be true)
     */
    private Integer durationMinutes;

    /**
     * Severity: info, warning, critical
     */
    private String severity;

    /**
     * Rule status: active, disabled
     */
    private String status;

    /**
     * Notification channels: email, sms, webhook
     */
    private String notificationChannels;

    /**
     * Notification recipients (JSON array of user IDs or emails)
     */
    private String recipients;

    /**
     * Last triggered at
     */
    private LocalDateTime lastTriggeredAt;

    /**
     * Total trigger count
     */
    private Integer totalTriggerCount;

    /**
     * Cooldown period in minutes (minimum time between alerts)
     */
    private Integer cooldownMinutes;

    /**
     * Created by user ID
     */
    private Long createdBy;

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

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotificationChannels() {
        return notificationChannels;
    }

    public void setNotificationChannels(String notificationChannels) {
        this.notificationChannels = notificationChannels;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public LocalDateTime getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }

    public Integer getTotalTriggerCount() {
        return totalTriggerCount;
    }

    public void setTotalTriggerCount(Integer totalTriggerCount) {
        this.totalTriggerCount = totalTriggerCount;
    }

    public Integer getCooldownMinutes() {
        return cooldownMinutes;
    }

    public void setCooldownMinutes(Integer cooldownMinutes) {
        this.cooldownMinutes = cooldownMinutes;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
