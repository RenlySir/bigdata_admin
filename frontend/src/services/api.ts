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

/**
 * ETL API
 */
export const etlApi = {
  getTransformations: (params): Promise<ApiResponse> => {
    return apiClient.get('/etl/transformations', { params })
  },

  getTransformationById: (id): Promise<ApiResponse> => {
    return apiClient.get(`/etl/transformations/${id}`)
  },

  createTransformation: (data): Promise<ApiResponse> => {
    return apiClient.post('/etl/transformations', data)
  },

  updateTransformation: (id, data): Promise<ApiResponse> => {
    return apiClient.put(`/etl/transformations/${id}`, data)
  },

  deleteTransformation: (id): Promise<ApiResponse> => {
    return apiClient.delete(`/etl/transformations/${id}`)
  },

  executeTransformation: (id, triggeredBy = 'manual'): Promise<ApiResponse> => {
    return apiClient.post(`/etl/transformations/${id}/execute`, null, {
      params: { triggeredBy }
    })
  },

  getExecutions: (params): Promise<ApiResponse> => {
    return apiClient.get('/etl/executions', { params })
  }
}

/**
 * Monitoring API
 */
export const monitoringApi = {
  getCurrentMetrics: (): Promise<ApiResponse> => {
    return apiClient.get('/monitoring/metrics/current')
  },

  getMetricsHistory: (params): Promise<ApiResponse> => {
    return apiClient.get('/monitoring/metrics/history', { params })
  },

  getAlertRules: (params): Promise<ApiResponse> => {
    return apiClient.get('/monitoring/alerts/rules', { params })
  },

  getActiveAlertRules: (): Promise<ApiResponse> => {
    return apiClient.get('/monitoring/alerts/rules/active')
  },

  createAlertRule: (data): Promise<ApiResponse> => {
    return apiClient.post('/monitoring/alerts/rules', data)
  },

  updateAlertRule: (id, data): Promise<ApiResponse> => {
    return apiClient.put(`/monitoring/alerts/rules/${id}`, data)
  },

  deleteAlertRule: (id): Promise<ApiResponse> => {
    return apiClient.delete(`/monitoring/alerts/rules/${id}`)
  },

  getAlertHistory: (params): Promise<ApiResponse> => {
    return apiClient.get('/monitoring/alerts/history', { params })
  },

  getActiveAlerts: (): Promise<ApiResponse> => {
    return apiClient.get('/monitoring/alerts/active')
  },

  acknowledgeAlert: (id, userId): Promise<ApiResponse> => {
    return apiClient.post(`/monitoring/alerts/${id}/acknowledge`, null, {
      params: { userId }
    })
  },

  resolveAlert: (id): Promise<ApiResponse> => {
    return apiClient.post(`/monitoring/alerts/${id}/resolve`)
  }
}

export default apiClient
