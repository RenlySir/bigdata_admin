import { ref, onErrorCaptured } from 'vue'

/**
 * Error handler composable for catching and handling errors
 */
export function useErrorHandler() {
  const error = ref(null)
  const errorMessage = ref('')
  const hasError = ref(false)

  /**
   * Capture Vue component errors
   */
  onErrorCaptured((err, instance, info) => {
    console.error('Vue error captured:', err, info)
    error.value = err
    errorMessage.value = err.message || '未知错误'
    hasError.value = true

    // You can send error to monitoring service here
    // logErrorToService(err, instance, info)

    // Return false to prevent error from propagating
    return false
  })

  /**
   * Clear error state
   */
  const clearError = () => {
    error.value = null
    errorMessage.value = ''
    hasError.value = false
  }

  /**
   * Set error manually
   */
  const setError = (err) => {
    error.value = err
    errorMessage.value = err.message || '未知错误'
    hasError.value = true
  }

  return {
    error,
    errorMessage,
    hasError,
    clearError,
    setError
  }
}

/**
 * Global error handler for unhandled errors
 */
export function setupGlobalErrorHandler() {
  // Handle unhandled promise rejections
  window.addEventListener('unhandledrejection', (event) => {
    console.error('Unhandled promise rejection:', event.reason)

    // Prevent default browser error logging
    event.preventDefault()

    // You can send error to monitoring service here
    // logErrorToService(event.reason)
  })

  // Handle global errors
  window.addEventListener('error', (event) => {
    console.error('Global error:', event.error)

    // You can send error to monitoring service here
    // logErrorToService(event.error)
  })
}

/**
 * Async error wrapper for async operations
 */
export async function withErrorHandling(asyncFn, errorHandler) {
  try {
    return await asyncFn()
  } catch (error) {
    console.error('Async operation error:', error)

    if (errorHandler) {
      errorHandler(error)
    } else {
      // Default error handling
      throw error
    }
  }
}
