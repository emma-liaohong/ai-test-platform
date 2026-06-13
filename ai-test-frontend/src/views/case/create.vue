<template>
  <div class="case-create-container">
    <!-- Header -->
    <div class="page-header">
      <div class="page-header-left">
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
        <span class="page-title">新建测试用例</span>
      </div>
      <div class="page-header-right">
        <el-button type="primary" :loading="saveLoading" :disabled="!typeSelected" @click="handleSave">
          <el-icon><Check /></el-icon>保存
        </el-button>
        <el-button @click="goBack">取消</el-button>
      </div>
    </div>

    <!-- Step 1: Select case type -->
    <div v-if="!typeSelected" class="type-selection">
      <el-card shadow="never">
        <template #header>
          <span class="card-title">选择用例类型</span>
        </template>
        <div class="type-cards">
          <div
            v-for="ct in caseTypes"
            :key="ct.value"
            class="type-card"
            :class="{ active: form.caseType === ct.value }"
            @click="selectType(ct.value)"
          >
            <div class="type-icon">{{ ct.icon }}</div>
            <div class="type-info">
              <div class="type-name">{{ ct.label }}</div>
              <div class="type-desc">{{ ct.desc }}</div>
            </div>
            <el-icon v-if="form.caseType === ct.value" class="type-check" :size="20">
              <CircleCheckFilled />
            </el-icon>
          </div>
        </div>
        <div class="type-action">
          <el-button type="primary" :disabled="!form.caseType" @click="confirmType">
            下一步：填写用例信息
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- Step 2: Fill form -->
    <template v-if="typeSelected">
      <div class="type-badge">
        <el-tag :type="caseTypeTagMap[form.caseType]?.type || 'info'" effect="plain" size="small">
          {{ caseTypeTagMap[form.caseType]?.icon || '' }} {{ caseTypeLabel }}
        </el-tag>
        <el-button text type="primary" size="small" @click="typeSelected = false">切换类型</el-button>
      </div>

      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <!-- Basic Info -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="card-title">基本信息</span>
          </template>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="用例名称" prop="caseName">
                <el-input v-model="form.caseName" placeholder="请输入用例名称" maxlength="200" show-word-limit />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="所属系统" prop="systemId">
                <el-select v-model="form.systemId" placeholder="请选择所属系统" filterable clearable style="width: 100%">
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
                <el-select v-model="form.priority" placeholder="请选择优先级" style="width: 100%">
                  <el-option label="P0 - 阻塞" value="P0" />
                  <el-option label="P1 - 严重" value="P1" />
                  <el-option label="P2 - 一般" value="P2" />
                  <el-option label="P3 - 轻微" value="P3" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="用例等级" prop="caseLevel">
                <el-select v-model="form.caseLevel" placeholder="请选择用例等级" clearable style="width: 100%">
                  <el-option label="L1 - 核心流程" value="L1" />
                  <el-option label="L2 - 主要功能" value="L2" />
                  <el-option label="L3 - 次要功能" value="L3" />
                  <el-option label="L4 - 边界场景" value="L4" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="模块路径">
                <el-cascader
                  v-model="form.modulePath"
                  :options="moduleOptions"
                  :props="{ checkStrictly: true, value: 'label', label: 'label', emitPath: false }"
                  placeholder="请选择模块路径"
                  clearable
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="标签">
                <el-select
                  v-model="form.tags"
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
              <el-form-item label="前置条件">
                <el-input
                  v-model="form.preconditions"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入测试前置条件"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="预期结果">
                <el-input
                  v-model="form.expectedResult"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入预期结果描述"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <!-- PC specific -->
        <template v-if="form.caseType === 'PC'">
          <el-card shadow="never" class="section-card">
            <template #header>
              <div class="card-header-row">
                <span class="card-title">执行步骤</span>
                <el-button type="primary" size="small" :icon="Plus" @click="addStep">添加步骤</el-button>
              </div>
            </template>
            <el-table :data="form.steps" stripe border size="small" style="width: 100%">
              <el-table-column label="序号" width="60" align="center">
                <template #default="{ $index }">
                  <span class="step-order">{{ $index + 1 }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作类型" width="130">
                <template #default="{ row }">
                  <el-select v-model="row.stepAction" size="small" style="width: 100%">
                    <el-option v-for="a in stepActions" :key="a.value" :label="a.label" :value="a.value" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="操作目标" min-width="160">
                <template #default="{ row }">
                  <el-input v-model="row.stepTarget" size="small" placeholder="如: #login-btn" />
                </template>
              </el-table-column>
              <el-table-column label="操作值" min-width="140">
                <template #default="{ row }">
                  <el-input v-model="row.stepValue" size="small" placeholder="如: admin" />
                </template>
              </el-table-column>
              <el-table-column label="步骤描述" min-width="160">
                <template #default="{ row }">
                  <el-input v-model="row.stepDescription" size="small" placeholder="步骤说明" />
                </template>
              </el-table-column>
              <el-table-column label="预期结果" min-width="160">
                <template #default="{ row }">
                  <el-input v-model="row.expectedResult" size="small" placeholder="该步骤预期结果" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="110" align="center" fixed="right">
                <template #default="{ $index }">
                  <el-button text type="primary" size="small" :disabled="$index === 0" @click="moveStep($index, -1)">
                    <el-icon><Top /></el-icon>
                  </el-button>
                  <el-button text type="primary" size="small" :disabled="$index === form.steps!.length - 1" @click="moveStep($index, 1)">
                    <el-icon><Bottom /></el-icon>
                  </el-button>
                  <el-button text type="danger" size="small" @click="removeStep($index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>

          <el-card shadow="never" class="section-card">
            <el-tabs v-model="activeTab">
              <el-tab-pane label="自然语言步骤" name="nl">
                <el-input
                  v-model="form.naturalLanguageSteps"
                  type="textarea"
                  :rows="8"
                  placeholder="请用自然语言描述测试步骤，每行一步"
                />
              </el-tab-pane>
              <el-tab-pane label="Playwright 脚本" name="script">
                <el-input
                  v-model="form.playwrightScript"
                  type="textarea"
                  :rows="12"
                  placeholder="// Playwright 脚本（代码编辑器即将支持）"
                  class="code-textarea"
                />
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </template>

        <!-- API specific -->
        <template v-if="form.caseType === 'API'">
          <el-card shadow="never" class="section-card">
            <template #header>
              <span class="card-title">接口配置</span>
            </template>
            <el-row :gutter="24">
              <el-col :span="6">
                <el-form-item label="请求方法" prop="apiMethod">
                  <el-select v-model="form.apiMethod" style="width: 100%">
                    <el-option v-for="m in httpMethods" :key="m" :label="m" :value="m" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="18">
                <el-form-item label="请求URL" prop="apiUrl">
                  <el-input v-model="form.apiUrl" placeholder="如: /api/v1/users" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="请求头">
                  <el-input
                    v-model="form.apiHeaders"
                    type="textarea"
                    :rows="4"
                    placeholder='JSON 格式请求头'
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
              v-model="form.apiRequestSchema"
              type="textarea"
              :rows="8"
              placeholder="JSON 格式请求体"
              class="code-textarea"
            />
          </el-card>

          <el-card shadow="never" class="section-card">
            <template #header>
              <span class="card-title">响应体 Schema</span>
            </template>
            <el-input
              v-model="form.apiResponseSchema"
              type="textarea"
              :rows="8"
              placeholder="JSON 格式响应体"
              class="code-textarea"
            />
          </el-card>

          <el-card shadow="never" class="section-card">
            <template #header>
              <div class="card-header-row">
                <span class="card-title">断言规则</span>
                <el-button type="primary" size="small" :icon="Plus" @click="addAssertion">添加断言</el-button>
              </div>
            </template>
            <el-table :data="form.assertionRules" stripe border size="small" style="width: 100%">
              <el-table-column label="序号" width="60" align="center">
                <template #default="{ $index }">{{ $index + 1 }}</template>
              </el-table-column>
              <el-table-column label="字段路径" min-width="180">
                <template #default="{ row }">
                  <el-input v-model="row.field" size="small" placeholder="如: $.data.token" />
                </template>
              </el-table-column>
              <el-table-column label="断言操作符" width="160">
                <template #default="{ row }">
                  <el-select v-model="row.operator" size="small" style="width: 100%">
                    <el-option label="等于 (==)" value="eq" />
                    <el-option label="不等于 (!=)" value="neq" />
                    <el-option label="包含" value="contains" />
                    <el-option label="不包含" value="notContains" />
                    <el-option label="大于 (>)" value="gt" />
                    <el-option label="小于 (<)" value="lt" />
                    <el-option label="为空" value="empty" />
                    <el-option label="不为空" value="notEmpty" />
                    <el-option label="匹配正则" value="regex" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="期望值" min-width="160">
                <template #default="{ row }">
                  <el-input v-model="row.expectedValue" size="small" placeholder="期望值" />
                </template>
              </el-table-column>
              <el-table-column label="描述" min-width="140">
                <template #default="{ row }">
                  <el-input v-model="row.description" size="small" placeholder="断言说明" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center" fixed="right">
                <template #default="{ $index }">
                  <el-button text type="danger" size="small" @click="removeAssertion($index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </template>

        <!-- APP placeholder -->
        <template v-if="form.caseType === 'APP'">
          <el-card shadow="never" class="section-card">
            <el-result icon="info" title="APP 用例" sub-title="APP 移动端测试用例功能即将支持，敬请期待">
              <template #extra>
                <el-tag type="warning" size="large" effect="plain">即将支持</el-tag>
              </template>
            </el-result>
          </el-card>
        </template>
      </el-form>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowLeft, Check, Plus, Delete, Top, Bottom, CircleCheckFilled,
} from '@element-plus/icons-vue'
import {
  createCase,
  getSystemOptions,
  type TestCase,
  type TestCaseStep,
  type AssertionRule,
  type CaseType,
} from '@/api/case'

