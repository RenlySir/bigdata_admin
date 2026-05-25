package com.bigdata.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bigdata.admin.common.Result;
import com.bigdata.admin.entity.AlertHistory;
import com.bigdata.admin.entity.AlertRule;
import com.bigdata.admin.entity.SystemMetric;
import com.bigdata.admin.service.MonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Monitoring Controller
 * Provides REST API for system monitoring and alerting
 */
@RestController
@RequestMapping("/monitoring")
@Tag(name = "System Monitoring", description = "System metrics and alerting")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    /**
     * Get current metrics
     */
    @GetMapping("/metrics/current")
    @Operation(summary = "Get current metrics", description = "Get current system metrics")
    public Result<List<SystemMetric>> getCurrentMetrics() {
        List<SystemMetric> metrics = monitoringService.getCurrentMetrics();
        return Result.success(metrics);
    }

    /**
     * Get metrics history
     */
    @GetMapping("/metrics/history")
    @Operation(summary = "Get metrics history", description = "Get historical metrics data")
    public Result<Page<SystemMetric>> getMetricsHistory(
            @Parameter(description = "Metric name") @RequestParam(required = false) String metricName,
            @Parameter(description = "Source") @RequestParam(required = false) String source,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Page<SystemMetric> result = monitoringService.getMetrics(metricName, source, page, size);
        return Result.success(result);
    }

    /**
     * Get alert rules
     */
    @GetMapping("/alerts/rules")
    @Operation(summary = "Get alert rules", description = "Get all alert rules")
    public Result<Page<AlertRule>> getAlertRules(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Page<AlertRule> result = monitoringService.getAlertRules(page, size);
        return Result.success(result);
    }

    /**
     * Get active alert rules
     */
    @GetMapping("/alerts/rules/active")
    @Operation(summary = "Get active alert rules", description = "Get all active alert rules")
    public Result<List<AlertRule>> getActiveAlertRules() {
        List<AlertRule> result = monitoringService.getActiveAlertRules();
        return Result.success(result);
    }

    /**
     * Create alert rule
     */
    @PostMapping("/alerts/rules")
    @Operation(summary = "Create alert rule", description = "Create a new alert rule")
    public Result<AlertRule> createAlertRule(@RequestBody AlertRule rule) {
        AlertRule result = monitoringService.createAlertRule(rule);
        return Result.success(result);
    }

    /**
     * Update alert rule
     */
    @PutMapping("/alerts/rules/{id}")
    @Operation(summary = "Update alert rule", description = "Update an existing alert rule")
    public Result<AlertRule> updateAlertRule(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @RequestBody AlertRule rule) {
        AlertRule result = monitoringService.updateAlertRule(id, rule);
        return Result.success(result);
    }

    /**
     * Delete alert rule
     */
    @DeleteMapping("/alerts/rules/{id}")
    @Operation(summary = "Delete alert rule", description = "Delete an alert rule")
    public Result<Void> deleteAlertRule(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        monitoringService.deleteAlertRule(id);
        return Result.success();
    }

    /**
     * Get alert history
     */
    @GetMapping("/alerts/history")
    @Operation(summary = "Get alert history", description = "Get triggered alert history")
    public Result<Page<AlertHistory>> getAlertHistory(
            @Parameter(description = "Rule ID") @RequestParam(required = false) Long ruleId,
            @Parameter(description = "Status") @RequestParam(required = false) String status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Page<AlertHistory> result = monitoringService.getAlertHistory(ruleId, status, page, size);
        return Result.success(result);
    }

    /**
     * Get active alerts
     */
    @GetMapping("/alerts/active")
    @Operation(summary = "Get active alerts", description = "Get all active (triggered) alerts")
    public Result<List<AlertHistory>> getActiveAlerts() {
        List<AlertHistory> result = monitoringService.getActiveAlerts();
        return Result.success(result);
    }

    /**
     * Acknowledge alert
     */
    @PostMapping("/alerts/{id}/acknowledge")
    @Operation(summary = "Acknowledge alert", description = "Acknowledge an alert")
    public Result<Void> acknowledgeAlert(
            @Parameter(description = "Alert ID") @PathVariable Long id,
            @Parameter(description = "User ID") @RequestParam Long userId) {
        monitoringService.acknowledgeAlert(id, userId);
        return Result.success();
    }

    /**
     * Resolve alert
     */
    @PostMapping("/alerts/{id}/resolve")
    @Operation(summary = "Resolve alert", description = "Mark an alert as resolved")
    public Result<Void> resolveAlert(
            @Parameter(description = "Alert ID") @PathVariable Long id) {
        monitoringService.resolveAlert(id);
        return Result.success();
    }
}
