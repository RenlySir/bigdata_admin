/**
 * Application constants
 */

export const APP_NAME = 'Big Data Admin Platform'

export const API_BASE_URL = '/api'

export const CACHE_KEYS = {
  TOKEN: 'auth_token',
  USER: 'user_info',
  THEME: 'theme',
  LANGUAGE: 'language'
}

export const PAGINATION = {
  DEFAULT_PAGE: 1,
  DEFAULT_SIZE: 10,
  PAGE_SIZES: [10, 20, 50, 100]
}

export const STATUS = {
  ACTIVE: 1,
  INACTIVE: 0,
  DELETED: 1
}

export const STATUS_TAGS = {
  [STATUS.ACTIVE]: { text: '活跃', type: 'success' },
  [STATUS.INACTIVE]: { text: '停用', type: 'info' }
}

export const DATA_TYPES = {
  JSON: 'json',
  TEXT: 'text',
  BINARY: 'binary',
  STRUCTURED: 'structured'
}

export const EXPORT_FORMATS = ['json', 'csv', 'excel'] as const

export const IMPORT_FORMATS = ['json', 'csv', 'excel'] as const

export const ROLES = {
  ADMIN: 'admin',
  USER: 'user',
  VIEWER: 'viewer'
}
