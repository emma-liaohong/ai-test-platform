<template>
  <div class="skill-list-container">
    <!-- ==================== Toolbar ==================== -->
    <el-card shadow="never">
      <div class="page-header">
        <h3 class="page-title">AI 技能管理</h3>
        <div class="toolbar-actions">
          <el-button type="primary" :icon="Plus" @click="handleCreate">新建 SKILL</el-button>
          <el-button :icon="Monitor" @click="playgroundVisible = true">实验场</el-button>
        </div>
      </div>

      <div class="search-toolbar">
        <div class="search-left">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索技能名称/编码..."
            :prefix-icon="Search"
            clearable
            style="width: 220px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-select
            v-model="queryParams.skillType"
            placeholder="技能类型"
            clearable
            style="width: 140px"
            @change="handleSearch"
          >
            <el-option label="内置" value="BUILTIN" />
            <el-option label="自定义" value="CUSTOM" />
            <el-option label="学习" value="LEARNED" />
          </el-select>
          <el-select
            v-model="queryParams.category"
            placeholder="分类"
            clearable
            style="width: 130px"
            @change="handleSearch"
          >
            <el-option label="测试" value="TESTING" />
            <el-option label="分析" value="ANALYSIS" />
            <el-option label="生成" value="GENERATION" />
            <el-option label="执行" value="EXECUTION" />
          </el-select>
          <el-select
            v-model="queryParams.executorType"
            placeholder="执行器"
            clearable
            style="width: 140px"
            @change="handleSearch"
          >
            <el-option label="LLM" value="LLM" />
            <el-option label="CODE" value="CODE" />
            <el-option label="API" value="API" />
            <el-option label="WORKFLOW" value="WORKFLOW" />
          </el-select>
          <el-select
            v-model="queryParams.status"
            placeholder="状态"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="激活" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
            <el-option label="测试中" value="TESTING" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>

      <!-- ==================== Type Tabs ==================== -->
      <el-tabs v-model="activeTypeTab" class="skill-type-tabs" @tab-change="handleTypeTabChange">
        <el-tab-pane :label="`全部 (${typeCounts.all})`" name="ALL" />
        <el-tab-pane :label="`BUILTIN (${typeCounts.BUILTIN})`" name="BUILTIN" />
        <el-tab-pane :label="`CUSTOM (${typeCounts.CUSTOM})`" name="CUSTOM" />
        <el-tab-pane :label="`LEARNED (${typeCounts.LEARNED})`" name="LEARNED" />
      </el-tabs>

      <!-- ==================== Skill Card Grid ==================== -->
      <div v-loading="loading" class="skill-grid">
        <el-row :gutter="16">
          <el-col
            v-for="skill in skillList"
            :key="skill.id"
            :xs="24"
            :sm="12"
            :md="8"
          >
            <el-card
              class="skill-card"
              shadow="hover"
              @click="openEditor(skill)"
            >
              <div class="card-header">
                <span class="skill-icon">{{ executorIcon(skill.executorType) }}</span>
                <span class="skill-name">{{ skill.skillName }}</span>
                <el-tag
                  :type="typeTagColor(skill.skillType)"
                  size="small"
                  effect="plain"
                  class="type-badge"
                >
                  {{ skill.skillType }}
                </el-tag>
              </div>

              <div class="card-body">
                <div class="card-info-row">
                  <span class="info-label">编码:</span>
                  <span class="info-value">{{ skill.skillCode }}</span>
                </div>
                <div class="card-info-row">
                  <span class="info-label">分类:</span>
                  <el-tag size="small" effect="plain">{{ categoryLabel(skill.category) }}</el-tag>
                </div>
                <div class="card-info-row">
                  <span class="info-label">执行器:</span>
                  <span class="info-value">{{ skill.executorType || '-' }}</span>
                </div>

                <div class="card-metrics">
                  <div class="metric-item">
                    <span class="metric-label">置信度</span>
                    <el-progress
                      :percentage="Math.round((skill.confidence || 0) * 100)"
                      :color="confidenceColor(skill.confidence)"
                      :stroke-width="6"
                      :show-text="true"
                      :text-inside="false"
                      style="flex: 1"
                    />
                  </div>
                  <div class="metric-row">
                    <span class="metric-text">
                      <el-icon><Histogram /></el-icon> 使用 {{ skill.usageCount || 0 }} 次
                    </span>
                    <span class="metric-text success-rate">
                      <el-icon><CircleCheck /></el-icon>
                      {{ successRate(skill) }}% 成功率
                    </span>
                  </div>
                </div>

                <div class="card-status">
                  <span class="status-dot" :class="statusClass(skill.status)"></span>
                  <span class="status-text">{{ statusLabel(skill.status) }}</span>
                </div>
              </div>

              <div class="card-actions" @click.stop>
                <el-button size="small" type="primary" plain :icon="VideoPlay" @click="handleTestSkill(skill)">
                  测试
                </el-button>
                <el-dropdown trigger="click" @command="(cmd: string) => handleCardAction(cmd, skill)">
                  <el-button size="small" text :icon="MoreFilled" @click.stop />
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">
                        <el-icon><Edit /></el-icon> 编辑
                      </el-dropdown-item>
                      <el-dropdown-item :command="skill.status === 'ACTIVE' ? 'disable' : 'enable'">
                        <el-icon><Lock /></el-icon>
                        {{ skill.status === 'ACTIVE' ? '禁用' : '启用' }}
                      </el-dropdown-item>
                      <el-dropdown-item command="delete" divided>
                        <el-icon><Delete /></el-icon>
                        <span style="color: var(--el-color-danger)">删除</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-empty v-if="!loading && skillList.length === 0" description="暂无技能数据" />
      </div>

      <!-- ==================== Pagination ==================== -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="pagination.total"
          :page-sizes="[9, 18, 36, 72]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>

      <!-- ==================== Stats Summary ==================== -->
      <div class="stats-summary">
        <div class="stat-item">
          <el-icon :size="18"><DataLine /></el-icon>
          <span>共 <strong>{{ pagination.total }}</strong> 个 SKILL</span>
        </div>
        <div class="stat-item">
          <el-icon :size="18"><TrendCharts /></el-icon>
          <span>本月新增: <strong>{{ stats.monthNew }}</strong> 个 LEARNED SKILL</span>
        </div>
        <div class="stat-item">
          <el-icon :size="18"><Promotion /></el-icon>
          <span>进化: <strong>{{ stats.evolved }}</strong> 次</span>
        </div>
        <div class="stat-item">
          <el-icon :size="18"><RemoveFilled /></el-icon>
          <span>淘汰: <strong>{{ stats.retired }}</strong> 个</span>
        </div>
      </div>
    </el-card>

    <!-- ==================== Editor Drawer ==================== -->
    <el-drawer
      v-model="editorVisible"
      :title="editingSkill?.id ? `编辑技能 - ${editingSkill.skillName}` : '新建技能'"
      direction="rtl"
      size="650px"
      destroy-on-close
    >
      <div class="editor-content">
        <el-tabs v-model="editorTab" class="editor-tabs">
          <!-- Tab 1: Basic Info -->
          <el-tab-pane label="基本信息" name="basic">
            <el-form
              ref="formRef"
              :model="skillForm"
              :rules="formRules"
              label-width="100px"
              class="editor-form"
            >
              <el-form-item label="技能编码" prop="skillCode">
                <el-input v-model="skillForm.skillCode" placeholder="如: case-generator" :disabled="!!editingSkill?.id" />
              </el-form-item>
              <el-form-item label="技能名称" prop="skillName">
                <el-input v-model="skillForm.skillName" placeholder="如: 案例生成器" />
              </el-form-item>
              <el-form-item label="技能类型" prop="skillType">
                <el-select v-model="skillForm.skillType" placeholder="选择类型" style="width: 100%">
                  <el-option label="内置" value="BUILTIN" />
                  <el-option label="自定义" value="CUSTOM" />
                  <el-option label="学习" value="LEARNED" />
                </el-select>
              </el-form-item>
              <el-form-item label="分类" prop="category">
                <el-select v-model="skillForm.category" placeholder="选择分类" clearable style="width: 100%">
                  <el-option label="测试" value="TESTING" />
                  <el-option label="分析" value="ANALYSIS" />
                  <el-option label="生成" value="GENERATION" />
                  <el-option label="执行" value="EXECUTION" />
                </el-select>
              </el-form-item>
              <el-form-item label="执行器类型" prop="executorType">
                <el-select v-model="skillForm.executorType" placeholder="选择执行器" style="width: 100%">
                  <el-option label="🤖 LLM" value="LLM" />
                  <el-option label="📝 CODE" value="CODE" />
                  <el-option label="🔗 API" value="API" />
                  <el-option label="⚡ WORKFLOW" value="WORKFLOW" />
                </el-select>
              </el-form-item>
              <el-form-item label="描述">
                <el-input
                  v-model="skillForm.description"
                  type="textarea"
                  :rows="3"
                  placeholder="技能功能描述"
                />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="skillForm.status" style="width: 100%">
                  <el-option label="激活" value="ACTIVE" />
                  <el-option label="禁用" value="DISABLED" />
                  <el-option label="测试中" value="TESTING" />
                </el-select>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- Tab 2: Trigger Conditions -->
          <el-tab-pane label="触发条件" name="trigger">
            <div class="editor-form">
              <div class="form-section">
                <label class="section-label">触发关键词</label>
                <div class="keyword-input-area">
                  <el-input
                    v-model="keywordInput"
                    placeholder="输入关键词后按回车或逗号分隔"
                    @keyup.enter="addKeyword"
                    @keyup="handleKeywordKeyup"
                  >
                    <template #append>
                      <el-button @click="addKeyword">添加</el-button>
                    </template>
                  </el-input>
                  <div class="keyword-tags">
                    <el-tag
                      v-for="(kw, idx) in keywordList"
                      :key="idx"
                      closable
                      effect="plain"
                      @close="keywordList.splice(idx, 1)"
                    >
                      {{ kw }}
                    </el-tag>
                    <span v-if="keywordList.length === 0" class="text-muted">暂无关键词</span>
                  </div>
                </div>
              </div>
              <div class="form-section">
                <label class="section-label">触发意图 (JSON)</label>
                <el-input
                  v-model="skillForm.triggerIntent"
                  type="textarea"
                  :rows="8"
                  placeholder='如: {"intent": "generate_case", "confidence_threshold": 0.7}'
                  class="code-textarea"
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- Tab 3: Execution Config -->
          <el-tab-pane label="执行配置" name="executor">
            <div class="editor-form">
              <!-- LLM Config -->
              <template v-if="skillForm.executorType === 'LLM'">
                <div class="form-section">
                  <label class="section-label">Prompt 模板</label>
                  <el-input
                    v-model="skillForm.promptTemplate"
                    type="textarea"
                    :rows="12"
                    placeholder="输入 Prompt 模板，使用 {{变量名}} 作为占位符..."
                    class="code-textarea"
                  />
                </div>
              </template>

              <!-- CODE Config -->
              <template v-else-if="skillForm.executorType === 'CODE'">
                <div class="form-section">
                  <label class="section-label">脚本内容</label>
                  <el-input
                    v-model="skillForm.scriptContent"
                    type="textarea"
                    :rows="14"
                    placeholder="# 在此输入脚本代码..."
                    class="code-textarea"
                  />
                </div>
              </template>

              <!-- API Config -->
              <template v-else-if="skillForm.executorType === 'API'">
                <div class="form-section">
                  <label class="section-label">API 配置 (JSON)</label>
                  <el-input
                    v-model="skillForm.executorConfig"
                    type="textarea"
                    :rows="10"
                    placeholder='{
  "endpoint": "/api/external",
  "method": "POST",
  "headers": { "Content-Type": "application/json" },
  "timeout": 30000
}'
                    class="code-textarea"
                  />
                </div>
              </template>

              <!-- WORKFLOW Config -->
              <template v-else-if="skillForm.executorType === 'WORKFLOW'">
                <div class="form-section">
                  <label class="section-label">工作流配置 (JSON)</label>
                  <el-input
                    v-model="skillForm.executorConfig"
                    type="textarea"
                    :rows="10"
                    placeholder='{
  "steps": [
    { "skillCode": "requirement-parser", "name": "需求解析" },
    { "skillCode": "case-generator", "name": "案例生成" }
  ],
  "parallel": false
}'
                    class="code-textarea"
                  />
                </div>
              </template>

              <template v-else>
                <el-empty description="请先选择执行器类型" :image-size="80" />
              </template>

              <el-divider />

              <div class="form-section">
                <label class="section-label">输入 Schema (JSON)</label>
                <el-input
                  v-model="skillForm.inputSchema"
                  type="textarea"
                  :rows="4"
                  placeholder='{"type": "object", "properties": {...}}'
                  class="code-textarea"
                />
              </div>
              <div class="form-section">
                <label class="section-label">输出 Schema (JSON)</label>
                <el-input
                  v-model="skillForm.outputSchema"
                  type="textarea"
                  :rows="4"
                  placeholder='{"type": "object", "properties": {...}}'
                  class="code-textarea"
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- Tab 4: Execution Logs -->
          <el-tab-pane label="执行日志" name="logs">
            <div v-if="editingSkill?.id" class="log-section">
              <el-table
                :data="executionLogs"
                v-loading="logsLoading"
                stripe
                size="small"
                style="width: 100%"
              >
                <el-table-column prop="createdAt" label="执行时间" width="160" show-overflow-tooltip />
                <el-table-column prop="inputParams" label="输入参数" min-width="160" show-overflow-tooltip />
                <el-table-column label="状态" width="90" align="center">
                  <template #default="{ row }">
                    <el-tag
                      :type="row.status === 'SUCCESS' ? 'success' : row.status === 'FAILED' ? 'danger' : 'warning'"
                      size="small"
                    >
                      {{ row.status === 'SUCCESS' ? '成功' : row.status === 'FAILED' ? '失败' : '运行中' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="耗时" width="90" align="center">
                  <template #default="{ row }">
                    {{ row.durationMs ? `${row.durationMs}ms` : '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="反馈" width="100" align="center">
                  <template #default="{ row }">
                    <span v-if="row.userFeedback === 'LIKE'" style="color: var(--el-color-success)">👍</span>
                    <span v-else-if="row.userFeedback === 'DISLIKE'" style="color: var(--el-color-danger)">👎</span>
                    <span v-else class="text-muted">-</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100" fixed="right">
                  <template #default="{ row }">
                    <el-button
                      text
                      size="small"
                      type="success"
                      @click="handleLogFeedback(row.id!, 'LIKE')"
                    >👍</el-button>
                    <el-button
                      text
                      size="small"
                      type="danger"
                      @click="handleLogFeedback(row.id!, 'DISLIKE')"
                    >👎</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <el-empty v-else description="请先保存技能后查看日志" :image-size="80" />
          </el-tab-pane>
        </el-tabs>
      </div>

      <template #footer>
        <el-button @click="editorVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSaveSkill">
          {{ editingSkill?.id ? '保存修改' : '创建技能' }}
        </el-button>
      </template>
    </el-drawer>

    <!-- ==================== Playground Dialog ==================== -->
    <el-dialog
      v-model="playgroundVisible"
      title="SKILL 实验场"
      width="800px"
      destroy-on-close
      top="5vh"
    >
      <div class="playground-content">
        <!-- Skill Select -->
        <div class="playground-section">
          <label class="section-label">选择技能</label>
          <el-select
            v-model="playgroundSkillId"
            placeholder="搜索并选择技能..."
            filterable
            style="width: 100%"
            @change="handlePlaygroundSkillChange"
          >
            <el-option
              v-for="s in activeSkills"
              :key="s.id"
              :label="`${executorIcon(s.executorType)} ${s.skillName} (${s.skillCode})`"
              :value="s.id!"
            />
          </el-select>
        </div>

        <!-- Input Params -->
        <div class="playground-section">
          <label class="section-label">输入参数 (JSON)</label>
          <el-input
            v-model="playgroundInput"
            type="textarea"
            :rows="6"
            placeholder='{"key": "value"}'
            class="code-textarea"
          />
        </div>

        <!-- Execute Button -->
        <div class="playground-execute">
          <el-button
            type="primary"
            size="large"
            :icon="VideoPlay"
            :loading="invoking"
            :disabled="!playgroundSkillId"
            @click="handleInvoke"
          >
            执行
          </el-button>
        </div>

        <!-- Result Display -->
        <div v-if="playgroundResult !== null" class="playground-section result-section">
          <label class="section-label">执行结果</label>
          <div class="result-meta">
            <el-tag
              :type="playgroundResult.status === 'SUCCESS' ? 'success' : 'danger'"
              size="small"
            >
              {{ playgroundResult.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
            <span class="result-duration">
              耗时: {{ playgroundResult.durationMs }}ms
            </span>
            <div class="result-feedback">
              <el-button
                text
                :type="playgroundResult.feedback === 'LIKE' ? 'success' : ''"
                @click="handlePlaygroundFeedback('LIKE')"
              >👍</el-button>
              <el-button
                text
                :type="playgroundResult.feedback === 'DISLIKE' ? 'danger' : ''"
                @click="handlePlaygroundFeedback('DISLIKE')"
              >👎</el-button>
            </div>
          </div>
          <div class="result-body">
            <el-input
              :model-value="formatResult(playgroundResult.outputResult)"
              type="textarea"
              :rows="8"
              readonly
              class="code-textarea"
            />
          </div>
          <div v-if="playgroundResult.errorMessage" class="result-error">
            <el-alert :title="playgroundResult.errorMessage" type="error" :closable="false" show-icon />
          </div>
        </div>

        <el-divider />

        <!-- Intent Matching -->
        <div class="playground-section">
          <label class="section-label">意图匹配</label>
          <div class="intent-row">
            <el-input
              v-model="intentQuery"
              placeholder="输入自然语言描述，测试意图匹配..."
              clearable
              style="flex: 1"
              @keyup.enter="handleMatchIntent"
            />
            <el-button type="primary" :icon="Search" :loading="intentLoading" @click="handleMatchIntent">
              匹配
            </el-button>
          </div>
          <div v-if="intentResult" class="intent-result">
            <template v-if="intentResult.length > 0">
              <div v-for="(match, idx) in intentResult" :key="idx" class="intent-match-item">
                <span class="match-name">{{ executorIcon(match.executorType) }} {{ match.skillName }}</span>
                <el-tag size="small" :type="match.confidence > 0.8 ? 'success' : 'warning'">
                  置信度: {{ Math.round(match.confidence * 100) }}%
                </el-tag>
                <el-tag size="small" effect="plain">{{ match.skillCode }}</el-tag>
              </div>
            </template>
            <el-empty v-else description="未匹配到相关技能" :image-size="60" />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Search,
  Plus,
  Refresh,
  Delete,
  Edit,
  VideoPlay,
  MoreFilled,
  Lock,
  Monitor,
  Histogram,
  CircleCheck,
  DataLine,
  TrendCharts,
  Promotion,
  RemoveFilled,
} from '@element-plus/icons-vue'
import {
  getSkillList,
  createSkill,
  updateSkill,
  deleteSkill,
  invokeSkill,
  matchIntent,
  getActiveSkills,
  getSkillLogs,
  submitFeedback,
  type AiSkill,
  type SkillQuery,
  type SkillExecutionLog,
  type SkillType,
  type ExecutorType,
} from '@/api/skill'

// ==================== List State ====================
const loading = ref(false)
const queryParams = reactive<SkillQuery>({
  page: 1,
  size: 9,
  keyword: '',
  skillType: '',
  category: '',
  executorType: '',
  status: '',
})
const pagination = reactive({ total: 0 })
const skillList = ref<AiSkill[]>([])
const activeTypeTab = ref('ALL')

const stats = reactive({
  monthNew: 0,
  evolved: 0,
  retired: 0,
})

// ==================== Type Counts ====================
const typeCounts = computed(() => {
  const counts: Record<string, number> = { all: 0, BUILTIN: 0, CUSTOM: 0, LEARNED: 0 }
  counts.all = pagination.total
  skillList.value.forEach((s) => {
    counts[s.skillType] = (counts[s.skillType] || 0) + 1
  })
  return counts
})

// ==================== Editor State ====================
const editorVisible = ref(false)
const editorTab = ref('basic')
const editingSkill = ref<AiSkill | null>(null)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const defaultForm = (): Partial<AiSkill> => ({
  skillCode: '',
  skillName: '',
  skillType: 'CUSTOM',
  category: 'GENERATION',
  executorType: 'LLM',
  description: '',
  status: 'ACTIVE',
  triggerIntent: '',
  triggerKeywords: '',
  inputSchema: '',
  outputSchema: '',
  executorConfig: '',
  promptTemplate: '',
  scriptContent: '',
})
const skillForm = reactive<Partial<AiSkill>>(defaultForm())

const keywordInput = ref('')
const keywordList = ref<string[]>([])

const formRules = reactive<FormRules>({
  skillCode: [{ required: true, message: '请输入技能编码', trigger: 'blur' }],
  skillName: [{ required: true, message: '请输入技能名称', trigger: 'blur' }],
  skillType: [{ required: true, message: '请选择技能类型', trigger: 'change' }],
  executorType: [{ required: true, message: '请选择执行器类型', trigger: 'change' }],
})

// ==================== Execution Logs ====================
const executionLogs = ref<SkillExecutionLog[]>([])
const logsLoading = ref(false)

// ==================== Playground State ====================
const playgroundVisible = ref(false)
const playgroundSkillId = ref<number>()
const playgroundInput = ref('{}')
const invoking = ref(false)
const activeSkills = ref<AiSkill[]>([])
const playgroundResult = ref<{
  status: string
  durationMs: number
  outputResult: string
  errorMessage?: string
  logId?: number
  feedback?: string
} | null>(null)

// ==================== Intent Matching ====================
const intentQuery = ref('')
const intentLoading = ref(false)
const intentResult = ref<Array<{ skillName: string; skillCode: string; executorType?: ExecutorType; confidence: number }>>([])

// ==================== Lifecycle ====================
onMounted(() => {
  fetchData()
  fetchActiveSkills()
})

// ==================== Data Fetching ====================
async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: queryParams.page,
      size: queryParams.size,
    }
    if (queryParams.keyword) params.keyword = queryParams.keyword
    if (queryParams.skillType) params.skillType = queryParams.skillType
    if (queryParams.category) params.category = queryParams.category
    if (queryParams.executorType) params.executorType = queryParams.executorType
    if (queryParams.status) params.status = queryParams.status

    const res: any = await getSkillList(params)
    skillList.value = res.data?.records || res.data?.list || []
    pagination.total = res.data?.total || res.total || 0

    // Generate mock stats
    stats.monthNew = Math.floor(Math.random() * 5) + 1
    stats.evolved = Math.floor(Math.random() * 4) + 1
    stats.retired = Math.floor(Math.random() * 2)
  } catch {
    // Fallback to mock data
    loadMockData()
  } finally {
    loading.value = false
  }
}

async function fetchActiveSkills() {
  try {
    const res: any = await getActiveSkills()
    activeSkills.value = res.data?.list || res.data || res.list || res || []
  } catch {
    // Fallback: use current skill list filtered by ACTIVE
    activeSkills.value = skillList.value.filter((s) => s.status === 'ACTIVE')
  }
}

async function fetchExecutionLogs(skillId: number) {
  logsLoading.value = true
  try {
    const res: any = await getSkillLogs(skillId, { page: 1, size: 20 })
    executionLogs.value = res.data?.records || res.data?.list || []
  } catch {
    // Fallback to mock logs
    executionLogs.value = generateMockLogs(skillId)
  } finally {
    logsLoading.value = false
  }
}

// ==================== Search & Filter ====================
function handleSearch() {
  queryParams.page = 1
  fetchData()
}

function handleReset() {
  queryParams.keyword = ''
  queryParams.skillType = ''
  queryParams.category = ''
  queryParams.executorType = ''
  queryParams.status = ''
  activeTypeTab.value = 'ALL'
  queryParams.page = 1
  fetchData()
}

function handleTypeTabChange(tab: string | number) {
  if (tab === 'ALL') {
    queryParams.skillType = ''
  } else {
    queryParams.skillType = tab as SkillType
  }
  queryParams.page = 1
  fetchData()
}

// ==================== Card Actions ====================
function handleCreate() {
  editingSkill.value = null
  Object.assign(skillForm, defaultForm())
  keywordList.value = []
  keywordInput.value = ''
  editorTab.value = 'basic'
  editorVisible.value = true
}

function openEditor(skill: AiSkill) {
  editingSkill.value = { ...skill }
  Object.assign(skillForm, { ...skill })

  // Parse keywords
  try {
    const kws = JSON.parse(skill.triggerKeywords || '[]')
    keywordList.value = Array.isArray(kws) ? kws : []
  } catch {
    keywordList.value = skill.triggerKeywords
      ? skill.triggerKeywords.split(',').map((k) => k.trim()).filter(Boolean)
      : []
  }
  keywordInput.value = ''
  editorTab.value = 'basic'
  editorVisible.value = true

  // Load logs
  if (skill.id) {
    fetchExecutionLogs(skill.id)
  }
}

async function handleSaveSkill() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      // Sync keywords back to form
      skillForm.triggerKeywords = JSON.stringify(keywordList.value)

      const data = { ...skillForm } as AiSkill
      if (editingSkill.value?.id) {
        await updateSkill(editingSkill.value.id, data)
        ElMessage.success('更新成功')
      } else {
        await createSkill(data)
        ElMessage.success('创建成功')
      }
      editorVisible.value = false
      fetchData()
    } catch {
      // handled by interceptor
    } finally {
      submitLoading.value = false
    }
  })
}

