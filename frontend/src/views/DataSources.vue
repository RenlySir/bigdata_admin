<template>
  <div class="datasources">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据源管理</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            添加数据源
          </el-button>
        </div>
      </template>

      <el-table :data="dataSources" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '活跃' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="testConnection(row)">
              <el-icon><Connection /></el-icon>
              测试连接
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadDataSources"
        @current-change="loadDataSources"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑数据源' : '添加数据源'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入数据源名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择数据源类型" style="width: 100%">
            <el-option label="MySQL" value="mysql" />
            <el-option label="PostgreSQL" value="postgresql" />
            <el-option label="MongoDB" value="mongodb" />
            <el-option label="Kafka" value="kafka" />
            <el-option label="API" value="api" />
            <el-option label="文件" value="file" />
          </el-select>
        </el-form-item>
        <el-form-item label="连接配置" prop="connectionConfig">
          <el-input
            v-model="form.connectionConfig"
            type="textarea"
            :rows="4"
            placeholder='JSON格式，例如：{"host":"localhost","port":3306,"database":"test"}'
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiDataSource } from '@/api'

const loading = ref(false)
const dataSources = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  name: '',
  type: '',
  connectionConfig: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入数据源名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择数据源类型', trigger: 'change' }],
  connectionConfig: [{ required: true, message: '请输入连接配置', trigger: 'blur' }]
}

const loadDataSources = async () => {
  loading.value = true
  try {
    const res = await apiDataSource.list({
      page: pagination.page,
      size: pagination.size
    })
    dataSources.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载数据源失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    type: '',
    connectionConfig: '',
    description: ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await apiDataSource.update(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await apiDataSource.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadDataSources()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

const testConnection = async (row) => {
  try {
    const res = await apiDataSource.test(row.id)
    if (res.data) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error('连接测试失败')
    }
  } catch (error) {
    ElMessage.error('连接测试失败')
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该数据源吗？', '提示', {
    type: 'warning'
  })
  try {
    await apiDataSource.delete(row.id)
    ElMessage.success('删除成功')
    loadDataSources()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadDataSources()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-pagination) {
  display: flex;
}
</style>
