package com.bigdata.admin.service;

import com.bigdata.admin.entity.AlertHistory;
import com.bigdata.admin.entity.AlertRule;
import com.bigdata.admin.entity.SystemMetric;
import com.bigdata.admin.mapper.AlertHistoryMapper;
import com.bigdata.admin.mapper.AlertRuleMapper;
import com.bigdata.admin.mapper.SystemMetricMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MonitoringService
 */
@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

    @Mock
    private SystemMetricMapper metricMapper;

    @Mock
    private AlertRuleMapper alertRuleMapper;

    @Mock
    private AlertHistoryMapper alertHistoryMapper;

    @InjectMocks
    private MonitoringService monitoringService;

    private SystemMetric testMetric;
    private AlertRule testAlertRule;
    private AlertHistory testAlertHistory;

    @BeforeEach
    void setUp() {
        testMetric = new SystemMetric();
        testMetric.setId(1L);
        testMetric.setMetricName("cpu_usage");
        testMetric.setMetricValue(85.5);
        testMetric.setMetricUnit("%");
        testMetric.setMetricSource("system");
        testMetric.setRecordedAt(LocalDateTime.now());

        testAlertRule = new AlertRule();
        testAlertRule.setId(1L);
        testAlertRule.setName("CPU 告警");
        testAlertRule.setMetricName("cpu_usage");
        testAlertRule.setCondition("gt");
        testAlertRule.setThreshold(80.0);
        testAlertRule.setSeverity("warning");
        testAlertRule.setStatus("active");
        testAlertRule.setTotalTriggerCount(0);
        testAlertRule.setCooldownMinutes(5);

        testAlertHistory = new AlertHistory();
        testAlertHistory.setId(1L);
        testAlertHistory.setRuleId(1L);
        testAlertHistory.setRuleName("CPU 告警");
        testAlertHistory.setSeverity("warning");
        testAlertHistory.setStatus("triggered");
        testAlertHistory.setTriggeredAt(LocalDateTime.now());
    }

    @Test
    void getMetrics_WhenValidParams_ShouldReturnPage() {
        Page<SystemMetric> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testMetric));
        mockPage.setTotal(1);

        when(metricMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        Page<SystemMetric> result = monitoringService.getMetrics("cpu_usage", "system", 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("cpu_usage", result.getRecords().get(0).getMetricName());
    }

    @Test
    void getCurrentMetrics_ShouldReturnRecentMetrics() {
        when(metricMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testMetric));

        List<SystemMetric> result = monitoringService.getCurrentMetrics();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAlertRules_WhenValidParams_ShouldReturnPage() {
        Page<AlertRule> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testAlertRule));
        mockPage.setTotal(1);

        when(alertRuleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        Page<AlertRule> result = monitoringService.getAlertRules(1, 10);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("CPU 告警", result.getRecords().get(0).getName());
    }

    @Test
    void getActiveAlertRules_ShouldReturnActiveRulesOnly() {
        when(alertRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testAlertRule));

        List<AlertRule> result = monitoringService.getActiveAlertRules();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("active", result.get(0).getStatus());
    }

    @Test
    void createAlertRule_WhenValidData_ShouldSucceed() {
        when(alertRuleMapper.insert(any(AlertRule.class))).thenReturn(1);

        AlertRule rule = new AlertRule();
        rule.setName("新告警规则");
        rule.setMetricName("memory_usage");
        rule.setCondition("gt");
        rule.setThreshold(90.0);
        rule.setSeverity("critical");

        AlertRule result = monitoringService.createAlertRule(rule);

        assertNotNull(result);
        assertEquals("新告警规则", result.getName());
        assertEquals("active", result.getStatus());
        assertEquals(0, result.getTotalTriggerCount());
    }

    @Test
    void updateAlertRule_WhenValidId_ShouldSucceed() {
        when(alertRuleMapper.updateById(any(AlertRule.class))).thenReturn(1);

        AlertRule rule = new AlertRule();
        rule.setName("更新告警规则");
        rule.setMetricName("memory_usage");
        rule.setCondition("gt");
        rule.setThreshold(95.0);

        AlertRule result = monitoringService.updateAlertRule(1L, rule);

        assertNotNull(result);
        assertEquals("更新告警规则", result.getName());
    }

    @Test
    void deleteAlertRule_WhenValidId_ShouldSucceed() {
        when(alertRuleMapper.deleteById(1L)).thenReturn(1);

        monitoringService.deleteAlertRule(1L);

        verify(alertRuleMapper).deleteById(1L);
    }

    @Test
    void getAlertHistory_WhenValidParams_ShouldReturnPage() {
        Page<AlertHistory> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testAlertHistory));
        mockPage.setTotal(1);

        when(alertHistoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        Page<AlertHistory> result = monitoringService.getAlertHistory(1L, "triggered", 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void getActiveAlerts_ShouldReturnTriggeredAlertsOnly() {
        when(alertHistoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testAlertHistory));

        List<AlertHistory> result = monitoringService.getActiveAlerts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("triggered", result.get(0).getStatus());
    }

    @Test
    void acknowledgeAlert_WhenValidId_ShouldUpdateStatus() {
        when(alertHistoryMapper.selectById(1L)).thenReturn(testAlertHistory);
        when(alertHistoryMapper.updateById(any(AlertHistory.class))).thenReturn(1);

        monitoringService.acknowledgeAlert(1L, 100L);

        verify(alertHistoryMapper).updateById(any(AlertHistory.class));
    }

    @Test
    void acknowledgeAlert_WhenAlreadyAcknowledged_ShouldNotUpdate() {
        testAlertHistory.setStatus("acknowledged");
        when(alertHistoryMapper.selectById(1L)).thenReturn(testAlertHistory);

        monitoringService.acknowledgeAlert(1L, 100L);

        verify(alertHistoryMapper, never()).updateById(any(AlertHistory.class));
    }

    @Test
    void resolveAlert_WhenValidId_ShouldUpdateStatus() {
        when(alertHistoryMapper.selectById(1L)).thenReturn(testAlertHistory);
        when(alertHistoryMapper.updateById(any(AlertHistory.class))).thenReturn(1);

        monitoringService.resolveAlert(1L);

        verify(alertHistoryMapper).updateById(any(AlertHistory.class));
    }
}
