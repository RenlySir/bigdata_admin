/**
 * Global error handling utilities
 */

/**
 * Safe function execution with error handling
 */
export function safeExecute(fn, fallback = null) {
  try {
    return fn()
  } catch (error) {
    console.error('Function execution error:', error)
    return fallback
  }
}

/**
 * Safe async function execution with error handling
 */
export async function safeAsyncExecute(fn, fallback = null) {
  try {
    return await fn()
  } catch (error) {
    console.error('Async function execution error:', error)
    return fallback
  }
}

/**
 * Validate data and throw error if invalid
 */
export function validateRequired(data, fieldName) {
  if (!data || data === null || data === undefined || data === '') {
    throw new Error(`${fieldName} is required`)
  }
  return data
}

/**
 * Validate email format
 */
export function validateEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email)) {
    throw new Error('Invalid email format')
  }
  return email
}

/**
 * Create a consistent error response
 */
export function createErrorResponse(message, code = 'ERROR', details = null) {
  const error = new Error(message)
  error.code = code
  error.details = details
  return error
}

/**
 * Check if error is network related
 */
export function isNetworkError(error) {
  return (
    error instanceof TypeError &&
    (error.message.includes('fetch') ||
     error.message.includes('network') ||
     error.message.includes('NetworkError'))
  )
}

/**
 * Check if error is authentication related
 */
export function isAuthError(error) {
  return (
    error.code === 401 ||
    error.code === 403 ||
    error.message?.toLowerCase().includes('unauthorized') ||
    error.message?.toLowerCase().includes('forbidden')
  )
}

/**
 * Get user-friendly error message
 */
export function getUserFriendlyMessage(error) {
  if (isNetworkError(error)) {
    return '网络连接失败，请检查您的网络连接'
  }

  if (isAuthError(error)) {
    return '您没有权限执行此操作，请先登录'
  }

  if (error.message) {
    return error.message
  }

  return '操作失败，请稍后重试'
}