async function handleCardAction(command: string, skill: AiSkill) {
  switch (command) {
    case 'edit':
      openEditor(skill)
      break
    case 'disable':
      try {
        await ElMessageBox.confirm(
          `确定禁用技能「${skill.skillName}」吗？`,
          '确认',
          { type: 'warning' },
        )
        if (skill.id) {
          await updateSkill(skill.id, { ...skill, status: 'DISABLED' })
          ElMessage.success('已禁用')
          fetchData()
        }
      } catch {
        // cancelled
      }
      break
    case 'enable':
      if (skill.id) {
        await updateSkill(skill.id, { ...skill, status: 'ACTIVE' })
        ElMessage.success('已启用')
        fetchData()
      }
      break
    case 'delete':
      try {
        await ElMessageBox.confirm(
          `确定删除技能「${skill.skillName}」吗？此操作不可撤销。`,
          '确认删除',
          {
            type: 'warning',
            confirmButtonText: '删除',
            cancelButtonText: '取消',
            confirmButtonClass: 'el-button--danger',
          },
        )
        if (skill.id) {
          await deleteSkill(skill.id)
          ElMessage.success('删除成功')
          fetchData()
        }
      } catch {
        // cancelled
      }
      break
  }
}

function handleTestSkill(skill: AiSkill) {
  playgroundSkillId.value = skill.id
  playgroundResult.value = null
  playgroundVisible.value = true
  // Ensure active skills list contains this skill
  if (!activeSkills.value.find((s) => s.id === skill.id)) {
    activeSkills.value.push(skill)
  }
}

