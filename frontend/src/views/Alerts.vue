<template>
  <div class="alerts">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="title-section">
        <h2>系统监控与告警</h2>
        <p>实时监控系统指标，管理告警规则</p>
      </div>
      <div class="header-actions">
        <el-button @click="refreshMetrics">
          <el-icon><Refresh /></el-icon>
          刷新指标
        </el-button>
        <el-button type="primary" @click="openRuleDialog">
          <el-icon><Plus /></el-icon>
          创建告警规则
        </el-button>
      </div>
    </div>

    <!-- 系统指标 -->
    <el-row :gutter="20" class="metrics-section">
      <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <span class="metric-title">CPU 使用率</span>
            <el-icon class="metric-icon cpu"><Cpu /></el-icon>
          </div>
          <div class="metric-value">
            {{ currentMetrics.cpuUsage }}%
          </div>
          <el-progress
            :percentage="currentMetrics.cpuUsage"
            :color="getMetricColor(currentMetrics.cpuUsage)"
            :stroke-width="8"
            :show-text="false"
          />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <span class="metric-title">内存使用率</span>
            <el-icon class="metric-icon memory"><Memo /></el-icon>
          </div>
          <div class="metric-value">
            {{ currentMetrics.memoryUsage }}%
          </div>
          <el-progress
            :percentage="currentMetrics.memoryUsage"
            :color="getMetricColor(currentMetrics.memoryUsage)"
            :stroke-width="8"
            :show-text="false"
          />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <span class="metric-title">磁盘使用率</span>
            <el-icon class="metric-icon disk"><Coin /></el-icon>
          </div>
          <div class="metric-value">
            {{ currentMetrics.diskUsage }}%
          </div>
          <el-progress
            :percentage="currentMetrics.diskUsage"
            :color="getMetricColor(currentMetrics.diskUsage)"
            :stroke-width="8"
            :show-text="false"
          />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card class="metric-card">
          <div class="metric-header">
            <span class="metric-title">活跃线程</span>
            <el-icon class="metric-icon thread"><Operation /></el-icon>
          </div>
          <div class="metric-value">
            {{ currentMetrics.threadCount }}
          </div>
          <div class="metric-footer">
            JVM: {{ currentMetrics.jvmMemoryUsage }}%
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="content-tabs">
      <!-- 活跃告警 -->
      <el-tab-pane label="活跃告警" name="active">
        <el-card>
          <el-table :data="activeAlerts" v-loading="alertsLoading" stripe>
            <el-table-column prop="ruleName" label="告警规则" min-width="150" />
            <el-table-column prop="metricName" label="监控指标" width="120" />
            <el-table-column prop="severity" label="级别" width="80">
              <template #default="{ row }">
                <el-tag :type="getSeverityTagType(row.severity)" size="small">
                  {{ getSeverityLabel(row.severity) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="当前值 / 阈值" width="150">
              <template #default="{ row }">
                <span style="color: #f56c6c">{{ row.metricValue }}</span>
                <span> / </span>
                <span>{{ row.threshold }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="message" label="告警信息" min-width="200" show-overflow-tooltip />
            <el-table-column prop="triggeredAt" label="触发时间" width="160" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  text
                  size="small"
                  @click="handleAcknowledge(row)"
                  :disabled="row.status !== 'triggered'"
                >
                  确认
                </el-button>
                <el-button
                  type="success"
                  text
                  size="small"
                  @click="handleResolve(row)"
                >
                  解决
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!alertsLoading && activeAlerts.length === 0" description="暂无活跃告警" />
        </el-card>
      </el-tab-pane>

      <!-- 告警规则 -->
      <el-tab-pane label="告警规则" name="rules">
        <el-card>
          <el-table :data="alertRules" v-loading="rulesLoading" stripe>
            <el-table-column prop="name" label="规则名称" min-width="150" />
            <el-table-column prop="metricName" label="监控指标" width="120" />
            <el-table-column label="触发条件" width="150">
              <template #default="{ row }">
                {{ row.metricName }} {{ getConditionLabel(row.condition) }} {{ row.threshold }}
              </template>
            </el-table-column>
            <el-table-column prop="severity" label="级别" width="80">
              <template #default="{ row }">
                <el-tag :type="getSeverityTagType(row.severity)" size="small">
                  {{ getSeverityLabel(row.severity) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-switch
                  v-model="row.status"
                  active-value="active"
                  inactive-value="disabled"
                  @change="handleToggleRule(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="统计" width="120">
              <template #default="{ row }">
                触发: {{ row.totalTriggerCount || 0 }}次
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button text size="small" @click="handleEditRule(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-popconfirm
                  title="确定删除此规则吗？"
                  @confirm="handleDeleteRule(row)"
                >
                  <template #reference>
                    <el-button type="danger" text size="small">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 告警历史 -->
      <el-tab-pane label="告警历史" name="history">
        <el-card>
          <el-form :inline="true" class="history-filters">
            <el-form-item label="规则">
              <el-select v-model="historyFilters.ruleId" placeholder="全部规则" clearable>
                <el-option
                  v-for="rule in alertRules"
                  :key="rule.id"
                  :label="rule.name"
                  :value="rule.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="historyFilters.status" placeholder="全部状态" clearable>
                <el-option label="已触发" value="triggered" />
                <el-option label="已确认" value="acknowledged" />
                <el-option label="已解决" value="resolved" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadAlertHistory">查询</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="alertHistory" v-loading="historyLoading" stripe>
            <el-table-column prop="ruleName" label="告警规则" width="150" />
            <el-table-column prop="severity" label="级别" width="80">
              <template #default="{ row }">
                <el-tag :type="getSeverityTagType(row.severity)" size="small">
                  {{ getSeverityLabel(row.severity) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getHistoryStatusTagType(row.status)" size="small">
                  {{ getHistoryStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="metricName" label="指标" width="100" />
            <el-table-column prop="metricValue" label="当前值" width="80" />
            <el-table-column prop="threshold" label="阈值" width="80" />
            <el-table-column prop="message" label="告警信息" min-width="200" show-overflow-tooltip />
            <el-table-column prop="triggeredAt" label="触发时间" width="160" />
            <el-table-column prop="resolvedAt" label="解决时间" width="160">
              <template #default="{ row }">
                {{ row.resolvedAt || '-' }}
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="historyPagination.page"
              v-model:page-size="historyPagination.size"
              :total="historyPagination.total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadAlertHistory"
              @current-change="loadAlertHistory"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建/编辑规则对话框 -->
    <el-dialog
      v-model="ruleDialogVisible"
      :title="isEditRule ? '编辑告警规则' : '创建告警规则'"
      width="600px"
      @close="handleRuleDialogClose"
    >
      <el-form :model="ruleForm" :rules="ruleFormRules" ref="ruleFormRef" label-width="120px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="ruleForm.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则描述">
          <el-input
            v-model="ruleForm.description"
            type="textarea"
            :rows="2"
            placeholder="请输入规则描述"
          />
        </el-form-item>
        <el-form-item label="监控指标" prop="metricName">
          <el-select v-model="ruleForm.metricName" placeholder="请选择监控指标">
            <el-option label="CPU 使用率" value="cpu_usage" />
            <el-option label="内存使用率" value="memory_usage" />
            <el-option label="磁盘使用率" value="disk_usage" />
            <el-option label="JVM 内存使用率" value="jvm_memory_usage" />
            <el-option label="线程数" value="thread_count" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发条件" prop="condition">
          <el-select v-model="ruleForm.condition" placeholder="请选择条件">
            <el-option label="大于" value="gt" />
            <el-option label="小于" value="lt" />
            <el-option label="等于" value="eq" />
            <el-option label="大于等于" value="gte" />
            <el-option label="小于等于" value="lte" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值" prop="threshold">
          <el-input-number
            v-model="ruleForm.threshold"
            :min="0"
            :max="100"
            :precision="2"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="告警级别" prop="severity">
          <el-radio-group v-model="ruleForm.severity">
            <el-radio label="info">信息</el-radio>
            <el-radio label="warning">警告</el-radio>
            <el-radio label="critical">严重</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="持续时间(分钟)">
          <el-input-number
            v-model="ruleForm.durationMinutes"
            :min="1"
            :max="60"
            controls-position="right"
          />
          <span class="form-tip">条件持续满足该时间后触发告警</span>
        </el-form-item>
        <el-form-item label="冷却时间(分钟)">
          <el-input-number
            v-model="ruleForm.cooldownMinutes"
            :min="1"
            :max="60"
            controls-position="right"
          />
          <span class="form-tip">同一告警的最小间隔时间</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRule" :loading="submittingRule">
          {{ isEditRule ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiMonitoring } from '@/api'

// 数据
const activeTab = ref('active')
const alertsLoading = ref(false)
const rulesLoading = ref(false)
const historyLoading = ref(false)

const currentMetrics = ref({
  cpuUsage: 0,
  memoryUsage: 0,
  diskUsage: 0,
  jvmMemoryUsage: 0,
  threadCount: 0
})

const activeAlerts = ref([])
const alertRules = ref([])
const alertHistory = ref([])

const historyFilters = ref({
  ruleId: null,
  status: null
})

const historyPagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 规则对话框
const ruleDialogVisible = ref(false)
const isEditRule = ref(false)
const submittingRule = ref(false)
const ruleFormRef = ref(null)
const ruleForm = ref({
  name: '',
  description: '',
  metricName: '',
  condition: 'gt',
  threshold: 80,
  severity: 'warning',
  durationMinutes: 5,
  cooldownMinutes: 5
})

const ruleFormRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  metricName: [{ required: true, message: '请选择监控指标', trigger: 'change' }],
  condition: [{ required: true, message: '请选择触发条件', trigger: 'change' }],
  threshold: [{ required: true, message: '请输入阈值', trigger: 'blur' }],
  severity: [{ required: true, message: '请选择告警级别', trigger: 'change' }]
}

// 自动刷新定时器
let refreshTimer = null

// 方法
const loadCurrentMetrics = async () => {
  try {
    const res = await apiMonitoring.getCurrentMetrics()
    if (res.data) {
      const metrics = res.data
      // 获取最新的指标值
      metrics.forEach(metric => {
        const value = Math.round(metric.metricValue)
        switch (metric.metricName) {
          case 'cpu_usage':
            currentMetrics.value.cpuUsage = value
            break
          case 'memory_usage':
            currentMetrics.value.memoryUsage = value
            break
          case 'disk_usage':
            currentMetrics.value.diskUsage = value
            break
          case 'jvm_memory_usage':
            currentMetrics.value.jvmMemoryUsage = value
            break
          case 'thread_count':
            currentMetrics.value.threadCount = Math.round(metric.metricValue)
            break
        }
      })
    }
  } catch (error) {
    console.error('Failed to load metrics:', error)
  }
}

const loadActiveAlerts = async () => {
  try {
    alertsLoading.value = true
    const res = await apiMonitoring.getActiveAlerts()
    if (res.data) {
      activeAlerts.value = res.data
    }
  } catch (error) {
    console.error('Failed to load active alerts:', error)
  } finally {
    alertsLoading.value = false
  }
}

const loadAlertRules = async () => {
  try {
    rulesLoading.value = true
    const res = await apiMonitoring.getAlertRules({ page: 1, size: 100 })
    if (res.data) {
      alertRules.value = res.data.records || []
    }
  } catch (error) {
    console.error('Failed to load alert rules:', error)
  } finally {
    rulesLoading.value = false
  }
}

const loadAlertHistory = async () => {
  try {
    historyLoading.value = true
    const params = {
      ruleId: historyFilters.value.ruleId,
      status: historyFilters.value.status,
      page: historyPagination.value.page,
      size: historyPagination.value.size
    }
    const res = await apiMonitoring.getAlertHistory(params)
    if (res.data) {
      alertHistory.value = res.data.records || []
      historyPagination.value.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载告警历史失败')
    console.error(error)
  } finally {
    historyLoading.value = false
  }
}

const refreshMetrics = () => {
  loadCurrentMetrics()
  loadActiveAlerts()
  ElMessage.success('指标已刷新')
}

const handleAcknowledge = async (alert) => {
  try {
    await apiMonitoring.acknowledgeAlert(alert.id, 1) // 假设用户ID为1
    ElMessage.success('告警已确认')
    loadActiveAlerts()
    loadAlertHistory()
  } catch (error) {
    ElMessage.error('确认告警失败')
    console.error(error)
  }
}

const handleResolve = async (alert) => {
  try {
    await apiMonitoring.resolveAlert(alert.id)
    ElMessage.success('告警已解决')
    loadActiveAlerts()
    loadAlertHistory()
  } catch (error) {
    ElMessage.error('解决告警失败')
    console.error(error)
  }
}

const handleToggleRule = async (rule) => {
  try {
    const newStatus = rule.status === 'active' ? 'disabled' : 'active'
    await apiMonitoring.updateAlertRule(rule.id, { ...rule, status: newStatus })
    ElMessage.success('规则状态已更新')
    loadAlertRules()
  } catch (error) {
    ElMessage.error('更新规则状态失败')
    console.error(error)
  }
}

const openRuleDialog = () => {
  isEditRule.value = false
  ruleForm.value = {
    name: '',
    description: '',
    metricName: '',
    condition: 'gt',
    threshold: 80,
    severity: 'warning',
    durationMinutes: 5,
    cooldownMinutes: 5
  }
  ruleDialogVisible.value = true
}

const handleEditRule = (row) => {
  isEditRule.value = true
  ruleForm.value = { ...row }
  ruleDialogVisible.value = true
}

const handleRuleDialogClose = () => {
  ruleFormRef.value?.resetFields()
}

const handleSubmitRule = async () => {
  try {
    await ruleFormRef.value.validate()
    submittingRule.value = true

    if (isEditRule.value) {
      await apiMonitoring.updateAlertRule(ruleForm.value.id, ruleForm.value)
      ElMessage.success('更新成功')
    } else {
      await apiMonitoring.createAlertRule(ruleForm.value)
      ElMessage.success('创建成功')
    }

    ruleDialogVisible.value = false
    loadAlertRules()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(isEditRule.value ? '更新失败' : '创建失败')
      console.error(error)
    }
  } finally {
    submittingRule.value = false
  }
}

const handleDeleteRule = async (row) => {
  try {
    await apiMonitoring.deleteAlertRule(row.id)
    ElMessage.success('删除成功')
    loadAlertRules()
  } catch (error) {
    ElMessage.error('删除失败')
    console.error(error)
  }
}

// 辅助方法
const getMetricColor = (value) => {
  if (value >= 90) return '#f56c6c'
  if (value >= 70) return '#e6a23c'
  if (value >= 50) return '#409eff'
  return '#67c23a'
}

const getSeverityLabel = (severity) => {
  const map = {
    info: '信息',
    warning: '警告',
    critical: '严重'
  }
  return map[severity] || severity
}

const getSeverityTagType = (severity) => {
  const map = {
    info: 'info',
    warning: 'warning',
    critical: 'danger'
  }
  return map[severity] || ''
}

const getConditionLabel = (condition) => {
  const map = {
    gt: '>',
    lt: '<',
    eq: '=',
    gte: '≥',
    lte: '≤'
  }
  return map[condition] || condition
}

const getHistoryStatusLabel = (status) => {
  const map = {
    triggered: '已触发',
    acknowledged: '已确认',
    resolved: '已解决'
  }
  return map[status] || status
}

const getHistoryStatusTagType = (status) => {
  const map = {
    triggered: 'danger',
    acknowledged: 'warning',
    resolved: 'success'
  }
  return map[status] || ''
}

// 初始化
onMounted(() => {
  loadCurrentMetrics()
  loadActiveAlerts()
  loadAlertRules()
  loadAlertHistory()

  // 每30秒自动刷新指标
  refreshTimer = setInterval(() => {
    loadCurrentMetrics()
    if (activeTab.value === 'active') {
      loadActiveAlerts()
    }
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.alerts {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.title-section h2 {
  margin: 0 0 5px 0;
  font-size: 24px;
  color: #303133;
}

.title-section p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.metrics-section {
  margin-bottom: 20px;
}

.metric-card {
  margin-bottom: 20px;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.metric-title {
  font-size: 14px;
  color: #606266;
}

.metric-icon {
  font-size: 20px;
  opacity: 0.6;
}

.metric-icon.cpu { color: #409eff; }
.metric-icon.memory { color: #67c23a; }
.metric-icon.disk { color: #e6a23c; }
.metric-icon.thread { color: #f56c6c; }

.metric-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.metric-footer {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}

.content-tabs {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}

.history-filters {
  margin-bottom: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
  }

  .header-actions .el-button {
    flex: 1;
  }
}
</style>
