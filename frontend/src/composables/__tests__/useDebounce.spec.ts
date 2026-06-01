import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ref } from 'vue'
import { useDebounce, useWatchDebounce, useInputDebounce } from '../useDebounce'

describe('useDebounce', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should debounce function calls', async () => {
    const callback = vi.fn()
    const { debounceFn } = useDebounce(callback, 300)

    debounceFn('test1')
    debounceFn('test2')
    debounceFn('test3')

    vi.advanceTimersByTime(100)
    expect(callback).not.toHaveBeenCalled()

    vi.advanceTimersByTime(300)
    expect(callback).toHaveBeenCalledTimes(1)
    expect(callback).toHaveBeenCalledWith('test3')
  })

  it('should execute callback immediately when using immediateCallback', () => {
    const callback = vi.fn()
    const { immediateCallback } = useDebounce(callback, 300)

    immediateCallback('test')

    expect(callback).toHaveBeenCalledTimes(1)
    expect(callback).toHaveBeenCalledWith('test')
  })

  it('should clear pending debounced execution', () => {
    const callback = vi.fn()
    const { debounceFn, clearDebounce } = useDebounce(callback, 300)

    debounceFn('test')

    vi.advanceTimersByTime(100)
    clearDebounce()

    vi.advanceTimersByTime(300)
    expect(callback).not.toHaveBeenCalled()
  })
})

describe('useWatchDebounce', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should debounce source changes', () => {
    const callback = vi.fn()
    const source = ref('initial')

    useWatchDebounce(source, callback, 300)

    source.value = 'value1'
    source.value = 'value2'

    vi.advanceTimersByTime(100)
    expect(callback).not.toHaveBeenCalled()

    vi.advanceTimersByTime(300)
    expect(callback).toHaveBeenCalledTimes(1)
    expect(callback).toHaveBeenCalledWith('value2')
  })
})

describe('useInputDebounce', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should debounce input changes', () => {
    const callback = vi.fn()
    const { input, updateInput } = useInputDebounce(callback, 300)

    updateInput('test1')
    expect(input.value).toBe('test1')

    updateInput('test2')
    updateInput('test3')

    vi.advanceTimersByTime(100)
    expect(callback).not.toHaveBeenCalled()

    vi.advanceTimersByTime(300)
    expect(callback).toHaveBeenCalledTimes(1)
    expect(callback).toHaveBeenCalledWith('test3')
  })
})
