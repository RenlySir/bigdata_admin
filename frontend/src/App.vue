<template>
  <el-container class="app-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <el-icon><DataAnalysis /></el-icon>
        <span>BigData Admin</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        class="nav-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/datasources">
          <el-icon><Connection /></el-icon>
          <span>数据源</span>
        </el-menu-item>
        <el-menu-item index="/collections">
          <el-icon><FolderOpened /></el-icon>
          <span>数据集合</span>
        </el-menu-item>
        <el-menu-item index="/records">
          <el-icon><Document /></el-icon>
          <span>数据记录</span>
        </el-menu-item>
        <el-menu-item index="/etl">
          <el-icon><Operation /></el-icon>
          <span>ETL 转换</span>
        </el-menu-item>
        <el-menu-item index="/alerts">
          <el-icon><Warning /></el-icon>
          <span>监控告警</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <span>设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb>
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentPageName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-icon><User /></el-icon>
              管理员
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人设置</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <ErrorBoundary @error="handleGlobalError" @reset="handleErrorReset">
          <router-view />
        </ErrorBoundary>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import ErrorBoundary from '@/components/ErrorBoundary.vue'
import { setupGlobalErrorHandler } from '@/composables/useErrorHandler'

const route = useRoute()
const currentPageName = computed(() => {
  const names = {
    '/dashboard': '仪表盘',
    '/datasources': '数据源管理',
    '/collections': '数据集合',
    '/records': '数据记录',
    '/etl': 'ETL 转换',
    '/alerts': '监控告警',
    '/settings': '系统设置'
  }
  return names[route.path] || '首页'
})

// Error state
const globalError = ref(null)

// Handle global errors
const handleGlobalError = ({ error }) => {
  console.error('Global application error:', error)
  ElMessage.error(`应用错误: ${error.message || '未知错误'}`)
}

// Handle error reset
const handleErrorReset = () => {
  console.log('Error boundary reset')
}

// Setup global error handling on mount
onMounted(() => {
  setupGlobalErrorHandler()
})
</script>

<style scoped>
.app-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  color: #fff;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  font-size: 18px;
  font-weight: bold;
  gap: 8px;
}

.nav-menu {
  border-right: none;
  background-color: #304156;
}

.nav-menu .el-menu-item {
  color: #bfcbd9;
}

.nav-menu .el-menu-item:hover,
.nav-menu .el-menu-item.is-active {
  background-color: #263445;
  color: #409eff;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
