import { apiClient } from './api'
import type { ApiResponse, TiDBConnectionInfo, TiDBDatabaseInfo, TiDBTableInfo } from '@/types'

/**
 * TiDB API Service
 */
export const tidbApi = {
  /**
   * Test TiDB connection
   */
  testConnection: (connectionInfo: TiDBConnectionInfo): Promise<ApiResponse<TiDBConnectionInfo>> => {
    return apiClient.post('/datasources/tidb/test', connectionInfo)
  },

  /**
   * Get default TiDB info
   */
  getDefaultInfo: (): Promise<ApiResponse<TiDBConnectionInfo>> => {
    return apiClient.get('/datasources/tidb/info')
  },

  /**
   * Get databases from TiDB
   */
  getDatabases: (dataSourceId: number): Promise<ApiResponse<TiDBDatabaseInfo[]>> => {
    return apiClient.get(`/datasources/${dataSourceId}/tidb/databases`)
  },

  /**
   * Get tables from TiDB database
   */
  getTables: (dataSourceId: number, database: string): Promise<ApiResponse<TiDBTableInfo[]>> => {
    return apiClient.get(`/datasources/${dataSourceId}/tidb/tables`, {
      params: { database }
    })
  },

  /**
   * Execute query on TiDB
   */
  executeQuery: (dataSourceId: number, database: string, query: string): Promise<ApiResponse<Record<string, any>[]>> => {
    return apiClient.post(`/datasources/${dataSourceId}/tidb/query`, {
      database,
      query
    })
  }
}
