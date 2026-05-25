package com.bigdata.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bigdata.admin.entity.AlertHistory;
import com.bigdata.admin.entity.AlertRule;
import com.bigdata.admin.entity.SystemMetric;
import com.bigdata.admin.mapper.AlertHistoryMapper;
import com.bigdata.admin.mapper.AlertRuleMapper;
import com.bigdata.admin.mapper.SystemMetricMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Monitoring Service
 * Collects system metrics and evaluates alert rules
 */
@Service
public class MonitoringService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringService.class);

    private final SystemMetricMapper metricMapper;
    private final AlertRuleMapper alertRuleMapper;
    private final AlertHistoryMapper alertHistoryMapper;

    private static final int METRIC_RETENTION_DAYS = 30;

    public MonitoringService(SystemMetricMapper metricMapper,
                              AlertRuleMapper alertRuleMapper,
                              AlertHistoryMapper alertHistoryMapper) {
        this.metricMapper = metricMapper;
        this.alertRuleMapper = alertRuleMapper;
        this.alertHistoryMapper = alertHistoryMapper;
    }

    /**
     * Collect system metrics (scheduled every minute)
     */
    @Scheduled(fixedRate = 60000)
    public void collectSystemMetrics() {
        try {
            List<SystemMetric> metrics = new ArrayList<>();

            // CPU Usage (approximate)
            double cpuUsage = getCpuUsage();
            metrics.add(createMetric("cpu_usage", cpuUsage, "%", "system"));

            // Memory Usage
            double memoryUsage = getMemoryUsage();
            metrics.add(createMetric("memory_usage", memoryUsage, "%", "system"));

            // JVM Memory
            double jvmMemoryUsage = getJvmMemoryUsage();
            metrics.add(createMetric("jvm_memory_usage", jvmMemoryUsage, "%", "application"));

            // Disk Usage
            double diskUsage = getDiskUsage();
            metrics.add(createMetric("disk_usage", diskUsage, "%", "system"));

            // Thread Count
            int threadCount = getThreadCount();
            metrics.add(createMetric("thread_count", (double) threadCount, "count", "application"));

            // Save metrics
            for (SystemMetric metric : metrics) {
                metricMapper.insert(metric);
            }

            log.debug("Collected {} system metrics", metrics.size());

            // Evaluate alert rules
            evaluateAlertRules(metrics);

        } catch (Exception e) {
            log.error("Error collecting system metrics", e);
        }
    }

    /**
     * Get metrics history
     */
    public Page<SystemMetric> getMetrics(String metricName, String source, int page, int size) {
        Page<SystemMetric> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SystemMetric> wrapper = new LambdaQueryWrapper<>();

        if (metricName != null && !metricName.isEmpty()) {
            wrapper.eq(SystemMetric::getMetricName, metricName);
        }

        if (source != null && !source.isEmpty()) {
            wrapper.eq(SystemMetric::getMetricSource, source);
        }

        wrapper.orderByDesc(SystemMetric::getRecordedAt);
        return metricMapper.selectPage(pageParam, wrapper);
    }

    /**
     * Get current metrics summary
     */
    public List<SystemMetric> getCurrentMetrics() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

        LambdaQueryWrapper<SystemMetric> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(SystemMetric::getRecordedAt, fiveMinutesAgo);
        wrapper.orderByDesc(SystemMetric::getRecordedAt);
        wrapper.last("LIMIT 100");

        return metricMapper.selectList(wrapper);
    }

    /**
     * Get alert rules
     */
    public Page<AlertRule> getAlertRules(int page, int size) {
        Page<AlertRule> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AlertRule::getCreatedAt);
        return alertRuleMapper.selectPage(pageParam, wrapper);
    }

    /**
     * Get active alert rules
     */
    public List<AlertRule> getActiveAlertRules() {
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRule::getStatus, "active");
        return alertRuleMapper.selectList(wrapper);
    }

    /**
     * Create alert rule
     */
    @Transactional
    public AlertRule createAlertRule(AlertRule rule) {
        rule.setStatus("active");
        rule.setTotalTriggerCount(0);
        alertRuleMapper.insert(rule);
        log.info("Created alert rule: {}", rule.getName());
        return rule;
    }

    /**
     * Update alert rule
     */
    @Transactional
    public AlertRule updateAlertRule(Long id, AlertRule rule) {
        rule.setId(id);
        alertRuleMapper.updateById(rule);
        log.info("Updated alert rule: {}", id);
        return rule;
    }

    /**
     * Delete alert rule
     */
    @Transactional
    public void deleteAlertRule(Long id) {
        alertRuleMapper.deleteById(id);
        log.info("Deleted alert rule: {}", id);
    }

    /**
     * Get alert history
     */
    public Page<AlertHistory> getAlertHistory(Long ruleId, String status, int page, int size) {
        Page<AlertHistory> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AlertHistory> wrapper = new LambdaQueryWrapper<>();

        if (ruleId != null) {
            wrapper.eq(AlertHistory::getRuleId, ruleId);
        }

        if (status != null && !status.isEmpty()) {
            wrapper.eq(AlertHistory::getStatus, status);
        }

        wrapper.orderByDesc(AlertHistory::getTriggeredAt);
        return alertHistoryMapper.selectPage(pageParam, wrapper);
    }

    /**
     * Get active alerts (not resolved or acknowledged)
     */
    public List<AlertHistory> getActiveAlerts() {
        LambdaQueryWrapper<AlertHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertHistory::getStatus, "triggered");
        wrapper.orderByDesc(AlertHistory::getTriggeredAt);
        return alertHistoryMapper.selectList(wrapper);
    }

    /**
     * Acknowledge alert
     */
    @Transactional
    public void acknowledgeAlert(Long alertId, Long userId) {
        AlertHistory alert = alertHistoryMapper.selectById(alertId);
        if (alert != null && "triggered".equals(alert.getStatus())) {
            alert.setStatus("acknowledged");
            alert.setAcknowledgedBy(userId);
            alert.setAcknowledgedAt(LocalDateTime.now());
            alertHistoryMapper.updateById(alert);
            log.info("Acknowledged alert: {}", alertId);
        }
    }

    /**
     * Resolve alert
     */
    @Transactional
    public void resolveAlert(Long alertId) {
        AlertHistory alert = alertHistoryMapper.selectById(alertId);
        if (alert != null) {
            alert.setStatus("resolved");
            alert.setResolvedAt(LocalDateTime.now());
            alertHistoryMapper.updateById(alert);
            log.info("Resolved alert: {}", alertId);
        }
    }

    /**
     * Evaluate alert rules against current metrics
     */
    private void evaluateAlertRules(List<SystemMetric> currentMetrics) {
        List<AlertRule> activeRules = getActiveAlertRules();

        for (AlertRule rule : activeRules) {
            try {
                // Find matching metric
                SystemMetric matchingMetric = currentMetrics.stream()
                        .filter(m -> m.getMetricName().equals(rule.getMetricName()))
                        .findFirst()
                        .orElse(null);

                if (matchingMetric == null) {
                    continue;
                }

                // Check if condition is met
                boolean triggered = checkCondition(matchingMetric.getMetricValue(), rule.getCondition(), rule.getThreshold());

                if (triggered) {
                    // Check cooldown
                    if (rule.getLastTriggeredAt() != null) {
                        LocalDateTime cooldownEnd = rule.getLastTriggeredAt().plusMinutes(rule.getCooldownMinutes() != null ? rule.getCooldownMinutes() : 5);
                        if (LocalDateTime.now().isBefore(cooldownEnd)) {
                            continue; // Still in cooldown period
                        }
                    }

                    // Trigger alert
                    triggerAlert(rule, matchingMetric);
                }

            } catch (Exception e) {
                log.error("Error evaluating alert rule: {}", rule.getId(), e);
            }
        }
    }

    /**
     * Check if metric value meets alert condition
     */
    private boolean checkCondition(Double value, String condition, Double threshold) {
        if (value == null || threshold == null) {
            return false;
        }

        return switch (condition) {
            case "gt" -> value > threshold;
            case "lt" -> value < threshold;
            case "eq" -> Math.abs(value - threshold) < 0.001;
            case "gte" -> value >= threshold;
            case "lte" -> value <= threshold;
            default -> false;
        };
    }

    /**
     * Trigger alert
     */
    @Transactional
    private void triggerAlert(AlertRule rule, SystemMetric metric) {
        // Create alert history
        AlertHistory alert = new AlertHistory();
        alert.setRuleId(rule.getId());
        alert.setRuleName(rule.getName());
        alert.setSeverity(rule.getSeverity());
        alert.setMetricName(metric.getMetricName());
        alert.setMetricValue(metric.getMetricValue());
        alert.setThreshold(rule.getThreshold());
        alert.setMessage(String.format("Alert %s: %s is %.2f%s (threshold: %.2f%s)",
                rule.getName(),
                metric.getMetricName(),
                metric.getMetricValue(),
                metric.getMetricUnit(),
                rule.getThreshold(),
                metric.getMetricUnit()));
        alert.setStatus("triggered");
        alert.setTriggeredAt(LocalDateTime.now());
        alert.setNotificationSent(false);
        alert.setNotificationChannels(rule.getNotificationChannels());

        alertHistoryMapper.insert(alert);

        // Update rule stats
        rule.setLastTriggeredAt(LocalDateTime.now());
        rule.setTotalTriggerCount(rule.getTotalTriggerCount() + 1);
        alertRuleMapper.updateById(rule);

        log.warn("Alert triggered: {} - {}", rule.getName(), alert.getMessage());

        // Send notifications (to be implemented)
        // sendNotifications(alert);
    }

    /**
     * Clean up old metrics (scheduled daily)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldMetrics() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(METRIC_RETENTION_DAYS);

        LambdaQueryWrapper<SystemMetric> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(SystemMetric::getRecordedAt, cutoffDate);

        int deleted = metricMapper.delete(wrapper);
        log.info("Cleaned up {} old metrics (older than {} days)", deleted, METRIC_RETENTION_DAYS);
    }

    private SystemMetric createMetric(String name, double value, String unit, String source) {
        SystemMetric metric = new SystemMetric();
        metric.setMetricName(name);
        metric.setMetricValue(value);
        metric.setMetricUnit(unit);
        metric.setMetricSource(source);
        metric.setRecordedAt(LocalDateTime.now());
        return metric;
    }

    private double getCpuUsage() {
        // Approximate CPU usage using operating system bean
        try {
            com.sun.management.OperatingSystemMXBean osBean =
                    (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return osBean.getCpuLoad() * 100;
        } catch (Exception e) {
            return 0;
        }
    }

    private double getMemoryUsage() {
        try {
            com.sun.management.OperatingSystemMXBean osBean =
                    (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            long total = osBean.getTotalMemorySize();
            long free = osBean.getFreeMemorySize();
            return ((double) (total - free) / total) * 100;
        } catch (Exception e) {
            return 0;
        }
    }

    private double getJvmMemoryUsage() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long max = runtime.maxMemory();
            long used = runtime.totalMemory() - runtime.freeMemory();
            return ((double) used / max) * 100;
        } catch (Exception e) {
            return 0;
        }
    }

    private double getDiskUsage() {
        try {
            java.io.File root = new java.io.File("/");
            long total = root.getTotalSpace();
            long free = root.getFreeSpace();
            return ((double) (total - free) / total) * 100;
        } catch (Exception e) {
            return 0;
        }
    }

    private int getThreadCount() {
        try {
            return ManagementFactory.getThreadMXBean().getThreadCount();
        } catch (Exception e) {
            return 0;
        }
    }
}