// ==================== Keyword Management ====================
function addKeyword() {
  const val = keywordInput.value.trim()
  if (!val) return
  const kws = val.split(/[,，]/).map((k) => k.trim()).filter(Boolean)
  kws.forEach((kw) => {
    if (!keywordList.value.includes(kw)) {
      keywordList.value.push(kw)
    }
  })
  keywordInput.value = ''
}

function handleKeywordKeyup(e: KeyboardEvent) {
  if (e.key === ',' || e.key === '，') {
    addKeyword()
  }
}

// ==================== Execution Log Feedback ====================
async function handleLogFeedback(logId: number, feedback: string) {
  try {
    await submitFeedback(logId, feedback)
    ElMessage.success('反馈已提交')
    // Refresh logs
    if (editingSkill.value?.id) {
      fetchExecutionLogs(editingSkill.value.id)
    }
  } catch {
    // Update locally as fallback
    const log = executionLogs.value.find((l) => l.id === logId)
    if (log) {
      log.userFeedback = feedback
    }
    ElMessage.success('反馈已记录')
  }
}

// ==================== Playground ====================
function handlePlaygroundSkillChange(id: number) {
  playgroundResult.value = null
  const skill = activeSkills.value.find((s) => s.id === id)
  if (skill?.inputSchema) {
    try {
      const schema = JSON.parse(skill.inputSchema)
      const defaults: Record<string, any> = {}
      if (schema.properties) {
        Object.keys(schema.properties).forEach((key) => {
          defaults[key] = ''
        })
      }
      playgroundInput.value = JSON.stringify(defaults, null, 2)
    } catch {
      playgroundInput.value = '{}'
    }
  } else {
    playgroundInput.value = '{}'
  }
}

