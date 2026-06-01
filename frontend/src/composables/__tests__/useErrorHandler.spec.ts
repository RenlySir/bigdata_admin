import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useErrorHandler, setupGlobalErrorHandler, withErrorHandling } from '../useErrorHandler'

describe('useErrorHandler', () => {
  beforeEach(() => {
    // Reset window error handlers
    vi.restoreAllMocks()
  })

  it('should capture errors and set error state', () => {
    const { hasError, errorMessage, setError, clearError } = useErrorHandler()

    expect(hasError.value).toBe(false)

    const testError = new Error('Test error')
    setError(testError)

    expect(hasError.value).toBe(true)
    expect(errorMessage.value).toBe('Test error')

    clearError()

    expect(hasError.value).toBe(false)
    expect(errorMessage.value).toBe('')
  })

  it('should handle async operations with error handling', async () => {
    const successFn = vi.fn(() => 'success')
    const errorFn = vi.fn()

    const result = await withErrorHandling(successFn, errorFn)

    expect(result).toBe('success')
    expect(successFn).toHaveBeenCalled()
    expect(errorFn).not.toHaveBeenCalled()
  })

  it('should catch async errors and call error handler', async () => {
    const errorFn = vi.fn()
    const failingFn = () => Promise.reject(new Error('Async error'))

    try {
      await withErrorHandling(failingFn, errorFn)
    } catch (error) {
      expect(error.message).toBe('Async error')
    }

    expect(errorFn).toHaveBeenCalled()
  })
})

describe('setupGlobalErrorHandler', () => {
  it('should set up global error handlers', () => {
    const unhandledRejectionHandler = vi.fn()
    window.addEventListener('unhandledrejection', unhandledRejectionHandler)

    setupGlobalErrorHandler()

    const testError = new Error('Test rejection')
    const event = new PromiseRejectionEvent('unhandledrejection', {
      promise: Promise.reject(testError),
      reason: testError
    })

    window.dispatchEvent(event)

    expect(event.defaultPrevented).toBe(true)

    window.removeEventListener('unhandledrejection', unhandledRejectionHandler)
  })
})
