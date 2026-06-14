<template>
  <div class="suite-list-container">
    <el-card shadow="never">
      <!-- Search Toolbar -->
      <div class="search-toolbar">
        <div class="search-left">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索套件名称"
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
            v-model="queryParams.suiteType"
            placeholder="套件类型"
            clearable
            style="width: 130px"
            @change="handleSearch"
          >
            <el-option label="冒烟测试" value="smoke" />
            <el-option label="回归测试" value="regression" />
            <el-option label="全量测试" value="full" />
            <el-option label="自定义" value="custom" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
        <div class="search-right">
          <el-button type="primary" :icon="Plus" @click="createDialogVisible = true">新建套件</el-button>
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
        <el-table-column prop="suiteName" label="套件名称" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="suite-name-link">{{ row.suiteName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="systemName" label="所属系统" width="130" show-overflow-tooltip />
        <el-table-column prop="suiteType" label="套件类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="suiteTypeTagMap[row.suiteType]?.type || 'info'" size="small" effect="plain">
              {{ suiteTypeTagMap[row.suiteType]?.label || row.suiteType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="caseCount" label="用例数" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" round effect="plain">{{ row.caseCount ?? 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastExecutionTime" label="最近执行" width="170">
          <template #default="{ row }">
            <span v-if="row.lastExecutionTime">{{ row.lastExecutionTime }}</span>
            <span v-else class="text-muted">未执行</span>
          </template>
        </el-table-column>
        <el-table-column prop="passRate" label="通过率" width="120" align="center">
          <template #default="{ row }">
            <template v-if="row.passRate !== undefined && row.passRate !== null">
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
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" @click.stop>
          <template #default="{ row }">
            <el-button text type="success" size="small" @click.stop="handleExecute(row)" :loading="executingId === row.id">
              <el-icon><VideoPlay /></el-icon>执行
            </el-button>
            <el-button text type="primary" size="small" @click.stop="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-popconfirm
              title="确定删除该套件吗？删除后不可恢复。"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button text type="danger" size="small" @click.stop>
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-popconfirm>
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

    <!-- Create / Edit Dialog -->
    <el-dialog
      v-model="createDialogVisible"
      :title="editingSuite ? '编辑套件' : '新建套件'"
      width="520px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="suiteForm" :rules="formRules" label-width="90px">
        <el-form-item label="套件名称" prop="suiteName">
          <el-input v-model="suiteForm.suiteName" placeholder="请输入套件名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="所属系统" prop="systemId">
          <el-select v-model="suiteForm.systemId" placeholder="请选择所属系统" filterable clearable style="width: 100%">
            <el-option
              v-for="sys in systemOptions"
              :key="sys.id"
              :label="sys.name"
              :value="sys.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="套件类型" prop="suiteType">
          <el-select v-model="suiteForm.suiteType" placeholder="请选择套件类型" style="width: 100%">
            <el-option label="冒烟测试" value="smoke" />
            <el-option label="回归测试" value="regression" />
            <el-option label="全量测试" value="full" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="suiteForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入套件描述（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Plus, Refresh, Delete, Edit, VideoPlay } from '@element-plus/icons-vue'
import {
  getSuiteList,
  createSuite,
  updateSuite,
  deleteSuite,
  executeSuite,
  getSystemOptions,
  type TestSuite,
  type SuiteQuery,
} from '@/api/suite'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const executingId = ref<number | null>(null)
const createDialogVisible = ref(false)
const editingSuite = ref<TestSuite | null>(null)
const formRef = ref<FormInstance>()

const suiteTypeTagMap: Record<string, { type: 'primary' | 'success' | 'warning' | 'danger'; label: string }> = {
  smoke: { type: 'success', label: '冒烟测试' },
  regression: { type: 'primary', label: '回归测试' },
  full: { type: 'warning', label: '全量测试' },
  custom: { type: 'danger', label: '自定义' },
}

const queryParams = reactive<SuiteQuery>({
  page: 1,
  size: 20,
  keyword: '',
  systemId: '',
  suiteType: '',
  status: '',
})

const pagination = reactive({ total: 0 })
const tableData = ref<TestSuite[]>([])
const systemOptions = ref<Array<{ id: number; name: string }>>([])

const suiteForm = reactive<Partial<TestSuite>>({
  suiteName: '',
  systemId: null,
  suiteType: 'smoke',
  description: '',
})

const formRules = reactive<FormRules>({
  suiteName: [{ required: true, message: '请输入套件名称', trigger: 'blur' }],
  suiteType: [{ required: true, message: '请选择套件类型', trigger: 'change' }],
})

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
      size: queryParams.size,
    }
    if (queryParams.keyword) params.keyword = queryParams.keyword
    if (queryParams.systemId) params.systemId = queryParams.systemId
    if (queryParams.suiteType) params.suiteType = queryParams.suiteType

    const res: any = await getSuiteList(params)
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
  queryParams.systemId = ''
  queryParams.suiteType = ''
  queryParams.page = 1
  fetchData()
}

function handleRowClick(row: TestSuite) {
  if (row.id) {
    router.push(`/suites/${row.id}`)
  }
}

function handleEdit(row: TestSuite) {
  editingSuite.value = row
  Object.assign(suiteForm, {
    suiteName: row.suiteName,
    systemId: row.systemId,
    suiteType: row.suiteType,
    description: row.description,
  })
  createDialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (editingSuite.value?.id) {
        await updateSuite(editingSuite.value.id, suiteForm as TestSuite)
        ElMessage.success('更新成功')
      } else {
        await createSuite(suiteForm as TestSuite)
        ElMessage.success('创建成功')
      }
      createDialogVisible.value = false
      fetchData()
    } catch {
      // Error handled by interceptor
    } finally {
      submitLoading.value = false
    }
  })
}

function resetForm() {
  editingSuite.value = null
  suiteForm.suiteName = ''
  suiteForm.systemId = null
  suiteForm.suiteType = 'smoke'
  suiteForm.description = ''
}

async function handleExecute(row: TestSuite) {
  if (!row.id) return
  try {
    await ElMessageBox.confirm('确定执行该测试套件？', '确认执行', {
      confirmButtonText: '执行',
      cancelButtonText: '取消',
      type: 'info',
    })
    executingId.value = row.id
    await executeSuite(row.id)
    ElMessage.success('套件已提交执行')
  } catch {
    // cancelled or error
  } finally {
    executingId.value = null
  }
}

async function handleDelete(id: number) {
  try {
    await deleteSuite(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // Error handled by interceptor
  }
}

function getPassRateColor(rate: number): string {
  if (rate >= 90) return '#67c23a'
  if (rate >= 70) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped lang="scss">
.suite-list-container {
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

  .suite-name-link {
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
