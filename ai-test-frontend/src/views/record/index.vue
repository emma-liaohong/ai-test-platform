<template>
  <div class="record-page">
    <!-- ===== Start New Recording Section ===== -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header-row">
          <span class="card-title">
            <el-icon><VideoPlay /></el-icon>
            开始新录制
          </span>
          <el-tag type="info" size="small" effect="plain">
            录制用户操作并自动生成测试用例
          </el-tag>
        </div>
      </template>
      <el-form :model="startForm" label-width="90px" class="start-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="会话名称" required>
              <el-input
                v-model="startForm.sessionName"
                placeholder="例如：登录流程测试"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="所属系统">
              <el-select
                v-model="startForm.systemId"
                placeholder="选择系统"
                clearable
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="sys in systemOptions"
                  :key="sys.id"
                  :label="sys.name"
                  :value="sys.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="目标 URL">
              <el-input
                v-model="startForm.targetUrl"
                placeholder="https://example.com/login"
                clearable
              >
                <template #prefix>
                  <el-icon><Link /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="3">
            <el-form-item label="&nbsp;">
              <el-button
                type="danger"
                :loading="startLoading"
                @click="handleStartRecording"
                style="width: 100%"
              >
                <el-icon><Monitor /></el-icon>
                开始录制
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- ===== Session History Section ===== -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header-row">
          <span class="card-title">
            <el-icon><List /></el-icon>
            录制历史
          </span>
        </div>
      </template>

      <!-- Filter Toolbar -->
      <div class="search-toolbar">
        <div class="search-left">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索会话名称/ID"
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
            style="width: 150px"
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
            v-model="queryParams.status"
            placeholder="录制状态"
            clearable
            style="width: 130px"
            @change="handleSearch"
          >
            <el-option label="🔴 录制中" value="RECORDING" />
            <el-option label="⏹ 已停止" value="STOPPED" />
            <el-option label="✅ 已生成" value="GENERATED" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>

      <!-- Data Table -->
      <el-table
        :data="tableData"
        stripe
        v-loading="loading"
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="sessionId" label="会话 ID" width="130" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag size="small" effect="plain" type="info">{{ row.sessionId }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sessionName" label="会话名称" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="session-name-link" @click="handleView(row)">{{ row.sessionName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="systemName" label="所属系统" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.systemName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <span
              v-if="row.status === 'RECORDING'"
              class="status-dot status-dot--recording"
            />
            <el-tag
              :type="statusTagMap[row.status]?.type || 'info'"
              size="small"
              :class="{ 'status-tag--pulse': row.status === 'RECORDING' }"
            >
              {{ statusTagMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stepCount" label="步骤" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" round effect="plain">{{ row.stepCount ?? 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时长" width="90" align="center">
          <template #default="{ row }">
            <span class="mono-text">{{ formatDuration(row.durationMs) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="startedAt" label="开始时间" width="170" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.startedAt || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleView(row)">
              <el-icon><VideoPlay /></el-icon>查看
            </el-button>
            <el-button
              v-if="row.status === 'STOPPED'"
              text
              type="success"
              size="small"
              @click="handleGenerateCase(row)"
            >
              <el-icon><DocumentAdd /></el-icon>生成
            </el-button>
            <el-popconfirm
              title="确定删除该录制会话吗？删除后不可恢复。"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button text type="danger" size="small">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无录制会话" :image-size="80" />
        </template>
      </el-table>

      <!-- Pagination -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- ===== Start Recording Dialog (mobile validation / confirmation) ===== -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="480px"
      destroy-on-close
      @closed="handleDialogClosed"
    >
      <el-form
        ref="dialogFormRef"
        :model="startForm"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="会话名称" prop="sessionName">
          <el-input
            v-model="startForm.sessionName"
            placeholder="例如：登录流程测试"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="所属系统" prop="systemId">
          <el-select
            v-model="startForm.systemId"
            placeholder="请选择系统（可选）"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="sys in systemOptions"
              :key="sys.id"
              :label="sys.name"
              :value="sys.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标 URL" prop="targetUrl">
          <el-input
            v-model="startForm.targetUrl"
            placeholder="https://example.com/login"
          >
            <template #prefix>
              <el-icon><Link /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        style="margin-top: 8px"
      >
        <template #title>
          开始录制后，将打开浏览器预览窗口，您的操作将被实时记录。
        </template>
      </el-alert>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="startLoading" @click="handleSubmitStart">
          <el-icon><Monitor /></el-icon>开始录制
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Search, Refresh, Delete, VideoPlay, DocumentAdd, Link, List, Monitor,
} from '@element-plus/icons-vue'
import {
  getSessionList,
  deleteSession,
  startSession,
  generateCase,
  getSystemOptions,
  type RecordSession,
  type RecordSessionQuery,
  type RecordStatus,
} from '@/api/record'

const router = useRouter()
const loading = ref(false)
const startLoading = ref(false)
const dialogVisible = ref(false)
const dialogFormRef = ref<FormInstance>()

// ==================== Status / Action Maps ====================

const statusTagMap: Record<string, { type: 'danger' | 'warning' | 'success'; label: string }> = {
  RECORDING: { type: 'danger', label: '录制中' },
  STOPPED: { type: 'warning', label: '已停止' },
  GENERATED: { type: 'success', label: '已生成' },
}

// ==================== Query & Data ====================

const queryParams = reactive<RecordSessionQuery>({
  page: 1,
  pageSize: 10,
  keyword: '',
  systemId: '',
  status: '',
})

const pagination = reactive({ total: 0 })
const tableData = ref<RecordSession[]>([])
const systemOptions = ref<Array<{ id: number; name: string }>>([])

const startForm = reactive({
  sessionName: '',
  systemId: undefined as number | undefined,
  targetUrl: '',
})

const formRules = reactive<FormRules>({
  sessionName: [{ required: true, message: '请输入会话名称', trigger: 'blur' }],
})

const dialogTitle = '确认录制信息'

// ==================== Mock Data ====================

const mockSessions: RecordSession[] = [
  {
    id: 1, sessionId: 'REC-a1b2c3', sessionName: '登录流程验证', systemId: 1, systemName: '订单管理系统',
    targetUrl: 'https://order.example.com/login', status: 'RECORDING', stepCount: 6,
    durationMs: 45000, startedAt: '2026-06-12 14:30:00', createdAt: '2026-06-12 14:30:00',
  },
  {
    id: 2, sessionId: 'REC-c3d4e5', sessionName: '下单支付完整流程', systemId: 2, systemName: '支付中心',
    targetUrl: 'https://pay.example.com/checkout', status: 'STOPPED', stepCount: 12,
    durationMs: 150000, startedAt: '2026-06-11 10:15:00', stoppedAt: '2026-06-11 10:17:30',
    createdAt: '2026-06-11 10:15:00',
  },
  {
    id: 3, sessionId: 'REC-e5f6g7', sessionName: '用户注册流程', systemId: 3, systemName: '用户中心',
    targetUrl: 'https://user.example.com/register', status: 'GENERATED', stepCount: 8,
    durationMs: 75000, generatedCaseId: 42, startedAt: '2026-06-10 09:00:00',
    stoppedAt: '2026-06-10 09:01:15', createdAt: '2026-06-10 09:00:00',
  },
  {
    id: 4, sessionId: 'REC-h8i9j0', sessionName: '商品搜索与筛选', systemId: 1, systemName: '订单管理系统',
    targetUrl: 'https://order.example.com/products', status: 'STOPPED', stepCount: 5,
    durationMs: 30000, startedAt: '2026-06-09 16:45:00', stoppedAt: '2026-06-09 16:45:30',
    createdAt: '2026-06-09 16:45:00',
  },
  {
    id: 5, sessionId: 'REC-k1l2m3', sessionName: '个人信息修改', systemId: 3, systemName: '用户中心',
    targetUrl: 'https://user.example.com/profile', status: 'GENERATED', stepCount: 10,
    durationMs: 90000, generatedCaseId: 38, startedAt: '2026-06-08 11:20:00',
    stoppedAt: '2026-06-08 11:21:30', createdAt: '2026-06-08 11:20:00',
  },
  {
    id: 6, sessionId: 'REC-n4o5p6', sessionName: '退款申请流程', systemId: 2, systemName: '支付中心',
    targetUrl: 'https://pay.example.com/refund', status: 'RECORDING', stepCount: 3,
    durationMs: 18000, startedAt: '2026-06-12 15:00:00', createdAt: '2026-06-12 15:00:00',
  },
]

// ==================== Lifecycle ====================

onMounted(() => {
  fetchData()
  fetchSystemOptions()
})

// ==================== Data Fetching ====================

async function fetchSystemOptions() {
  try {
    const res: any = await getSystemOptions()
    systemOptions.value = res.data || res || []
  } catch {
    // Fallback mock system options
    systemOptions.value = [
      { id: 1, name: '订单管理系统' },
      { id: 2, name: '支付中心' },
      { id: 3, name: '用户中心' },
    ]
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: queryParams.page,
      pageSize: queryParams.pageSize,
    }
    if (queryParams.keyword) params.keyword = queryParams.keyword
    if (queryParams.systemId) params.systemId = queryParams.systemId
    if (queryParams.status) params.status = queryParams.status

    const res: any = await getSessionList(params)
    tableData.value = res.data?.list || res.list || []
    pagination.total = res.data?.total || res.total || 0
  } catch {
    // Fallback to mock data with client-side filtering
    let filtered = [...mockSessions]
    if (queryParams.keyword) {
      const kw = queryParams.keyword.toLowerCase()
      filtered = filtered.filter(
        (s) =>
          s.sessionName.toLowerCase().includes(kw) ||
          (s.sessionId && s.sessionId.toLowerCase().includes(kw)),
      )
    }
    if (queryParams.systemId) {
      filtered = filtered.filter((s) => s.systemId === queryParams.systemId)
    }
    if (queryParams.status) {
      filtered = filtered.filter((s) => s.status === queryParams.status)
    }
    pagination.total = filtered.length
    const start = (queryParams.page! - 1) * queryParams.pageSize!
    tableData.value = filtered.slice(start, start + queryParams.pageSize!)
  } finally {
    loading.value = false
  }
}

// ==================== Handlers ====================

function handleSearch() {
  queryParams.page = 1
  fetchData()
}

function handleReset() {
  queryParams.keyword = ''
  queryParams.systemId = ''
  queryParams.status = ''
  queryParams.page = 1
  fetchData()
}

function handleView(row: RecordSession) {
  router.push(`/record/${row.id}`)
}

function handleStartRecording() {
  if (!startForm.sessionName.trim()) {
    ElMessage.warning('请输入会话名称')
    return
  }
  dialogVisible.value = true
}

async function handleSubmitStart() {
  if (!dialogFormRef.value) return
  await dialogFormRef.value.validate(async (valid) => {
    if (!valid) return

    startLoading.value = true
    try {
      const res: any = await startSession({
        sessionName: startForm.sessionName,
        systemId: startForm.systemId,
        targetUrl: startForm.targetUrl || undefined,
      })
      const newId = res.data?.id || res.id
      ElMessage.success('录制会话已创建')
      dialogVisible.value = false
      router.push(`/record/${newId || 'new'}`)
    } catch {
      // Fallback: generate mock ID and navigate
      const mockId = Date.now()
      ElMessage.success('录制会话已创建（演示模式）')
      dialogVisible.value = false
      router.push(`/record/${mockId}`)
    } finally {
      startLoading.value = false
    }
  })
}

function handleDialogClosed() {
  dialogFormRef.value?.resetFields()
}

async function handleGenerateCase(row: RecordSession) {
  if (!row.id) return
  try {
    await ElMessageBox.confirm(
      '将根据录制步骤自动生成测试用例，确定继续？',
      '生成测试用例',
      { confirmButtonText: '生成', cancelButtonText: '取消', type: 'info' },
    )
    await generateCase(row.id)
    ElMessage.success('测试用例生成成功')
    fetchData()
  } catch {
    // Cancelled or error
  }
}

async function handleDelete(id: number) {
  try {
    await deleteSession(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // Fallback for demo
    ElMessage.success('删除成功（演示模式）')
    fetchData()
  }
}

// ==================== Utilities ====================

function formatDuration(ms?: number): string {
  if (!ms) return '00:00'
  const totalSeconds = Math.floor(ms / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}
</script>

<style scoped lang="scss">
.record-page {
  .section-card {
    margin-bottom: 16px;

    .card-title {
      font-size: 15px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .card-header-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  /* ===== Start Form ===== */
  .start-form {
    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }

  /* ===== Search Toolbar ===== */
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

  /* ===== Session Name Link ===== */
  .session-name-link {
    color: var(--el-color-primary);
    cursor: pointer;
    font-weight: 500;

    &:hover {
      text-decoration: underline;
    }
  }

  /* ===== Status Indicators ===== */
  .status-dot {
    display: inline-block;
    width: 7px;
    height: 7px;
    border-radius: 50%;
    margin-right: 4px;
    vertical-align: middle;

    &--recording {
      background-color: var(--el-color-danger);
      animation: pulse-dot 1.5s ease-in-out infinite;
    }
  }

  .status-tag--pulse {
    animation: pulse-tag 2s ease-in-out infinite;
  }

  /* ===== Mono Text ===== */
  .mono-text {
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    font-size: 13px;
    color: var(--el-text-color-regular);
  }

  /* ===== Pagination ===== */
  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  /* ===== Table Deep Overrides ===== */
  :deep(.el-table) {
    .el-table__row {
      cursor: pointer;
    }
  }
}

/* ===== Animations ===== */
@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(1.3); }
}

@keyframes pulse-tag {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}
</style>
