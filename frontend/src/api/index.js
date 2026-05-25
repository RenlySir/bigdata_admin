// API exports
export { collectionApi, dataSourceApi, recordApi, etlApi, monitoringApi } from '@/services/api'

import { collectionApi, dataSourceApi, recordApi, etlApi, monitoringApi } from '@/services/api'

// Legacy API exports for backward compatibility
export const apiCollection = collectionApi
export const apiDataSource = dataSourceApi
export const apiRecord = recordApi
export const apiEtl = etlApi
export const apiMonitoring = monitoringApi
