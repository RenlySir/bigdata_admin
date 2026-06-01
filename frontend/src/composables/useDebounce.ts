import { ref, watch, onScopeDispose } from 'vue'

/**
 * Debounce composable for delaying function execution
 * @param {Function} callback - Function to debounce
 * @param {number} delay - Delay in milliseconds (default: 300ms)
 * @returns {Object} - { debouncedValue, immediateCallback }
 */
export function useDebounce(callback, delay = 300) {
  const timeoutId = ref(null)
  const debouncedValue = ref(null)

  /**
   * Debounced function
   * @param {*} value - Value to pass to callback
   */
  const debounceFn = (value) => {
    if (timeoutId.value) {
      clearTimeout(timeoutId.value)
    }

    timeoutId.value = setTimeout(() => {
      callback(value)
      debouncedValue.value = value
    }, delay)
  }

  /**
   * Execute callback immediately without debounce
   * @param {*} value - Value to pass to callback
   */
  const immediateCallback = (value) => {
    if (timeoutId.value) {
      clearTimeout(timeoutId.value)
      timeoutId.value = null
    }
    callback(value)
    debouncedValue.value = value
  }

  /**
   * Clear any pending debounced execution
   */
  const clearDebounce = () => {
    if (timeoutId.value) {
      clearTimeout(timeoutId.value)
      timeoutId.value = null
    }
  }

  // Cleanup on unmount
  onScopeDispose(() => {
    clearDebounce()
  })

  return {
    debounceFn,
    immediateCallback,
    clearDebounce,
    debouncedValue
  }
}

/**
 * Watch-based debounce composable
 * @param {Ref} source - Reactive source to watch
 * @param {Function} callback - Function to call when debounced
 * @param {number} delay - Delay in milliseconds (default: 300ms)
 */
export function useWatchDebounce(source, callback, delay = 300) {
  const timeoutId = ref(null)

  watch(
    source,
    (newValue) => {
      if (timeoutId.value) {
        clearTimeout(timeoutId.value)
      }

      timeoutId.value = setTimeout(() => {
        callback(newValue)
      }, delay)
    },
    { immediate: false }
  )

  // Cleanup
  onScopeDispose(() => {
    if (timeoutId.value) {
      clearTimeout(timeoutId.value)
    }
  })
}

/**
 * Debounce hook for input fields
 * @param {Function} callback - Function to call on debounced change
 * @param {number} delay - Delay in milliseconds
 */
export function useInputDebounce(callback, delay = 300) {
  const input = ref('')
  const timeoutId = ref(null)

  const updateInput = (value) => {
    input.value = value

    if (timeoutId.value) {
      clearTimeout(timeoutId.value)
    }

    timeoutId.value = setTimeout(() => {
      callback(value)
    }, delay)
  }

  const clearDebounce = () => {
    if (timeoutId.value) {
      clearTimeout(timeoutId.value)
    }
  }

  onScopeDispose(() => {
    clearDebounce()
  })

  return {
    input,
    updateInput,
    clearDebounce
  }
}
