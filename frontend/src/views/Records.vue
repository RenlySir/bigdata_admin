<template>
  <div class="records">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据记录 - {{ collectionName }}</span>
          <div>
            <el-button @click="showImportDialog">
              <el-icon><Upload /></el-icon>
              导入数据
            </el-button>
            <el-button type="primary" @click="showCreateDialog">
              <el-icon><Plus /></el-icon>
              添加记录
            </el-button>
          </div>
        </div>
      </template>

      <el-input
        v-model="searchKeyword"
        placeholder="搜索记录内容"
        style="width: 300px; margin-bottom: 15px"
        clearable
        @clear="loadRecords"
      >
        <template #append>
          <el-button @click="loadRecords">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>

      <el-table :data="records" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="dataType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.dataType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="jsonData" label="数据内容" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatJsonData(row.jsonData) }}
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon>
              详情
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
        @size-change="loadRecords"
        @current-change="loadRecords"
        style="margin-top: 20px"
      />
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑记录' : '添加记录'" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="数据类型" prop="dataType">
          <el-select v-model="form.dataType" style="width: 100%">
            <el-option label="JSON" value="json" />
            <el-option label="TEXT" value="text" />
            <el-option label="DOCUMENT" value="document" />
            <el-option label="BINARY" value="binary" />
          </el-select>
        </el-form-item>
        <el-form-item label="JSON数据" prop="jsonData">
          <el-input
            v-model="form.jsonData"
            type="textarea"
            :rows="8"
            placeholder="请输入JSON格式的数据"
          />
        </el-form-item>
        <el-form-item label="文本内容">
          <el-input
            v-model="form.textContent"
            type="textarea"
            :rows="3"
            placeholder="用于全文搜索的文本内容"
          />
        </el-form-item>
        <el-form-item label="元数据">
          <el-input
            v-model="form.metadata"
            type="textarea"
            :rows="2"
            placeholder="JSON格式的元数据"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="记录详情" width="700px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ currentRecord.id }}</el-descriptions-item>
        <el-descriptions-item label="数据类型">
          <el-tag>{{ currentRecord.dataType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="版本">{{ currentRecord.version }}</el-descriptions-item>
        <el-descriptions-item label="校验和">
          <el-text tag="code" size="small">{{ currentRecord.checksum }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="JSON数据">
          <pre class="json-preview">{{ formatJson(currentRecord.jsonData) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="文本内容">{{ currentRecord.textContent }}</el-descriptions-item>
        <el-descriptions-item label="元数据">
          <pre class="json-preview">{{ formatJson(currentRecord.metadata) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRecord.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentRecord.updatedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiRecord, apiCollection } from '@/api'
import { debounce } from '@/utils/debounce'

const route = useRoute()
const collectionId = ref(route.params.id)
const collectionName = ref('')
const loading = ref(false)
const records = ref([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const currentRecord = ref({})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  dataType: 'json',
  jsonData: '',
  textContent: '',
  metadata: ''
})

const rules = {
  dataType: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  jsonData: [{ required: true, message: '请输入JSON数据', trigger: 'blur' }]
}

const formatJson = (data) => {
  if (!data) return ''
  try {
    return JSON.stringify(JSON.parse(data), null, 2)
  } catch {
    return data
  }
}

const formatJsonData = (data) => {
  if (!data) return ''
  const str = String(data)
  return str.length > 100 ? str.substring(0, 100) + '...' : str
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await apiRecord.list(collectionId.value, {
      page: pagination.page,
      size: pagination.size,
      keyword: searchKeyword.value
    })
    records.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载记录失败')
  } finally {
    loading.value = false
  }
}

const loadCollection = async () => {
  try {
    const res = await apiCollection.get(collectionId.value)
    collectionName.value = res.data.name
  } catch (error) {
    ElMessage.error('加载集合信息失败')
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    dataType: 'json',
    jsonData: '',
    textContent: '',
    metadata: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const viewDetail = (row) => {
  currentRecord.value = row
  detailVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await apiRecord.update(collectionId.value, form.id, form)
      ElMessage.success('更新成功')
    } else {
      await apiRecord.create(collectionId.value, form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadRecords()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该记录吗？', '提示', {
    type: 'warning'
  })
  try {
    await apiRecord.delete(collectionId.value, row.id)
    ElMessage.success('删除成功')
    loadRecords()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const showImportDialog = () => {
  ElMessage.info('导入功能开发中...')
}

// Debounced search function
const debouncedSearch = debounce(() => {
  pagination.page = 1
  loadRecords()
}, 500)

// Watch for search keyword changes
watch(searchKeyword, () => {
  debouncedSearch()
})

onMounted(() => {
  loadCollection()
  loadRecords()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.json-preview {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  font-size: 12px;
  max-height: 200px;
  overflow-y: auto;
  margin: 0;
}
</style>
