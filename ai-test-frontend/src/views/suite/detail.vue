<template>
  <div class="suite-detail-container">
    <!-- Header -->
    <div class="page-header">
      <div class="page-header-left">
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
        <span class="page-title">
          <el-icon><FolderOpened /></el-icon>
          套件详情
          <el-tag v-if="suite.suiteType" :type="suiteTypeTagMap[suite.suiteType]?.type || 'info'" size="small" effect="plain">
            {{ suiteTypeTagMap[suite.suiteType]?.label || suite.suiteType }}
          </el-tag>
        </span>
      </div>
      <div class="page-header-right">
        <el-button type="success" :icon="VideoPlay" @click="handleExecute" :loading="executeLoading">执行套件</el-button>
      </div>
    </div>

    <!-- Suite Info Card -->
    <el-card shadow="never" class="section-card" v-loading="pageLoading">
      <el-descriptions :column="4" border size="small">
        <el-descriptions-item label="套件名称">{{ suite.suiteName }}</el-descriptions-item>
        <el-descriptions-item label="所属系统">{{ suite.systemName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="套件类型">
          <el-tag :type="suiteTypeTagMap[suite.suiteType || 'custom']?.type || 'info'" size="small" effect="plain">
            {{ suiteTypeTagMap[suite.suiteType || 'custom']?.label || suite.suiteType }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="suite.status === 'active' ? 'success' : 'info'" size="small">
            {{ suite.status === 'active' ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用例数量">{{ suite.caseCount ?? suiteCases.length }}</el-descriptions-item>
        <el-descriptions-item label="最近执行">{{ suite.lastExecutionTime || '未执行' }}</el-descriptions-item>
        <el-descriptions-item label="通过率">{{ suite.passRate != null ? suite.passRate + '%' : '—' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ suite.createdAt || '—' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="4">{{ suite.description || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- Main Content: Left (Cases) + Right (Execution History) -->
    <div class="main-content">
      <!-- Left Panel: Case List -->
      <div class="left-panel">
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header-row">
              <span class="card-title">关联用例（{{ suiteCases.length }}）</span>
              <el-button type="primary" size="small" :icon="Plus" @click="addCaseDialogVisible = true">
                添加用例
              </el-button>
            </div>
          </template>

          <div v-if="suiteCases.length === 0" class="empty-cases">
            <el-empty description="暂无关联用例，点击上方按钮添加">
              <template #image>
                <el-icon :size="48" color="var(--el-color-info)"><Document /></el-icon>
              </template>
            </el-empty>
          </div>

          <div v-else class="case-list-draggable">
            <div
              v-for="(item, index) in suiteCases"
              :key="item.caseId"
              class="case-item"
              draggable="true"
              @dragstart="onDragStart(index, $event)"
              @dragover.prevent="onDragOver(index, $event)"
              @drop="onDrop(index)"
              @dragend="onDragEnd"
              :class="{ 'dragging': dragIndex === index, 'drag-over': dragOverIndex === index }"
            >
              <div class="case-item-left">
                <el-icon class="drag-handle"><Rank /></el-icon>
                <span class="case-order">{{ index + 1 }}</span>
                <span class="case-name" @click="goToCase(item.caseId)">{{ item.caseName }}</span>
              </div>
              <div class="case-item-right">
                <el-tag v-if="item.priority" :type="priorityTagMap[item.priority]?.type || 'info'" size="small">
                  {{ item.priority }}
                </el-tag>
                <el-tag v-if="item.caseType" size="small" effect="plain">
                  {{ item.caseType }}
                </el-tag>
                <el-button text type="danger" size="small" @click="handleRemoveCase(item.caseId)">
                  <el-icon><Close /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- Right Panel: Execution History -->
      <div class="right-panel">
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="card-title">执行历史</span>
          </template>
          <el-table :data="executionHistory" stripe size="small" style="width: 100%" v-loading="historyLoading">
            <el-table-column prop="executionName" label="执行名称" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="exec-link" @click="goToExecution(row.id)">{{ row.executionName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="execStatusTagMap[row.status]?.type || 'info'" size="small">
                  <template v-if="row.status === 'running'">
                    <el-icon class="is-loading"><Loading /></el-icon>
                  </template>
                  {{ execStatusTagMap[row.status]?.label || row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="passRate" label="通过率" width="90" align="center">
              <template #default="{ row }">
                <span :style="{ color: getPassRateColor(row.passRate) }">{{ row.passRate }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="durationText" label="耗时" width="90" />
            <el-table-column prop="startedAt" label="执行时间" width="160" />
          </el-table>
          <el-empty v-if="executionHistory.length === 0 && !historyLoading" description="暂无执行记录" :image-size="60" />
        </el-card>
      </div>
    </div>

    <!-- Add Cases Dialog -->
    <el-dialog v-model="addCaseDialogVisible" title="添加用例到套件" width="700px" destroy-on-close>
      <div class="add-case-toolbar">
        <el-input
          v-model="caseSearchKeyword"
          placeholder="搜索用例名称"
          :prefix-icon="Search"
          clearable
          style="width: 260px"
          @input="filterAvailableCases"
        />
      </div>
      <el-table
        ref="caseSelectTableRef"
        :data="availableCases"
        stripe
        size="small"
        style="width: 100%"
        @selection-change="handleCaseSelectionChange"
        max-height="400"
      >
        <el-table-column type="selection" width="45" />
        <el-table-column prop="caseName" label="用例名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="caseType" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.caseType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="priorityTagMap[row.priority]?.type || 'info'" size="small">{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="systemName" label="所属系统" width="120" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button @click="addCaseDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="selectedCaseIds.length === 0" :loading="addCaseLoading" @click="handleAddCases">
          添加已选（{{ selectedCaseIds.length }}）
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, VideoPlay, FolderOpened, Plus, Document, Rank, Close, Search, Loading,
} from '@element-plus/icons-vue'
import {
  getSuiteDetail,
  getSuiteCases,
  addCasesToSuite,
  removeCasesFromSuite,
  executeSuite,
  getExecutionList,
  getSystemOptions,
  type TestSuite,
  type TestSuiteCase,
  type TestExecution,
} from '@/api/suite'
import { getCaseList, type TestCase } from '@/api/case'

const route = useRoute()
const router = useRouter()

const suiteId = computed(() => Number(route.params.id))
const pageLoading = ref(false)
const executeLoading = ref(false)
const historyLoading = ref(false)
const addCaseLoading = ref(false)
const addCaseDialogVisible = ref(false)
const caseSearchKeyword = ref('')
const selectedCaseIds = ref<number[]>([])
const caseSelectTableRef = ref<any>(null)

// Drag state
const dragIndex = ref<number | null>(null)
const dragOverIndex = ref<number | null>(null)

const suiteTypeTagMap: Record<string, { type: 'primary' | 'success' | 'warning' | 'danger'; label: string }> = {
  smoke: { type: 'success', label: '冒烟测试' },
  regression: { type: 'primary', label: '回归测试' },
  full: { type: 'warning', label: '全量测试' },
  custom: { type: 'danger', label: '自定义' },
}

const priorityTagMap: Record<string, { type: 'danger' | 'warning' | 'primary' | 'info' }> = {
  P0: { type: 'danger' },
  P1: { type: 'warning' },
  P2: { type: 'primary' },
  P3: { type: 'info' },
}

const execStatusTagMap: Record<string, { type: 'info' | 'success' | 'warning' | 'danger'; label: string }> = {
  running: { type: 'warning', label: '执行中' },
  success: { type: 'success', label: '成功' },
  failed: { type: 'danger', label: '失败' },
  cancelled: { type: 'info', label: '已取消' },
  pending: { type: 'info', label: '等待中' },
}

const suite = reactive<Partial<TestSuite>>({
  suiteName: '',
  suiteType: 'custom',
  status: 'active',
})

const suiteCases = ref<TestSuiteCase[]>([])
const executionHistory = ref<TestExecution[]>([])
const availableCases = ref<TestCase[]>([])

onMounted(async () => {
  if (suiteId.value) {
    await fetchSuiteDetail()
    await fetchSuiteCases()
    await fetchExecutionHistory()
  }
})

async function fetchSuiteDetail() {
  pageLoading.value = true
  try {
    const res: any = await getSuiteDetail(suiteId.value)
    const data = res.data || res
    Object.assign(suite, data)
  } catch {
    // handled by interceptor
  } finally {
    pageLoading.value = false
  }
}

async function fetchSuiteCases() {
  try {
    const res: any = await getSuiteCases(suiteId.value)
    suiteCases.value = (res.data || res || []).sort((a: TestSuiteCase, b: TestSuiteCase) => a.sortOrder - b.sortOrder)
  } catch {
    // handled by interceptor
  }
}

async function fetchExecutionHistory() {
  historyLoading.value = true
  try {
    const res: any = await getExecutionList({ suiteId: suiteId.value, pageSize: 20 })
    executionHistory.value = res.data?.list || res.list || []
  } catch {
    // handled by interceptor
  } finally {
    historyLoading.value = false
  }
}

async function filterAvailableCases() {
  // Re-fetch available cases with keyword filter
  try {
    const res: any = await getCaseList({
      pageSize: 100,
      keyword: caseSearchKeyword.value || undefined,
    })
    const allCases: TestCase[] = res.data?.list || res.list || []
    const existingIds = new Set(suiteCases.value.map(c => c.caseId))
    availableCases.value = allCases.filter(c => !existingIds.has(c.id!))
  } catch {
    // non-critical
  }
}

// Open the add-case dialog and load available cases
watch(addCaseDialogVisible, async (val) => {
  if (val) {
    caseSearchKeyword.value = ''
    selectedCaseIds.value = []
    await filterAvailableCases()
  }
})

function handleCaseSelectionChange(rows: TestCase[]) {
  selectedCaseIds.value = rows.map(r => r.id!).filter(Boolean)
}

async function handleAddCases() {
  if (selectedCaseIds.value.length === 0) return
  addCaseLoading.value = true
  try {
    await addCasesToSuite(suiteId.value, selectedCaseIds.value)
    ElMessage.success('添加成功')
    addCaseDialogVisible.value = false
    await fetchSuiteCases()
  } catch {
    // handled by interceptor
  } finally {
    addCaseLoading.value = false
  }
}

async function handleRemoveCase(caseId: number) {
  try {
    await ElMessageBox.confirm('确定将该用例从套件中移除？', '确认移除', {
      confirmButtonText: '移除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await removeCasesFromSuite(suiteId.value, [caseId])
    ElMessage.success('移除成功')
    await fetchSuiteCases()
  } catch {
    // cancelled or error
  }
}

// Drag to reorder
function onDragStart(index: number, e: DragEvent) {
  dragIndex.value = index
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = 'move'
  }
}

function onDragOver(index: number, e: DragEvent) {
  dragOverIndex.value = index
  if (e.dataTransfer) {
    e.dataTransfer.dropEffect = 'move'
  }
}

function onDrop(index: number) {
  if (dragIndex.value === null || dragIndex.value === index) return
  const item = suiteCases.value.splice(dragIndex.value, 1)[0]
  suiteCases.value.splice(index, 0, item)
  // Update sort orders
  suiteCases.value.forEach((c, i) => { c.sortOrder = i + 1 })
  dragIndex.value = null
  dragOverIndex.value = null
}

function onDragEnd() {
  dragIndex.value = null
  dragOverIndex.value = null
}

async function handleExecute() {
  try {
    await ElMessageBox.confirm('确定执行该测试套件？', '确认执行', {
      confirmButtonText: '执行',
      cancelButtonText: '取消',
      type: 'info',
    })
    executeLoading.value = true
    await executeSuite(suiteId.value)
    ElMessage.success('套件已提交执行')
    setTimeout(fetchExecutionHistory, 2000)
  } catch {
    // cancelled or error
  } finally {
    executeLoading.value = false
  }
}

function goBack() {
  router.push('/suites')
}

function goToCase(caseId: number) {
  router.push(`/cases/${caseId}`)
}

function goToExecution(executionId: number) {
  router.push(`/executions/${executionId}`)
}

function getPassRateColor(rate: number): string {
  if (rate >= 90) return '#67c23a'
  if (rate >= 70) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped lang="scss">
.suite-detail-container {
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
    }

    .page-header-right {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .section-card {
    margin-bottom: 16px;

    .card-title {
      font-size: 15px;
      font-weight: 600;
    }

    .card-header-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .main-content {
    display: flex;
    gap: 16px;

    .left-panel {
      flex: 1;
      min-width: 0;
    }

    .right-panel {
      flex: 1.2;
      min-width: 0;
    }
  }

  .case-list-draggable {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .case-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 12px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 6px;
    background: var(--el-bg-color);
    cursor: grab;
    transition: all 0.2s ease;

    &:hover {
      border-color: var(--el-color-primary-light-5);
      background: var(--el-color-primary-light-9);
    }

    &.dragging {
      opacity: 0.4;
      border-style: dashed;
    }

    &.drag-over {
      border-color: var(--el-color-primary);
      box-shadow: 0 0 0 2px var(--el-color-primary-light-7);
    }

    .case-item-left {
      display: flex;
      align-items: center;
      gap: 8px;

      .drag-handle {
        color: var(--el-text-color-placeholder);
        cursor: grab;
      }

      .case-order {
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
        flex-shrink: 0;
      }

      .case-name {
        font-size: 13px;
        color: var(--el-color-primary);
        cursor: pointer;
        &:hover { text-decoration: underline; }
      }
    }

    .case-item-right {
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }

  .empty-cases {
    padding: 20px 0;
  }

  .add-case-toolbar {
    margin-bottom: 12px;
  }

  .exec-link {
    color: var(--el-color-primary);
    cursor: pointer;
    font-weight: 500;
    &:hover { text-decoration: underline; }
  }
}
</style>