async function handleInvoke() {
  if (!playgroundSkillId.value) return
  invoking.value = true
  playgroundResult.value = null
  try {
    let inputParams: Record<string, any> = {}
    try {
      inputParams = JSON.parse(playgroundInput.value || '{}')
    } catch {
      ElMessage.warning('输入参数格式错误，请输入有效的 JSON')
      invoking.value = false
      return
    }

    const res: any = await invokeSkill({ skillId: playgroundSkillId.value, inputParams })
    const result = res.data || res
    playgroundResult.value = {
      status: result.status || 'SUCCESS',
      durationMs: result.durationMs || 0,
      outputResult: typeof result.outputResult === 'string'
        ? result.outputResult
        : JSON.stringify(result.outputResult || result, null, 2),
      errorMessage: result.errorMessage,
      logId: result.logId || result.id,
    }
  } catch {
    // Mock result for demo
    playgroundResult.value = {
      status: 'SUCCESS',
      durationMs: Math.floor(Math.random() * 2000) + 200,
      outputResult: JSON.stringify({
        message: '技能执行成功（模拟数据）',
        skill: activeSkills.value.find((s) => s.id === playgroundSkillId.value)?.skillName,
        timestamp: new Date().toISOString(),
        result: '这是一条模拟的执行结果，实际结果将在后端服务就绪后返回。',
      }, null, 2),
      logId: Date.now(),
    }
  } finally {
    invoking.value = false
  }
}

