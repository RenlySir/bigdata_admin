import axios, { AxiosError, AxiosResponse } from 'axios'
import type { ApiResponse, PageResult, ListParams, CreateCollectionParams, UpdateCollectionParams, DataCollection } from '@/types'

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
apiClient.interceptors.request.use(
  (config) => {
    // Add auth token if exists
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    return response.data
  },
  (error: AxiosError<ApiResponse>) => {
    const message = error.response?.data?.message || error.message || '请求失败'
    console.error('API Error:', message)

    // Handle specific error codes
    if (error.response?.status === 401) {
      // Unauthorized - clear token and redirect to login
      localStorage.removeItem('auth_token')
      window.location.href = '/login'
    }

    return Promise.reject(new Error(message))
  }
)

/**
 * Collection API
 */
export const collectionApi = {
  list: (params: ListParams): Promise<ApiResponse<PageResult<DataCollection>>> => {
    return apiClient.get('/collections', { params })
  },

  getById: (id: number): Promise<ApiResponse<DataCollection>> => {
    return apiClient.get(`/collections/${id}`)
  },

  create: (data: CreateCollectionParams): Promise<ApiResponse<DataCollection>> => {
    return apiClient.post('/collections', data)
  },

  update: (id: number, data: UpdateCollectionParams): Promise<ApiResponse<DataCollection>> => {
    return apiClient.put(`/collections/${id}`, data)
  },

  delete: (id: number): Promise<ApiResponse<void>> => {
    return apiClient.delete(`/collections/${id}`)
  },

  updateStats: (id: number): Promise<ApiResponse<number>> => {
    return apiClient.post(`/collections/${id}/stats`)
  }
}

export default apiClient
