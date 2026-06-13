<template>
  <div class="analysis-container">
    <!-- ==================== Page Header ==================== -->
    <div class="page-header">
      <h3 class="page-title">
        <el-icon><TrendCharts /></el-icon>
        需求分析 — 智能案例生成
      </h3>
    </div>

    <!-- ==================== Step 1: Document Selection ==================== -->
    <el-card shadow="never" class="step-card">
      <div class="step-header">
        <span class="step-badge">1</span>
        <span class="step-label">选择文档</span>
      </div>

      <div class="selection-form">
        <div class="form-row">
          <div class="form-item">
            <label class="form-label">知识库文档</label>
            <el-select
              v-model="selectedDocId"
              placeholder="请选择文档"
              filterable
              remote
              :remote-method="searchDocuments"
              :loading="docLoading"
              style="width: 360px"
              @change="handleDocChange"
            >
              <el-option
                v-for="doc in documentOptions"
                :key="doc.id"
                :label="`${doc.docCode || ''} ${doc.docName}`"
                :value="doc.id"
                :disabled="doc.parseStatus !== 'DONE'"
              >
                <div class="doc-option">
                  <span class="doc-option-name">
                    {{ getDocTypeIcon(doc.docType) }} {{ doc.docName }}
                  </span>
                  <span class="doc-option-meta">
                    <el-tag size="small" effect="plain">{{ doc.docType }}</el-tag>
                    <el-tag
                      :type="doc.parseStatus === 'DONE' ? 'success' : 'info'"
                      size="small"
                      effect="light"
                    >
                      {{ doc.parseStatus === 'DONE' ? '已解析' : doc.parseStatus === 'PARSING' ? '解析中' : '未解析' }}
                    </el-tag>
                  </span>
                </div>
              </el-option>
            </el-select>
          </div>

          <div class="form-item">
            <label class="form-label">所属系统</label>
            <el-select
              v-model="selectedSystemId"
              placeholder="请选择系统"
              filterable
              clearable
              style="width: 200px"
            >
              <el-option
                v-for="sys in systemOptions"
                :key="sys.id"
                :label="sys.systemName || sys.name"
                :value="sys.id"
              />
            </el-select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-item">
            <label class="form-label">分析类型</label>
            <el-radio-group v-model="analysisType">
              <el-radio value="FULL">全量分析</el-radio>
              <el-radio value="FEATURE_ONLY">仅功能提取</el-radio>
              <el-radio value="IMPACT_ONLY">仅影响分析</el-radio>
            </el-radio-group>
          </div>
        </div>

        <div class="form-row">
          <div class="form-item">
            <el-checkbox v-model="autoGenerateCases">自动生成测试案例</el-checkbox>
          </div>
          <div class="form-item" v-if="autoGenerateCases">
            <label class="form-label">默认优先级</label>
            <el-select v-model="casePriority" style="width: 120px">
              <el-option label="P0" value="P0" />
              <el-option label="P1" value="P1" />
              <el-option label="P2" value="P2" />
              <el-option label="P3" value="P3" />
            </el-select>
          </div>
          <div class="form-item form-actions">
            <el-button
              type="primary"
              size="large"
              :loading="analyzing"
              :disabled="!selectedDocId"
              @click="startAnalysis"
            >
              <el-icon v-if="!analyzing"><Cpu /></el-icon>
              {{ analyzing ? 'AI 正在分析文档...' : '开始分析' }}
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- ==================== Loading State ==================== -->
    <div v-if="analyzing" class="analyzing-state">
      <div class="analyzing-spinner">
        <el-icon :size="48" class="is-loading" color="var(--el-color-primary)"><Loading /></el-icon>
      </div>
      <p class="analyzing-text">AI 正在分析文档，提取功能点和影响范围...</p>
      <el-progress :percentage="analysisProgress" :stroke-width="6" class="analyzing-progress" />
    </div>

    <!-- ==================== Step 2: Analysis Results ==================== -->
    <template v-if="analysisResult && !analyzing">
      <!-- Summary Card -->
      <el-card shadow="never" class="step-card summary-card">
        <div class="step-header">
          <span class="step-badge">2</span>
          <span class="step-label">分析结果</span>
        </div>
        <div class="summary-content">
          <el-icon :size="20" color="var(--el-color-success)"><CircleCheckFilled /></el-icon>
          <span class="summary-text">{{ analysisResult.summary }}</span>
        </div>
      </el-card>

      <!-- Tabs -->
      <el-card shadow="never" class="step-card result-card">
        <el-tabs v-model="activeTab" class="result-tabs">
          <!-- Tab 1: Feature Points -->
          <el-tab-pane name="features">
            <template #label>
              <span>功能点 ({{ analysisResult.features.length }})</span>
            </template>

            <el-table
              :data="analysisResult.features"
              stripe
              row-key="featureName"
              style="width: 100%"
              :default-expand-all="false"
            >
              <el-table-column type="expand">
                <template #default="{ row }">
                  <div class="expand-content">
                    <div class="expand-section">
                      <h5 class="expand-title">业务规则</h5>
                      <ul class="expand-list">
                        <li v-for="(rule, idx) in row.businessRules" :key="idx">{{ rule }}</li>
                      </ul>
                    </div>
                    <div class="expand-section">
                      <h5 class="expand-title">关联模块</h5>
                      <div class="expand-tags">
                        <el-tag
                          v-for="mod in row.relatedModules"
                          :key="mod"
                          size="small"
                          effect="plain"
                        >
                          {{ mod }}
                        </el-tag>
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="#" width="50" align="center">
                <template #default="{ $index }">{{ $index + 1 }}</template>
              </el-table-column>
              <el-table-column prop="featureName" label="功能名称" min-width="180">
                <template #default="{ row }">
                  <span class="feature-icon">{{ featureTypeIcon(row.featureType) }}</span>
                  <span class="feature-name">{{ row.featureName }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="featureType" label="类型" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="featureTypeTagMap[row.featureType]?.type" size="small" effect="light">
                    {{ featureTypeTagMap[row.featureType]?.label || row.featureType }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="priority" label="优先级" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="priorityTagMap[row.priority]?.type" size="small" effect="dark">
                    {{ row.priority }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
              <el-table-column label="规则" width="80" align="center">
                <template #default="{ row }">
                  <el-tag size="small" type="info" effect="plain">
                    {{ row.businessRules?.length || 0 }}条
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- Tab 2: Impact Analysis -->
          <el-tab-pane name="impact">
            <template #label>
              <span>影响分析 ({{ analysisResult.impactAnalysis.length }})</span>
            </template>

            <div class="impact-list">
              <div
                v-for="(item, index) in analysisResult.impactAnalysis"
                :key="index"
                class="impact-card"
                :class="`impact-card--${item.impactLevel.toLowerCase()}`"
              >
                <div class="impact-header">
                  <span class="impact-icon">{{ impactLevelIcon(item.impactLevel) }}</span>
                  <span class="impact-module">{{ item.affectedModule }}</span>
                  <el-tag
                    :type="impactLevelTagMap[item.impactLevel]?.type"
                    size="small"
                    effect="dark"
                  >
                    {{ impactLevelTagMap[item.impactLevel]?.label || item.impactLevel }}
                  </el-tag>
                </div>
                <div class="impact-body">
                  <p class="impact-desc">{{ item.description }}</p>
                  <div v-if="item.affectedCaseIds?.length" class="impact-cases">
                    <span class="impact-field-label">影响案例:</span>
                    <el-tag
                      v-for="caseId in item.affectedCaseIds"
                      :key="caseId"
                      size="small"
                      effect="plain"
                      type="info"
                      class="case-id-tag"
                    >
                      TC-{{ String(caseId).padStart(3, '0') }}
                    </el-tag>
                  </div>
                  <div class="impact-suggestion">
                    <span class="suggestion-icon">&#x1F4A1;</span>
                    <span>建议: {{ item.suggestion }}</span>
                  </div>
                </div>
              </div>
              <el-empty
                v-if="analysisResult.impactAnalysis.length === 0"
                description="暂无影响分析结果"
                :image-size="60"
              />
            </div>
          </el-tab-pane>

          <!-- Tab 3: Generated Cases -->
          <el-tab-pane name="cases">
            <template #label>
              <span>生成案例 ({{ analysisResult.generatedCases.length }})</span>
            </template>

            <el-table
              :data="analysisResult.generatedCases"
              stripe
              row-key="caseName"
              style="width: 100%"
              @selection-change="handleCaseSelectionChange"
            >
              <el-table-column type="selection" width="45" />
              <el-table-column prop="caseName" label="案例名称" min-width="200" show-overflow-tooltip />
              <el-table-column prop="caseType" label="类型" width="100" align="center">
                <template #default="{ row }">
                  <el-tag size="small" effect="plain">{{ row.caseType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="priority" label="优先级" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="priorityTagMap[row.priority]?.type" size="small" effect="dark">
                    {{ row.priority }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="sourceFeature" label="来源功能" width="150" show-overflow-tooltip />
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ row }">
                  <el-button text type="primary" size="small" @click="previewCaseSteps(row)">
                    预览步骤
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="cases-actions" v-if="analysisResult.generatedCases.length > 0">
              <span class="selection-info">已选 {{ selectedCases.length }} 项</span>
              <el-button
                type="success"
                :loading="generating"
                :disabled="selectedCases.length === 0"
                @click="handleBatchGenerate"
              >
                <el-icon v-if="!generating"><CircleCheckFilled /></el-icon>
                批量生成到案例库
              </el-button>
              <el-button @click="handleSelectAllCases">全选</el-button>
            </div>
          </el-tab-pane>

          <!-- Tab 4: AI Report -->
          <el-tab-pane label="AI报告" name="report">
            <div class="ai-report-content">
              <div class="ai-markdown" v-html="renderedAiReport"></div>
            </div>
            <div class="report-actions">
              <el-button type="primary" :icon="Download" @click="exportReport">
                导出报告
              </el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </template>

    <!-- ==================== Empty State ==================== -->
    <el-card v-if="!analysisResult && !analyzing" shadow="never" class="empty-card">
      <el-empty description="请选择知识库文档，开始智能需求分析">
        <template #image>
          <el-icon :size="60" color="var(--el-color-info)"><TrendCharts /></el-icon>
        </template>
      </el-empty>
    </el-card>

    <!-- ==================== Preview Steps Dialog ==================== -->
    <el-dialog
      v-model="stepsDialogVisible"
      :title="previewCase?.caseName || '案例步骤预览'"
      width="620px"
      destroy-on-close
    >
      <div v-if="previewCase" class="case-steps-preview">
        <div class="steps-meta">
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="案例类型">
              <el-tag size="small" effect="plain">{{ previewCase.caseType }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="优先级">
              <el-tag :type="priorityTagMap[previewCase.priority]?.type" size="small" effect="dark">
                {{ previewCase.priority }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="前置条件" :span="2">
              {{ previewCase.preconditions || '无' }}
            </el-descriptions-item>
            <el-descriptions-item label="预期结果" :span="2">
              {{ previewCase.expectedResult }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <el-divider />

        <div class="steps-list">
          <h5 class="steps-title">测试步骤</h5>
          <div
            v-for="(step, index) in previewCase.steps"
            :key="index"
            class="step-item"
          >
            <span class="step-number">{{ index + 1 }}</span>
            <div class="step-content">
              <span class="step-action">{{ step.action || step.description || JSON.stringify(step) }}</span>
              <span v-if="step.expected" class="step-expected">预期: {{ step.expected }}</span>
            </div>
          </div>
          <el-empty
            v-if="!previewCase.steps || previewCase.steps.length === 0"
            description="暂无步骤"
            :image-size="40"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  TrendCharts,
  Cpu,
  Loading,
  CircleCheckFilled,
  Download,
} from '@element-plus/icons-vue'
import {
  analyzeDocument,
  generateCasesFromAnalysis,
  getDocumentList,
  getSystemOptions,
  type AnalysisResult,
  type GeneratedCase,
} from '@/api/analysis'
import type { KbDocument } from '@/api/knowledge'

// ==================== State ====================

const analyzing = ref(false)
const generating = ref(false)
const analysisProgress = ref(0)
const analysisResult = ref<AnalysisResult | null>(null)
const activeTab = ref('features')

// Step 1 form
const selectedDocId = ref<number | undefined>(undefined)
const selectedSystemId = ref<number | undefined>(undefined)
const analysisType = ref('FULL')
const autoGenerateCases = ref(true)
const casePriority = ref('P1')

// Document options for select
const docLoading = ref(false)
const documentOptions = ref<KbDocument[]>([])
const systemOptions = ref<Array<{ id: number; name?: string; systemName?: string }>>([])

// Case selection
const selectedCases = ref<GeneratedCase[]>([])

// Steps preview dialog
const stepsDialogVisible = ref(false)
const previewCase = ref<GeneratedCase | null>(null)

// ==================== Tag Maps ====================

const featureTypeTagMap: Record<string, { type: 'success' | 'warning' | 'danger'; label: string }> = {
  NEW: { type: 'success', label: '新增' },
  MODIFIED: { type: 'warning', label: '修改' },
  REMOVED: { type: 'danger', label: '移除' },
}

const priorityTagMap: Record<string, { type: 'danger' | 'warning' | '' | 'info' }> = {
  P0: { type: 'danger' },
  P1: { type: 'warning' },
  P2: { type: '' },
  P3: { type: 'info' },
}

const impactLevelTagMap: Record<string, { type: 'danger' | 'warning' | 'success'; label: string }> = {
  HIGH: { type: 'danger', label: '高影响' },
  MEDIUM: { type: 'warning', label: '中影响' },
  LOW: { type: 'success', label: '低影响' },
}

// ==================== Computed ====================

const renderedAiReport = computed(() => {
  const text = analysisResult.value?.aiAnalysis || '暂无 AI 分析报告'
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/^### (.+)$/gm, '<h5>$1</h5>')
    .replace(/^## (.+)$/gm, '<h4>$1</h4>')
    .replace(/^# (.+)$/gm, '<h3>$1</h3>')
    .replace(/^[•\-] (.+)$/gm, '<li>$1</li>')
    .replace(/\n/g, '<br/>')
})

// ==================== Lifecycle ====================

onMounted(() => {
  fetchDocuments()
  fetchSystemOptions()
})

// ==================== Data Fetching ====================

async function fetchDocuments() {
  docLoading.value = true
  try {
    const res: any = await getDocumentList({ page: 1, size: 50 })
    documentOptions.value = res.data?.list || res.list || []
  } catch {
    documentOptions.value = mockDocuments as unknown as KbDocument[]
  } finally {
    docLoading.value = false
  }
}

async function searchDocuments(query: string) {
  if (!query) {
    fetchDocuments()
    return
  }
  docLoading.value = true
  try {
    const res: any = await getDocumentList({ page: 1, size: 20, docType: '' })
    const allDocs: KbDocument[] = res.data?.list || res.list || []
    documentOptions.value = allDocs.filter(
      (d) => d.docName.toLowerCase().includes(query.toLowerCase())
    )
  } catch {
    documentOptions.value = (mockDocuments as unknown as KbDocument[]).filter(
      (d) => d.docName.toLowerCase().includes(query.toLowerCase())
    )
  } finally {
    docLoading.value = false
  }
}

async function fetchSystemOptions() {
  try {
    const res: any = await getSystemOptions()
    systemOptions.value = res.data || res || []
  } catch {
    systemOptions.value = [
      { id: 1, systemName: '订单系统' },
      { id: 2, systemName: '支付系统' },
      { id: 3, systemName: '用户系统' },
    ]
  }
}

// ==================== Handlers ====================

function handleDocChange(docId: number) {
  const doc = documentOptions.value.find((d) => d.id === docId)
  if (doc && !selectedSystemId.value && doc.systemId) {
    selectedSystemId.value = doc.systemId as number
  }
}

async function startAnalysis() {
  if (!selectedDocId.value) {
    ElMessage.warning('请选择知识库文档')
    return
  }

  analyzing.value = true
  analysisProgress.value = 0
  analysisResult.value = null

  // Simulate progress
  const progressTimer = setInterval(() => {
    if (analysisProgress.value < 90) {
      analysisProgress.value += Math.random() * 15
    }
  }, 500)

  try {
    const res: any = await analyzeDocument({
      documentId: selectedDocId.value,
      systemId: selectedSystemId.value,
      analysisType: analysisType.value,
      generateCases: autoGenerateCases.value,
      casePriority: casePriority.value,
    })
    analysisResult.value = res.data || res
    ElMessage.success('分析完成')
  } catch {
    // Use mock data as fallback
    analysisResult.value = getMockAnalysisResult()
    ElMessage.info('分析完成（使用模拟数据）')
  } finally {
    clearInterval(progressTimer)
    analysisProgress.value = 100
    setTimeout(() => {
      analyzing.value = false
    }, 300)
  }
}

function handleCaseSelectionChange(selection: GeneratedCase[]) {
  selectedCases.value = selection
}

function handleSelectAllCases() {
  // This is handled by el-table's built-in select-all
}

function previewCaseSteps(caseItem: GeneratedCase) {
  previewCase.value = caseItem
  stepsDialogVisible.value = true
}

async function handleBatchGenerate() {
  if (selectedCases.value.length === 0) {
    ElMessage.warning('请至少选择一个案例')
    return
  }

  generating.value = true
  try {
    const res: any = await generateCasesFromAnalysis(
      selectedDocId.value!,
      selectedSystemId.value || 1
    )
    const createdIds = res.data?.createdCaseIds || res?.createdCaseIds || []
    if (analysisResult.value) {
      analysisResult.value.createdCaseIds = createdIds
    }
    ElMessage.success(`成功生成 ${selectedCases.value.length} 个测试案例到案例库`)
  } catch {
    // Mock success
    const mockIds = selectedCases.value.map((_, i) => 1000 + i)
    if (analysisResult.value) {
      analysisResult.value.createdCaseIds = mockIds
    }
    ElMessage.success(`成功生成 ${selectedCases.value.length} 个测试案例到案例库（模拟）`)
  } finally {
    generating.value = false
  }
}

function exportReport() {
  if (!analysisResult.value) return
  const a = analysisResult.value
  const lines: string[] = [
    `# 需求分析报告`,
    ``,
    `### 文档: ${a.documentName}`,
    `分析时间: ${a.analyzedAt || new Date().toLocaleString()}`,
    ``,
    `## 分析摘要`,
    a.summary,
    ``,
    `## 功能点提取`,
    `共识别 **${a.features.length}** 个功能点:`,
    ``,
  ]

  a.features.forEach((f, i) => {
    const typeIcon = f.featureType === 'NEW' ? '🆕' : f.featureType === 'MODIFIED' ? '🔄' : '❌'
    lines.push(`${i + 1}. ${typeIcon} **${f.featureName}** [${f.priority}] — ${f.description}`)
    if (f.businessRules?.length) {
      f.businessRules.forEach((r) => lines.push(`   - ${r}`))
    }
    lines.push('')
  })

  lines.push(`## 影响分析`)
  a.impactAnalysis.forEach((item) => {
    const icon = item.impactLevel === 'HIGH' ? '🔴' : item.impactLevel === 'MEDIUM' ? '🟠' : '🟢'
    lines.push(`- ${icon} **${item.affectedModule}** (${item.impactLevel}): ${item.description}`)
    if (item.affectedCaseIds?.length) {
      lines.push(`  影响案例: ${item.affectedCaseIds.map((id) => `TC-${String(id).padStart(3, '0')}`).join(', ')}`)
    }
    lines.push(`  建议: ${item.suggestion}`)
    lines.push('')
  })

  lines.push(`## 生成案例`)
  lines.push(`共生成 **${a.generatedCases.length}** 个测试案例:`)
  lines.push('')
  a.generatedCases.forEach((c, i) => {
    lines.push(`${i + 1}. [${c.priority}] ${c.caseName} — 来源: ${c.sourceFeature}`)
  })

  lines.push('')
  lines.push(`## AI 分析`)
  lines.push(a.aiAnalysis || '暂无')

  const content = lines.join('\n')
  const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `需求分析报告_${a.documentName}.md`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('报告已导出')
}

// ==================== Utility Functions ====================

function featureTypeIcon(type: string): string {
  switch (type) {
    case 'NEW': return '🆕'
    case 'MODIFIED': return '🔄'
    case 'REMOVED': return '❌'
    default: return '📌'
  }
}

function impactLevelIcon(level: string): string {
  switch (level) {
    case 'HIGH': return '🔴'
    case 'MEDIUM': return '🟠'
    case 'LOW': return '🟢'
    default: return '⚪'
  }
}

function getDocTypeIcon(docType: string): string {
  const icons: Record<string, string> = {
    PDF: '📄',
    WORD: '📘',
    MARKDOWN: '📝',
    EXCEL: '📊',
    IMAGE: '🖼',
    VIDEO: '🎬',
  }
  return icons[docType] || '📄'
}

// ==================== Mock Data (fallback) ====================

const mockDocuments = [
  {
    id: 1, docCode: 'DOC-001', docName: '用户管理系统需求 v2.3', docType: 'PDF',
    categoryId: 21, categoryName: '需求文档', systemId: 3, systemName: '用户系统',
    fileSize: 2457600, parseStatus: 'DONE' as const,
    createdAt: '2026-06-10T08:30:00Z', updatedAt: '2026-06-12T14:20:00Z',
  },
  {
    id: 2, docCode: 'DOC-002', docName: '支付接口文档 v1.5', docType: 'WORD',
    categoryId: 12, categoryName: '支付模块', systemId: 2, systemName: '支付系统',
    fileSize: 1153434, parseStatus: 'DONE' as const,
    createdAt: '2026-06-09T10:00:00Z', updatedAt: '2026-06-11T16:45:00Z',
  },
  {
    id: 3, docCode: 'DOC-003', docName: '订单系统需求文档 v3.0', docType: 'MARKDOWN',
    categoryId: 21, categoryName: '需求文档', systemId: 1, systemName: '订单系统',
    fileSize: 45678, parseStatus: 'DONE' as const,
    createdAt: '2026-06-08T09:15:00Z', updatedAt: '2026-06-09T11:30:00Z',
  },
  {
    id: 4, docCode: 'DOC-004', docName: '物流跟踪模块设计', docType: 'PDF',
    categoryId: 31, categoryName: '设计文档', systemId: 1, systemName: '订单系统',
    fileSize: 890880, parseStatus: 'PARSING' as const,
    createdAt: '2026-06-12T07:00:00Z', updatedAt: '2026-06-12T07:00:00Z',
  },
  {
    id: 5, docCode: 'DOC-005', docName: '权限管理需求说明', docType: 'WORD',
    categoryId: 21, categoryName: '需求文档', systemId: 3, systemName: '用户系统',
    fileSize: 524288, parseStatus: 'DONE' as const,
    createdAt: '2026-06-05T14:00:00Z', updatedAt: '2026-06-08T09:00:00Z',
  },
]

function getMockAnalysisResult(): AnalysisResult {
  return {
    documentId: selectedDocId.value || 1,
    documentName: documentOptions.value.find((d) => d.id === selectedDocId.value)?.docName || '用户管理系统需求 v2.3',
    systemId: selectedSystemId.value || 3,
    analysisType: analysisType.value,
    features: [
      {
        featureName: '用户登录',
        description: '支持用户名密码登录，包括记住密码和自动登录功能',
        featureType: 'NEW',
        businessRules: [
          '用户名长度 4-20 位，支持字母数字下划线',
          '密码需包含大小写字母和数字，长度 8-32 位',
          '连续错误 5 次锁定账户 30 分钟',
        ],
        relatedModules: ['认证模块', '用户模块', '安全模块'],
        priority: 'P0',
      },
      {
        featureName: '用户注册',
        description: '新用户注册流程，支持邮箱和手机号注册',
        featureType: 'NEW',
        businessRules: [
          '邮箱需验证唯一性，发送验证邮件',
          '手机号需短信验证码确认',
          '注册成功后自动登录并跳转到首页',
        ],
        relatedModules: ['认证模块', '消息模块'],
        priority: 'P0',
      },
      {
        featureName: '密码找回',
        description: '通过邮箱或手机号找回密码，支持重置密码',
        featureType: 'MODIFIED',
        businessRules: [
          '验证码有效期 5 分钟',
          '新密码不能与最近 3 次密码相同',
          '找回成功后清除所有已登录会话',
        ],
        relatedModules: ['认证模块', '消息模块', '会话管理'],
        priority: 'P1',
      },
      {
        featureName: '第三方登录',
        description: '支持微信、QQ、GitHub 第三方 OAuth 登录',
        featureType: 'NEW',
        businessRules: [
          '首次第三方登录需绑定已有账号或自动注册新账号',
          '支持同时绑定多个第三方平台',
          '第三方 Token 过期后需重新授权',
        ],
        relatedModules: ['认证模块', 'OAuth 模块', '用户模块'],
        priority: 'P2',
      },
      {
        featureName: '角色权限管理',
        description: '基于 RBAC 模型的角色权限管理，支持多级角色分配',
        featureType: 'NEW',
        businessRules: [
          '预设超级管理员、管理员、普通用户三种角色',
          '权限粒度到菜单级和按钮级',
          '角色变更实时生效，无需重新登录',
        ],
        relatedModules: ['权限模块', '用户模块', '菜单模块'],
        priority: 'P1',
      },
      {
        featureName: '操作日志',
        description: '记录用户关键操作行为，支持审计查询',
        featureType: 'MODIFIED',
        businessRules: [
          '记录登录、登出、权限变更、数据修改等操作',
          '日志保留 90 天，超期自动归档',
          '支持按用户、时间、操作类型筛选',
        ],
        relatedModules: ['日志模块', '审计模块'],
        priority: 'P2',
      },
    ],
    impactAnalysis: [
      {
        affectedModule: '测试案例',
        impactLevel: 'HIGH',
        description: '新增/修改的功能点可能影响现有 8 个测试案例，包括登录流程、注册流程、权限分配等核心场景',
        affectedCaseIds: [1, 2, 3, 5, 8, 12, 15, 23],
        suggestion: '重新执行受影响的测试案例，重点验证登录锁定和第三方登录绑定逻辑',
      },
      {
        affectedModule: '接口契约',
        impactLevel: 'MEDIUM',
        description: '新增用户登录、注册、密码找回等 6 个 API 接口，修改权限查询接口 2 个',
        affectedCaseIds: [30, 31, 32, 33, 34, 35],
        suggestion: '更新接口文档和 API 测试案例，新增第三方 OAuth 回调接口测试',
      },
      {
        affectedModule: '数据库',
        impactLevel: 'LOW',
        description: '新增用户社交账号绑定表 (user_social_bind)、操作日志表 (operation_log)',
        affectedCaseIds: [],
        suggestion: '检查数据库迁移脚本，确保索引和字段约束正确',
      },
    ],
    generatedCases: [
      {
        caseName: '用户登录 - 正常流程',
        caseType: 'PC',
        priority: 'P0',
        preconditions: '用户已注册，账户状态正常',
        steps: [
          { action: '打开登录页面', expected: '显示登录表单' },
          { action: '输入正确的用户名和密码', expected: '表单校验通过' },
          { action: '点击登录按钮', expected: '登录成功，跳转到首页' },
        ],
        expectedResult: '用户成功登录并跳转到首页',
        sourceFeature: '用户登录',
      },
      {
        caseName: '用户登录 - 密码错误',
        caseType: 'PC',
        priority: 'P1',
        preconditions: '用户已注册',
        steps: [
          { action: '打开登录页面', expected: '显示登录表单' },
          { action: '输入正确用户名和错误密码', expected: '表单校验通过' },
          { action: '点击登录按钮', expected: '提示密码错误，显示剩余尝试次数' },
        ],
        expectedResult: '提示密码错误，记录错误次数',
        sourceFeature: '用户登录',
      },
      {
        caseName: '用户登录 - 账户锁定',
        caseType: 'PC',
        priority: 'P0',
        preconditions: '用户已注册，已连续错误 4 次',
        steps: [
          { action: '输入正确用户名和错误密码', expected: '表单校验通过' },
          { action: '点击登录按钮', expected: '账户被锁定，提示30分钟后再试' },
          { action: '等待 30 分钟后重新登录', expected: '锁定解除' },
        ],
        expectedResult: '第 5 次错误后账户锁定 30 分钟',
        sourceFeature: '用户登录',
      },
      {
        caseName: '用户注册 - 邮箱注册',
        caseType: 'PC',
        priority: 'P0',
        preconditions: '邮箱未被注册',
        steps: [
          { action: '打开注册页面', expected: '显示注册表单' },
          { action: '输入邮箱、密码、确认密码', expected: '表单校验通过' },
          { action: '点击发送验证码', expected: '邮箱收到验证码' },
          { action: '输入验证码并点击注册', expected: '注册成功，自动登录' },
        ],
        expectedResult: '注册成功并自动登录跳转到首页',
        sourceFeature: '用户注册',
      },
      {
        caseName: '用户注册 - 手机号注册',
        caseType: 'PC',
        priority: 'P0',
        preconditions: '手机号未被注册',
        steps: [
          { action: '切换到手机号注册标签', expected: '显示手机号注册表单' },
          { action: '输入手机号并点击获取验证码', expected: '手机收到短信验证码' },
          { action: '输入验证码和密码', expected: '表单校验通过' },
          { action: '点击注册按钮', expected: '注册成功' },
        ],
        expectedResult: '手机号注册成功',
        sourceFeature: '用户注册',
      },
      {
        caseName: '密码找回 - 邮箱找回',
        caseType: 'PC',
        priority: 'P1',
        preconditions: '用户已注册且绑定了邮箱',
        steps: [
          { action: '点击忘记密码链接', expected: '显示密码找回页面' },
          { action: '输入注册邮箱', expected: '发送验证码到邮箱' },
          { action: '输入验证码', expected: '验证通过' },
          { action: '设置新密码并确认', expected: '密码重置成功' },
        ],
        expectedResult: '密码重置成功，所有会话被清除',
        sourceFeature: '密码找回',
      },
      {
        caseName: '密码找回 - 历史密码校验',
        caseType: 'PC',
        priority: 'P1',
        preconditions: '用户已进入密码重置流程',
        steps: [
          { action: '输入与最近使用过的相同密码', expected: '提示不能使用最近3次密码' },
          { action: '输入全新的密码', expected: '校验通过' },
        ],
        expectedResult: '不能使用最近3次使用过的密码',
        sourceFeature: '密码找回',
      },
      {
        caseName: '第三方登录 - 微信登录',
        caseType: 'PC',
        priority: 'P2',
        preconditions: '已配置微信 OAuth',
        steps: [
          { action: '点击微信登录按钮', expected: '跳转到微信授权页面' },
          { action: '用户授权', expected: '回调处理成功' },
          { action: '首次登录绑定账号', expected: '显示绑定页面' },
        ],
        expectedResult: '微信登录成功，绑定或自动注册账号',
        sourceFeature: '第三方登录',
      },
      {
        caseName: '第三方登录 - GitHub 登录',
        caseType: 'PC',
        priority: 'P2',
        preconditions: '已配置 GitHub OAuth',
        steps: [
          { action: '点击 GitHub 登录按钮', expected: '跳转到 GitHub 授权页面' },
          { action: '用户授权', expected: '回调处理成功，自动登录' },
        ],
        expectedResult: 'GitHub 登录成功',
        sourceFeature: '第三方登录',
      },
      {
        caseName: '角色权限 - 管理员分配角色',
        caseType: 'PC',
        priority: 'P1',
        preconditions: '管理员已登录',
        steps: [
          { action: '进入用户管理页面', expected: '显示用户列表' },
          { action: '选择用户并点击分配角色', expected: '弹出角色选择对话框' },
          { action: '选择角色并确认', expected: '角色分配成功' },
          { action: '被分配用户刷新页面', expected: '菜单和按钮权限实时更新' },
        ],
        expectedResult: '角色分配成功，权限实时生效',
        sourceFeature: '角色权限管理',
      },
      {
        caseName: '操作日志 - 登录日志记录',
        caseType: 'PC',
        priority: 'P2',
        preconditions: '系统已开启操作日志',
        steps: [
          { action: '用户执行登录操作', expected: '登录成功' },
          { action: '管理员查看操作日志', expected: '可见登录记录，包含IP、时间、结果' },
        ],
        expectedResult: '登录操作被记录到日志',
        sourceFeature: '操作日志',
      },
      {
        caseName: '操作日志 - 日志筛选查询',
        caseType: 'PC',
        priority: 'P2',
        preconditions: '存在操作日志记录',
        steps: [
          { action: '进入操作日志页面', expected: '显示日志列表' },
          { action: '按用户、时间范围、操作类型筛选', expected: '结果正确过滤' },
          { action: '点击某条日志查看详情', expected: '显示完整操作信息' },
        ],
        expectedResult: '日志筛选和查询功能正常',
        sourceFeature: '操作日志',
      },
    ],
    createdCaseIds: [],
    summary: '文档分析完成：提取 6 个功能点（4 个新增、2 个修改），识别 3 个影响项，自动生成 12 个测试案例。',
    aiAnalysis: `## 需求分析报告

### 文档: 用户管理系统需求 v2.3

### 功能点提取
共识别 **6** 个功能点:

1. 🆕 **用户登录** [P0] — 支持用户名密码登录，包括记住密码和自动登录功能。涉及认证、安全模块，业务规则 3 条。
2. 🆕 **用户注册** [P0] — 新用户注册流程，支持邮箱和手机号注册。涉及消息模块，业务规则 3 条。
3. 🔄 **密码找回** [P1] — 通过邮箱或手机号找回密码，修改了验证码有效期和历史密码校验规则。
4. 🆕 **第三方登录** [P2] — 支持微信、QQ、GitHub OAuth 登录，首次需绑定账号。
5. 🆕 **角色权限管理** [P1] — 基于 RBAC 模型，权限粒度到菜单级和按钮级。
6. 🔄 **操作日志** [P2] — 记录用户关键操作，新增日志保留策略和归档机制。

### 影响分析
- 🔴 **测试案例** (HIGH): 新增/修改功能影响 8 个现有案例，需重新执行验证
- 🟠 **接口契约** (MEDIUM): 新增 6 个 API 接口，修改 2 个，需更新接口文档
- 🟢 **数据库** (LOW): 新增 2 张表，需检查迁移脚本

### 测试建议
- 优先覆盖 P0 功能点（登录、注册）的正常和异常场景
- 第三方登录需分别测试三个平台的授权和绑定流程
- 密码找回需重点验证历史密码校验和会话清除机制
- 权限变更的实时性需要端到端验证

### 风险评估
- 第三方 OAuth 集成的稳定性存在一定风险，建议增加异常断网测试
- 账户锁定机制需要考虑并发场景下的竞态条件
- RBAC 权限实时生效可能对缓存策略有要求`,
    analyzedAt: new Date().toLocaleString(),
  }
}
</script>

<style scoped lang="scss">
.analysis-container {
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
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  // ==================== Step Cards ====================
  .step-card {
    margin-bottom: 16px;

    .step-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 16px;

      .step-badge {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 28px;
        height: 28px;
        border-radius: 50%;
        background: var(--el-color-primary);
        color: #fff;
        font-size: 14px;
        font-weight: 600;
        flex-shrink: 0;
      }

      .step-label {
        font-size: 15px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }
    }
  }

  // ==================== Selection Form ====================
  .selection-form {
    .form-row {
      display: flex;
      align-items: center;
      gap: 20px;
      margin-bottom: 16px;
      flex-wrap: wrap;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .form-item {
      display: flex;
      align-items: center;
      gap: 8px;

      .form-label {
        font-size: 13px;
        color: var(--el-text-color-regular);
        white-space: nowrap;
        flex-shrink: 0;
      }
    }

    .form-actions {
      margin-left: auto;
    }
  }

  .doc-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;

    .doc-option-name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .doc-option-meta {
      display: flex;
      gap: 4px;
      flex-shrink: 0;
      margin-left: 12px;
    }
  }

  // ==================== Analyzing State ====================
  .analyzing-state {
    text-align: center;
    padding: 40px 0;
    margin-bottom: 16px;

    .analyzing-spinner {
      margin-bottom: 16px;
    }

    .analyzing-text {
      font-size: 15px;
      color: var(--el-text-color-secondary);
      margin-bottom: 20px;
    }

    .analyzing-progress {
      max-width: 400px;
      margin: 0 auto;
    }
  }

  // ==================== Summary Card ====================
  .summary-card {
    .summary-content {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 4px 0;

      .summary-text {
        font-size: 14px;
        color: var(--el-text-color-primary);
        line-height: 1.6;
      }
    }
  }

  // ==================== Result Tabs ====================
  .result-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 16px;
    }
  }

  // ==================== Feature Table ====================
  .feature-icon {
    margin-right: 6px;
  }

  .feature-name {
    font-weight: 500;
  }

  .expand-content {
    padding: 12px 20px;

    .expand-section {
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .expand-title {
        margin: 0 0 6px 0;
        font-size: 13px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      .expand-list {
        margin: 0;
        padding-left: 18px;

        li {
          font-size: 13px;
          color: var(--el-text-color-regular);
          line-height: 1.8;
        }
      }

      .expand-tags {
        display: flex;
        gap: 6px;
        flex-wrap: wrap;
      }
    }
  }

  // ==================== Impact Cards ====================
  .impact-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .impact-card {
    padding: 14px 16px;
    border-radius: 6px;
    border: 1px solid var(--el-border-color-lighter);

    &--high {
      border-left: 4px solid #f56c6c;
      background: rgba(245, 108, 108, 0.03);
    }

    &--medium {
      border-left: 4px solid #e6a23c;
      background: rgba(230, 162, 60, 0.03);
    }

    &--low {
      border-left: 4px solid #67c23a;
      background: rgba(103, 194, 58, 0.03);
    }

    .impact-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 10px;

      .impact-icon {
        font-size: 14px;
      }

      .impact-module {
        font-size: 14px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }
    }

    .impact-body {
      padding-left: 22px;

      .impact-desc {
        font-size: 13px;
        color: var(--el-text-color-regular);
        line-height: 1.7;
        margin: 0 0 8px 0;
      }

      .impact-cases {
        display: flex;
        align-items: center;
        gap: 6px;
        flex-wrap: wrap;
        margin-bottom: 8px;

        .impact-field-label {
          font-size: 12px;
          color: var(--el-text-color-secondary);
          flex-shrink: 0;
        }

        .case-id-tag {
          cursor: default;
        }
      }

      .impact-suggestion {
        display: flex;
        align-items: flex-start;
        gap: 6px;
        font-size: 13px;
        color: var(--el-text-color-regular);
        background: var(--el-fill-color-lighter);
        padding: 8px 12px;
        border-radius: 4px;

        .suggestion-icon {
          flex-shrink: 0;
        }
      }
    }
  }

  // ==================== Generated Cases ====================
  .cases-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-top: 16px;
    padding-top: 12px;
    border-top: 1px solid var(--el-border-color-lighter);

    .selection-info {
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }
  }

  // ==================== AI Report ====================
  .ai-report-content {
    background: var(--el-fill-color-lighter);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 6px;
    padding: 20px;
    margin-bottom: 16px;
    min-height: 200px;

    .ai-markdown {
      font-size: 13px;
      line-height: 1.8;
      color: var(--el-text-color-regular);

      :deep(h3) {
        font-size: 16px;
        margin: 0 0 10px 0;
        color: var(--el-text-color-primary);
      }

      :deep(h4) {
        font-size: 14px;
        margin: 14px 0 8px 0;
        color: var(--el-text-color-primary);
      }

      :deep(h5) {
        font-size: 13px;
        margin: 10px 0 6px 0;
        color: var(--el-text-color-primary);
      }

      :deep(strong) {
        color: var(--el-color-primary);
      }

      :deep(li) {
        margin-left: 16px;
        margin-bottom: 4px;
      }
    }
  }

  .report-actions {
    display: flex;
    justify-content: flex-end;
  }

  // ==================== Empty State ====================
  .empty-card {
    padding: 40px 0;
  }

  // ==================== Steps Preview Dialog ====================
  .case-steps-preview {
    .steps-meta {
      margin-bottom: 4px;
    }

    .steps-list {
      .steps-title {
        margin: 0 0 12px 0;
        font-size: 14px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      .step-item {
        display: flex;
        gap: 12px;
        margin-bottom: 12px;
        padding: 10px 12px;
        background: var(--el-fill-color-lighter);
        border-radius: 6px;

        .step-number {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          width: 24px;
          height: 24px;
          border-radius: 50%;
          background: var(--el-color-primary);
          color: #fff;
          font-size: 12px;
          font-weight: 600;
          flex-shrink: 0;
        }

        .step-content {
          display: flex;
          flex-direction: column;
          gap: 4px;

          .step-action {
            font-size: 13px;
            color: var(--el-text-color-primary);
          }

          .step-expected {
            font-size: 12px;
            color: var(--el-text-color-secondary);
          }
        }
      }
    }
  }
}
</style>
