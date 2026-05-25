import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default api

export const apiDataSource = {
  list: (params) => api.get('/datasources', { params }),
  get: (id) => api.get(`/datasources/${id}`),
  create: (data) => api.post('/datasources', data),
  update: (id, data) => api.put(`/datasources/${id}`, data),
  delete: (id) => api.delete(`/datasources/${id}`),
  test: (id) => api.post(`/datasources/${id}/test`)
}

export const apiCollection = {
  list: (params) => api.get('/collections', { params }),
  get: (id) => api.get(`/collections/${id}`),
  create: (data) => api.post('/collections', data),
  update: (id, data) => api.put(`/collections/${id}`, data),
  delete: (id) => api.delete(`/collections/${id}`),
  updateStats: (id) => api.post(`/collections/${id}/stats`)
}

export const apiRecord = {
  list: (collectionId, params) => api.get(`/collections/${collectionId}/records`, { params }),
  get: (collectionId, id) => api.get(`/collections/${collectionId}/records/${id}`),
  create: (collectionId, data) => api.post(`/collections/${collectionId}/records`, data),
  batchCreate: (collectionId, data) => api.post(`/collections/${collectionId}/records/batch`, data),
  update: (collectionId, id, data) => api.put(`/collections/${collectionId}/records/${id}`, data),
  delete: (collectionId, id) => api.delete(`/collections/${collectionId}/records/${id}`)
}

export const apiHealth = {
  check: () => api.get('/health')
}
