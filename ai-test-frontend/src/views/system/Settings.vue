<template>
  <div class="settings-container">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- LLM Settings -->
        <el-tab-pane label="LLM 配置" name="llm">
          <el-form label-width="160px" class="settings-form">
            <el-divider content-position="left">大语言模型配置</el-divider>
            <el-form-item label="LLM 提供商">
              <el-select v-model="llmConfig.provider" placeholder="请选择" style="width: 300px">
                <el-option label="OpenAI" value="openai" />
                <el-option label="Azure OpenAI" value="azure" />
                <el-option label="通义千问" value="qwen" />
                <el-option label="DeepSeek" value="deepseek" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </el-form-item>
            <el-form-item label="API Key">
              <el-input v-model="llmConfig.apiKey" type="password" show-password placeholder="请输入 API Key" style="width: 400px" />
            </el-form-item>
            <el-form-item label="API Base URL">
              <el-input v-model="llmConfig.baseUrl" placeholder="如 https://api.openai.com/v1" style="width: 400px" />
            </el-form-item>
            <el-form-item label="模型名称">
              <el-input v-model="llmConfig.model" placeholder="如 gpt-4o" style="width: 300px" />
            </el-form-item>
            <el-form-item label="Temperature">
              <el-slider v-model="llmConfig.temperature" :min="0" :max="2" :step="0.1" style="width: 300px" show-input />
            </el-form-item>
            <el-form-item label="最大 Token 数">
              <el-input-number v-model="llmConfig.maxTokens" :min="100" :max="128000" :step="100" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSave('llm')">保存配置</el-button>
              <el-button @click="handleTestConnection('llm')">测试连接</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Playwright Settings -->
        <el-tab-pane label="Playwright 配置" name="playwright">
          <el-form label-width="160px" class="settings-form">
            <el-divider content-position="left">Playwright 执行配置</el-divider>
            <el-form-item label="默认浏览器">
              <el-select v-model="pwConfig.browser" placeholder="请选择" style="width: 300px">
                <el-option label="Chromium" value="chromium" />
                <el-option label="Firefox" value="firefox" />
                <el-option label="WebKit" value="webkit" />
              </el-select>
            </el-form-item>
            <el-form-item label="超时时间 (ms)">
              <el-input-number v-model="pwConfig.timeout" :min="5000" :max="300000" :step="5000" />
            </el-form-item>
            <el-form-item label="截图策略">
              <el-select v-model="pwConfig.screenshot" placeholder="请选择" style="width: 300px">
                <el-option label="仅失败时截图" value="only-on-failure" />
                <el-option label="每步都截图" value="on" />
                <el-option label="不截图" value="off" />
              </el-select>
            </el-form-item>
            <el-form-item label="录制视频">
              <el-switch v-model="pwConfig.video" />
            </el-form-item>
            <el-form-item label="无头模式">
              <el-switch v-model="pwConfig.headless" />
            </el-form-item>
            <el-form-item label="并行执行数">
              <el-input-number v-model="pwConfig.workers" :min="1" :max="20" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSave('playwright')">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Storage Settings -->
        <el-tab-pane label="存储配置" name="storage">
          <el-form label-width="160px" class="settings-form">
            <el-divider content-position="left">文件存储配置</el-divider>
            <el-form-item label="存储类型">
              <el-select v-model="storageConfig.type" placeholder="请选择" style="width: 300px">
                <el-option label="本地存储" value="local" />
                <el-option label="MinIO" value="minio" />
                <el-option label="阿里云 OSS" value="oss" />
                <el-option label="AWS S3" value="s3" />
              </el-select>
            </el-form-item>
            <el-form-item label="Endpoint">
              <el-input v-model="storageConfig.endpoint" placeholder="如 http://localhost:9000" style="width: 400px" />
            </el-form-item>
            <el-form-item label="Access Key">
              <el-input v-model="storageConfig.accessKey" placeholder="请输入 Access Key" style="width: 300px" />
            </el-form-item>
            <el-form-item label="Secret Key">
              <el-input v-model="storageConfig.secretKey" type="password" show-password placeholder="请输入 Secret Key" style="width: 300px" />
            </el-form-item>
            <el-form-item label="Bucket">
              <el-input v-model="storageConfig.bucket" placeholder="请输入 Bucket 名称" style="width: 300px" />
            </el-form-item>
            <el-form-item label="最大文件大小 (MB)">
              <el-input-number v-model="storageConfig.maxFileSize" :min="1" :max="1024" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSave('storage')">保存配置</el-button>
              <el-button @click="handleTestConnection('storage')">测试连接</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('llm')

const llmConfig = reactive({
  provider: 'openai',
  apiKey: '',
  baseUrl: '',
  model: 'gpt-4o',
  temperature: 0.7,
  maxTokens: 4096,
})

const pwConfig = reactive({
  browser: 'chromium',
  timeout: 30000,
  screenshot: 'only-on-failure',
  video: true,
  headless: true,
  workers: 4,
})

const storageConfig = reactive({
  type: 'local',
  endpoint: '',
  accessKey: '',
  secretKey: '',
  bucket: 'ai-test',
  maxFileSize: 100,
})

function handleSave(section: string) {
  ElMessage.success(`${section} 配置已保存`)
}

function handleTestConnection(section: string) {
  ElMessage.info(`正在测试 ${section} 连接...`)
}
</script>

<style scoped lang="scss">
.settings-container {
  max-width: 900px;
}

.settings-form {
  padding: 20px 0;
}
</style>
