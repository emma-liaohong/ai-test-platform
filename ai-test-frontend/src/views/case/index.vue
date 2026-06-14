<template>
  <div class="case-list-container">
    <el-card shadow="never">
      <!-- Search Toolbar -->
      <div class="search-toolbar">
        <div class="search-left">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索用例名称/编码"
            :prefix-icon="Search"
            clearable
            style="width: 240px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-select
            v-model="queryParams.caseType"
            placeholder="用例类型"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="🖥 PC" value="PC" />
            <el-option label="📱 APP" value="APP" />
            <el-option label="🔌 API" value="API" />
          </el-select>
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
            v-model="queryParams.priority"
            placeholder="优先级"
            clearable
            style="width: 110px"
            @change="handleSearch"
          >
            <el-option label="P0 - 阻塞" value="P0" />
            <el-option label="P1 - 严重" value="P1" />
            <el-option label="P2 - 一般" value="P2" />
            <el-option label="P3 - 轻微" value="P3" />
          </el-select>
          <el-select
            v-model="queryParams.status"
            placeholder="状态"
            clearable
            style="width: 110px"
            @change="handleSearch"
          >
            <el-option label="草稿" value="draft" />
            <el-option label="就绪" value="ready" />
            <el-option label="已归档" value="archived" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
        <div class="search-right">
          <el-button type="primary" :icon="Plus" @click="handleCreate">新建案例</el-button>
        </div>
      </div>

      <!-- Batch Operation Bar -->
      <div v-if="selectedIds.length > 0" class="batch-bar">
        <span class="batch-info">
          已选择 <el-tag type="primary" size="small" effect="dark" round>{{ selectedIds.length }}</el-tag> 项
        </span>
        <el-button size="small" type="danger" plain @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>批量删除
        </el-button>
        <el-dropdown size="small" @command="handleBatchStatusChange">
          <el-button size="small" type="warning" plain>
            批量变更状态<el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="draft">设为草稿</el-dropdown-item>
              <el-dropdown-item command="ready">设为就绪</el-dropdown-item>
              <el-dropdown-item command="archived">设为已归档</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button size="small" text @click="selectedIds = []">取消选择</el-button>
      </div>

      <!-- Data Table -->
      <el-table
        :data="tableData"
        stripe
        v-loading="loading"
        style="width: 100%"
        row-key="id"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
        :row-class-name="tableRowClassName"
      >
        <el-table-column type="selection" width="45" @click.stop />
        <el-table-column prop="caseCode" label="用例编码" width="130" show-overflow-tooltip />
        <el-table-column prop="caseName" label="用例名称" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="case-name-link">{{ row.caseName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="caseType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="caseTypeTagMap[row.caseType]?.type || 'info'" size="small" effect="plain">
              {{ caseTypeTagMap[row.caseType]?.icon || '' }} {{ row.caseType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="systemName" label="所属系统" width="130" show-overflow-tooltip />
        <el-table-column prop="priority" label="优先级" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="priorityTagMap[row.priority]?.type || 'info'"
              size="small"
              :effect="row.priority === 'P0' ? 'dark' : 'light'"
            >
              {{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status]?.type || 'info'" size="small">
              {{ statusTagMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" width="150">
          <template #default="{ row }">
            <template v-if="row.tags && row.tags.length">
              <el-tag
                v-for="tag in row.tags.slice(0, 3)"
                :key="tag"
                size="small"
                effect="plain"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
              <el-tag v-if="row.tags.length > 3" size="small" type="info" effect="plain">+{{ row.tags.length - 3 }}</el-tag>
            </template>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right" align="center" @click.stop>
          <template #default="{ row }">
            <div style="display: flex; align-items: center; justify-content: center; gap: 2px;">
              <el-tooltip content="执行" placement="top" :show-after="300">
                <el-button text type="success" size="small" :icon="VideoPlay" @click.stop="handleExecute(row)" />
              </el-tooltip>
              <el-tooltip content="编辑" placement="top" :show-after="300">
                <el-button text type="primary" size="small" :icon="Edit" @click.stop="handleEdit(row)" />
              </el-tooltip>
              <el-popconfirm
                title="确定删除该测试用例吗？删除后不可恢复。"
                confirm-button-text="删除"
                cancel-button-text="取消"
                @confirm="handleDelete(row.id)"
              >
                <template #reference>
                  <el-tooltip content="删除" placement="top" :show-after="300">
                    <el-button text type="danger" size="small" :icon="Delete" @click.stop />
                  </el-tooltip>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh, Delete, Edit, VideoPlay, ArrowDown } from '@element-plus/icons-vue'
import {
  getCaseList,
  deleteCase,
  batchDeleteCases,
  executeCase,
  getSystemOptions,
  type TestCase,
  type CaseQuery,
  type CaseStatus,
} from '@/api/case'

const router = useRouter()
const loading = ref(false)

// Tag style maps
const caseTypeTagMap: Record<string, { type: 'primary' | 'success' | 'warning'; icon: string }> = {
  PC: { type: 'primary', icon: '🖥' },
  APP: { type: 'success', icon: '📱' },
  API: { type: 'warning', icon: '🔌' },
}

const priorityTagMap: Record<string, { type: 'danger' | 'warning' | 'primary' | 'info' }> = {
  P0: { type: 'danger' },
  P1: { type: 'warning' },
  P2: { type: 'primary' },
  P3: { type: 'info' },
}

const statusTagMap: Record<string, { type: 'info' | 'success' | 'warning'; label: string }> = {
  draft: { type: 'info', label: '草稿' },
  ready: { type: 'success', label: '就绪' },
  archived: { type: 'warning', label: '已归档' },
}

// Query params
const queryParams = reactive<CaseQuery>({
  page: 1,
  size: 20,
  keyword: '',
  caseType: '',
  systemId: '',
  priority: '',
  status: '',
})

const pagination = reactive({ total: 0 })
const tableData = ref<TestCase[]>([])
const selectedIds = ref<number[]>([])
const systemOptions = ref<Array<{ id: number; name: string }>>([])

onMounted(() => {
  fetchData()
  fetchSystemOptions()
})

async function fetchSystemOptions() {
  try {
    const res: any = await getSystemOptions()
    systemOptions.value = res.data || res || []
  } catch {
    // fallback - system options not critical
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
    if (queryParams.caseType) params.caseType = queryParams.caseType
    if (queryParams.systemId) params.systemId = queryParams.systemId
    if (queryParams.priority) params.priority = queryParams.priority
    if (queryParams.status) params.status = queryParams.status

    const res: any = await getCaseList(params)
    tableData.value = res.data?.records || res.data?.list || []
    pagination.total = res.data?.total || res.total || 0
  } catch {
    // Error handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 1
  fetchData()
}

function handleReset() {
  queryParams.keyword = ''
  queryParams.caseType = ''
  queryParams.systemId = ''
  queryParams.priority = ''
  queryParams.status = ''
  queryParams.page = 1
  fetchData()
}

function handleCreate() {
  router.push('/cases/create')
}

function handleRowClick(row: TestCase) {
  if (row.id) {
    router.push(`/cases/${row.id}`)
  }
}

function handleEdit(row: TestCase) {
  if (row.id) {
    router.push(`/cases/${row.id}?edit=1`)
  }
}

async function handleExecute(row: TestCase) {
  if (!row.id) return
  try {
    await ElMessageBox.confirm('确定执行该测试用例？', '确认执行', {
      confirmButtonText: '执行',
      cancelButtonText: '取消',
      type: 'info',
    })
    const res: any = await executeCase(row.id)
    const executionId = res.data?.id || res.id
    if (executionId) {
      ElMessage.success('用例执行完成')
      router.push(`/executions/${executionId}`)
    } else {
      ElMessage.success('用例已提交执行')
    }
  } catch {
    // cancelled or error
  }
}

async function handleDelete(id: number) {
  try {
    await deleteCase(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // Error handled by interceptor
  }
}

function handleSelectionChange(rows: TestCase[]) {
  selectedIds.value = rows.map((r) => r.id!).filter(Boolean)
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) return
  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${selectedIds.value.length} 条用例吗？删除后不可恢复。`,
      '批量删除确认',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    await batchDeleteCases(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    fetchData()
  } catch {
    // cancelled or error
  }
}

async function handleBatchStatusChange(status: CaseStatus) {
  // This would be a batch update API - placeholder
  ElMessage.info(`批量变更状态为「${statusTagMap[status]?.label || status}」功能开发中`)
}

function tableRowClassName({ row }: { row: TestCase }) {
  if (row.priority === 'P0') return 'row-p0'
  return ''
}
</script>

<style scoped lang="scss">
.case-list-container {
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

    .search-right {
      flex-shrink: 0;
    }
  }

  .batch-bar {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 16px;
    margin-bottom: 12px;
    background: var(--el-color-primary-light-9);
    border: 1px solid var(--el-color-primary-light-7);
    border-radius: 4px;

    .batch-info {
      font-size: 13px;
      color: var(--el-text-color-regular);
      margin-right: 8px;
    }
  }

  .case-name-link {
    color: var(--el-color-primary);
    cursor: pointer;
    font-weight: 500;

    &:hover {
      text-decoration: underline;
    }
  }

  .tag-item {
    margin-right: 4px;
    margin-bottom: 2px;
  }

  .text-muted {
    color: var(--el-text-color-placeholder);
    font-size: 13px;
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  :deep(.el-table) {
    .el-table__row {
      cursor: pointer;
    }

    .row-p0 {
      background-color: var(--el-color-danger-light-9) !important;
    }
  }
}
</style>
