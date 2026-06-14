<template>
  <div class="case-detail-container">
    <!-- Header Actions -->
    <div class="page-header">
      <div class="page-header-left">
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
        <span class="page-title">
          <el-tag v-if="testCase.caseType" :type="caseTypeTagMap[testCase.caseType]?.type || 'info'" size="small" effect="plain">
            {{ caseTypeTagMap[testCase.caseType]?.icon || '' }} {{ testCase.caseType }}
          </el-tag>
          {{ isEditMode ? '编辑用例' : '用例详情' }}
          <span v-if="testCase.caseCode" class="case-code">（{{ testCase.caseCode }}）</span>
        </span>
      </div>
      <div class="page-header-right">
        <el-button v-if="!isEditMode" type="primary" :icon="Edit" @click="enterEdit">编辑</el-button>
        <el-button type="success" :icon="VideoPlay" @click="handleExecute" :loading="executeLoading">执行</el-button>
        <el-button v-if="isEditMode" type="primary" :loading="saveLoading" @click="handleSave">
          <el-icon><Check /></el-icon>保存
        </el-button>
        <el-button v-if="isEditMode" @click="cancelEdit">取消</el-button>
      </div>
    </div>

    <el-form
      ref="formRef"
      :model="testCase"
      :rules="formRules"
      label-width="110px"
      :disabled="!isEditMode"
      v-loading="pageLoading"
    >
      <!-- Basic Info Card -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <span class="card-title">基本信息</span>
        </template>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="用例名称" prop="caseName">
              <el-input v-model="testCase.caseName" placeholder="请输入用例名称" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属系统" prop="systemId">
              <el-select v-model="testCase.systemId" placeholder="请选择所属系统" filterable clearable style="width: 100%">
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
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="testCase.priority" placeholder="请选择优先级" style="width: 100%">
                <el-option label="P0 - 阻塞" value="P0" />
                <el-option label="P1 - 严重" value="P1" />
                <el-option label="P2 - 一般" value="P2" />
                <el-option label="P3 - 轻微" value="P3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用例等级" prop="caseLevel">
              <el-select v-model="testCase.caseLevel" placeholder="请选择用例等级" clearable style="width: 100%">
                <el-option label="L1 - 核心流程" value="L1" />
                <el-option label="L2 - 主要功能" value="L2" />
                <el-option label="L3 - 次要功能" value="L3" />
                <el-option label="L4 - 边界场景" value="L4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模块路径" prop="modulePath">
              <el-cascader
                v-model="testCase.modulePath"
                :options="moduleOptions"
                :props="{ checkStrictly: true, value: 'label', label: 'label', emitPath: false }"
                placeholder="请选择模块路径"
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签" prop="tags">
              <el-select
                v-model="testCase.tags"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="输入后按回车添加标签"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="前置条件" prop="preconditions">
              <el-input
                v-model="testCase.preconditions"
                type="textarea"
                :rows="3"
                placeholder="请输入测试前置条件，如：需要登录管理员账号、需要准备测试数据等"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="预期结果" prop="expectedResult">
              <el-input
                v-model="testCase.expectedResult"
                type="textarea"
                :rows="2"
                placeholder="请输入预期结果描述"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- =================== PC Case Section =================== -->
      <template v-if="testCase.caseType === 'PC'">
        <!-- Steps Card -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header-row">
              <span class="card-title">执行步骤</span>
              <el-button v-if="isEditMode" type="primary" size="small" :icon="Plus" @click="addStep">
                添加步骤
              </el-button>
            </div>
          </template>
          <el-table :data="testCase.steps" stripe border size="small" style="width: 100%">
            <el-table-column label="序号" width="60" align="center">
              <template #default="{ $index }">
                <span class="step-order">{{ $index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作类型" width="130">
              <template #default="{ row }">
                <el-select v-model="row.stepAction" size="small" :disabled="!isEditMode" style="width: 100%">
                  <el-option v-for="a in stepActions" :key="a.value" :label="a.label" :value="a.value" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="操作目标" min-width="160">
              <template #default="{ row }">
                <el-input v-model="row.stepTarget" size="small" placeholder="如: #login-btn" :disabled="!isEditMode" />
              </template>
            </el-table-column>
            <el-table-column label="操作值" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.stepValue" size="small" placeholder="如: admin" :disabled="!isEditMode" />
              </template>
            </el-table-column>
            <el-table-column label="步骤描述" min-width="160">
              <template #default="{ row }">
                <el-input v-model="row.stepDescription" size="small" placeholder="步骤说明" :disabled="!isEditMode" />
              </template>
            </el-table-column>
            <el-table-column label="预期结果" min-width="160">
              <template #default="{ row }">
                <el-input v-model="row.expectedResult" size="small" placeholder="该步骤预期结果" :disabled="!isEditMode" />
              </template>
            </el-table-column>
            <el-table-column label="截图" width="80" align="center">
              <template #default="{ row }">
                <el-image
                  v-if="row.screenshot"
                  :src="row.screenshot"
                  :preview-src-list="[row.screenshot]"
                  fit="cover"
                  style="width: 40px; height: 40px; border-radius: 4px"
                  preview-teleported
                />
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column v-if="isEditMode" label="操作" width="110" align="center" fixed="right">
              <template #default="{ $index }">
                <el-button text type="primary" size="small" :disabled="$index === 0" @click="moveStep($index, -1)">
                  <el-icon><Top /></el-icon>
                </el-button>
                <el-button text type="primary" size="small" :disabled="$index === testCase.steps!.length - 1" @click="moveStep($index, 1)">
                  <el-icon><Bottom /></el-icon>
                </el-button>
                <el-button text type="danger" size="small" @click="removeStep($index)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- NL Steps & Script Tabs -->
        <el-card shadow="never" class="section-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="自然语言步骤" name="nl">
              <el-input
                v-model="testCase.naturalLanguageSteps"
                type="textarea"
                :rows="8"
                :disabled="!isEditMode"
                placeholder="请用自然语言描述测试步骤，每行一步，如：&#10;1. 打开浏览器，访问登录页面&#10;2. 在用户名输入框输入 admin&#10;3. 在密码输入框输入 123456&#10;4. 点击登录按钮&#10;5. 验证跳转到首页"
              />
            </el-tab-pane>
            <el-tab-pane label="Playwright 脚本" name="script">
              <div class="code-editor-placeholder">
                <el-input
                  v-model="testCase.playwrightScript"
                  type="textarea"
                  :rows="12"
                  :disabled="!isEditMode"
                  placeholder="// Playwright 脚本（代码编辑器即将支持）&#10;import { test, expect } from '@playwright/test';&#10;&#10;test('test case', async ({ page }) => {&#10;  await page.goto('/login');&#10;  await page.fill('#username', 'admin');&#10;  await page.fill('#password', '123456');&#10;  await page.click('#login-btn');&#10;  await expect(page).toHaveURL('/dashboard');&#10;});"
                  class="code-textarea"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>

        <!-- Data Driven Section -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header-row">
              <span class="card-title">测试数据（数据驱动）</span>
              <el-switch
                v-model="testCase.dataDrivenEnabled"
                :disabled="!isEditMode"
                active-text="启用"
                inactive-text="关闭"
                inline-prompt
              />
            </div>
          </template>
          <div v-if="testCase.dataDrivenEnabled">
            <TestDataTable
              ref="dataTableRef"
              :case-id="testCase.id"
              :data-table="testCase.dataTable"
              :editable="isEditMode"
              @update="handleDataTableUpdate"
            />
          </div>
          <el-empty v-else description="数据驱动未启用" :image-size="60" />
        </el-card>
      </template>

      <!-- =================== API Case Section =================== -->
      <template v-if="testCase.caseType === 'API'">
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="card-title">接口配置</span>
          </template>
          <el-row :gutter="24">
            <el-col :span="6">
              <el-form-item label="请求方法" prop="apiMethod">
                <el-select v-model="testCase.apiMethod" style="width: 100%">
                  <el-option v-for="m in httpMethods" :key="m" :label="m" :value="m" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="18">
              <el-form-item label="请求URL" prop="apiUrl">
                <el-input v-model="testCase.apiUrl" placeholder="如: /api/v1/users 或 https://api.example.com/v1/users" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="请求头" prop="apiHeaders">
                <el-input
                  v-model="testCase.apiHeaders"
                  type="textarea"
                  :rows="4"
                  placeholder='JSON 格式，如：&#10;{&#10;  "Content-Type": "application/json",&#10;  "Authorization": "Bearer {{token}}",&#10;  "X-Request-Id": "{{uuid}}"}'
                  class="code-textarea"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="card-title">请求体 Schema</span>
          </template>
          <el-input
            v-model="testCase.apiRequestSchema"
            type="textarea"
            :rows="8"
            placeholder='JSON Schema 或示例请求体，如：&#10;{&#10;  "username": "admin",&#10;  "password": "123456",&#10;  "rememberMe": true&#10;}'
            class="code-textarea"
          />
        </el-card>

        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="card-title">响应体 Schema</span>
          </template>
          <el-input
            v-model="testCase.apiResponseSchema"
            type="textarea"
            :rows="8"
            placeholder='JSON Schema 或示例响应体，如：&#10;{&#10;  "code": 200,&#10;  "data": { "token": "string" },&#10;  "message": "success"&#10;}'
            class="code-textarea"
          />
        </el-card>

        <!-- Assertion Rules -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header-row">
              <span class="card-title">断言规则</span>
              <el-button v-if="isEditMode" type="primary" size="small" :icon="Plus" @click="addAssertion">
                添加断言
              </el-button>
            </div>
          </template>
          <el-table :data="testCase.assertionRules" stripe border size="small" style="width: 100%">
            <el-table-column label="序号" width="60" align="center">
              <template #default="{ $index }">{{ $index + 1 }}</template>
            </el-table-column>
            <el-table-column label="字段路径" min-width="180">
              <template #default="{ row }">
                <el-input v-model="row.field" size="small" placeholder="如: $.data.token" :disabled="!isEditMode" />
              </template>
            </el-table-column>
            <el-table-column label="断言操作符" width="160">
              <template #default="{ row }">
                <el-select v-model="row.operator" size="small" :disabled="!isEditMode" style="width: 100%">
                  <el-option label="等于 (==)" value="eq" />
                  <el-option label="不等于 (!=)" value="neq" />
                  <el-option label="包含 (contains)" value="contains" />
                  <el-option label="不包含" value="notContains" />
                  <el-option label="大于 (>)" value="gt" />
                  <el-option label="大于等于 (>=)" value="gte" />
                  <el-option label="小于 (<)" value="lt" />
                  <el-option label="小于等于 (<=)" value="lte" />
                  <el-option label="为空" value="empty" />
                  <el-option label="不为空" value="notEmpty" />
                  <el-option label="匹配正则" value="regex" />
                  <el-option label="类型是" value="isType" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="期望值" min-width="160">
              <template #default="{ row }">
                <el-input v-model="row.expectedValue" size="small" placeholder="期望值" :disabled="!isEditMode" />
              </template>
            </el-table-column>
            <el-table-column label="描述" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.description" size="small" placeholder="断言说明" :disabled="!isEditMode" />
              </template>
            </el-table-column>
            <el-table-column v-if="isEditMode" label="操作" width="70" align="center" fixed="right">
              <template #default="{ $index }">
                <el-button text type="danger" size="small" @click="removeAssertion($index)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!testCase.assertionRules?.length" description="暂无断言规则，点击上方按钮添加" :image-size="60" />
        </el-card>
      </template>

      <!-- =================== APP Case Section =================== -->
      <template v-if="testCase.caseType === 'APP'">
        <el-card shadow="never" class="section-card">
          <el-result icon="info" title="APP 用例" sub-title="APP 移动端测试用例功能即将支持，敬请期待">
            <template #extra>
              <el-tag type="warning" size="large" effect="plain">即将支持</el-tag>
            </template>
          </el-result>
        </el-card>
      </template>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowLeft, Edit, VideoPlay, Check, Plus, Delete, Top, Bottom,
} from '@element-plus/icons-vue'
import {
  getCaseDetail,
  updateCase,
  executeCase,
  getSystemOptions,
  type TestCase,
  type TestCaseStep,
  type AssertionRule,
  type DataTable,
} from '@/api/case'
import TestDataTable from './TestDataTable.vue'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const pageLoading = ref(false)
const saveLoading = ref(false)
const executeLoading = ref(false)
const activeTab = ref('nl')
const dataTableRef = ref<InstanceType<typeof TestDataTable>>()

const caseId = computed(() => Number(route.params.id))
const isEditMode = ref(false)

// Case type tag map
const caseTypeTagMap: Record<string, { type: 'primary' | 'success' | 'warning'; icon: string }> = {
  PC: { type: 'primary', icon: '🖥' },
  APP: { type: 'success', icon: '📱' },
  API: { type: 'warning', icon: '🔌' },
}

const stepActions = [
  { label: '点击 (Click)', value: 'click' },
  { label: '输入 (Input)', value: 'input' },
  { label: '选择 (Select)', value: 'select' },
  { label: '滑动 (Swipe)', value: 'swipe' },
  { label: '等待 (Wait)', value: 'wait' },
  { label: '断言 (Assert)', value: 'assert' },
  { label: '悬停 (Hover)', value: 'hover' },
  { label: '滚动 (Scroll)', value: 'scroll' },
]

const httpMethods = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS']

const systemOptions = ref<Array<{ id: number; name: string }>>([])

const moduleOptions = [
  {
    label: '用户模块', children: [
      { label: '登录' }, { label: '注册' }, { label: '权限管理' },
    ],
  },
  {
    label: '订单模块', children: [
      { label: '创建订单' }, { label: '支付' }, { label: '退款' },
    ],
  },
  {
    label: '商品模块', children: [
      { label: '商品列表' }, { label: '商品详情' }, { label: '库存管理' },
    ],
  },
]

const testCase = reactive<TestCase>({
  caseName: '',
  caseType: 'PC',
  systemId: null as any,
  modulePath: [],
  priority: 'P2',
  caseLevel: '',
  tags: [],
  preconditions: '',
  expectedResult: '',
  status: 'draft',
  steps: [],
  naturalLanguageSteps: '',
  playwrightScript: '',
  dataDrivenEnabled: false,
  dataTable: undefined,
  // API fields
  apiMethod: 'POST',
  apiUrl: '',
  apiHeaders: '{\n  "Content-Type": "application/json"\n}',
  apiRequestSchema: '',
  apiResponseSchema: '',
  assertionRules: [],
})

const formRules = reactive<FormRules>({
  caseName: [{ required: true, message: '请输入用例名称', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
})

onMounted(async () => {
  await fetchSystemOptions()
  if (caseId.value) {
    await fetchCaseDetail()
  }
  // Check if edit mode from query param
  if (route.query.edit === '1') {
    isEditMode.value = true
  }
})

async function fetchSystemOptions() {
  try {
    const res: any = await getSystemOptions()
    systemOptions.value = res.data || res || []
  } catch {
    // non-critical
  }
}

async function fetchCaseDetail() {
  pageLoading.value = true
  try {
    const res: any = await getCaseDetail(caseId.value)
    const data = res.data || res
    Object.assign(testCase, {
      ...data,
      modulePath: data.modulePath || [],
      tags: data.tags || [],
      steps: data.steps || [],
      assertionRules: data.assertionRules || [],
    })
  } catch {
    // Error handled by interceptor
  } finally {
    pageLoading.value = false
  }
}

function enterEdit() {
  isEditMode.value = true
}

function cancelEdit() {
  if (caseId.value) {
    // Re-fetch to discard changes
    fetchCaseDetail()
  }
  isEditMode.value = false
}

function goBack() {
  router.push('/cases')
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    saveLoading.value = true
    try {
      const payload = { ...testCase }
      // Clean up empty strings
      if (!payload.caseCode) delete payload.caseCode

      await updateCase(caseId.value, payload)
      ElMessage.success('保存成功')
      // If data table was modified, save it too
      if (testCase.dataDrivenEnabled && dataTableRef.value) {
        // Data table save is handled internally
      }
    } catch {
      // Error handled by interceptor
    } finally {
      saveLoading.value = false
    }
  })
}

async function handleExecute() {
  try {
    await ElMessageBox.confirm('确定执行该测试用例？', '确认执行', {
      confirmButtonText: '执行',
      cancelButtonText: '取消',
      type: 'info',
    })
    executeLoading.value = true
    await executeCase(caseId.value)
    ElMessage.success('用例已提交执行')
  } catch {
    // cancelled or error
  } finally {
    executeLoading.value = false
  }
}

// ---- Step operations ----
function addStep() {
  if (!testCase.steps) testCase.steps = []
  const newStep: TestCaseStep = {
    stepOrder: testCase.steps.length + 1,
    stepAction: 'click',
    stepTarget: '',
    stepValue: '',
    stepDescription: '',
    expectedResult: '',
    screenshot: '',
  }
  testCase.steps.push(newStep)
}

function removeStep(index: number) {
  testCase.steps?.splice(index, 1)
  // Reorder
  testCase.steps?.forEach((step, i) => {
    step.stepOrder = i + 1
  })
}

function moveStep(index: number, direction: number) {
  if (!testCase.steps) return
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= testCase.steps.length) return
  const temp = testCase.steps[index]
  testCase.steps[index] = testCase.steps[newIndex]
  testCase.steps[newIndex] = temp
  // Reorder
  testCase.steps.forEach((step, i) => {
    step.stepOrder = i + 1
  })
}