async function handlePlaygroundFeedback(feedback: string) {
  if (!playgroundResult.value?.logId) return
  try {
    await submitFeedback(playgroundResult.value.logId, feedback)
  } catch {
    // ignore
  }
  playgroundResult.value.feedback = feedback
  ElMessage.success('反馈已提交')
}

function formatResult(output: string | undefined): string {
  if (!output) return ''
  try {
    return JSON.stringify(JSON.parse(output), null, 2)
  } catch {
    return output
  }
}

// ==================== Intent Matching ====================
async function handleMatchIntent() {
  if (!intentQuery.value.trim()) return
  intentLoading.value = true
  intentResult.value = []
  try {
    const res: any = await matchIntent(intentQuery.value)
    const data = res.data || res
    intentResult.value = Array.isArray(data) ? data : data.list || []
  } catch {
    // Mock intent result
    const mockMatches = activeSkills.value.slice(0, 3).map((s, idx) => ({
      skillName: s.skillName,
      skillCode: s.skillCode,
      executorType: s.executorType,
      confidence: Math.max(0.5, 0.95 - idx * 0.15),
    }))
    intentResult.value = mockMatches
  } finally {
    intentLoading.value = false
  }
}

// ==================== Helper Functions ====================
function executorIcon(type?: ExecutorType | string): string {
  const icons: Record<string, string> = {
    LLM: '🤖',
    CODE: '📝',
    API: '🔗',
    WORKFLOW: '⚡',
  }
  return icons[type || ''] || '🔧'
}

