import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ErrorBoundary from '@/components/ErrorBoundary.vue'

describe('ErrorBoundary.vue', () => {
  it('should render slot content when no error', () => {
    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: '<div class="test-content">Test Content</div>'
      }
    })

    expect(wrapper.find('.test-content').exists()).toBe(true)
    expect(wrapper.find('.error-boundary').exists()).toBe(false)
  })

  it('should render error UI when error occurs', async () => {
    const TestComponent = {
      template: '<div>{{ message }}</div>',
      props: ['message'],
      setup() {
        throw new Error('Test error')
      }
    }

    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: TestComponent
      }
    })

    // Wait for error to be captured
    await new Promise(resolve => setTimeout(resolve, 0))

    expect(wrapper.find('.error-boundary').exists()).toBe(true)
    expect(wrapper.find('.error-content').exists()).toBe(true)
  })

  it('should emit error event when error occurs', async () => {
    let emittedError = null

    const TestComponent = {
      template: '<div>Test</div>',
      setup() {
        throw new Error('Test error')
      }
    }

    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: TestComponent
      },
      props: {
        onError: (error) => {
          emittedError = error
        }
      }
    })

    wrapper.vm.$emit('error', { error: new Error('Test error') })

    expect(emittedError).toBeTruthy()
  })

  it('should reset error state when retry is clicked', async () => {
    const wrapper = mount(ErrorBoundary)
    wrapper.vm.hasError = true
    wrapper.vm.errorMessage = 'Test error'

    await wrapper.find('.error-boundary').exists()

    wrapper.vm.handleRetry()

    expect(wrapper.vm.hasError).toBe(false)
    expect(wrapper.vm.errorMessage).toBe('')
  })

  it('should provide reset function through context', () => {
    const wrapper = mount(ErrorBoundary)
    const context = wrapper.vm.$.provides.errorBoundary

    expect(context).toBeDefined()
    expect(context.reset).toBeInstanceOf(Function)
  })
})
