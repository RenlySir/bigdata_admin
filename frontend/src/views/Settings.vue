<template>
  <div class="settings">
    <el-card>
      <template #header>
        <span>系统设置</span>
      </template>

      <el-form label-width="150px" style="max-width: 600px">
        <el-form-item label="系统名称">
          <el-input v-model="settings.systemName" />
        </el-form-item>
        <el-form-item label="数据保留天数">
          <el-input-number v-model="settings.retentionDays" :min="1" :max="3650" />
        </el-form-item>
        <el-form-item label="单文件最大大小">
          <el-input-number v-model="settings.maxFileSize" :min="1" :max="1024" />
          <span style="margin-left: 10px">MB</span>
        </el-form-item>
        <el-form-item label="启用全文搜索">
          <el-switch v-model="settings.fullTextSearch" />
        </el-form-item>
        <el-form-item label="启用数据压缩">
          <el-switch v-model="settings.compression" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveSettings">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>关于</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="系统名称">Big Data Admin Platform</el-descriptions-item>
        <el-descriptions-item label="版本">1.0.0</el-descriptions-item>
        <el-descriptions-item label="后端技术栈">Spring Boot 3.2 + TiDB</el-descriptions-item>
        <el-descriptions-item label="前端技术栈">Vue 3 + Element Plus</el-descriptions-item>
        <el-descriptions-item label="数据库" :span="2">
          <a href="https://www.pingcap.com/tidb/" target="_blank">TiDB - 分布式关系型数据库</a>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'

const settings = reactive({
  systemName: 'Big Data Admin Platform',
  retentionDays: 90,
  maxFileSize: 100,
  fullTextSearch: true,
  compression: false
})

const saveSettings = () => {
  ElMessage.success('设置保存成功')
}
</script>

<style scoped>
.settings {
  max-width: 800px;
}
</style>
