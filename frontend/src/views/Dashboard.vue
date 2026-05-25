<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" v-loading="loading.stats">
          <div class="stat-content">
            <div class="stat-icon data-sources">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.dataSources }}</div>
              <div class="stat-label">数据源</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" v-loading="loading.stats">
          <div class="stat-content">
            <div class="stat-icon collections">
              <el-icon><FolderOpened /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.collections }}</div>
              <div class="stat-label">数据集合</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" v-loading="loading.stats">
          <div class="stat-content">
            <div class="stat-icon records">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.records }}</div>
              <div class="stat-label">数据记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" v-loading="loading.stats">
          <div class="stat-content">
            <div class="stat-icon storage">
              <el-icon><Database /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatSize(stats.totalSize) }}</div>
              <div class="stat-label">存储容量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表和活动 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>数据类型分布</span>
              <el-button text @click="refreshChart">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-loading="loading.chart" style="height: 300px">
            <v-chart v-if="!loading.chart" :option="chartOption" style="height: 100%" />
            <el-empty v-else description="暂无数据" />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
              <el-button text @click="refreshActivities">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-loading="loading.activities" style="height: 300px; overflow-y: auto">
            <el-timeline v-if="activities.length > 0">
              <el-timeline-item
                v-for="item in activities"
                :key="item.id"
                :timestamp="item.timestamp"
                placement="top"
              >
                {{ item.content }}
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无活动" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统状态 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统状态</span>
              <el-button text @click="refreshSystemStatus">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <el-row :gutter="20" v-loading="loading.system">
            <el-col :xs="24" :sm="12" :md="6" :lg="6">
              <div class="system-metric">
                <div class="metric-label">CPU 使用率</div>
                <el-progress
                  :percentage="systemStatus.cpuUsage"
                  :color="getProgressColor(systemStatus.cpuUsage)"
                  :stroke-width="12"
                />
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6" :lg="6">
              <div class="system-metric">
                <div class="metric-label">内存使用率</div>
                <el-progress
                  :percentage="systemStatus.memoryUsage"
                  :color="getProgressColor(systemStatus.memoryUsage)"
                  :stroke-width="12"
                />
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6" :lg="6">
              <div class="system-metric">
                <div class="metric-label">磁盘使用率</div>
                <el-progress
                  :percentage="systemStatus.diskUsage"
                  :color="getProgressColor(systemStatus.diskUsage)"
                  :stroke-width="12"
                />
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6" :lg="6">
              <div class="system-metric">
                <div class="metric-label">活跃线程</div>
                <el-progress
                  :percentage="systemStatus.threadUsage"
                  :color="getProgressColor(systemStatus.threadUsage)"
                  :stroke-width="12"
                />
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <!-- 活跃告警 -->
    <el-row :gutter="20" style="margin-top: 20px" v-if="activeAlerts.length > 0">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span style="color: #f56c6c">
                <el-icon><Warning /></el-icon>
                活跃告警 ({{ activeAlerts.length }})
              </span>
              <el-button text @click="goToAlerts">查看全部</el-button>
            </div>
          </template>
          <el-table :data="activeAlerts" style="width: 100%">
            <el-table-column prop="ruleName" label="告警规则" width="180" />
            <el-table-column prop="metricName" label="指标" width="120" />
            <el-table-column prop="metricValue" label="当前值" width="100" />
            <el-table-column prop="threshold" label="阈值" width="100" />
            <el-table-column prop="severity" label="级别" width="100">
              <template #default="{ row }">
                <el-tag :type="getSeverityType(row.severity)" size="small">
                  {{ row.severity }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="triggeredAt" label="触发时间" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" text size="small" @click="acknowledgeAlert(row)">
                  确认
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { ElMessage } from 'element-plus'
import { apiCollection, apiMonitoring } from '@/api'

use([CanvasRenderer, PieChart, TitleComponent, TooltipComponent, LegendComponent])

const router = useRouter()

// 加载状态
const loading = ref({
  stats: true,
  chart: true,
  activities: true,
  system: true
})

// 统计数据
const stats = ref({
  dataSources: 0,
  collections: 0,
  records: 0,
  totalSize: 0
})

// 最近活动
const activities = ref([])

// 图表配置
const chartOption = ref({
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)'
  },
  legend: {
    bottom: '5%',
    left: 'center'
  },
  series: [
    {
      name: '数据类型',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      data: []
    }
  ]
})

// 系统状态
const systemStatus = ref({
  cpuUsage: 0,
  memoryUsage: 0,
  diskUsage: 0,
  threadUsage: 0
})