const router = useRouter()
const formRef = ref<FormInstance>()
const saveLoading = ref(false)
const typeSelected = ref(false)
const activeTab = ref('nl')

const caseTypeTagMap: Record<string, { type: 'primary' | 'success' | 'warning'; icon: string }> = {
  PC: { type: 'primary', icon: '🖥' },
  APP: { type: 'success', icon: '📱' },
  API: { type: 'warning', icon: '🔌' },
}

const caseTypes = [
  { value: 'PC' as CaseType, label: 'PC Web 测试', icon: '🖥', desc: '基于 Playwright 的 PC 端 Web 自动化测试' },
  { value: 'APP' as CaseType, label: 'APP 移动端测试', icon: '📱', desc: '移动端 APP 自动化测试（即将支持）' },
  { value: 'API' as CaseType, label: 'API 接口测试', icon: '🔌', desc: 'HTTP 接口自动化测试，支持断言和数据驱动' },
]

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

const form = reactive<TestCase>({
  caseName: '',
  caseType: 'PC',
  systemId: null,
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

const caseTypeLabel = computed(() => {
  return caseTypes.find((ct) => ct.value === form.caseType)?.label || ''
})

onMounted(async () => {
  try {
    const res: any = await getSystemOptions()
    systemOptions.value = res.data || res || []
  } catch {
    // non-critical
  }
})

function selectType(type: CaseType) {
  form.caseType = type
}

function confirmType() {
  typeSelected.value = true
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
      const res: any = await createCase(form)
      ElMessage.success('用例创建成功')
      const newId = res.data?.id || res.id
      if (newId) {
        router.replace(`/cases/${newId}`)
      } else {
        router.push('/cases')
      }
    } catch {
      // Error handled by interceptor
    } finally {
      saveLoading.value = false
    }
  })
}

