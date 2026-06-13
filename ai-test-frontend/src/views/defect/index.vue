<template>
  <div class="defect-list-container">
    <el-card shadow="never">
      <!-- ==================== Page Header ==================== -->
      <div class="page-header">
        <h3 class="page-title">缺陷管理</h3>
        <div class="toolbar-actions">
          <el-button type="primary" :icon="Plus" @click="handleCreate">新建缺陷</el-button>
          <el-button :icon="DataAnalysis" @click="goRiskAnalysis">风险分析</el-button>
        </div>
      </div>

      <!-- ==================== Stats Cards ==================== -->
      <div class="stats-cards">
        <div
          v-for="card in statsCards"
          :key="card.key"
          class="stats-card"
          :class="card.class"
          @click="handleStatsCardClick(card)"
        >
          <div class="stats-icon">{{ card.icon }}</div>
          <div class="stats-info">
            <div class="stats-count">{{ card.count }}</div>
            <div class="stats-label">{{ card.label }}</div>
          </div>
        </div>
      </div>

      <!-- ==================== Search Toolbar ==================== -->
      <div class="search-toolbar">
        <div class="search-left">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索缺陷编号/标题"
            :prefix-icon="Search"
            clearable
            style="width: 220px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-select
            v-model="queryParams.systemId"
            placeholder="所属系统"
            clearable
            filterable
            style="width: 160px"
            @change="handleSearch"
          >
            <el-option
              v-for="sys in systemOptions"
              :key="sys.id"
              :label="sys.name"
              :value="sys.id"
            />
          </el-select>
          <el-select
            v-model="queryParams.severity"
            placeholder="严重度"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="S1 - 致命" value="S1" />
            <el-option label="S2 - 严重" value="S2" />
            <el-option label="S3 - 一般" value="S3" />
            <el-option label="S4 - 轻微" value="S4" />
          </el-select>
          <el-select
            v-model="queryParams.priority"
            placeholder="优先级"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="P0 - 紧急" value="P0" />
            <el-option label="P1 - 高" value="P1" />
            <el-option label="P2 - 中" value="P2" />
            <el-option label="P3 - 低" value="P3" />
          </el-select>
          <el-select
            v-model="queryParams.status"
            placeholder="状态"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="打开" value="OPEN" />
            <el-option label="已修复" value="FIXED" />
            <el-option label="已验证" value="VERIFIED" />
            <el-option label="已关闭" value="CLOSED" />
            <el-option label="重新打开" value="REOPENED" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>

      <!-- ==================== Data Table ==================== -->
      <el-table
        :data="tableData"
        stripe
        v-loading="loading"
        style="width: 100%"
        row-key="id"
        @row-click="handleRowClick"
      >
        <el-table-column prop="defectCode" label="编号" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="defect-code-link">{{ row.defectCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="defectTitle" label="标题" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="defect-title-link">{{ row.defectTitle }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="severity" label="严重度" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="severityTagMap[row.severity]?.type || 'info'" size="small" effect="dark">
              {{ severityTagMap[row.severity]?.label || row.severity }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="priorityTagMap[row.priority]?.type || 'info'" size="small" effect="plain">
              {{ priorityTagMap[row.priority]?.label || row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="systemName" label="系统" width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status]?.type || 'info'" size="small">
              {{ statusTagMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assignedToName" label="指派" width="90" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="110">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" @click.stop>
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click.stop="handleEdit(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-dropdown trigger="click" @command="(cmd: string) => handleStatusChange(row, cmd)" @click.stop>
              <el-button text type="primary" size="small" @click.stop>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="row.status !== 'FIXED'" command="FIXED">
                    <el-icon><CircleCheck /></el-icon>修复
                  </el-dropdown-item>
                  <el-dropdown-item v-if="row.status === 'FIXED'" command="VERIFIED">
                    <el-icon><Select /></el-icon>验证
                  </el-dropdown-item>
                  <el-dropdown-item v-if="row.status !== 'CLOSED'" command="CLOSED">
                    <el-icon><CircleClose /></el-icon>关闭
                  </el-dropdown-item>
                  <el-dropdown-item v-if="row.status === 'FIXED' || row.status === 'VERIFIED'" command="REOPENED">
                    <el-icon><RefreshRight /></el-icon>重新打开
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-popconfirm
              title="确定删除该缺陷吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button text type="danger" size="small" @click.stop>
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- ==================== Pagination ==================== -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- ==================== Detail Drawer ==================== -->
    <el-drawer
      v-model="drawerVisible"
      title="缺陷详情"
      size="600px"
      direction="rtl"
      destroy-on-close
    >
      <div v-if="currentDefect" class="detail-content">
          <div class="detail-section">
            <div class="detail-row">
              <span class="detail-label">缺陷编号</span>
              <span class="detail-value">{{ currentDefect.defectCode }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">缺陷标题</span>
              <span class="detail-value detail-title">{{ currentDefect.defectTitle }}</span>
            </div>
            <div class="detail-row-inline">
              <div class="detail-row half">
                <span class="detail-label">严重度</span>
                <el-tag :type="severityTagMap[currentDefect.severity || '']?.type || 'info'" size="small" effect="dark">
                  {{ severityTagMap[currentDefect.severity || '']?.label || currentDefect.severity }}
                </el-tag>
              </div>
              <div class="detail-row half">
                <span class="detail-label">优先级</span>
                <el-tag :type="priorityTagMap[currentDefect.priority || '']?.type || 'info'" size="small" effect="plain">
                  {{ priorityTagMap[currentDefect.priority || '']?.label || currentDefect.priority }}
                </el-tag>
              </div>
            </div>
            <div class="detail-row-inline">
              <div class="detail-row half">
                <span class="detail-label">状态</span>
                <el-tag :type="statusTagMap[currentDefect.status || '']?.type || 'info'" size="small">
                  {{ statusTagMap[currentDefect.status || '']?.label || currentDefect.status }}
                </el-tag>
              </div>
              <div class="detail-row half">
                <span class="detail-label">所属系统</span>
                <span class="detail-value">{{ currentDefect.systemName || '—' }}</span>
              </div>
            </div>
            <div class="detail-row-inline">
              <div class="detail-row half">
                <span class="detail-label">指派人</span>
                <span class="detail-value">{{ currentDefect.assignedToName || '—' }}</span>
              </div>
              <div class="detail-row half">
                <span class="detail-label">创建时间</span>
                <span class="detail-value">{{ currentDefect.createdAt || '—' }}</span>
              </div>
            </div>
          </div>

          <el-divider />

          <div class="detail-section">
            <h4 class="section-title">缺陷描述</h4>
            <p class="detail-text">{{ currentDefect.description || '暂无描述' }}</p>
          </div>

          <div class="detail-section">
            <h4 class="section-title">复现步骤</h4>
            <p class="detail-text detail-pre">{{ currentDefect.stepsToReproduce || '—' }}</p>
          </div>

          <div class="detail-section">
            <h4 class="section-title">预期结果</h4>
            <p class="detail-text">{{ currentDefect.expectedResult || '—' }}</p>
          </div>

          <div class="detail-section">
            <h4 class="section-title">实际结果</h4>
            <p class="detail-text">{{ currentDefect.actualResult || '—' }}</p>
          </div>

          <div v-if="currentDefect.environment" class="detail-section">
            <h4 class="section-title">环境信息</h4>
            <p class="detail-text">{{ currentDefect.environment }}</p>
          </div>

          <el-divider />

          <div class="detail-section">
            <h4 class="section-title">状态流转</h4>
            <el-timeline>
              <el-timeline-item
                v-for="(item, index) in statusTimeline"
                :key="index"
                :type="item.type"
                :timestamp="item.time"
                placement="top"
              >
                {{ item.content }}
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>

      <template #footer>
        <template v-if="currentDefect">
          <el-button type="primary" @click="handleEdit(currentDefect)">编辑</el-button>
          <el-button @click="drawerVisible = false">关闭</el-button>
        </template>
      </template>
    </el-drawer>

    <!-- ==================== Create / Edit Dialog ==================== -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑缺陷' : '新建缺陷'"
      width="700px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
        label-position="right"
      >
        <el-form-item label="缺陷标题" prop="defectTitle">
          <el-input v-model="formData.defectTitle" placeholder="请输入缺陷标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属系统" prop="systemId">
              <el-select v-model="formData.systemId" placeholder="请选择系统" filterable style="width: 100%">
                <el-option
                  v-for="sys in systemOptions"
                  :key="sys.id"
                  :label="sys.name"
                  :value="sys.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="指派给" prop="assignedToName">
              <el-input v-model="formData.assignedToName" placeholder="请输入指派人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="严重度" prop="severity">
              <el-select v-model="formData.severity" placeholder="请选择严重度" style="width: 100%">
                <el-option label="S1 - 致命" value="S1" />
                <el-option label="S2 - 严重" value="S2" />
                <el-option label="S3 - 一般" value="S3" />
                <el-option label="S4 - 轻微" value="S4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="formData.priority" placeholder="请选择优先级" style="width: 100%">
                <el-option label="P0 - 紧急" value="P0" />
                <el-option label="P1 - 高" value="P1" />
                <el-option label="P2 - 中" value="P2" />
                <el-option label="P3 - 低" value="P3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="缺陷描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入缺陷描述" />
        </el-form-item>
        <el-form-item label="复现步骤" prop="stepsToReproduce">
          <el-input v-model="formData.stepsToReproduce" type="textarea" :rows="3" placeholder="请输入复现步骤" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="预期结果" prop="expectedResult">
              <el-input v-model="formData.expectedResult" type="textarea" :rows="2" placeholder="请输入预期结果" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际结果" prop="actualResult">
              <el-input v-model="formData.actualResult" type="textarea" :rows="2" placeholder="请输入实际结果" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="环境信息" prop="environment">
          <el-input v-model="formData.environment" placeholder="如: Chrome 120 / Windows 11 / 测试环境" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  Delete,
  MoreFilled,
  DataAnalysis,
  CircleCheck,
  CircleClose,
  Select,
  RefreshRight,
} from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  getDefectList,
  createDefect,
  updateDefect,
  deleteDefect,
  updateDefectStatus,
  getDefectStatistics,
  getSystemOptions,
  type Defect,
  type DefectQuery,
  type Severity,
  type DefectStatistics,
} from '@/api/defect'

const router = useRouter()

// ==================== State ====================

const loading = ref(false)
const submitting = ref(false)
const drawerVisible = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const queryParams = reactive<DefectQuery>({
  page: 1,
  size: 20,
  keyword: '',
  systemId: '',
  severity: '',
  priority: '',
  status: '',
})

const pagination = reactive({ total: 0 })
const tableData = ref<Defect[]>([])
const systemOptions = ref<Array<{ id: number; name: string }>>([])
const statistics = ref<DefectStatistics>({
  totalCount: 0,
  bySeverity: {},
  byStatus: {},
  byPriority: {},
})

const currentDefect = ref<Defect | null>(null)

// ==================== Tag Maps ====================

const severityTagMap: Record<string, { type: 'danger' | 'warning' | 'info' | 'primary'; label: string }> = {
  S1: { type: 'danger', label: 'S1 致命' },
  S2: { type: 'warning', label: 'S2 严重' },
  S3: { type: 'info', label: 'S3 一般' },
  S4: { type: 'primary', label: 'S4 轻微' },
}

const priorityTagMap: Record<string, { type: 'danger' | 'warning' | 'info' | 'primary'; label: string }> = {
  P0: { type: 'danger', label: 'P0' },
  P1: { type: 'warning', label: 'P1' },
  P2: { type: 'primary', label: 'P2' },
  P3: { type: 'info', label: 'P3' },
}

const statusTagMap: Record<string, { type: 'danger' | 'success' | 'primary' | 'info' | 'warning'; label: string }> = {
  OPEN: { type: 'danger', label: '打开' },
  FIXED: { type: 'success', label: '已修复' },
  VERIFIED: { type: 'primary', label: '已验证' },
  CLOSED: { type: 'info', label: '已关闭' },
  REOPENED: { type: 'warning', label: '重新打开' },
}

// ==================== Form ====================

const formData = reactive<Defect>({
  defectTitle: '',
  systemId: undefined as unknown as number,
  severity: undefined,
  priority: undefined,
  status: 'OPEN',
  assignedToName: '',
  description: '',
  stepsToReproduce: '',
  expectedResult: '',
  actualResult: '',
  environment: '',
})

const formRules = reactive<FormRules>({
  defectTitle: [{ required: true, message: '请输入缺陷标题', trigger: 'blur' }],
  systemId: [{ required: true, message: '请选择所属系统', trigger: 'change' }],
  severity: [{ required: true, message: '请选择严重度', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
})

// ==================== Computed ====================

const statsCards = computed(() => {
  const byS = statistics.value.bySeverity
  return [
    { key: 'S1', icon: '🔴', label: 'S1 致命', count: byS['S1'] || 0, class: 'stats-card--s1', severity: 'S1' as Severity },
    { key: 'S2', icon: '🟠', label: 'S2 严重', count: byS['S2'] || 0, class: 'stats-card--s2', severity: 'S2' as Severity },
    { key: 'S3', icon: '🟡', label: 'S3 一般', count: byS['S3'] || 0, class: 'stats-card--s3', severity: 'S3' as Severity },
    { key: 'S4', icon: '🔵', label: 'S4 轻微', count: byS['S4'] || 0, class: 'stats-card--s4', severity: 'S4' as Severity },
    { key: 'total', icon: '📋', label: '总计', count: statistics.value.totalCount || 0, class: 'stats-card--total', severity: '' as Severity },
  ]
})

const statusTimeline = computed(() => {
  if (!currentDefect.value) return []
  const items: Array<{ content: string; time: string; type: 'primary' | 'success' | 'warning' | 'danger' | 'info' }> = []
  const d = currentDefect.value
  items.push({ content: '缺陷创建', time: d.createdAt || '', type: 'primary' })
  if (d.status === 'FIXED' || d.status === 'VERIFIED' || d.status === 'CLOSED') {
    items.push({ content: '修复完成', time: d.updatedAt || '', type: 'success' })
  }
  if (d.status === 'VERIFIED' || d.status === 'CLOSED') {
    items.push({ content: '验证通过', time: d.updatedAt || '', type: 'primary' })
  }
  if (d.status === 'CLOSED') {
    items.push({ content: '缺陷关闭', time: d.updatedAt || '', type: 'info' })
  }
  if (d.status === 'REOPENED') {
    items.push({ content: '重新打开', time: d.updatedAt || '', type: 'warning' })
  }
  return items
})

// ==================== Lifecycle ====================

onMounted(() => {
  fetchData()
  fetchStatistics()
  fetchSystemOptions()
})

// ==================== Data Fetching ====================

async function fetchSystemOptions() {
  try {
    const res: any = await getSystemOptions()
    systemOptions.value = res.data || res || []
  } catch {
    // non-critical
  }
}

async function fetchStatistics() {
  try {
    const res: any = await getDefectStatistics(
      queryParams.systemId ? { systemId: queryParams.systemId as number } : undefined,
    )
    const data = res.data || res || {}
    statistics.value = {
      totalCount: data.totalCount || 0,
      bySeverity: data.bySeverity || {},
      byStatus: data.byStatus || {},
      byPriority: data.byPriority || {},
    }
  } catch {
    // non-critical, use mock fallback
    statistics.value = {
      totalCount: 33,
      bySeverity: { S1: 3, S2: 8, S3: 15, S4: 7 },
      byStatus: { OPEN: 12, FIXED: 10, VERIFIED: 6, CLOSED: 4, REOPENED: 1 },
      byPriority: { P0: 5, P1: 10, P2: 12, P3: 6 },
    }
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: queryParams.page,
      size: queryParams.size,
    }
    if (queryParams.keyword) params.keyword = queryParams.keyword
    if (queryParams.systemId) params.systemId = queryParams.systemId
    if (queryParams.severity) params.severity = queryParams.severity
    if (queryParams.priority) params.priority = queryParams.priority
    if (queryParams.status) params.status = queryParams.status

    const res: any = await getDefectList(params)
    tableData.value = res.data?.list || res.list || []
    pagination.total = res.data?.total || res.total || 0
  } catch {
    // Error handled by interceptor — use mock fallback
    tableData.value = mockDefects
    pagination.total = mockDefects.length
  } finally {
    loading.value = false
  }
}

// ==================== Handlers ====================

function handleSearch() {
  queryParams.page = 1
  fetchData()
  fetchStatistics()
}

function handleReset() {
  queryParams.keyword = ''
  queryParams.systemId = ''
  queryParams.severity = ''
  queryParams.priority = ''
  queryParams.status = ''
  queryParams.page = 1
  fetchData()
  fetchStatistics()
}

function handleStatsCardClick(card: { severity: Severity }) {
  if (card.severity) {
    queryParams.severity = card.severity
  } else {
    queryParams.severity = ''
  }
  queryParams.page = 1
  fetchData()
}

function handleRowClick(row: Defect) {
  currentDefect.value = row
  drawerVisible.value = true
}

function handleCreate() {
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(row: Defect) {
  isEdit.value = true
  drawerVisible.value = false
  Object.assign(formData, {
    id: row.id,
    defectTitle: row.defectTitle,
    systemId: row.systemId,
    severity: row.severity,
    priority: row.priority,
    status: row.status,
    assignedToName: row.assignedToName,
    description: row.description,
    stepsToReproduce: row.stepsToReproduce,
    expectedResult: row.expectedResult,
    actualResult: row.actualResult,
    environment: row.environment,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateDefect(formData.id, { ...formData })
      ElMessage.success('缺陷已更新')
    } else {
      await createDefect({ ...formData })
      ElMessage.success('缺陷已创建')
    }
    dialogVisible.value = false
    fetchData()
    fetchStatistics()
  } catch {
    // Error handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleStatusChange(row: Defect, status: string) {
  try {
    await updateDefectStatus(row.id!, status)
    ElMessage.success('状态已更新')
    fetchData()
    fetchStatistics()
  } catch {
    // Error handled by interceptor
  }
}

async function handleDelete(row: Defect) {
  try {
    await deleteDefect(row.id!)
    ElMessage.success('缺陷已删除')
    fetchData()
    fetchStatistics()
  } catch {
    // Error handled by interceptor
  }
}

function goRiskAnalysis() {
  router.push('/risk-analysis')
}

function resetForm() {
  formData.id = undefined
  formData.defectTitle = ''
  formData.systemId = undefined as unknown as number
  formData.severity = undefined
  formData.priority = undefined
  formData.status = 'OPEN'
  formData.assignedToName = ''
  formData.description = ''
  formData.stepsToReproduce = ''
  formData.expectedResult = ''
  formData.actualResult = ''
  formData.environment = ''
  formRef.value?.resetFields()
}

function formatDate(dateStr?: string) {
  if (!dateStr) return '—'
  // Show only MM-DD for compact display
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${month}-${day}`
}

// ==================== Mock Data (fallback) ====================

const mockDefects: Defect[] = [
  { id: 1, defectCode: 'BUG-001', defectTitle: '支付超时未提示，用户重复扣款', systemId: 1, systemName: '支付系统', severity: 'S1', priority: 'P0', status: 'OPEN', assignedToName: '张三', createdAt: '2026-06-12T10:00:00', description: '用户在支付过程中超时后，页面没有任何提示，导致用户多次点击造成重复扣款。', stepsToReproduce: '1. 选择商品下单\n2. 选择支付方式\n3. 等待支付超时\n4. 观察页面表现', expectedResult: '超时后弹出提示"支付超时，请重试"', actualResult: '页面无响应，用户点击后再次扣款', environment: 'Chrome 120 / Windows 11' },
  { id: 2, defectCode: 'BUG-002', defectTitle: '登录密码不校验强度，弱密码可通过', systemId: 2, systemName: '用户系统', severity: 'S2', priority: 'P1', status: 'FIXED', assignedToName: '李四', createdAt: '2026-06-11T14:30:00', description: '注册时密码不做强度校验，输入"123"也能注册成功。', updatedAt: '2026-06-12T09:00:00' },
  { id: 3, defectCode: 'BUG-003', defectTitle: '订单列表翻页数据重复显示', systemId: 3, systemName: '订单系统', severity: 'S3', priority: 'P2', status: 'OPEN', assignedToName: '王五', createdAt: '2026-06-10T09:15:00', description: '订单列表第二页与第一页数据有3条重复记录。' },
  { id: 4, defectCode: 'BUG-004', defectTitle: '导出报表Excel格式错乱', systemId: 3, systemName: '订单系统', severity: 'S3', priority: 'P2', status: 'VERIFIED', assignedToName: '赵六', createdAt: '2026-06-09T16:40:00', updatedAt: '2026-06-11T10:00:00', description: '导出的Excel中日期列显示为数字而非日期格式。' },
  { id: 5, defectCode: 'BUG-005', defectTitle: '商品图片上传后缩略图未生成', systemId: 4, systemName: '商品系统', severity: 'S2', priority: 'P1', status: 'OPEN', assignedToName: '张三', createdAt: '2026-06-12T08:20:00', description: '商品图片上传成功后，列表页缩略图一直显示加载中。' },
  { id: 6, defectCode: 'BUG-006', defectTitle: '用户昵称修改后未同步到评论区', systemId: 2, systemName: '用户系统', severity: 'S3', priority: 'P2', status: 'FIXED', assignedToName: '李四', createdAt: '2026-06-08T11:30:00', updatedAt: '2026-06-10T15:00:00', description: '修改昵称后，历史评论仍显示旧昵称。' },
  { id: 7, defectCode: 'BUG-007', defectTitle: '优惠券叠加使用金额计算错误', systemId: 1, systemName: '支付系统', severity: 'S1', priority: 'P0', status: 'REOPENED', assignedToName: '王五', createdAt: '2026-06-07T13:00:00', updatedAt: '2026-06-11T17:00:00', description: '两张优惠券叠加后，实际支付金额出现负数。' },
  { id: 8, defectCode: 'BUG-008', defectTitle: '搜索结果为空时页面布局塌陷', systemId: 4, systemName: '商品系统', severity: 'S4', priority: 'P3', status: 'CLOSED', assignedToName: '赵六', createdAt: '2026-06-06T10:00:00', updatedAt: '2026-06-09T14:00:00', description: '搜索无结果时，底部推荐区域被拉到页面中间。' },
  { id: 9, defectCode: 'BUG-009', defectTitle: '接口返回时间超过5秒无loading状态', systemId: 3, systemName: '订单系统', severity: 'S4', priority: 'P3', status: 'OPEN', assignedToName: '张三', createdAt: '2026-06-12T07:45:00', description: '订单查询接口慢时，用户以为页面卡死反复刷新。' },
  { id: 10, defectCode: 'BUG-010', defectTitle: '短信验证码有效期与提示不一致', systemId: 2, systemName: '用户系统', severity: 'S1', priority: 'P1', status: 'FIXED', assignedToName: '李四', createdAt: '2026-06-11T16:00:00', updatedAt: '2026-06-12T08:30:00', description: '提示验证码5分钟有效，实际2分钟就过期。' },
]
</script>

<style scoped lang="scss">
.defect-list-container {
  // ==================== Page Header ====================
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .page-title {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .toolbar-actions {
      display: flex;
      gap: 8px;
    }
  }

  // ==================== Stats Cards ====================
  .stats-cards {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    flex-wrap: wrap;

    .stats-card {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 14px 20px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.25s ease;
      border: 1px solid var(--el-border-color-light);
      background: var(--el-bg-color);
      min-width: 140px;
      flex: 1;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      }

      .stats-icon {
        font-size: 24px;
        line-height: 1;
      }

      .stats-info {
        .stats-count {
          font-size: 22px;
          font-weight: 700;
          line-height: 1.2;
        }
        .stats-label {
          font-size: 12px;
          color: var(--el-text-color-secondary);
          margin-top: 2px;
        }
      }

      &--s1 {
        border-left: 3px solid #f56c6c;
        .stats-count { color: #f56c6c; }
      }
      &--s2 {
        border-left: 3px solid #e6a23c;
        .stats-count { color: #e6a23c; }
      }
      &--s3 {
        border-left: 3px solid #909399;
        .stats-count { color: #909399; }
      }
      &--s4 {
        border-left: 3px solid #409eff;
        .stats-count { color: #409eff; }
      }
      &--total {
        border-left: 3px solid var(--el-color-primary);
        .stats-count { color: var(--el-color-primary); }
      }
    }
  }

  // ==================== Search Toolbar ====================
  .search-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
    gap: 12px;
    flex-wrap: wrap;

    .search-left {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }
  }

  // ==================== Table Links ====================
  .defect-code-link {
    color: var(--el-color-primary);
    font-family: 'Courier New', monospace;
    font-weight: 500;
    cursor: pointer;
    &:hover {
      text-decoration: underline;
    }
  }

  .defect-title-link {
    color: var(--el-text-color-primary);
    cursor: pointer;
    &:hover {
      color: var(--el-color-primary);
    }
  }

  // ==================== Pagination ====================
  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  // ==================== Table Rows ====================
  :deep(.el-table) {
    .el-table__row {
      cursor: pointer;
    }
  }

  // ==================== Detail Drawer ====================
  .detail-content {
    .detail-section {
      margin-bottom: 16px;

      .section-title {
        font-size: 14px;
        font-weight: 600;
        color: var(--el-text-color-primary);
        margin: 0 0 8px 0;
      }
    }

    .detail-row {
      display: flex;
      align-items: flex-start;
      margin-bottom: 12px;

      .detail-label {
        flex-shrink: 0;
        width: 80px;
        font-size: 13px;
        color: var(--el-text-color-secondary);
      }

      .detail-value {
        flex: 1;
        font-size: 13px;
        color: var(--el-text-color-primary);
      }

      .detail-title {
        font-weight: 600;
        font-size: 14px;
      }
    }

    .detail-row-inline {
      display: flex;
      gap: 16px;
      margin-bottom: 12px;

      .detail-row {
        flex: 1;
        margin-bottom: 0;
      }

      .half {
        flex: 1;
      }
    }

    .detail-text {
      font-size: 13px;
      color: var(--el-text-color-regular);
      line-height: 1.6;
      margin: 0;
    }

    .detail-pre {
      white-space: pre-wrap;
      font-family: inherit;
    }
  }
}
</style>
