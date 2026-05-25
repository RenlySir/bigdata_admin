/**
 * API Response types
 */

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageResult<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * Entity types
 */
export interface DataSource {
  id?: number
  name: string
  type: string
  connectionConfig: string
  description?: string
  status: number
  createdBy?: number
  createdAt?: string
  updatedAt?: string
}

export interface DataCollection {
  id?: number
  name: string
  description?: string
  dataSourceId?: number
  schemaDefinition?: string
  recordCount?: number
  sizeInBytes?: number
  tags?: string
  status: number
  createdAt?: string
  updatedAt?: string
}

export interface DataRecord {
  id?: number
  collectionId: number
  dataType?: string
  jsonData?: string
  textContent?: string
  metadata?: string
  version?: number
  checksum?: string
  createdAt?: string
  updatedAt?: string
}

export interface User {
  id?: number
  username: string
  email?: string
  nickname?: string
  avatar?: string
  role?: number
  status?: number
}

/**
 * API Request types
 */
export interface ListParams {
  page: number
  size: number
  keyword?: string
}

export interface CreateCollectionParams {
  name: string
  description?: string
  tags?: string
  schemaDefinition?: string
}

export interface UpdateCollectionParams extends CreateCollectionParams {
  id: number
}