// ---- Assertion operations ----
function addAssertion() {
  if (!testCase.assertionRules) testCase.assertionRules = []
  const newRule: AssertionRule = {
    ruleOrder: testCase.assertionRules.length + 1,
    field: '',
    operator: 'eq',
    expectedValue: '',
    description: '',
  }
  testCase.assertionRules.push(newRule)
}

function removeAssertion(index: number) {
  testCase.assertionRules?.splice(index, 1)
  testCase.assertionRules?.forEach((rule, i) => {
    rule.ruleOrder = i + 1
  })
}

// ---- Data table events ----
function handleDataTableUpdate(dataTable: DataTable) {
  testCase.dataTable = dataTable
}
</script>

<style scoped lang="scss">
.case-detail-container {
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

        .case-code {
          color: var(--el-text-color-secondary);
          font-weight: 400;
          font-size: 14px;
        }
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

  .step-order {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: var(--el-color-primary-light-8);
    color: var(--el-color-primary);
    font-size: 12px;
    font-weight: 600;
  }

  .code-textarea {
    :deep(textarea) {
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
      font-size: 13px;
      line-height: 1.6;
    }
  }

  .code-editor-placeholder {
    position: relative;
  }

  .text-muted {
    color: var(--el-text-color-placeholder);
    font-size: 13px;
  }
}
</style>