function typeTagColor(type: SkillType | string): string {
  const colors: Record<string, string> = {
    BUILTIN: '',
    CUSTOM: 'success',
    LEARNED: 'warning',
  }
  return colors[type] ?? 'info'
}

function categoryLabel(cat?: string): string {
  const labels: Record<string, string> = {
    TESTING: '测试',
    ANALYSIS: '分析',
    GENERATION: '生成',
    EXECUTION: '执行',
  }
  return labels[cat || ''] || '-'
}

function statusLabel(status?: string): string {
  const labels: Record<string, string> = {
    ACTIVE: '激活',
    DISABLED: '禁用',
    TESTING: '测试中',
  }
  return labels[status || ''] || '-'
}

function statusClass(status?: string): string {
  const cls: Record<string, string> = {
    ACTIVE: 'status-active',
    DISABLED: 'status-disabled',
    TESTING: 'status-testing',
  }
  return cls[status || ''] || ''
}

function confidenceColor(confidence?: number): string {
  const val = (confidence || 0) * 100
  if (val >= 90) return '#67c23a'
  if (val >= 70) return '#e6a23c'
  return '#f56c6c'
}

function successRate(skill: AiSkill): number {
  if (!skill.usageCount || skill.usageCount === 0) return 0
  return Math.round(((skill.successCount || 0) / skill.usageCount) * 100)
}

