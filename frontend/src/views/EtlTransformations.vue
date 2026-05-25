<template>
  <div class="etl-transformations">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="title-section">
        <h2>ETL 数据转换</h2>
        <p>管理数据转换规则和执行任务</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        创建转换
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="转换名称">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入转换名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" value="draft" />
            <el-option label="活跃" value="active" />
            <el-option label="暂停" value="paused" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 转换列表 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="name" label="转换名称" min-width="150" />
        <el-table-column prop="transformationType" label="转换类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.transformationType)">
              {{ getTypeLabel(row.transformationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="执行统计" width="150">
          <template #default="{ row }">
            <span>总数: {{ row.totalExecutions || 0 }}</span>
            <el-divider direction="vertical" />
            <span style="color: #67c23a">成功: {{ row.successExecutions || 0 }}</span>
            <el-divider direction="vertical" />
            <span style="color: #f56c6c">失败: {{ row.failureExecutions || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastExecutedAt" label="最后执行" width="160">
          <template #default="{ row }">
            {{ row.lastExecutedAt || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              text
              size="small"
              @click="handleExecute(row)"
              :loading="executing[row.id]"
            >
              <el-icon><VideoPlay /></el-icon>
              执行
            </el-button>
            <el-button text size="small" @click="handleViewHistory(row)">
              <el-icon><Clock /></el-icon>
              历史
            </el-button>
            <el-button text size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-popconfirm
              title="确定删除此转换吗？"
              @confirm="handleDelete(row)"
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

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑转换' : '创建转换'"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="转换名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入转换名称" />
        </el-form-item>
        <el-form-item label="转换描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入转换描述"
          />
        </el-form-item>
        <el-form-item label="转换类型" prop="transformationType">
          <el-select v-model="formData.transformationType" placeholder="请选择转换类型">
            <el-option label="字段映射 (Mapping)" value="mapping" />
            <el-option label="数据过滤 (Filter)" value="filter" />
            <el-option label="数据聚合 (Aggregate)" value="aggregate" />
            <el-option label="数据导出 (Export)" value="export" />
          </el-select>
        </el-form-item>
        <el-form-item label="源数据集合" prop="sourceCollectionId">
          <el-select
            v-model="formData.sourceCollectionId"
            placeholder="请选择源数据集合"
            filterable
          >
            <el-option
              v-for="collection in collections"
              :key="collection.id"
              :label="collection.name"
              :value="collection.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标数据集合">
          <el-select
            v-model="formData.targetCollectionId"
            placeholder="请选择目标数据集合（可选）"
            filterable
            clearable
          >
            <el-option
              v-for="collection in collections"
              :key="collection.id"
              :label="collection.name"
              :value="collection.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="转换规则" prop="transformationRules">
          <el-input
            v-model="formData.transformationRules"
            type="textarea"
            :rows="5"
            placeholder='请输入JSON格式的转换规则，例如：{"fieldMapping":{"source":"target"}}'
          />
        </el-form-item>
        <el-form-item label="定时表达式">
          <el-input
            v-model="formData.scheduleExpression"
            placeholder="请输入Cron表达式，例如：0 0 * * * ?"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 执行历史对话框 -->
    <el-dialog v-model="historyDialogVisible" title="执行历史" width="900px">
      <el-table :data="historyData" v-loading="historyLoading" stripe>
        <el-table-column prop="id" label="执行ID" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getExecutionStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理记录" width="200">
          <template #default="{ row }">
            <span>总计: {{ row.recordsProcessed || 0 }}</span>
            <el-divider direction="vertical" />
            <span style="color: #67c23a">成功: {{ row.recordsSuccess || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="durationMs" label="耗时(ms)" width="100" />
        <el-table-column prop="startedAt" label="开始时间" width="160" />
        <el-table-column prop="errorMessage" label="错误信息" min-width="200">
          <template #default="{ row }">
            <span v-if="row.errorMessage" style="color: #f56c6c">{{ row.errorMessage }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="historyPagination.page"
          v-model:page-size="historyPagination.size"
          :total="historyPagination.total"
          layout="total, prev, pager, next"
          @current-change="loadHistory"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiEtl, apiCollection } from '@/api'

// 数据
const loading = ref(false)
const tableData = ref([])
const collections = ref([])
const executing = ref({})

const searchForm = ref({
  keyword: '',
  status: ''
})

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const formData = ref({
  name: '',
  description: '',
  transformationType: '',
  sourceCollectionId: null,
  targetCollectionId: null,
  transformationRules: '',
  scheduleExpression: ''
})

const formRules = {
  name: [
    { required: true, message: '请输入转换名称', trigger: 'blur' }
  ],
  transformationType: [
    { required: true, message: '请选择转换类型', trigger: 'change' }
  ],
  sourceCollectionId: [
    { required: true, message: '请选择源数据集合', trigger: 'change' }
  ],
  transformationRules: [
    { required: true, message: '请输入转换规则', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        try {
          JSON.parse(value)
          callback()
        } catch (e) {
          callback(new Error('请输入有效的JSON格式'))
        }
      },
      trigger: 'blur'
    }
  ]
}

// 执行历史
const historyDialogVisible = ref(false)
const historyLoading = ref(false)
const historyData = ref([])
const currentTransformation = ref(null)
const historyPagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 方法
const loadData = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      keyword: searchForm.value.keyword || undefined,
      status: searchForm.value.status || undefined
    }
    const res = await apiEtl.getTransformations(params)
    if (res.data) {
      tableData.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadCollections = async () => {
  try {
    const res = await apiCollection.list({ page: 1, size: 100 })
    if (res.data) {
      collections.value = res.data.records || []
    }
  } catch (error) {
    console.error('Failed to load collections:', error)
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.value = {
    keyword: '',
    status: ''
  }
  handleSearch()
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadData()
}

const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  loadData()
}

const openCreateDialog = () => {
  isEdit.value = false
  formData.value = {
    name: '',
    description: '',
    transformationType: '',
    sourceCollectionId: null,
    targetCollectionId: null,
    transformationRules: '',
    scheduleExpression: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    if (isEdit.value) {
      await apiEtl.updateTransformation(formData.value.id, formData.value)
      ElMessage.success('更新成功')
    } else {
      await apiEtl.createTransformation(formData.value)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== false) { // 表单验证失败
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
      console.error(error)
    }
  } finally {
    submitting.value = false
  }
}

const handleExecute = async (row) => {
  try {
    executing.value[row.id] = true
    const res = await apiEtl.executeTransformation(row.id, 'manual')
    if (res.data) {
      ElMessage.success('转换执行已启动')
      // 延迟刷新以显示执行状态
      setTimeout(() => {
        loadData()
      }, 1000)
    }
  } catch (error) {
    ElMessage.error('执行失败')
    console.error(error)
  } finally {
    executing.value[row.id] = false
  }
}

const handleViewHistory = async (row) => {
  currentTransformation.value = row
  historyDialogVisible.value = true
  await loadHistory()
}

const loadHistory = async () => {
  try {
    historyLoading.value = true
    const params = {
      transformationId: currentTransformation.value.id,
      page: historyPagination.value.page,
      size: historyPagination.value.size
    }
    const res = await apiEtl.getExecutions(params)
    if (res.data) {
      historyData.value = res.data.records || []
      historyPagination.value.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载历史记录失败')
    console.error(error)
  } finally {
    historyLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await apiEtl.deleteTransformation(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
    console.error(error)
  }
}

// 辅助方法
const getTypeLabel = (type) => {
  const map = {
    mapping: '映射',
    filter: '过滤',
    aggregate: '聚合',
    export: '导出'
  }
  return map[type] || type
}

const getTypeTagType = (type) => {
  const map = {
    mapping: 'primary',
    filter: 'success',
    aggregate: 'warning',
    export: 'info'
  }
  return map[type] || ''
}

const getStatusLabel = (status) => {
  const map = {
    draft: '草稿',
    active: '活跃',
    paused: '暂停',
    archived: '已归档'
  }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = {
    draft: 'info',
    active: 'success',
    paused: 'warning',
    archived: 'danger'
  }
  return map[status] || ''
}

const getExecutionStatusType = (status) => {
  const map = {
    running: 'warning',
    completed: 'success',
    failed: 'danger',
    cancelled: 'info'
  }
  return map[status] || ''
}

// 初始化
onMounted(() => {
  loadData()
  loadCollections()
})
</script>

<style scoped>
.etl-transformations {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
