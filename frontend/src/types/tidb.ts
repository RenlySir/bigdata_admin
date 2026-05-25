/**
 * TiDB related types
 */

export interface TiDBConnectionInfo {
  host?: string
  port?: number
  username?: string
  password?: string
  database?: string
  ssl?: boolean
  timezone?: string
  connected?: boolean
  version?: string
  message?: string
}

export interface TiDBDatabaseInfo {
  name: string
  dataSourceId?: number
  characterSet?: string
  collation?: string
  tableCount?: number
}

export interface TiDBTableInfo {
  name: string
  database: string
  dataSourceId?: number
  engine?: string
  rowCount?: number
  dataLength?: number
  indexLength?: number
  createTime?: string
  updateTime?: string
}

export interface TiDBQueryRequest {
  database: string
  query: string
}

export interface TiDBQueryResult {
  columns: string[]
  rows: Record<string, any>[]
  rowCount: number
  executionTime?: number
}