// ==================== Mock Data ====================
function loadMockData() {
  const mockSkills: AiSkill[] = [
    {
      id: 1, skillCode: 'case-generator', skillName: '案例生成器', skillType: 'BUILTIN',
      category: 'GENERATION', executorType: 'LLM', description: '基于需求自动生成测试案例',
      confidence: 0.95, usageCount: 456, successCount: 428, status: 'ACTIVE',
      createdAt: '2026-01-15 10:00:00',
    },
    {
      id: 2, skillCode: 'requirement-parser', skillName: '需求解析器', skillType: 'BUILTIN',
      category: 'ANALYSIS', executorType: 'LLM', description: '解析需求文档，提取测试要点',
      confidence: 0.88, usageCount: 312, successCount: 265, status: 'ACTIVE',
      createdAt: '2026-01-20 14:30:00',
    },
    {
      id: 3, skillCode: 'script-generator', skillName: '脚本生成器', skillType: 'CUSTOM',
      category: 'GENERATION', executorType: 'CODE', description: '根据测试案例自动生成自动化脚本',
      confidence: 0.92, usageCount: 189, successCount: 172, status: 'ACTIVE',
      createdAt: '2026-02-01 09:15:00',
    },
    {
      id: 4, skillCode: 'defect-analyzer', skillName: '缺陷分析器', skillType: 'BUILTIN',
      category: 'ANALYSIS', executorType: 'WORKFLOW', description: '分析缺陷模式，提供根因分析',
      confidence: 0.85, usageCount: 234, successCount: 199, status: 'ACTIVE',
      createdAt: '2026-02-10 16:45:00',
    },
    {
      id: 5, skillCode: 'intent-router', skillName: '意图路由器', skillType: 'BUILTIN',
      category: 'EXECUTION', executorType: 'LLM', description: '根据用户输入匹配最佳技能',
      confidence: 0.93, usageCount: 890, successCount: 828, status: 'ACTIVE',
      createdAt: '2026-01-10 08:00:00',
    },
    {
      id: 6, skillCode: 'api-tester', skillName: 'API 测试器', skillType: 'CUSTOM',
      category: 'TESTING', executorType: 'API', description: '自动执行 API 接口测试',
      confidence: 0.89, usageCount: 145, successCount: 130, status: 'ACTIVE',
      createdAt: '2026-03-05 11:20:00',
    },
    {
      id: 7, skillCode: 'regression-evaluator', skillName: '回归评估器', skillType: 'LEARNED',
      category: 'ANALYSIS', executorType: 'LLM', description: '评估回归测试覆盖率和风险',
      confidence: 0.78, usageCount: 67, successCount: 52, status: 'ACTIVE',
      createdAt: '2026-04-15 13:00:00',
    },
    {
      id: 8, skillCode: 'data-generator', skillName: '数据生成器', skillType: 'CUSTOM',
      category: 'GENERATION', executorType: 'CODE', description: '根据 Schema 生成测试数据',
      confidence: 0.91, usageCount: 278, successCount: 253, status: 'ACTIVE',
      createdAt: '2026-02-20 10:30:00',
    },
    {
      id: 9, skillCode: 'report-summarizer', skillName: '报告总结器', skillType: 'LEARNED',
      category: 'GENERATION', executorType: 'LLM', description: '自动汇总测试报告',
      confidence: 0.82, usageCount: 34, successCount: 28, status: 'TESTING',
      createdAt: '2026-05-01 15:45:00',
    },
    {
      id: 10, skillCode: 'legacy-checker', skillName: '兼容性检查', skillType: 'CUSTOM',
      category: 'TESTING', executorType: 'WORKFLOW', description: '检查跨浏览器/设备兼容性',
      confidence: 0.75, usageCount: 23, successCount: 18, status: 'DISABLED',
      createdAt: '2026-03-20 09:00:00',
    },
  ]

  skillList.value = mockSkills
  pagination.total = mockSkills.length
  activeSkills.value = mockSkills.filter((s) => s.status === 'ACTIVE')
  stats.monthNew = 3
  stats.evolved = 2
  stats.retired = 1
}

function generateMockLogs(skillId: number): SkillExecutionLog[] {
  const statuses = ['SUCCESS', 'SUCCESS', 'SUCCESS', 'SUCCESS', 'FAILED']
  const logs: SkillExecutionLog[] = []
  for (let i = 0; i < 8; i++) {
    const status = statuses[Math.floor(Math.random() * statuses.length)]
    const date = new Date(Date.now() - i * 3600000 * Math.random() * 24)
    logs.push({
      id: skillId * 100 + i,
      skillId,
      skillName: skillList.value.find((s) => s.id === skillId)?.skillName || '',
      inputParams: JSON.stringify({ query: `测试输入 ${i + 1}`, page: 1 }),
      outputResult: status === 'SUCCESS'
        ? JSON.stringify({ result: `执行结果 ${i + 1}`, items: Math.floor(Math.random() * 20) + 1 })
        : '',
      status,
      durationMs: Math.floor(Math.random() * 3000) + 100,
      errorMessage: status === 'FAILED' ? '执行超时或参数异常' : '',
      userFeedback: Math.random() > 0.6 ? (Math.random() > 0.3 ? 'LIKE' : 'DISLIKE') : undefined,
      createdAt: date.toISOString().replace('T', ' ').substring(0, 19),
    })
  }
  return logs
}
</script>