// 活跃告警
const activeAlerts = ref([])

// 刷新定时器
let refreshTimer = null

const formatSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const getProgressColor = (percentage) => {
  if (percentage >= 90) return '#f56c6c'
  if (percentage >= 70) return '#e6a23c'
  if (percentage >= 50) return '#409eff'
  return '#67c23a'
}

const getSeverityType = (severity) => {
  const map = {
    critical: 'danger',
    warning: 'warning',
    info: 'info'
  }
  return map[severity] || 'info'
}

const loadStats = async () => {
  try {
    loading.value.stats = true
    const res = await apiCollection.list({ page: 1, size: 1 })
    if (res.data) {
      stats.value.collections = res.data.total || 0
      stats.value.records = res.data.total || 0 // 估算
      stats.value.totalSize = res.data.total ? res.data.total * 1024 : 0 // 估算
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
  } finally {
    loading.value.stats = false
  }
}

const loadActivities = async () => {
  try {
    loading.value.activities = true
    // 模拟数据，实际应从 API 获取
    activities.value = [
      { id: 1, content: '创建数据集合"用户数据"', timestamp: '2025-01-15 10:30' },
      { id: 2, content: '导入1000条数据记录', timestamp: '2025-01-15 09:15' },
      { id: 3, content: '添加MySQL数据源', timestamp: '2025-01-14 16:20' },
      { id: 4, content: '系统初始化完成', timestamp: '2025-01-14 10:00' }
    ]
  } catch (error) {
    console.error('Failed to load activities:', error)
  } finally {
    loading.value.activities = false
  }
}

const loadChart = async () => {
  try {
    loading.value.chart = true
    // 模拟数据
    chartOption.value.series[0].data = [
      { value: 1048, name: 'JSON' },
      { value: 735, name: 'TEXT' },
      { value: 580, name: 'DOCUMENT' },
      { value: 484, name: 'BINARY' }
    ]
  } catch (error) {
    console.error('Failed to load chart:', error)
  } finally {
    loading.value.chart = false
  }
}

const loadSystemStatus = async () => {
  try {
    loading.value.system = true
    const res = await apiMonitoring.getCurrentMetrics()
    if (res.data) {
      const metrics = res.data
      metrics.forEach(metric => {
        switch (metric.metricName) {
          case 'cpu_usage':
            systemStatus.value.cpuUsage = Math.round(metric.metricValue)
            break
          case 'memory_usage':
            systemStatus.value.memoryUsage = Math.round(metric.metricValue)
            break
          case 'disk_usage':
            systemStatus.value.diskUsage = Math.round(metric.metricValue)
            break
          case 'jvm_memory_usage':
            systemStatus.value.threadUsage = Math.round(metric.metricValue)
            break
        }
      })
    }
  } catch (error) {
    console.error('Failed to load system status:', error)
  } finally {
    loading.value.system = false
  }
}

const loadActiveAlerts = async () => {
  try {
    const res = await apiMonitoring.getActiveAlerts()
    if (res.data) {
      activeAlerts.value = res.data
    }
  } catch (error) {
    console.error('Failed to load active alerts:', error)
  }
}

const refreshChart = () => {
  loadChart()
  ElMessage.success('图表已刷新')
}

const refreshActivities = () => {
  loadActivities()
  ElMessage.success('活动已刷新')
}

const refreshSystemStatus = () => {
  loadSystemStatus()
  loadActiveAlerts()
  ElMessage.success('系统状态已刷新')
}

const acknowledgeAlert = async (alert) => {
  try {
    await apiMonitoring.acknowledgeAlert(alert.id, 1) // 假设用户ID为1
    ElMessage.success('告警已确认')
    loadActiveAlerts()
  } catch (error) {
    ElMessage.error('确认告警失败')
  }
}

const goToAlerts = () => {
  router.push('/alerts')
}

// 初始化
onMounted(() => {
  loadStats()
  loadActivities()
  loadChart()
  loadSystemStatus()
  loadActiveAlerts()

  // 每30秒自动刷新系统状态
  refreshTimer = setInterval(() => {
    loadSystemStatus()
    loadActiveAlerts()
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
}

.stat-icon.data-sources {
  background: linear-gradient(135deg, #409eff 0%, #53a8ff 100%);
}

.stat-icon.collections {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.stat-icon.records {
  background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);
}

.stat-icon.storage {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  transition: all 0.3s ease;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.system-metric {
  padding: 10px 0;
}

.metric-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stat-value {
    font-size: 20px;
  }

  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
}
</style>
