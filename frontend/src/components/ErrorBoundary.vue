<template>
  <div v-if="hasError" class="error-boundary">
    <div class="error-content">
      <el-icon :size="48" class="error-icon"><Warning /></el-icon>
      <h2>出错了</h2>
      <p class="error-message">{{ errorMessage }}</p>
      <div class="error-details" v-if="showDetails">
        <pre>{{ errorStack }}</pre>
      </div>
      <div class="error-actions">
        <el-button type="primary" @click="handleRetry">重试</el-button>
        <el-button @click="handleReload">刷新页面</el-button>
        <el-button link @click="showDetails = !showDetails">
          {{ showDetails ? '隐藏详情' : '显示详情' }}
        </el-button>
      </div>
    </div>
  </div>
  <slot v-else />
</template>

<script setup>
import { ref, onErrorCaptured, provide } from 'vue'
import { Warning } from '@element-plus/icons-vue'

const props = defineProps({
  // Fallback component to render when error occurs
  fallback: {
    type: [Object, String],
    default: null
  },
  // Custom error handler
  onError: {
    type: Function,
    default: null
  },
  // Whether to show error details to users
  showDetails: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['error', 'reset'])

const hasError = ref(false)
const errorMessage = ref('')
const errorStack = ref('')
const showDetails = ref(props.showDetails)

// Capture errors from child components
onErrorCaptured((error, instance, info) => {
  console.error('ErrorBoundary caught error:', error, info)

  hasError.value = true
  errorMessage.value = error.message || '未知错误'
  errorStack.value = error.stack || ''

  // Call custom error handler if provided
  if (props.onError) {
    props.onError(error, instance, info)
  }

  // Emit error event
  emit('error', { error, instance, info })

  // Return false to prevent error from propagating further
  return false
})

const handleRetry = () => {
  hasError.value = false
  errorMessage.value = ''
  errorStack.value = ''
  emit('reset')
}

const handleReload = () => {
  window.location.reload()
}

// Provide error boundary context to child components
provide('errorBoundary', {
  reset: handleRetry
})
</script>

<script>
export default {
  name: 'ErrorBoundary'
}
</script>

<style scoped>
.error-boundary {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 20px;
  background: #fff5f5;
  border-radius: 8px;
  margin: 20px;
}

.error-content {
  text-align: center;
  max-width: 600px;
}

.error-icon {
  color: #f56c6c;
  margin-bottom: 16px;
}

.error-message {
  font-size: 16px;
  color: #666;
  margin: 16px 0;
}

.error-details {
  margin: 16px 0;
  text-align: left;
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  max-height: 200px;
  overflow: auto;
}

.error-details pre {
  margin: 0;
  font-size: 12px;
  color: #666;
  white-space: pre-wrap;
  word-break: break-word;
}

.error-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 24px;
}
</style>
