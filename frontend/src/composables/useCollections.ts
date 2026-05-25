import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { collectionApi } from '@/services/api'
import type { DataCollection, ListParams, CreateCollectionParams } from '@/types'

export function useCollections() {
  const loading = ref(false)
  const collections = ref<DataCollection[]>([])

  const pagination = reactive({
    page: 1,
    size: 10,
    total: 0
  })

  const searchKeyword = ref('')

  /**
   * Load collections with pagination and search
   */
  const loadCollections = async () => {
    loading.value = true
    try {
      const params: ListParams = {
        page: pagination.page,
        size: pagination.size,
        keyword: searchKeyword.value || undefined
      }

      const response = await collectionApi.list(params)
      collections.value = response.data.records || []
      pagination.total = response.data.total || 0
    } catch (error) {
      ElMessage.error('加载数据集合失败: ' + (error as Error).message)
      console.error('Load collections error:', error)
    } finally {
      loading.value = false
    }
  }

  /**
   * Create new collection
   */
  const createCollection = async (data: CreateCollectionParams) => {
    loading.value = true
    try {
      await collectionApi.create(data)
      ElMessage.success('创建成功')
      await loadCollections()
      return true
    } catch (error) {
      ElMessage.error('创建失败: ' + (error as Error).message)
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Update collection
   */
  const updateCollection = async (id: number, data: CreateCollectionParams) => {
    loading.value = true
    try {
      await collectionApi.update(id, { id, ...data })
      ElMessage.success('更新成功')
      await loadCollections()
      return true
    } catch (error) {
      ElMessage.error('更新失败: ' + (error as Error).message)
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Delete collection
   */
  const deleteCollection = async (id: number) => {
    loading.value = true
    try {
      await collectionApi.delete(id)
      ElMessage.success('删除成功')
      await loadCollections()
      return true
    } catch (error) {
      ElMessage.error('删除失败: ' + (error as Error).message)
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Refresh collections
   */
  const refresh = () => {
    pagination.page = 1
    loadCollections()
  }

  return {
    loading,
    collections,
    pagination,
    searchKeyword,
    loadCollections,
    createCollection,
    updateCollection,
    deleteCollection,
    refresh
  }
}