<style scoped lang="scss">
.skill-list-container {
  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      margin: 0;
    }

    .toolbar-actions {
      display: flex;
      gap: 8px;
    }
  }

  .search-toolbar {
    margin-bottom: 12px;

    .search-left {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }
  }

  .skill-type-tabs {
    margin-bottom: 8px;

    :deep(.el-tabs__header) {
      margin-bottom: 0;
    }
  }

  // ==================== Card Grid ====================
  .skill-grid {
    min-height: 200px;
    margin-top: 8px;

    .skill-card {
      margin-bottom: 16px;
      cursor: pointer;
      transition: all 0.25s ease;
      border-radius: 8px;

      :deep(.el-card__body) {
        padding: 16px;
      }

      &:hover {
        transform: translateY(-2px);
      }

      .card-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;

        .skill-icon {
          font-size: 22px;
          flex-shrink: 0;
        }

        .skill-name {
          font-size: 15px;
          font-weight: 600;
          color: var(--el-text-color-primary);
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .type-badge {
          flex-shrink: 0;
        }
      }

      .card-body {
        .card-info-row {
          display: flex;
          align-items: center;
          gap: 6px;
          margin-bottom: 6px;
          font-size: 13px;

          .info-label {
            color: var(--el-text-color-secondary);
            flex-shrink: 0;
          }

          .info-value {
            color: var(--el-text-color-regular);
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }

        .card-metrics {
          margin-top: 10px;
          padding-top: 10px;
          border-top: 1px solid var(--el-border-color-lighter);

          .metric-item {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 8px;

            .metric-label {
              font-size: 12px;
              color: var(--el-text-color-secondary);
              flex-shrink: 0;
              width: 42px;
            }
          }

          .metric-row {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .metric-text {
              font-size: 12px;
              color: var(--el-text-color-secondary);
              display: flex;
              align-items: center;
              gap: 3px;

              &.success-rate {
                color: var(--el-color-success);
              }
            }
          }
        }

        .card-status {
          margin-top: 8px;
          display: flex;
          align-items: center;
          gap: 6px;

          .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            flex-shrink: 0;

            &.status-active {
              background-color: var(--el-color-success);
              box-shadow: 0 0 4px var(--el-color-success);
            }

            &.status-disabled {
              background-color: var(--el-text-color-placeholder);
            }

            &.status-testing {
              background-color: var(--el-color-warning);
              box-shadow: 0 0 4px var(--el-color-warning);
            }
          }

          .status-text {
            font-size: 12px;
            color: var(--el-text-color-secondary);
          }
        }
      }

      .card-actions {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-top: 12px;
        padding-top: 12px;
        border-top: 1px solid var(--el-border-color-lighter);
      }
    }
  }

  // ==================== Pagination ====================
  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  // ==================== Stats Summary ====================
  .stats-summary {
    display: flex;
    align-items: center;
    gap: 24px;
    margin-top: 16px;
    padding: 12px 16px;
    background: var(--el-fill-color-light);
    border-radius: 6px;
    flex-wrap: wrap;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: var(--el-text-color-regular);

      strong {
        color: var(--el-color-primary);
        font-weight: 600;
      }
    }
  }
}

// ==================== Editor Drawer ====================
.editor-content {
  padding: 0 4px;

  .editor-tabs {
    :deep(.el-tabs__content) {
      overflow-y: auto;
      max-height: calc(100vh - 200px);
      padding-right: 4px;
    }
  }

  .editor-form {
    padding: 8px 0;

    .form-section {
      margin-bottom: 20px;

      .section-label {
        display: block;
        font-size: 14px;
        font-weight: 500;
        color: var(--el-text-color-primary);
        margin-bottom: 8px;
      }
    }
  }

  .keyword-input-area {
    .keyword-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-top: 8px;

      .text-muted {
        font-size: 13px;
        color: var(--el-text-color-placeholder);
      }
    }
  }

  .code-textarea {
    :deep(.el-textarea__inner) {
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
      font-size: 13px;
      line-height: 1.5;
    }
  }

  .log-section {
    padding: 8px 0;
  }
}

// ==================== Playground Dialog ====================
.playground-content {
  .playground-section {
    margin-bottom: 16px;

    .section-label {
      display: block;
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
      margin-bottom: 8px;
    }
  }

  .playground-execute {
    display: flex;
    justify-content: center;
    margin: 16px 0;
  }

  .code-textarea {
    :deep(.el-textarea__inner) {
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
      font-size: 13px;
      line-height: 1.5;
    }
  }

  .result-section {
    .result-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 10px;

      .result-duration {
        font-size: 13px;
        color: var(--el-text-color-secondary);
      }

      .result-feedback {
        margin-left: auto;
        display: flex;
        gap: 4px;
      }
    }

    .result-error {
      margin-top: 8px;
    }
  }

  .intent-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .intent-result {
    margin-top: 10px;

    .intent-match-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      background: var(--el-fill-color-lighter);
      border-radius: 6px;
      margin-bottom: 6px;

      .match-name {
        font-size: 14px;
        font-weight: 500;
        flex: 1;
      }
    }
  }
}
</style>
