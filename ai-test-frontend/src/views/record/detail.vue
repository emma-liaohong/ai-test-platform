<template>
  <div class="record-detail-page">
    <!-- ===== Page Header ===== -->
    <div class="page-header">
      <div class="page-header-left">
        <el-button :icon="ArrowLeft" @click="goBack">返回列表</el-button>
        <span class="page-title">
          <el-icon><Monitor /></el-icon>
          录制详情
          <el-tag
            v-if="session.sessionId"
            size="small"
            effect="plain"
            type="info"
            class="session-id-tag"
          >
            {{ session.sessionId }}
          </el-tag>
        </span>
        <span
          v-if="session.status === 'RECORDING'"
          class="recording-indicator"
        >
          <span class="status-dot" />
          录制中
        </span>
      </div>
      <div class="page-header-right">
        <el-button @click="goBack">
          <el-icon><Back /></el-icon>返回列表
        </el-button>
      </div>
    </div>

    <!-- ===== Main Split View ===== -->
    <div class="detail-body">
      <!-- ===== Left Panel: Browser Preview ===== -->
      <div class="browser-panel">
        <div class="browser-frame">
          <!-- Browser Chrome -->
          <div class="browser-chrome">
            <div class="browser-dots">
              <span class="dot dot--red" />
              <span class="dot dot--yellow" />
              <span class="dot dot--green" />
            </div>
            <div class="browser-url">
              <el-icon :size="13"><Location /></el-icon>
              <span class="url-text">{{ session.targetUrl || 'about:blank' }}</span>
            </div>
            <div class="browser-actions">
              <el-icon :size="14" class="browser-action-icon"><Refresh /></el-icon>
            </div>
          </div>

          <!-- Browser Content — Mock Login Page -->
          <div class="browser-content">
            <div class="mock-login-page">
              <!-- Sidebar -->
              <div class="mock-sidebar">
                <div class="mock-sidebar-logo">📋 管理系统</div>
                <div
                  v-for="item in sidebarItems"
                  :key="item.label"
                  class="mock-sidebar-item"
                  :class="{
                    'mock-highlight': isStepHighlighting(item.highlightStep),
                  }"
                >
                  <span class="mock-sidebar-icon">{{ item.icon }}</span>
                  <span class="mock-sidebar-text">{{ item.label }}</span>
                </div>
              </div>

              <!-- Main Content Area -->
              <div class="mock-main">
                <!-- Top Bar -->
                <div class="mock-topbar">
                  <span class="mock-breadcrumb">首页 / 用户登录</span>
                  <span class="mock-user-area">👤 游客</span>
                </div>

                <!-- Login Card -->
                <div class="mock-content-body">
                  <div
                    class="mock-login-card"
                    :class="{
                      'mock-highlight': isStepHighlighting(1),
                    }"
                  >
                    <div class="mock-login-header">
                      <span class="mock-login-logo">🔐</span>
                      <span class="mock-login-title">用户登录</span>
                    </div>
                    <div
                      class="mock-input-group"
                      :class="{
                        'mock-highlight': isStepHighlighting(2) || isStepHighlighting(3),
                      }"
                    >
                      <span class="mock-input-label">用户名</span>
                      <div class="mock-input-box">
                        <span class="mock-input-text">
                          {{ currentStep >= 3 ? 'admin' : '' }}
                        </span>
                        <span
                          v-if="currentStep === 3"
                          class="mock-cursor-blink"
                        />
                      </div>
                    </div>
                    <div
                      class="mock-input-group"
                      :class="{
                        'mock-highlight': isStepHighlighting(4) || isStepHighlighting(5),
                      }"
                    >
                      <span class="mock-input-label">密码</span>
                      <div class="mock-input-box">
                        <span class="mock-input-text">
                          {{ currentStep >= 5 ? '••••••••' : '' }}
                        </span>
                        <span
                          v-if="currentStep === 5"
                          class="mock-cursor-blink"
                        />
                      </div>
                    </div>
                    <div
                      class="mock-remember-row"
                      :class="{ 'mock-highlight': isStepHighlighting(6) }"
                    >
                      <span class="mock-checkbox">☑ 记住登录</span>
                    </div>
                    <div
                      class="mock-login-btn"
                      :class="{
                        'mock-highlight': isStepHighlighting(7),
                        'mock-login-btn--active': currentStep >= 7,
                      }"
                    >
                      登 录
                    </div>
                  </div>
                </div>

                <!-- Footer -->
                <div class="mock-footer">© 2026 AI Test Platform</div>
              </div>
            </div>
          </div>
        </div>

        <!-- URL Input Bar (editable) -->
        <div class="target-url-bar">
          <span class="url-bar-label">目标 URL：</span>
          <el-input
            v-model="session.targetUrl"
            size="small"
            placeholder="输入目标地址"
            style="flex: 1"
          >
            <template #prefix>
              <el-icon><Link /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <!-- ===== Right Panel: Steps & Controls ===== -->
      <div class="steps-panel">
        <!-- Session Info -->
        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-header-row">
              <span class="card-title">录制信息</span>
              <el-tag
                :type="statusTagMap[session.status || '']?.type || 'info'"
                size="small"
              >
                {{ statusTagMap[session.status || '']?.label || session.status || '未知' }}
              </el-tag>
            </div>
          </template>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="会话名称">
              {{ session.sessionName }}
            </el-descriptions-item>
            <el-descriptions-item label="所属系统">
              {{ session.systemName || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="目标地址" :span="2">
              <el-link
                v-if="session.targetUrl"
                type="primary"
                :href="session.targetUrl"
                target="_blank"
                :underline="false"
              >
                {{ session.targetUrl }}
              </el-link>
              <span v-else>—</span>
            </el-descriptions-item>
            <el-descriptions-item label="开始时间">
              {{ session.startedAt || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="结束时间">
              {{ session.stoppedAt || '—' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- Steps Table -->
        <el-card shadow="never" class="steps-card">
          <template #header>
            <div class="card-header-row">
              <span class="card-title">
                操作步骤
                <el-tag size="small" round effect="plain" class="step-count-tag">
                  {{ session.steps?.length || 0 }}
                </el-tag>
              </span>
              <div class="card-header-actions">
                <span class="duration-text">
                  <el-icon><Timer /></el-icon>
                  {{ formatDuration(session.durationMs) }}
                </span>
              </div>
            </div>
          </template>

          <el-table
            :data="session.steps || []"
            stripe
            size="small"
            style="width: 100%"
            :row-class-name="stepRowClassName"
            @row-click="handleStepClick"
          >
            <el-table-column label="#" width="48" align="center">
              <template #default="{ row }">
                <span
                  class="step-badge"
                  :class="{ 'step-badge--active': currentStep === row.stepOrder }"
                >
                  {{ row.stepOrder }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="时间" width="70" align="center">
              <template #default="{ row }">
                <span class="mono-text">{{ formatStepTime(row.timestamp) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <span class="action-badge">
                  {{ actionIconMap[row.action] || '📌' }}
                  {{ actionLabelMap[row.action] || row.action }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="target" label="目标" min-width="130" show-overflow-tooltip />
            <el-table-column label="值" min-width="100" show-overflow-tooltip>
              <template #default="{ row }">
                <span>{{ maskValue(row) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="120" show-overflow-tooltip>
              <template #default="{ row }">
                <span>{{ row.description || '—' }}</span>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无操作步骤" :image-size="60" />
            </template>
          </el-table>
        </el-card>

        <!-- Control Bar -->
        <el-card shadow="never" class="control-card">
          <div class="control-bar">
            <div class="control-left">
              <!-- Playback controls (for STOPPED / GENERATED) -->
              <template v-if="session.status !== 'RECORDING'">
                <el-button
                  type="primary"
                  :icon="isPlaying ? VideoPause : VideoPlay"
                  @click="togglePlayback"
                  :disabled="!session.steps?.length"
                >
                  {{ isPlaying ? '暂停回放' : '回放' }}
                </el-button>
                <el-button
                  v-if="isPlaying"
                  :icon="VideoPlay"
                  @click="stopPlayback"
                >
                  停止
                </el-button>
              </template>

              <!-- Recording controls -->
              <template v-else>
                <el-button type="warning" :icon="VideoPause">
                  暂停录制
                </el-button>
                <el-button
                  type="danger"
                  :icon="CloseBold"
                  :loading="stopLoading"
                  @click="handleStopRecording"
                >
                  停止录制
                </el-button>
                <el-button :icon="Camera" @click="handleScreenshot">
                  截图
                </el-button>
              </template>
            </div>

            <div class="control-right">
              <!-- Add step (demo mode) -->
              <el-button
                v-if="session.status === 'RECORDING'"
                size="small"
                type="primary"
                plain
                @click="handleAddMockStep"
              >
                + 模拟添加步骤
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- Action Buttons -->
        <el-card shadow="never" class="action-card">
          <div class="action-bar">
            <el-button
              type="success"
              :icon="DocumentAdd"
              :loading="generateLoading"
              @click="handleGenerateCase"
              :disabled="!session.steps?.length || session.status === 'GENERATED'"
            >
              生成案例
            </el-button>
            <el-button :icon="Check" @click="handleSave">
              保存
            </el-button>
            <el-button @click="goBack">
              返回列表
            </el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- ===== Generate Case Dialog ===== -->
    <el-dialog
      v-model="generateDialogVisible"
      title="生成测试用例"
      width="680px"
      destroy-on-close
    >
      <div class="generate-dialog">
        <el-form label-width="100px">
          <el-form-item label="案例名称">
            <el-input v-model="generatedCaseName" placeholder="AI 建议的用例名称" />
          </el-form-item>
          <el-form-item label="所属系统">
            <el-input :model-value="session.systemName || '—'" disabled />
          </el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="generatedCasePriority" style="width: 200px">
              <el-option label="P0 - 阻塞" value="P0" />
              <el-option label="P1 - 严重" value="P1" />
              <el-option label="P2 - 一般" value="P2" />
              <el-option label="P3 - 轻微" value="P3" />
            </el-select>
          </el-form-item>
        </el-form>

        <el-divider content-position="left">自然语言步骤</el-divider>
        <div class="nl-steps-list">
          <div
            v-for="(step, index) in generatedNaturalSteps"
            :key="index"
            class="nl-step-item"
          >
            <span class="nl-step-num">{{ index + 1 }}</span>
            <span class="nl-step-text">{{ step }}</span>
          </div>
        </div>

        <el-divider content-position="left">Playwright 脚本预览</el-divider>
        <div class="script-preview">
          <pre><code>{{ generatedScript }}</code></pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="confirmGenerateLoading"
          @click="handleConfirmGenerate"
        >
          <el-icon><Check /></el-icon>确认生成
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Back, Monitor, VideoPlay, VideoPause, CloseBold,
  Camera, DocumentAdd, Check, Location, Link, Refresh, Timer,
} from '@element-plus/icons-vue'
import {
  getSessionDetail,
  stopSession,
  generateCase,
  type RecordSession,
  type RecordStep,
} from '@/api/record'

const route = useRoute()
const router = useRouter()

// ==================== Status / Action Maps ====================

const statusTagMap: Record<string, { type: 'danger' | 'warning' | 'success'; label: string }> = {
  RECORDING: { type: 'danger', label: '录制中' },
  STOPPED: { type: 'warning', label: '已停止' },
  GENERATED: { type: 'success', label: '已生成' },
}

const actionIconMap: Record<string, string> = {
  click: '👆',
  input: '⌨️',
  select: '📋',
  swipe: '👉',
  wait: '⏳',
  assert: '✅',
  hover: '🎯',
  scroll: '📜',
  open: '🌐',
}

const actionLabelMap: Record<string, string> = {
  click: '点击',
  input: '输入',
  select: '选择',
  swipe: '滑动',
  wait: '等待',
  assert: '断言',
  hover: '悬停',
  scroll: '滚动',
  open: '打开',
}

const sidebarItems = [
  { icon: '📊', label: '工作台', highlightStep: -1 },
  { icon: '📋', label: '订单管理', highlightStep: -1 },
  { icon: '📦', label: '商品管理', highlightStep: -1 },
  { icon: '👥', label: '用户管理', highlightStep: -1 },
  { icon: '🔧', label: '系统设置', highlightStep: -1 },
]

// ==================== State ====================

const pageLoading = ref(false)
const stopLoading = ref(false)
const generateLoading = ref(false)
const confirmGenerateLoading = ref(false)
const generateDialogVisible = ref(false)

const currentStep = ref(0)
const isPlaying = ref(false)
let playbackTimer: ReturnType<typeof setInterval> | null = null

// ==================== Session Data ====================

const session = reactive<RecordSession>({
  sessionId: '',
  sessionName: '',
  systemName: '',
  targetUrl: '',
  status: 'RECORDING',
  steps: [],
  stepCount: 0,
  durationMs: 0,
  startedAt: '',
  stoppedAt: '',
})

// ==================== Generated Case Data ====================

const generatedCaseName = ref('')
const generatedCasePriority = ref('P2')
const generatedNaturalSteps = ref<string[]>([])
const generatedScript = ref('')

// ==================== Computed ====================

const sessionId = computed(() => Number(route.params.sessionId))

// ==================== Mock Data ====================

const mockSteps: RecordStep[] = [
  { stepOrder: 1, action: 'open', target: '/login', description: '打开登录页面', timestamp: 1000 },
  { stepOrder: 2, action: 'click', target: '#username', description: '点击用户名输入框', timestamp: 3000 },
  { stepOrder: 3, action: 'input', target: '#username', value: 'admin', description: '输入用户名 admin', timestamp: 4500 },
  { stepOrder: 4, action: 'click', target: '#password', description: '点击密码输入框', timestamp: 6000 },
  { stepOrder: 5, action: 'input', target: '#password', value: '••••••••', description: '输入密码', timestamp: 8000 },
  { stepOrder: 6, action: 'click', target: '.remember-checkbox', description: '勾选记住登录', timestamp: 9500 },
  { stepOrder: 7, action: 'click', target: '.login-btn', description: '点击登录按钮', timestamp: 11000 },
  { stepOrder: 8, action: 'wait', target: '.loading-spinner', description: '等待页面加载', timestamp: 12500 },
  { stepOrder: 9, action: 'assert', target: '.home-page', value: 'visible', description: '验证进入首页', timestamp: 14000 },
]

const mockSessionData: RecordSession = {
  id: 1,
  sessionId: 'REC-a1b2c3',
  sessionName: '登录流程验证',
  systemId: 1,
  systemName: '订单管理系统',
  targetUrl: 'https://order.example.com/login',
  status: 'RECORDING',
  steps: mockSteps,
  stepCount: mockSteps.length,
  durationMs: 14000,
  startedAt: '2026-06-12 14:30:00',
  stoppedAt: undefined,
  createdAt: '2026-06-12 14:30:00',
}

// ==================== Lifecycle ====================

onMounted(async () => {
  await fetchSessionDetail()
})

onBeforeUnmount(() => {
  stopPlayback()
})

// ==================== Data Fetching ====================

async function fetchSessionDetail() {
  pageLoading.value = true
  try {
    const res: any = await getSessionDetail(sessionId.value)
    const data = res.data || res
    Object.assign(session, {
      ...data,
      steps: data.steps || [],
    })
  } catch {
    // Fallback to mock data
    Object.assign(session, { ...mockSessionData })
  } finally {
    pageLoading.value = false
  }
}

// ==================== Playback ====================

function togglePlayback() {
  if (isPlaying.value) {
    pausePlayback()
  } else {
    startPlayback()
  }
}

function startPlayback() {
  if (!session.steps?.length) return
  isPlaying.value = true

  if (currentStep.value === 0 || currentStep.value >= (session.steps?.length || 0)) {
    currentStep.value = 1
  }

  playbackTimer = setInterval(() => {
    if (currentStep.value >= (session.steps?.length || 0)) {
      stopPlayback()
      ElMessage.success('回放完成')
      return
    }
    currentStep.value++
  }, 2000)
}

function pausePlayback() {
  isPlaying.value = false
  if (playbackTimer) {
    clearInterval(playbackTimer)
    playbackTimer = null
  }
}

function stopPlayback() {
  isPlaying.value = false
  currentStep.value = 0
  if (playbackTimer) {
    clearInterval(playbackTimer)
    playbackTimer = null
  }
}

// ==================== Step Interactions ====================

function handleStepClick(row: RecordStep) {
  if (!isPlaying.value) {
    currentStep.value = row.stepOrder
  }
}

function stepRowClassName({ row }: { row: RecordStep }) {
  const classes: string[] = []
  if (currentStep.value === row.stepOrder) {
    classes.push('step-row--active')
  }
  if (row.action === 'assert') {
    classes.push('step-row--assert')
  }
  return classes.join(' ')
}

function isStepHighlighting(step: number): boolean {
  return currentStep.value === step
}

// ==================== Recording Controls ====================

async function handleStopRecording() {
  try {
    await ElMessageBox.confirm(
      '确定停止当前录制？停止后可生成测试用例。',
      '停止录制',
      { confirmButtonText: '停止', cancelButtonText: '继续录制', type: 'warning' },
    )
    stopLoading.value = true
    try {
      await stopSession(sessionId.value)
    } catch {
      // API may not be available
    }
    session.status = 'STOPPED'
    session.stoppedAt = new Date().toLocaleString('zh-CN', {
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit', second: '2-digit',
    }).replace(/\//g, '-')
    ElMessage.success('录制已停止')
  } catch {
    // Cancelled
  } finally {
    stopLoading.value = false
  }
}

function handleScreenshot() {
  ElMessage.success('截图已保存（演示模式）')
}

function handleAddMockStep() {
  if (!session.steps) session.steps = []
  const newOrder = session.steps.length + 1
  const lastTimestamp = session.steps.length
    ? session.steps[session.steps.length - 1].timestamp || 0
    : 0
  const newStep: RecordStep = {
    stepOrder: newOrder,
    action: 'click',
    target: `.element-${newOrder}`,
    description: `操作步骤 ${newOrder}`,
    timestamp: lastTimestamp + 2000,
  }
  session.steps.push(newStep)
  session.stepCount = session.steps.length
  session.durationMs = newStep.timestamp
  ElMessage.success(`已添加步骤 ${newOrder}`)
}

// ==================== Generate Case ====================

function handleGenerateCase() {
  // Build natural language steps from recorded steps
  generatedNaturalSteps.value = (session.steps || []).map((step) => {
    if (step.description) return step.description
    const icon = actionIconMap[step.action] || '📌'
    const label = actionLabelMap[step.action] || step.action
    return step.value ? `${icon} ${label} ${step.target} → ${step.value}` : `${icon} ${label} ${step.target}`
  })

  // Generate case name
  generatedCaseName.value = `${session.sessionName} - 自动化用例`
  generatedCasePriority.value = 'P2'

  // Build Playwright script
  const safeName = session.sessionName.replace(/[^a-zA-Z0-9一-龥]/g, '-').slice(0, 30)
  const scriptLines = [
    `import { test, expect } from '@playwright/test'`,
    ``,
    `test('${session.sessionName}', async ({ page }) => {`,
  ]
  for (const step of session.steps || []) {
    switch (step.action) {
      case 'open':
        scriptLines.push(`  await page.goto('${step.target}')`)
        break
      case 'click':
        scriptLines.push(`  await page.click('${step.target}')`)
        break
      case 'input':
        scriptLines.push(`  await page.fill('${step.target}', '${step.value || ''}')`)
        break
      case 'select':
        scriptLines.push(`  await page.selectOption('${step.target}', '${step.value || ''}')`)
        break
      case 'hover':
        scriptLines.push(`  await page.hover('${step.target}')`)
        break
      case 'scroll':
        scriptLines.push(`  await page.evaluate(() => window.scrollBy(0, 300))`)
        break
      case 'wait':
        scriptLines.push(`  await page.waitForSelector('${step.target}')`)
        break
      case 'assert':
        scriptLines.push(`  await expect(page.locator('${step.target}')).toBeVisible()`)
        break
      default:
        scriptLines.push(`  // ${step.action}: ${step.target}`)
    }
  }
  scriptLines.push(`})`)
  generatedScript.value = scriptLines.join('\n')

  generateDialogVisible.value = true
}

async function handleConfirmGenerate() {
  confirmGenerateLoading.value = true
  try {
    await generateCase(sessionId.value)
    ElMessage.success('测试用例生成成功')
    session.status = 'GENERATED'
    generateDialogVisible.value = false
  } catch {
    // Fallback for demo
    ElMessage.success('测试用例生成成功（演示模式）')
    session.status = 'GENERATED'
    generateDialogVisible.value = false
  } finally {
    confirmGenerateLoading.value = false
  }
}

// ==================== Save / Navigation ====================

function handleSave() {
  ElMessage.success('保存成功')
}

function goBack() {
  router.push('/record')
}

// ==================== Utilities ====================

function formatDuration(ms?: number): string {
  if (!ms) return '00:00'
  const totalSeconds = Math.floor(ms / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

function formatStepTime(timestamp?: number): string {
  if (!timestamp) return '00:00'
  const totalSeconds = Math.floor(timestamp / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

function maskValue(row: RecordStep): string {
  if (!row.value) return '—'
  // Mask password fields
  if (row.target?.toLowerCase().includes('password')) {
    return '••••••••'
  }
  return row.value
}
</script>

<style scoped lang="scss">
.record-detail-page {
  /* ===== Page Header ===== */
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 12px 20px;
    background: var(--el-bg-color);
    border-radius: 4px;
    border: 1px solid var(--el-border-color-lighter);

    .page-header-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .page-title {
        font-size: 16px;
        font-weight: 600;
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .session-id-tag {
        font-weight: 400;
        font-size: 12px;
      }
    }

    .page-header-right {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  /* ===== Recording Indicator ===== */
  .recording-indicator {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    color: var(--el-color-danger);
    font-size: 13px;
    font-weight: 600;
    padding: 2px 10px;
    border-radius: 12px;
    background: var(--el-color-danger-light-9);
    animation: pulse-indicator 2s ease-in-out infinite;
  }

  .status-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background-color: var(--el-color-danger);
    animation: pulse-dot 1.5s ease-in-out infinite;
  }

  /* ===== Split Layout ===== */
  .detail-body {
    display: flex;
    gap: 16px;
    align-items: flex-start;
  }

  /* ===== Browser Panel (Left 40%) ===== */
  .browser-panel {
    width: 40%;
    flex-shrink: 0;
  }

  .browser-frame {
    border: 1px solid var(--el-border-color);
    border-radius: 8px;
    overflow: hidden;
    background: var(--el-bg-color);
  }

  .browser-chrome {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 12px;
    background: var(--el-fill-color-light);
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .browser-dots {
    display: flex;
    gap: 6px;
    flex-shrink: 0;

    .dot {
      width: 11px;
      height: 11px;
      border-radius: 50%;

      &--red { background: #ff5f57; }
      &--yellow { background: #febc2e; }
      &--green { background: #28c840; }
    }
  }

  .browser-url {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 10px;
    background: var(--el-bg-color);
    border-radius: 4px;
    border: 1px solid var(--el-border-color-lighter);
    font-size: 12px;
    color: var(--el-text-color-secondary);
    overflow: hidden;

    .url-text {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .browser-actions {
    flex-shrink: 0;

    .browser-action-icon {
      cursor: pointer;
      color: var(--el-text-color-secondary);

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  /* ===== Browser Content — Mock Page ===== */
  .browser-content {
    height: 480px;
    overflow: hidden;
    background: #f5f7fa;
  }

  .mock-login-page {
    display: flex;
    height: 100%;
    font-size: 11px;
  }

  .mock-sidebar {
    width: 130px;
    background: #1d1e2c;
    color: #c0c4cc;
    padding: 0;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;

    .mock-sidebar-logo {
      padding: 14px 12px;
      font-size: 12px;
      font-weight: 600;
      color: #fff;
      border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    }

    .mock-sidebar-item {
      padding: 10px 12px;
      display: flex;
      align-items: center;
      gap: 8px;
      transition: background 0.2s;

      &:hover {
        background: rgba(255, 255, 255, 0.05);
      }

      .mock-sidebar-icon {
        font-size: 13px;
      }

      .mock-sidebar-text {
        font-size: 11px;
      }
    }
  }

  .mock-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;
  }

  .mock-topbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 16px;
    background: #fff;
    border-bottom: 1px solid #e4e7ed;
    font-size: 11px;
    color: #606266;

    .mock-breadcrumb {
      color: #909399;
    }

    .mock-user-area {
      color: #606266;
    }
  }

  .mock-content-body {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
  }

  .mock-login-card {
    width: 220px;
    background: #fff;
    border-radius: 8px;
    padding: 20px 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    border: 2px solid transparent;
    transition: border-color 0.3s, box-shadow 0.3s;

    .mock-login-header {
      text-align: center;
      margin-bottom: 16px;

      .mock-login-logo {
        font-size: 22px;
        display: block;
        margin-bottom: 4px;
      }

      .mock-login-title {
        font-size: 13px;
        font-weight: 600;
        color: #303133;
      }
    }

    .mock-input-group {
      margin-bottom: 10px;
      border-radius: 4px;
      transition: box-shadow 0.3s;

      .mock-input-label {
        font-size: 10px;
        color: #909399;
        margin-bottom: 3px;
        display: block;
      }

      .mock-input-box {
        padding: 6px 8px;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        font-size: 11px;
        color: #303133;
        min-height: 26px;
        display: flex;
        align-items: center;
        background: #fff;

        .mock-input-text {
          flex: 1;
        }

        .mock-cursor-blink {
          display: inline-block;
          width: 1px;
          height: 14px;
          background: var(--el-color-primary);
          animation: blink-cursor 0.8s step-end infinite;
        }
      }
    }

    .mock-remember-row {
      margin-bottom: 12px;
      font-size: 10px;
      color: #909399;
      border-radius: 3px;
      transition: box-shadow 0.3s;

      .mock-checkbox {
        cursor: default;
      }
    }

    .mock-login-btn {
      padding: 8px;
      text-align: center;
      background: var(--el-color-primary, #409eff);
      color: #fff;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 500;
      cursor: default;
      transition: background 0.3s, box-shadow 0.3s, transform 0.2s;

      &--active {
        background: var(--el-color-primary-light-3, #79bbff);
        transform: scale(0.98);
      }
    }
  }

  .mock-footer {
    padding: 8px;
    text-align: center;
    font-size: 10px;
    color: #c0c4cc;
    border-top: 1px solid #e4e7ed;
    background: #fff;
  }

  /* Highlight effect for mock elements */
  .mock-highlight {
    box-shadow: 0 0 0 2px var(--el-color-warning, #e6a23c), 0 0 12px rgba(230, 162, 60, 0.3) !important;
    border-radius: 4px;
    transition: box-shadow 0.3s ease;
  }

  /* Target URL Bar */
  .target-url-bar {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 12px;
    padding: 8px 12px;
    background: var(--el-fill-color-light);
    border-radius: 4px;

    .url-bar-label {
      font-size: 13px;
      color: var(--el-text-color-secondary);
      white-space: nowrap;
      flex-shrink: 0;
    }
  }

  /* ===== Steps Panel (Right 60%) ===== */
  .steps-panel {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  /* ===== Info Card ===== */
  .info-card {
    .card-title {
      font-size: 14px;
      font-weight: 600;
    }

    .card-header-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  /* ===== Steps Card ===== */
  .steps-card {
    .card-title {
      font-size: 14px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .card-header-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .card-header-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .step-count-tag {
      font-weight: 600;
    }

    .duration-text {
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
      font-size: 13px;
      color: var(--el-text-color-secondary);
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  /* Step Badge */
  .step-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background: var(--el-color-primary-light-8);
    color: var(--el-color-primary);
    font-size: 11px;
    font-weight: 600;
    transition: all 0.3s;

    &--active {
      background: var(--el-color-primary);
      color: #fff;
      transform: scale(1.15);
      box-shadow: 0 0 8px rgba(64, 158, 255, 0.4);
    }
  }

  /* Action Badge */
  .action-badge {
    font-size: 12px;
    white-space: nowrap;
  }

  /* Mono Text */
  .mono-text {
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    font-size: 12px;
    color: var(--el-text-color-regular);
  }

  /* ===== Control Card ===== */
  .control-card {
    .control-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .control-left {
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .control-right {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
  }

  /* ===== Action Card ===== */
  .action-card {
    .action-bar {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  /* ===== Table Row Styles ===== */
  :deep(.el-table) {
    .el-table__row {
      cursor: pointer;
      transition: background 0.2s;
    }

    .step-row--active {
      background-color: var(--el-color-primary-light-9) !important;

      td {
        font-weight: 500;
      }
    }

    .step-row--assert {
      .action-badge {
        color: var(--el-color-success);
      }
    }
  }

  /* ===== Generate Dialog ===== */
  .generate-dialog {
    .nl-steps-list {
      max-height: 240px;
      overflow-y: auto;
      border: 1px solid var(--el-border-color-lighter);
      border-radius: 4px;
      padding: 8px;
      background: var(--el-fill-color-lighter);

      .nl-step-item {
        display: flex;
        align-items: flex-start;
        gap: 10px;
        padding: 6px 8px;
        border-radius: 4px;

        &:hover {
          background: var(--el-fill-color);
        }

        .nl-step-num {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          min-width: 22px;
          height: 22px;
          border-radius: 50%;
          background: var(--el-color-primary-light-8);
          color: var(--el-color-primary);
          font-size: 11px;
          font-weight: 600;
          flex-shrink: 0;
        }

        .nl-step-text {
          font-size: 13px;
          line-height: 22px;
          color: var(--el-text-color-regular);
        }
      }
    }

    .script-preview {
      background: #1e1e2e;
      border-radius: 6px;
      padding: 16px;
      overflow-x: auto;
      max-height: 300px;

      pre {
        margin: 0;

        code {
          font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
          font-size: 12px;
          line-height: 1.7;
          color: #cdd6f4;
          white-space: pre;
        }
      }
    }
  }
}

/* ===== Animations ===== */
@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(1.4); }
}

@keyframes pulse-indicator {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.65; }
}

@keyframes blink-cursor {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* ===== Responsive ===== */
@media (max-width: 1200px) {
  .record-detail-page {
    .detail-body {
      flex-direction: column;
    }

    .browser-panel {
      width: 100%;
    }
  }
}
</style>
