// API exports
export { collectionApi, etlApi, monitoringApi } from '@/services/api'

// Legacy API exports for backward compatibility
export const apiCollection = collectionApi
export const apiEtl = etlApi
export const apiMonitoring = monitoringApi
