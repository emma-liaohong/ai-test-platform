<template>
  <div class="execution-list-container">
    <el-card shadow="never">
      <!-- Status Filter Tabs -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="status-tabs">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane name="running">
          <template #label>
            <span><el-icon class="is-loading"><Loading /></el-icon> 执行中</span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="success">
          <template #label>
            <span><el-icon color="#67c23a"><CircleCheckFilled /></el-icon> 成功</span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="failed">
          <template #label>
            <span><el-icon color="#f56c6c"><CircleCloseFilled /></el-icon> 失败</span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <!-- Search Toolbar -->
      <div class="search-toolbar">
        <div class="search-left">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索执行名称/套件名称"
            :prefix-icon="Search"
            clearable
            style="width: 240px"
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
            v-model="queryParams.triggerType"
            placeholder="触发方式"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="手动" value="manual" />
            <el-option label="定时" value="scheduled" />
            <el-option label="CI/CD" value="ci" />
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
        @row-click="handleRowClick"
      >
        <el-table-column prop="executionName" label="执行名称" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="exec-name-link">{{ row.executionName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="suiteName" label="所属套件" width="150" show-overflow-tooltip />
        <el-table-column prop="systemName" label="所属系统" width="120" show-overflow-tooltip />
        <el-table-column prop="triggerType" label="触发方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="triggerTypeTagMap[row.triggerType]?.type || 'info'" size="small" effect="plain">
              {{ triggerTypeTagMap[row.triggerType]?.label || row.triggerType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status]?.type || 'info'" size="small">
              <template v-if="row.status === 'running'">
                <el-icon class="is-loading"><Loading /></el-icon>
              </template>
              <template v-if="row.status === 'success'">
                <el-icon><CircleCheckFilled /></el-icon>
              </template>
              <template v-if="row.status === 'failed'">
                <el-icon><CircleCloseFilled /></el-icon>
              </template>
              {{ statusTagMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="passRate" label="通过率" width="130" align="center">
          <template #default="{ row }">
            <template v-if="row.status !== 'pending' && row.status !== 'running'">
              <el-progress
                :percentage="row.passRate"
                :color="getPassRateColor(row.passRate)"
                :stroke-width="6"
                style="width: 80px"
              />
            </template>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="通过/失败" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.status !== 'pending'">
              <span style="color: #67c23a">{{ row.passedCases }}</span>
              /
              <span style="color: #f56c6c">{{ row.failedCases }}</span>
              <span class="text-muted" style="margin-left: 2px">/ {{ row.totalCases }}</span>
            </span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="startedAt" label="开始时间" width="170" />
        <el-table-column prop="durationText" label="耗时" width="90" />
        <el-table-column label="操作" width="100" fixed="right" @click.stop>
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click.stop="handleViewDetail(row)">
              <el-icon><View /></el-icon>详情
            </el-button>
          </template>
        </el-table-column>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh, Loading, CircleCheckFilled, CircleCloseFilled, View } from '@element-plus/icons-vue'
import {
  getExecutionList,
  getSystemOptions,
  type TestExecution,
  type ExecutionQuery,
} from '@/api/suite'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')

const triggerTypeTagMap: Record<string, { type: 'primary' | 'success' | 'warning'; label: string }> = {
  manual: { type: 'primary', label: '手动' },
  scheduled: { type: 'success', label: '定时' },
  ci: { type: 'warning', label: 'CI/CD' },
}

const statusTagMap: Record<string, { type: 'info' | 'success' | 'warning' | 'danger'; label: string }> = {
  running: { type: 'warning', label: '执行中' },
  success: { type: 'success', label: '成功' },
  failed: { type: 'danger', label: '失败' },
  cancelled: { type: 'info', label: '已取消' },
  pending: { type: 'info', label: '等待中' },
}

const queryParams = reactive<ExecutionQuery>({
  page: 1,
  pageSize: 20,
  keyword: '',
  systemId: '',
  status: '',
  triggerType: '',
})

const pagination = reactive({ total: 0 })
const tableData = ref<TestExecution[]>([])
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
    // non-critical
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
    if (queryParams.triggerType) params.triggerType = queryParams.triggerType

    const res: any = await getExecutionList(params)
    tableData.value = res.data?.list || res.list || []
    pagination.total = res.data?.total || res.total || 0
  } catch {
    // Error handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleTabChange(tab: string) {
  queryParams.status = tab === 'all' ? '' : (tab as any)
  queryParams.page = 1
  fetchData()
}

function handleSearch() {
  queryParams.page = 1
  fetchData()
}

function handleReset() {
  queryParams.keyword = ''
  queryParams.systemId = ''
  queryParams.status = ''
  queryParams.triggerType = ''
  activeTab.value = 'all'
  queryParams.page = 1
  fetchData()
}

function handleRowClick(row: TestExecution) {
  if (row.id) {
    router.push(`/executions/${row.id}`)
  }
}

function handleViewDetail(row: TestExecution) {
  if (row.id) {
    router.push(`/executions/${row.id}`)
  }
}

function getPassRateColor(rate: number): string {
  if (rate >= 90) return '#67c23a'
  if (rate >= 70) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped lang="scss">
.execution-list-container {
  .status-tabs {
    margin-bottom: 8px;
    :deep(.el-tabs__nav-wrap::after) {
      height: 1px;
    }
  }

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

  .exec-name-link {
    color: var(--el-color-primary);
    cursor: pointer;
    font-weight: 500;
    &:hover {
      text-decoration: underline;
    }
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
  }
}
</style>
