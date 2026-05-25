<template>
  <div class="collections">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据集合</span>
          <div>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索数据集合"
              style="width: 200px; margin-right: 10px"
              @keyup.enter="loadCollections"
            />
            <el-button type="primary" @click="showCreateDialog">
              <el-icon><Plus /></el-icon>
              创建集合
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="collections" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="recordCount" label="记录数" width="100" />
        <el-table-column prop="sizeInBytes" label="大小" width="120">
          <template #default="{ row }">
            {{ formatSize(row.sizeInBytes) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '活跃' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewRecords(row)">
              <el-icon><View /></el-icon>
              查看记录
            </el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
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
        @size-change="loadCollections"
        @current-change="loadCollections"
        style="margin-top: 20px"
      />
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑数据集合' : '创建数据集合'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入集合名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="用逗号分隔，如：用户,日志" />
        </el-form-item>
        <el-form-item label="Schema定义">
          <el-input
            v-model="form.schemaDefinition"
            type="textarea"
            :rows="5"
            placeholder="JSON格式的Schema定义"
          />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiCollection } from '@/api'

const router = useRouter()
const loading = ref(false)
const collections = ref([])
const searchKeyword = ref('')
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
  description: '',
  tags: '',
  schemaDefinition: ''
})

const rules = {
  name: [{ required: true, message: '请输入集合名称', trigger: 'blur' }]
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const loadCollections = async () => {
  loading.value = true
  try {
    const res = await apiCollection.list({
      page: pagination.page,
      size: pagination.size,
      keyword: searchKeyword.value
    })
    collections.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载集合失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    tags: '',
    schemaDefinition: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await apiCollection.update(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await apiCollection.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadCollections()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

const viewRecords = (row) => {
  router.push(`/collections/${row.id}/records`)
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该集合及其所有记录吗？', '提示', {
    type: 'warning'
  })
  try {
    await apiCollection.delete(row.id)
    ElMessage.success('删除成功')
    loadCollections()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadCollections()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