// ---- Step operations ----
function addStep() {
  if (!form.steps) form.steps = []
  const newStep: TestCaseStep = {
    stepOrder: form.steps.length + 1,
    stepAction: 'click',
    stepTarget: '',
    stepValue: '',
    stepDescription: '',
    expectedResult: '',
    screenshot: '',
  }
  form.steps.push(newStep)
}

function removeStep(index: number) {
  form.steps?.splice(index, 1)
  form.steps?.forEach((step, i) => {
    step.stepOrder = i + 1
  })
}

function moveStep(index: number, direction: number) {
  if (!form.steps) return
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= form.steps.length) return
  const temp = form.steps[index]
  form.steps[index] = form.steps[newIndex]
  form.steps[newIndex] = temp
  form.steps.forEach((step, i) => {
    step.stepOrder = i + 1
  })
}

// ---- Assertion operations ----
function addAssertion() {
  if (!form.assertionRules) form.assertionRules = []
  const newRule: AssertionRule = {
    ruleOrder: form.assertionRules.length + 1,
    field: '',
    operator: 'eq',
    expectedValue: '',
    description: '',
  }
  form.assertionRules.push(newRule)
}

function removeAssertion(index: number) {
  form.assertionRules?.splice(index, 1)
}
</script>

<style scoped lang="scss">
.case-create-container {
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
      }
    }

    .page-header-right {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .type-selection {
    .card-title {
      font-size: 15px;
      font-weight: 600;
    }

    .type-cards {
      display: flex;
      gap: 20px;
      justify-content: center;
      padding: 20px 0;
    }

    .type-card {
      width: 280px;
      padding: 24px;
      border: 2px solid var(--el-border-color-light);
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      position: relative;
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;

      &:hover {
        border-color: var(--el-color-primary-light-3);
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      }

      &.active {
        border-color: var(--el-color-primary);
        background: var(--el-color-primary-light-9);
      }

      .type-icon {
        font-size: 48px;
        margin-bottom: 12px;
      }

      .type-info {
        .type-name {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 8px;
          color: var(--el-text-color-primary);
        }

        .type-desc {
          font-size: 13px;
          color: var(--el-text-color-secondary);
          line-height: 1.5;
        }
      }

      .type-check {
        position: absolute;
        top: 12px;
        right: 12px;
        color: var(--el-color-primary);
      }
    }

    .type-action {
      text-align: center;
      padding-top: 16px;
      border-top: 1px solid var(--el-border-color-lighter);
      margin-top: 16px;
    }
  }

  .type-badge {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    padding: 8px 16px;
    background: var(--el-fill-color-light);
    border-radius: 4px;
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
}
</style>
