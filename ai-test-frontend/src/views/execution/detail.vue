<template>
  <div class="execution-detail-container">
    <!-- Header -->
    <div class="page-header">
      <div class="page-header-left">
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
        <span class="page-title">
          <el-icon><VideoPlay /></el-icon>
          执行详情
          <el-tag :type="statusTagMap[execution.status]?.type || 'info'" size="small" effect="dark">
            <template v-if="execution.status === 'running'"><el-icon class="is-loading"><Loading /></el-icon></template>
            {{ statusTagMap[execution.status]?.label || execution.status }}
          </el-tag>
        </span>
      </div>
      <div class="page-header-right">
        <el-button v-if="execution.status === 'running'" type="danger" plain @click="handleCancel">
          <el-icon><VideoPause /></el-icon>取消执行
        </el-button>
        <el-button @click="handleRefresh" :icon="Refresh">刷新</el-button>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-value">{{ execution.totalCases }}</div>
        <div class="stat-label">总用例数</div>
        <el-icon class="stat-icon" :size="28" color="var(--el-color-primary)"><Document /></el-icon>
      </div>
      <div class="stat-card stat-pass">
        <div class="stat-value" style="color: #67c23a">{{ execution.passedCases }}</div>
        <div class="stat-label">通过</div>
        <el-icon class="stat-icon" :size="28" color="#67c23a"><CircleCheckFilled /></el-icon>
      </div>
      <div class="stat-card stat-fail">
        <div class="stat-value" style="color: #f56c6c">{{ execution.failedCases }}</div>
        <div class="stat-label">失败</div>
        <el-icon class="stat-icon" :size="28" color="#f56c6c"><CircleCloseFilled /></el-icon>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ execution.durationText }}</div>
        <div class="stat-label">耗时</div>
        <el-icon class="stat-icon" :size="28" color="var(--el-color-warning)"><Timer /></el-icon>
      </div>
      <div class="stat-card">
        <div class="stat-value" :style="{ color: getPassRateColor(execution.passRate) }">{{ execution.passRate }}%</div>
        <div class="stat-label">通过率</div>
        <el-icon class="stat-icon" :size="28" color="var(--el-color-primary)"><TrendCharts /></el-icon>
      </div>
    </div>

    <!-- Tabs -->
    <el-card shadow="never" class="tab-card">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- ==================== Tab 1: 步骤详情 ==================== -->
        <el-tab-pane label="步骤详情" name="steps">
          <div class="steps-tab-content">
            <el-collapse v-model="expandedCases" accordion>
              <el-collapse-item
                v-for="caseDetail in caseDetails"
                :key="caseDetail.caseId"
                :name="caseDetail.caseId"
              >
                <template #title>
                  <div class="case-header">
                    <el-tag
                      :type="caseStatusTagMap[caseDetail.status]?.type || 'info'"
                      size="small"
                      effect="dark"
                      round
                    >
                      {{ caseStatusTagMap[caseDetail.status]?.label || caseDetail.status }}
                    </el-tag>
                    <span class="case-name-text">{{ caseDetail.caseName }}</span>
                    <span class="case-duration">{{ caseDetail.durationText }}</span>
                    <el-tag v-if="caseDetail.hasVideo" size="small" type="success" effect="plain">
                      <el-icon><VideoCamera /></el-icon> 录屏
                    </el-tag>
                  </div>
                </template>

                <!-- Expanded Step Details -->
                <div class="step-details-panel">
                  <div class="step-screenshots-row">
                    <div
                      v-for="step in caseDetail.steps"
                      :key="step.stepOrder"
                      class="step-thumb"
                      :class="{ 'step-failed': step.status === 'failed' || step.status === 'error' }"
                      @click="selectStep(caseDetail, step)"
                    >
                      <div class="step-screenshot-placeholder" :style="{ background: stepColor(step) }">
                        <span class="step-num">{{ step.stepOrder }}</span>
                        <span class="step-action-text">{{ step.stepAction }}</span>
                      </div>
                      <div class="step-thumb-status">
                        <el-icon v-if="step.status === 'passed'" color="#67c23a"><CircleCheckFilled /></el-icon>
                        <el-icon v-else-if="step.status === 'failed'" color="#f56c6c"><CircleCloseFilled /></el-icon>
                        <el-icon v-else-if="step.status === 'running'" class="is-loading" color="#e6a23c"><Loading /></el-icon>
                        <el-icon v-else color="#909399"><RemoveFilled /></el-icon>
                      </div>
                    </div>
                  </div>
                  <div v-if="selectedStep" class="step-info-panel">
                    <el-descriptions :column="2" border size="small" title="步骤详情">
                      <el-descriptions-item label="步骤序号">{{ selectedStep.stepOrder }}</el-descriptions-item>
                      <el-descriptions-item label="操作类型">{{ selectedStep.stepAction }}</el-descriptions-item>
                      <el-descriptions-item label="操作目标">{{ selectedStep.stepTarget || '—' }}</el-descriptions-item>
                      <el-descriptions-item label="操作值">{{ selectedStep.stepValue || '—' }}</el-descriptions-item>
                      <el-descriptions-item label="步骤描述">{{ selectedStep.stepDescription || '—' }}</el-descriptions-item>
                      <el-descriptions-item label="耗时">{{ selectedStep.duration ? selectedStep.duration + 'ms' : '—' }}</el-descriptions-item>
                      <el-descriptions-item label="预期结果">{{ selectedStep.expected || '—' }}</el-descriptions-item>
                      <el-descriptions-item label="实际结果">
                        <span :style="{ color: selectedStep.status === 'passed' ? '#67c23a' : '#f56c6c' }">
                          {{ selectedStep.actual || '—' }}
                        </span>
                      </el-descriptions-item>
                      <el-descriptions-item v-if="selectedStep.errorMessage" label="错误信息" :span="2">
                        <span class="error-text">{{ selectedStep.errorMessage }}</span>
                      </el-descriptions-item>
                    </el-descriptions>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-tab-pane>

        <!-- ==================== Tab 2: 测试报告 ==================== -->
        <el-tab-pane label="测试报告" name="report">
          <div class="report-tab-content">
            <!-- Score Circle -->
            <div class="report-header">
              <div class="score-circle" :style="{ '--score-color': getScoreColor(report.score) }">
                <div class="score-value">{{ report.score }}</div>
                <div class="score-label">综合评分</div>
              </div>
              <div class="report-title-area">
                <h3>{{ report.executionName }}</h3>
                <p class="report-meta">{{ report.suiteName }} · {{ report.summary.startedAt }} - {{ report.summary.finishedAt }}</p>
                <p class="report-meta">耗时 {{ report.summary.durationText }} · 通过率 {{ report.summary.passRate }}%</p>
              </div>
            </div>

            <!-- Summary Grid -->
            <el-row :gutter="16" class="report-summary-grid">
              <el-col :span="6">
                <div class="summary-item">
                  <div class="summary-value">{{ report.summary.totalCases }}</div>
                  <div class="summary-label">总用例</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-item summary-pass">
                  <div class="summary-value" style="color:#67c23a">{{ report.summary.passedCases }}</div>
                  <div class="summary-label">通过</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-item summary-fail">
                  <div class="summary-value" style="color:#f56c6c">{{ report.summary.failedCases }}</div>
                  <div class="summary-label">失败</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="summary-item">
                  <div class="summary-value">{{ report.summary.durationText }}</div>
                  <div class="summary-label">耗时</div>
                </div>
              </el-col>
            </el-row>

            <!-- Charts Placeholder -->
            <el-row :gutter="16" style="margin-top: 16px">
              <el-col :span="12">
                <div class="chart-placeholder">
                  <div class="chart-title">结果分布</div>
                  <div class="chart-body">
                    <div class="pie-chart-mock">
                      <div class="pie-segment pie-pass" :style="{ '--pass-deg': (report.summary.passRate * 3.6) + 'deg' }"></div>
                    </div>
                    <div class="pie-legend">
                      <div><span class="legend-dot" style="background:#67c23a"></span>通过 {{ report.summary.passedCases }}</div>
                      <div><span class="legend-dot" style="background:#f56c6c"></span>失败 {{ report.summary.failedCases }}</div>
                      <div><span class="legend-dot" style="background:#909399"></span>跳过 {{ report.summary.skippedCases }}</div>
                    </div>
                  </div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="chart-placeholder">
                  <div class="chart-title">通过率趋势（近 7 次）</div>
                  <div class="chart-body">
                    <div class="trend-bars">
                      <div
                        v-for="(point, i) in report.trendData"
                        :key="i"
                        class="trend-bar-item"
                      >
                        <div class="trend-bar" :style="{ height: point.passRate + '%', background: getPassRateColor(point.passRate) }">
                          <span class="trend-bar-label">{{ point.passRate }}%</span>
                        </div>
                        <span class="trend-date">{{ point.date }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>

            <!-- Module Stats Table -->
            <div class="report-section">
              <h4>模块统计</h4>
              <el-table :data="report.moduleStats" stripe size="small" border>
                <el-table-column prop="moduleName" label="模块" min-width="160" />
                <el-table-column prop="total" label="总数" width="80" align="center" />
                <el-table-column prop="passed" label="通过" width="80" align="center">
                  <template #default="{ row }"><span style="color:#67c23a">{{ row.passed }}</span></template>
                </el-table-column>
                <el-table-column prop="failed" label="失败" width="80" align="center">
                  <template #default="{ row }"><span style="color:#f56c6c">{{ row.failed }}</span></template>
                </el-table-column>
                <el-table-column prop="passRate" label="通过率" width="140" align="center">
                  <template #default="{ row }">
                    <el-progress :percentage="row.passRate" :color="getPassRateColor(row.passRate)" :stroke-width="6" />
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- Priority Stats Table -->
            <div class="report-section">
              <h4>优先级统计</h4>
              <el-table :data="report.priorityStats" stripe size="small" border>
                <el-table-column prop="priority" label="优先级" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag :type="priorityTagMap[row.priority]?.type || 'info'" size="small">{{ row.priority }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="total" label="总数" width="80" align="center" />
                <el-table-column prop="passed" label="通过" width="80" align="center">
                  <template #default="{ row }"><span style="color:#67c23a">{{ row.passed }}</span></template>
                </el-table-column>
                <el-table-column prop="failed" label="失败" width="80" align="center">
                  <template #default="{ row }"><span style="color:#f56c6c">{{ row.failed }}</span></template>
                </el-table-column>
                <el-table-column prop="passRate" label="通过率" width="140" align="center">
                  <template #default="{ row }">
                    <el-progress :percentage="row.passRate" :color="getPassRateColor(row.passRate)" :stroke-width="6" />
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- Failed Case Details -->
            <div v-if="report.failedCases.length > 0" class="report-section">
              <h4>失败用例详情</h4>
              <el-table :data="report.failedCases" stripe size="small" border>
                <el-table-column prop="caseName" label="用例名称" min-width="200" show-overflow-tooltip />
                <el-table-column prop="priority" label="优先级" width="80" align="center">
                  <template #default="{ row }">
                    <el-tag :type="priorityTagMap[row.priority]?.type || 'info'" size="small">{{ row.priority }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="module" label="模块" width="120" />
                <el-table-column prop="errorMessage" label="错误信息" min-width="250" show-overflow-tooltip>
                  <template #default="{ row }">
                    <span class="error-text">{{ row.errorMessage }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="durationText" label="耗时" width="90" />
              </el-table>
            </div>

            <!-- AI Summary -->
            <div class="report-section">
              <h4><el-icon><MagicStick /></el-icon> AI 分析摘要</h4>
              <div class="ai-summary-box">
                <div v-html="formatAiSummary(report.aiSummary || '')"></div>
              </div>
            </div>

            <!-- Export Buttons -->
            <div class="report-actions">
              <el-button type="primary" :icon="Download">导出 PDF 报告</el-button>
              <el-button :icon="Document">导出 Excel</el-button>
              <el-button :icon="Share">分享报告</el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- ==================== Tab 3: 执行日志 ==================== -->
        <el-tab-pane label="执行日志" name="logs">
          <div class="logs-tab-content">
            <div class="log-actions-bar">
              <el-button size="small" :icon="CopyDocument" @click="handleCopyAllLogs">复制全部日志</el-button>
              <el-button size="small" :icon="Download" @click="handleDownloadLogs">下载日志</el-button>
            </div>
            <el-collapse v-model="expandedLogs">
              <el-collapse-item
                v-for="caseLog in allCaseLogs"
                :key="caseLog.caseId"
                :name="caseLog.caseId"
              >
                <template #title>
                  <div class="log-case-header">
                    <el-tag
                      :type="caseStatusTagMap[caseLog.status]?.type || 'info'"
                      size="small"
                      effect="dark"
                      round
                    >
                      {{ caseStatusTagMap[caseLog.status]?.label || caseLog.status }}
                    </el-tag>
                    <span class="log-case-name">{{ caseLog.caseName }}</span>
                    <span class="log-case-duration">{{ caseLog.durationText }}</span>
                    <span class="log-count">{{ caseLog.logs.length }} 条日志</span>
                  </div>
                </template>
                <div class="log-viewer">
                  <div
                    v-for="(log, idx) in caseLog.logs"
                    :key="idx"
                    class="log-line"
                    :class="'log-' + log.level.toLowerCase()"
                  >
                    <span class="log-timestamp">{{ log.timestamp }}</span>
                    <el-tag :type="logLevelTagMap[log.level]?.type || 'info'" size="small" effect="dark" class="log-level-badge">
                      {{ log.level }}
                    </el-tag>
                    <span class="log-message">{{ log.message }}</span>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-tab-pane>

        <!-- ==================== Tab 4: 截图对比 ==================== -->
        <el-tab-pane label="截图对比" name="screenshots">
          <div class="screenshots-tab-content">
            <!-- Summary Table -->
            <el-table :data="screenshotComparisons" stripe size="small" border style="margin-bottom: 20px">
              <el-table-column prop="caseName" label="用例名称" min-width="180" show-overflow-tooltip />
              <el-table-column prop="matchStatus" label="对比结果" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.matchStatus === 'match' ? 'success' : 'danger'" size="small" effect="dark" round>
                    {{ row.matchStatus === 'match' ? '✓ 一致' : '✗ 差异' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="similarity" label="相似度" width="140" align="center">
                <template #default="{ row }">
                  <el-progress
                    :percentage="row.similarity"
                    :color="row.similarity >= 95 ? '#67c23a' : row.similarity >= 80 ? '#e6a23c' : '#f56c6c'"
                    :stroke-width="6"
                  />
                </template>
              </el-table-column>
            </el-table>

            <!-- Side-by-side Comparison Cards -->
            <div v-for="comp in screenshotComparisons" :key="comp.id" class="comparison-card">
              <div class="comparison-header">
                <span class="comparison-case-name">{{ comp.caseName }}</span>
                <el-tag :type="comp.matchStatus === 'match' ? 'success' : 'danger'" size="small" effect="dark" round>
                  {{ comp.matchStatus === 'match' ? '✓ 一致' : '✗ 差异' }}
                </el-tag>
                <span class="comparison-similarity">相似度 {{ comp.similarity }}%</span>
              </div>
              <div class="comparison-images">
                <div class="comparison-image-col">
                  <div class="comparison-label">基准截图</div>
                  <div class="comparison-image-placeholder baseline" :style="{ background: comp.baselineColor }">
                    <span>Baseline</span>
                  </div>
                </div>
                <div class="comparison-image-col">
                  <div class="comparison-label">实际截图</div>
                  <div class="comparison-image-placeholder actual" :style="{ background: comp.actualColor }">
                    <span>Actual</span>
                  </div>
                </div>
                <div v-if="comp.matchStatus === 'diff'" class="comparison-image-col">
                  <div class="comparison-label">Diff 差异图</div>
                  <div class="comparison-image-placeholder diff" style="background: #4a1c1c">
                    <span style="color:#f56c6c">Diff</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- ==================== Tab 5: 执行回放 ==================== -->
        <el-tab-pane label="执行回放" name="replay">
          <div class="replay-tab-content">
            <el-row :gutter="16">
              <el-col :span="16">
                <div class="video-player-placeholder">
                  <div class="video-play-btn">
                    <el-icon :size="48"><VideoPlay /></el-icon>
                  </div>
                  <div class="video-player-label">执行录屏回放</div>
                  <div class="video-player-sub">选择右侧时间轴上的步骤以定位播放</div>
                </div>
                <!-- Video Grid -->
                <div class="video-grid">
                  <h4 style="margin-bottom: 12px">全部用例录屏</h4>
                  <div class="video-grid-items">
                    <div
                      v-for="caseDetail in caseDetails"
                      :key="caseDetail.caseId"
                      class="video-grid-item"
                      :class="{ 'has-video': caseDetail.hasVideo }"
                    >
                      <div class="video-thumb" :style="{ background: videoThumbColor(caseDetail) }">
                        <el-icon v-if="caseDetail.hasVideo" :size="24"><VideoPlay /></el-icon>
                        <span v-else class="no-video-text">无录屏</span>
                      </div>
                      <div class="video-thumb-label">{{ caseDetail.caseName }}</div>
                      <el-tag
                        :type="caseStatusTagMap[caseDetail.status]?.type || 'info'"
                        size="small"
                        effect="dark"
                        round
                        class="video-thumb-badge"
                      >
                        {{ caseStatusTagMap[caseDetail.status]?.label }}
                      </el-tag>
                    </div>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="step-timeline">
                  <h4>步骤时间轴</h4>
                  <el-timeline>
                    <el-timeline-item
                      v-for="caseDetail in caseDetails"
                      :key="'timeline-'+caseDetail.caseId"
                    >
                      <div class="timeline-case-title">{{ caseDetail.caseName }}</div>
                      <div
                        v-for="step in caseDetail.steps"
                        :key="step.stepOrder"
                        class="timeline-step"
                        @click="selectTimelineStep(caseDetail, step)"
                      >
                        <el-icon
                          :size="14"
                          :color="stepIconColor(step)"
                        >
                          <CircleCheckFilled v-if="step.status === 'passed'" />
                          <CircleCloseFilled v-else-if="step.status === 'failed'" />
                          <Loading v-else-if="step.status === 'running'" class="is-loading" />
                          <RemoveFilled v-else />
                        </el-icon>
                        <span class="timeline-step-text">
                          S{{ step.stepOrder }}: {{ step.stepDescription || step.stepAction }}
                        </span>
                        <span class="timeline-step-time">{{ step.duration }}ms</span>
                      </div>
                    </el-timeline-item>
                  </el-timeline>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- ==================== Tab 6: AI 分析 ==================== -->
        <el-tab-pane label="AI分析" name="ai">
          <div class="ai-tab-content">
            <!-- AI Summary -->
            <div class="ai-section">
              <h3><el-icon><MagicStick /></el-icon> AI 智能分析</h3>
              <div class="ai-summary-text" v-html="formatAiSummary(aiAnalysis.summary)"></div>
            </div>

            <!-- Failure Analysis Cards -->
            <div class="ai-section">
              <h3>失败原因分析</h3>
              <el-row :gutter="16">
                <el-col :span="8" v-for="(analysis, idx) in aiAnalysis.failureAnalysis" :key="idx">
                  <el-card shadow="hover" class="failure-card">
                    <div class="failure-card-header">
                      <el-tag :type="analysis.severity === 'high' ? 'danger' : analysis.severity === 'medium' ? 'warning' : 'info'" size="small" effect="dark">
                        {{ analysis.severity === 'high' ? '严重' : analysis.severity === 'medium' ? '中等' : '轻微' }}
                      </el-tag>
                      <span class="failure-card-title">{{ analysis.title }}</span>
                    </div>
                    <p class="failure-card-desc">{{ analysis.description }}</p>
                    <div class="failure-card-meta">
                      <span>影响用例: {{ analysis.affectedCases }}</span>
                    </div>
                    <div class="failure-card-suggestion">
                      <el-icon><ChatLineRound /></el-icon>
                      <span>{{ analysis.suggestion }}</span>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>

            <!-- Style Diff Warnings -->
            <div v-if="aiAnalysis.styleWarnings.length > 0" class="ai-section">
              <h3>样式差异告警</h3>
              <el-table :data="aiAnalysis.styleWarnings" stripe size="small" border>
                <el-table-column prop="component" label="组件/页面" width="160" />
                <el-table-column prop="property" label="样式属性" width="120" />
                <el-table-column prop="expected" label="期望值" width="160">
                  <template #default="{ row }"><code class="code-value">{{ row.expected }}</code></template>
                </el-table-column>
                <el-table-column prop="actual" label="实际值" width="160">
                  <template #default="{ row }"><code class="code-value code-actual">{{ row.actual }}</code></template>
                </el-table-column>
                <el-table-column prop="severity" label="严重程度" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.severity === 'high' ? 'danger' : 'warning'" size="small">
                      {{ row.severity === 'high' ? '高' : '中' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="suggestion" label="AI 建议" min-width="200" show-overflow-tooltip />
              </el-table>
            </div>

            <!-- Action Buttons -->
            <div class="ai-actions">
              <el-button type="primary" :icon="MagicStick">
                AI 自动修复（Beta）
              </el-button>
              <el-button type="danger" :icon="WarningFilled">
                一键创建缺陷单
              </el-button>
              <el-button type="success" :icon="Check">
                确认分析结果
              </el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, VideoPlay, VideoPause, Refresh, Loading, CircleCheckFilled, CircleCloseFilled,
  RemoveFilled, Document, VideoCamera, Timer, TrendCharts, MagicStick, Download, Share,
  CopyDocument, WarningFilled, Check, ChatLineRound,
} from '@element-plus/icons-vue'
import type {
  TestExecution,
  TestExecutionDetail,
  ExecutionStep,
  ExecutionReport,
  ScreenshotComparison,
  CaseExecutionLog,
} from '@/api/suite'

const route = useRoute()
const router = useRouter()
const executionId = computed(() => Number(route.params.id))
const activeTab = ref('steps')

const statusTagMap: Record<string, { type: 'info' | 'success' | 'warning' | 'danger'; label: string }> = {
  running: { type: 'warning', label: '执行中' },
  success: { type: 'success', label: '成功' },
  failed: { type: 'danger', label: '失败' },
  cancelled: { type: 'info', label: '已取消' },
  pending: { type: 'info', label: '等待中' },
}

const caseStatusTagMap: Record<string, { type: 'info' | 'success' | 'warning' | 'danger'; label: string }> = {
  passed: { type: 'success', label: '通过' },
  failed: { type: 'danger', label: '失败' },
  skipped: { type: 'info', label: '跳过' },
  running: { type: 'warning', label: '执行中' },
  pending: { type: 'info', label: '等待中' },
  error: { type: 'danger', label: '错误' },
}

const priorityTagMap: Record<string, { type: 'danger' | 'warning' | 'primary' | 'info' }> = {
  P0: { type: 'danger' }, P1: { type: 'warning' }, P2: { type: 'primary' }, P3: { type: 'info' },
}

const logLevelTagMap: Record<string, { type: 'info' | 'success' | 'warning' | 'danger' }> = {
  INFO: { type: 'info' }, PASS: { type: 'success' }, WARN: { type: 'warning' }, ERROR: { type: 'danger' }, DEBUG: { type: 'info' },
}

// ========== State ==========
const expandedCases = ref<string | number>('')
const expandedLogs = ref<number[]>([])
const selectedStep = ref<ExecutionStep | null>(null)

const execution = reactive<TestExecution>({
  executionName: '',
  triggerType: 'manual',
  status: 'success',
  totalCases: 0,
  passedCases: 0,
  failedCases: 0,
  skippedCases: 0,
  passRate: 0,
  startedAt: '',
  durationText: '',
})

const caseDetails = ref<TestExecutionDetail[]>([])
const report = reactive<ExecutionReport>({
  executionId: 0,
  executionName: '',
  score: 0,
  summary: { totalCases: 0, passedCases: 0, failedCases: 0, skippedCases: 0, duration: 0, durationText: '', passRate: 0, startedAt: '', finishedAt: '' },
  moduleStats: [],
  priorityStats: [],
  failedCases: [],
  aiSummary: '',
  trendData: [],
})

const screenshotComparisons = ref<(ScreenshotComparison & { baselineColor?: string; actualColor?: string })[]>([])
const allCaseLogs = ref<CaseExecutionLog[]>([])

// AI Analysis mock
const aiAnalysis = reactive({
  summary: '',
  failureAnalysis: [] as { severity: string; title: string; description: string; affectedCases: number; suggestion: string }[],
  styleWarnings: [] as { component: string; property: string; expected: string; actual: string; severity: string; suggestion: string }[],
})

onMounted(() => {
  loadMockData()
})

// ========== Mock Data ==========
function loadMockData() {
  // Execution overview
  Object.assign(execution, {
    executionName: '回归测试 - 用户模块 V2.3.1',
    suiteName: '用户模块回归套件',
    systemName: '用户中心系统',
    triggerType: 'manual',
    status: 'failed',
    totalCases: 12,
    passedCases: 9,
    failedCases: 2,
    skippedCases: 1,
    passRate: 75,
    startedAt: '2026-06-12 14:30:00',
    finishedAt: '2026-06-12 14:38:42',
    duration: 522,
    durationText: '8m 42s',
    executedBy: '张三',
  })

  // Case details with steps
  caseDetails.value = [
    {
      caseId: 1, caseName: '用户登录 - 正常流程', caseType: 'PC', priority: 'P0',
      status: 'passed', duration: 15200, durationText: '15.2s', hasVideo: true,
      steps: [
        { stepOrder: 1, stepAction: 'navigate', stepTarget: 'https://app.example.com/login', stepDescription: '打开登录页面', status: 'passed', duration: 2100, screenshot: '', expected: '页面加载完成', actual: '页面加载完成', timestamp: '14:30:02' },
        { stepOrder: 2, stepAction: 'input', stepTarget: '#username', stepValue: 'admin', stepDescription: '输入用户名', status: 'passed', duration: 800, screenshot: '', expected: '输入框包含 admin', actual: '输入框包含 admin', timestamp: '14:30:04' },
        { stepOrder: 3, stepAction: 'input', stepTarget: '#password', stepValue: '******', stepDescription: '输入密码', status: 'passed', duration: 750, screenshot: '', expected: '密码已输入', actual: '密码已输入', timestamp: '14:30:05' },
        { stepOrder: 4, stepAction: 'click', stepTarget: '#login-btn', stepDescription: '点击登录按钮', status: 'passed', duration: 3200, screenshot: '', expected: '请求发送成功', actual: '请求发送成功', timestamp: '14:30:08' },
        { stepOrder: 5, stepAction: 'assert', stepDescription: '验证跳转到首页', status: 'passed', duration: 1500, screenshot: '', expected: 'URL 包含 /dashboard', actual: 'URL: /dashboard', timestamp: '14:30:10' },
      ],
    },
    {
      caseId: 2, caseName: '用户登录 - 密码错误', caseType: 'PC', priority: 'P0',
      status: 'passed', duration: 12800, durationText: '12.8s', hasVideo: true,
      steps: [
        { stepOrder: 1, stepAction: 'navigate', stepDescription: '打开登录页面', status: 'passed', duration: 1800, timestamp: '14:30:15' },
        { stepOrder: 2, stepAction: 'input', stepTarget: '#username', stepValue: 'admin', stepDescription: '输入用户名', status: 'passed', duration: 600, timestamp: '14:30:16' },
        { stepOrder: 3, stepAction: 'input', stepTarget: '#password', stepValue: '******', stepDescription: '输入错误密码', status: 'passed', duration: 550, timestamp: '14:30:17' },
        { stepOrder: 4, stepAction: 'click', stepTarget: '#login-btn', stepDescription: '点击登录', status: 'passed', duration: 2800, timestamp: '14:30:20' },
        { stepOrder: 5, stepAction: 'assert', stepDescription: '验证显示错误提示', status: 'passed', duration: 1200, timestamp: '14:30:22', expected: '显示"密码错误"', actual: '显示"密码错误"' },
      ],
    },
    {
      caseId: 3, caseName: '用户注册 - 完整流程', caseType: 'PC', priority: 'P1',
      status: 'failed', duration: 28500, durationText: '28.5s', hasVideo: true,
      steps: [
        { stepOrder: 1, stepAction: 'navigate', stepDescription: '打开注册页面', status: 'passed', duration: 2000, timestamp: '14:30:30' },
        { stepOrder: 2, stepAction: 'input', stepTarget: '#reg-username', stepValue: 'newuser001', stepDescription: '输入用户名', status: 'passed', duration: 700, timestamp: '14:30:32' },
        { stepOrder: 3, stepAction: 'input', stepTarget: '#reg-email', stepValue: 'test@example.com', stepDescription: '输入邮箱', status: 'passed', duration: 650, timestamp: '14:30:33' },
        { stepOrder: 4, stepAction: 'input', stepTarget: '#reg-password', stepDescription: '输入密码', status: 'passed', duration: 800, timestamp: '14:30:34' },
        { stepOrder: 5, stepAction: 'input', stepTarget: '#reg-confirm', stepDescription: '确认密码', status: 'passed', duration: 600, timestamp: '14:30:35' },
        { stepOrder: 6, stepAction: 'click', stepTarget: '#reg-submit', stepDescription: '点击注册', status: 'failed', duration: 8500, errorMessage: 'TimeoutError: 等待 #success-toast 超时 (8000ms)', timestamp: '14:30:43', expected: '显示注册成功提示', actual: '超时 - 页面未响应' },
        { stepOrder: 7, stepAction: 'assert', stepDescription: '验证跳转', status: 'skipped', duration: 0, timestamp: '14:30:43' },
      ],
    },
    {
      caseId: 4, caseName: '个人信息 - 修改头像', caseType: 'PC', priority: 'P2',
      status: 'failed', duration: 18200, durationText: '18.2s', hasVideo: false,
      steps: [
        { stepOrder: 1, stepAction: 'navigate', stepDescription: '打开个人设置', status: 'passed', duration: 1900, timestamp: '14:31:00' },
        { stepOrder: 2, stepAction: 'click', stepTarget: '.avatar-upload', stepDescription: '点击头像区域', status: 'passed', duration: 500, timestamp: '14:31:01' },
        { stepOrder: 3, stepAction: 'input', stepTarget: 'input[type=file]', stepDescription: '上传头像文件', status: 'passed', duration: 3200, timestamp: '14:31:04' },
        { stepOrder: 4, stepAction: 'click', stepTarget: '#crop-confirm', stepDescription: '确认裁剪', status: 'passed', duration: 1500, timestamp: '14:31:06' },
        { stepOrder: 5, stepAction: 'assert', stepDescription: '验证头像更新', status: 'failed', duration: 5000, errorMessage: 'AssertionError: 预期头像URL包含 /avatars/newuser，实际为 /avatars/default.png', timestamp: '14:31:11', expected: '头像URL包含 /avatars/newuser', actual: '头像URL为 /avatars/default.png' },
      ],
    },
    {
      caseId: 5, caseName: '权限管理 - 角色分配', caseType: 'PC', priority: 'P1',
      status: 'passed', duration: 22100, durationText: '22.1s', hasVideo: true,
      steps: [
        { stepOrder: 1, stepAction: 'navigate', stepDescription: '打开权限管理页', status: 'passed', duration: 2200, timestamp: '14:31:20' },
        { stepOrder: 2, stepAction: 'click', stepTarget: '.role-item[data-role="editor"]', stepDescription: '选择编辑角色', status: 'passed', duration: 800, timestamp: '14:31:21' },
        { stepOrder: 3, stepAction: 'click', stepTarget: '#assign-user-btn', stepDescription: '点击分配用户', status: 'passed', duration: 600, timestamp: '14:31:22' },
        { stepOrder: 4, stepAction: 'select', stepTarget: '#user-select', stepValue: 'testuser', stepDescription: '选择用户 testuser', status: 'passed', duration: 1200, timestamp: '14:31:23' },
        { stepOrder: 5, stepAction: 'click', stepTarget: '#confirm-assign', stepDescription: '确认分配', status: 'passed', duration: 2500, timestamp: '14:31:26' },
        { stepOrder: 6, stepAction: 'assert', stepDescription: '验证角色列表', status: 'passed', duration: 1800, timestamp: '14:31:28', expected: 'testuser 在编辑角色列表中', actual: '验证通过' },
      ],
    },
    {
      caseId: 6, caseName: 'API - 用户列表查询', caseType: 'API', priority: 'P1',
      status: 'passed', duration: 3200, durationText: '3.2s', hasVideo: false,
      steps: [
        { stepOrder: 1, stepAction: 'http_request', stepDescription: '发送 GET /api/users', status: 'passed', duration: 1500, timestamp: '14:32:00', expected: '状态码 200', actual: '200 OK' },
        { stepOrder: 2, stepAction: 'assert', stepDescription: '验证响应结构', status: 'passed', duration: 800, timestamp: '14:32:01', expected: '包含 data.list 数组', actual: '验证通过' },
        { stepOrder: 3, stepAction: 'assert', stepDescription: '验证分页参数', status: 'passed', duration: 500, timestamp: '14:32:02', expected: 'total > 0', actual: 'total: 156' },
      ],
    },
  ]

  // Report
  Object.assign(report, {
    executionId: executionId.value,
    executionName: execution.executionName,
    suiteName: execution.suiteName,
    score: 78,
    summary: {
      totalCases: 12, passedCases: 9, failedCases: 2, skippedCases: 1,
      duration: 522, durationText: '8m 42s', passRate: 75,
      startedAt: '2026-06-12 14:30:00', finishedAt: '2026-06-12 14:38:42',
    },
    moduleStats: [
      { moduleName: '用户登录模块', total: 4, passed: 4, failed: 0, passRate: 100 },
      { moduleName: '用户注册模块', total: 3, passed: 1, failed: 2, passRate: 33 },
      { moduleName: '个人中心', total: 2, passed: 2, failed: 0, passRate: 100 },
      { moduleName: '权限管理', total: 2, passed: 1, failed: 1, passRate: 50 },
      { moduleName: 'API 接口', total: 1, passed: 1, failed: 0, passRate: 100 },
    ],
    priorityStats: [
      { priority: 'P0', total: 4, passed: 4, failed: 0, passRate: 100 },
      { priority: 'P1', total: 4, passed: 2, failed: 2, passRate: 50 },
      { priority: 'P2', total: 3, passed: 2, failed: 1, passRate: 67 },
      { priority: 'P3', total: 1, passed: 1, failed: 0, passRate: 100 },
    ],
    failedCases: [
      { caseId: 3, caseName: '用户注册 - 完整流程', priority: 'P1', module: '用户注册模块', errorMessage: 'TimeoutError: 等待 #success-toast 超时 (8000ms)', duration: 28500, durationText: '28.5s' },
      { caseId: 4, caseName: '个人信息 - 修改头像', priority: 'P2', module: '个人中心', errorMessage: 'AssertionError: 头像URL未更新，预期 /avatars/newuser 实际 /avatars/default.png', duration: 18200, durationText: '18.2s' },
    ],
    aiSummary: `## 执行总结

本次回归测试共执行 **12** 个用例，通过率 **75%**。

### 关键发现
1. **用户注册流程存在阻塞问题**：注册提交后页面未响应，疑似后端接口超时或服务异常
2. **头像上传功能异常**：上传后头像未实际更新，可能为文件存储路径配置问题
3. P0 优先级用例全部通过，核心功能稳定

### 建议
- 优先排查注册服务的可用性，建议检查数据库连接及消息队列状态
- 头像上传建议检查 CDN 配置和文件写入权限
- 本轮测试 P0 用例全部通过，可考虑灰度发布`,
    trendData: [
      { date: '06/06', passRate: 92, totalCases: 10 },
      { date: '06/07', passRate: 88, totalCases: 10 },
      { date: '06/08', passRate: 95, totalCases: 11 },
      { date: '06/09', passRate: 91, totalCases: 11 },
      { date: '06/10', passRate: 83, totalCases: 12 },
      { date: '06/11', passRate: 86, totalCases: 12 },
      { date: '06/12', passRate: 75, totalCases: 12 },
    ],
  })

  // Screenshot comparisons
  screenshotComparisons.value = [
    { id: 1, caseId: 1, caseName: '用户登录 - 正常流程', baselineUrl: '', actualUrl: '', matchStatus: 'match', similarity: 99.2, baselineColor: '#2d5a3d', actualColor: '#2d5a3d' },
    { id: 2, caseId: 2, caseName: '用户登录 - 密码错误', baselineUrl: '', actualUrl: '', matchStatus: 'match', similarity: 98.7, baselineColor: '#3d4a5a', actualColor: '#3d4a5a' },
    { id: 3, caseId: 3, caseName: '用户注册 - 完整流程', baselineUrl: '', actualUrl: '', matchStatus: 'diff', similarity: 72.3, baselineColor: '#4a3d2d', actualColor: '#5a3d2d' },
    { id: 4, caseId: 5, caseName: '权限管理 - 角色分配', baselineUrl: '', actualUrl: '', matchStatus: 'match', similarity: 97.8, baselineColor: '#2d3d5a', actualColor: '#2d3d5a' },
    { id: 5, caseId: 4, caseName: '个人信息 - 修改头像', baselineUrl: '', actualUrl: '', matchStatus: 'diff', similarity: 65.1, baselineColor: '#5a2d3d', actualColor: '#5a4a2d' },
  ]

  // Case logs
  allCaseLogs.value = [
    {
      caseId: 1, caseName: '用户登录 - 正常流程', status: 'passed', duration: 15200, durationText: '15.2s',
      logs: [
        { timestamp: '14:30:02.123', level: 'INFO', message: '[Browser] 启动 Chromium 浏览器实例' },
        { timestamp: '14:30:02.456', level: 'INFO', message: '[Navigate] 正在加载 https://app.example.com/login' },
        { timestamp: '14:30:04.234', level: 'PASS', message: '[Assert] 页面标题包含"登录" ✓' },
        { timestamp: '14:30:04.567', level: 'INFO', message: '[Input] #username ← "admin"' },
        { timestamp: '14:30:05.123', level: 'INFO', message: '[Input] #password ← "******"' },
        { timestamp: '14:30:08.456', level: 'INFO', message: '[Click] #login-btn 已点击' },
        { timestamp: '14:30:08.789', level: 'INFO', message: '[Network] POST /api/auth/login → 200 OK (1.2s)' },
        { timestamp: '14:30:10.012', level: 'PASS', message: '[Assert] URL 包含 /dashboard ✓' },
        { timestamp: '14:30:10.234', level: 'INFO', message: '[Screenshot] 步骤截图已保存' },
      ],
    },
    {
      caseId: 3, caseName: '用户注册 - 完整流程', status: 'failed', duration: 28500, durationText: '28.5s',
      logs: [
        { timestamp: '14:30:30.100', level: 'INFO', message: '[Browser] 启动 Chromium 浏览器实例' },
        { timestamp: '14:30:30.450', level: 'INFO', message: '[Navigate] 正在加载 https://app.example.com/register' },
        { timestamp: '14:30:32.200', level: 'INFO', message: '[Input] #reg-username ← "newuser001"' },
        { timestamp: '14:30:33.100', level: 'INFO', message: '[Input] #reg-email ← "test@example.com"' },
        { timestamp: '14:30:34.000', level: 'INFO', message: '[Input] #reg-password ← "******"' },
        { timestamp: '14:30:35.000', level: 'INFO', message: '[Input] #reg-confirm ← "******"' },
        { timestamp: '14:30:43.500', level: 'WARN', message: '[Network] POST /api/auth/register 响应缓慢 (>5s)' },
        { timestamp: '14:30:43.800', level: 'ERROR', message: '[TimeoutError] 等待 #success-toast 超时 (8000ms)' },
        { timestamp: '14:30:43.850', level: 'ERROR', message: '[Network] POST /api/auth/register → 504 Gateway Timeout (15.2s)' },
        { timestamp: '14:30:43.900', level: 'DEBUG', message: '[Screenshot] 失败截图已保存: error_register_timeout.png' },
        { timestamp: '14:30:43.950', level: 'INFO', message: '[Skip] 步骤 7 (验证跳转) 已跳过 - 前置步骤失败' },
      ],
    },
    {
      caseId: 4, caseName: '个人信息 - 修改头像', status: 'failed', duration: 18200, durationText: '18.2s',
      logs: [
        { timestamp: '14:31:00.100', level: 'INFO', message: '[Navigate] 正在加载 /settings/profile' },
        { timestamp: '14:31:01.500', level: 'INFO', message: '[Click] .avatar-upload 已点击' },
        { timestamp: '14:31:04.700', level: 'INFO', message: '[Upload] 文件已上传: test-avatar.png (256KB)' },
        { timestamp: '14:31:06.200', level: 'INFO', message: '[Click] #crop-confirm 已点击' },
        { timestamp: '14:31:11.200', level: 'ERROR', message: '[AssertionError] 预期头像URL包含 /avatars/newuser' },
        { timestamp: '14:31:11.250', level: 'ERROR', message: '[AssertionError] 实际值: /avatars/default.png' },
        { timestamp: '14:31:11.300', level: 'WARN', message: '[Hint] 可能是文件存储服务异常或路径配置错误' },
      ],
    },
    {
      caseId: 5, caseName: '权限管理 - 角色分配', status: 'passed', duration: 22100, durationText: '22.1s',
      logs: [
        { timestamp: '14:31:20.100', level: 'INFO', message: '[Navigate] 正在加载 /admin/permissions' },
        { timestamp: '14:31:21.800', level: 'INFO', message: '[Click] .role-item[data-role="editor"] 已点击' },
        { timestamp: '14:31:22.400', level: 'INFO', message: '[Click] #assign-user-btn 已点击' },
        { timestamp: '14:31:23.600', level: 'INFO', message: '[Select] #user-select ← "testuser"' },
        { timestamp: '14:31:26.100', level: 'INFO', message: '[Click] #confirm-assign 已点击' },
        { timestamp: '14:31:26.400', level: 'INFO', message: '[Network] POST /api/roles/editor/users → 200 OK' },
        { timestamp: '14:31:28.200', level: 'PASS', message: '[Assert] testuser 在编辑角色列表中 ✓' },
      ],
    },
  ]

  // AI Analysis
  aiAnalysis.summary = `## AI 智能分析结果

本次执行 **${execution.executionName}**，共 12 个用例，2 个失败。

### 失败根因分析

**1. 用户注册超时（严重）**
用户注册接口 \`/api/auth/register\` 返回 504 Gateway Timeout。可能原因：
- 后端注册服务数据库连接池耗尽
- 邮件发送服务阻塞（注册后发送验证邮件）
- 网络代理配置异常

**2. 头像上传未生效（中等）**
头像上传成功但实际URL仍指向默认头像。可能原因：
- 文件存储路径配置错误
- CDN 缓存未及时刷新
- 文件写入权限不足

### 风险评估
- 🔴 P1 级用例有 2 个失败，**不建议直接发布**
- P0 核心用例全部通过，基础功能稳定
- 建议修复后重新执行注册模块和头像上传相关用例`

  aiAnalysis.failureAnalysis = [
    {
      severity: 'high',
      title: '注册接口 504 超时',
      description: 'POST /api/auth/register 响应时间超过 15s，最终返回 504。该问题影响注册完整流程，包括表单提交和验证跳转。',
      affectedCases: 1,
      suggestion: '检查注册服务的数据库连接、邮件队列状态、以及上游依赖服务健康情况。建议增加超时重试机制。',
    },
    {
      severity: 'medium',
      title: '头像文件未持久化',
      description: '文件上传接口返回成功（200），但实际查询到的头像URL仍为默认值。可能是异步处理延迟或存储路径映射问题。',
      affectedCases: 1,
      suggestion: '检查文件存储服务日志，确认文件是否成功写入目标路径。排查 CDN 刷新策略。',
    },
  ]

  aiAnalysis.styleWarnings = [
    { component: '注册页面', property: '按钮颜色', expected: '#409eff', actual: '#3d8bcc', severity: 'medium', suggestion: 'CSS 变量 --el-color-primary 可能被局部覆盖' },
    { component: '登录页面', property: 'Logo 间距', expected: 'margin-top: 48px', actual: 'margin-top: 32px', severity: 'high', suggestion: '检查 layout 样式优先级，可能是新样式覆盖了全局布局' },
    { component: '权限管理', property: '表格行高', expected: 'height: 56px', actual: 'height: 48px', severity: 'medium', suggestion: 'el-table 的 row-style 配置可能被组件版本更新影响' },
  ]
}

// ========== Methods ==========
function selectStep(caseDetail: TestExecutionDetail, step: ExecutionStep) {
  selectedStep.value = step
}

function selectTimelineStep(caseDetail: TestExecutionDetail, step: ExecutionStep) {
  selectedStep.value = step
  activeTab.value = 'steps'
}

function stepColor(step: ExecutionStep): string {
  if (step.status === 'passed') return '#1a3a2a'
  if (step.status === 'failed') return '#3a1a1a'
  if (step.status === 'running') return '#3a3a1a'
  if (step.status === 'skipped') return '#2a2a2a'
  return '#1e2a3a'
}

function stepIconColor(step: ExecutionStep): string {
  if (step.status === 'passed') return '#67c23a'
  if (step.status === 'failed') return '#f56c6c'
  if (step.status === 'running') return '#e6a23c'
  return '#909399'
}

function videoThumbColor(caseDetail: TestExecutionDetail): string {
  if (!caseDetail.hasVideo) return '#2a2a2a'
  if (caseDetail.status === 'passed') return '#1a3a2a'
  if (caseDetail.status === 'failed') return '#3a1a1a'
  return '#1e2a3a'
}

function getPassRateColor(rate: number): string {
  if (rate >= 90) return '#67c23a'
  if (rate >= 70) return '#e6a23c'
  return '#f56c6c'
}

function getScoreColor(score: number): string {
  if (score >= 90) return '#67c23a'
  if (score >= 70) return '#e6a23c'
  return '#f56c6c'
}

function formatAiSummary(text: string): string {
  return text
    .replace(/^## (.+)$/gm, '<h4>$1</h4>')
    .replace(/^### (.+)$/gm, '<h5 style="margin:8px 0 4px;color:var(--el-text-color-primary)">$1</h5>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/`(.+?)`/g, '<code style="background:var(--el-fill-color-light);padding:1px 4px;border-radius:3px;font-size:12px">$1</code>')
    .replace(/^- (.+)$/gm, '<div style="padding-left:12px">• $1</div>')
    .replace(/\n/g, '<br>')
}

function handleCancel() {
  ElMessageBox.confirm('确定取消当前执行？', '确认取消', { type: 'warning' })
    .then(() => ElMessage.success('执行已取消'))
    .catch(() => {})
}

function handleRefresh() {
  loadMockData()
  ElMessage.success('数据已刷新')
}

function handleCopyAllLogs() {
  const allText = allCaseLogs.value.flatMap(cl =>
    cl.logs.map(l => `[${l.timestamp}] [${l.level}] ${l.message}`)
  ).join('\n')
  navigator.clipboard.writeText(allText).then(() => ElMessage.success('日志已复制到剪贴板'))
}

function handleDownloadLogs() {
  const allText = allCaseLogs.value.flatMap(cl =>
    [`\n=== ${cl.caseName} ===\n`, ...cl.logs.map(l => `[${l.timestamp}] [${l.level}] ${l.message}`)]
  ).join('\n')
  const blob = new Blob([allText], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `execution-${executionId.value}-logs.txt`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('日志已下载')
}

function goBack() {
  router.push('/executions')
}
</script>

<style scoped lang="scss">
.execution-detail-container {
  // ========== Header ==========
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

  // ========== Stats Row ==========
  .stats-row {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;

    .stat-card {
      flex: 1;
      padding: 16px 20px;
      background: var(--el-bg-color);
      border: 1px solid var(--el-border-color-lighter);
      border-radius: 8px;
      position: relative;
      overflow: hidden;

      .stat-value {
        font-size: 28px;
        font-weight: 700;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 13px;
        color: var(--el-text-color-secondary);
        margin-top: 4px;
      }

      .stat-icon {
        position: absolute;
        right: 16px;
        top: 50%;
        transform: translateY(-50%);
        opacity: 0.15;
      }
    }
  }

  // ========== Tab Card ==========
  .tab-card {
    :deep(.el-tabs__content) {
      padding: 16px;
    }
  }

  // ========== Tab 1: Steps ==========
  .steps-tab-content {
    .case-header {
      display: flex;
      align-items: center;
      gap: 8px;
      width: 100%;

      .case-name-text {
        font-weight: 500;
        flex: 1;
      }

      .case-duration {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }

    .step-details-panel {
      padding: 8px 0;

      .step-screenshots-row {
        display: flex;
        gap: 10px;
        overflow-x: auto;
        padding-bottom: 16px;
        margin-bottom: 16px;
        border-bottom: 1px solid var(--el-border-color-lighter);
      }

      .step-thumb {
        flex-shrink: 0;
        cursor: pointer;
        text-align: center;
        transition: transform 0.2s;

        &:hover {
          transform: scale(1.05);
        }

        &.step-failed {
          .step-screenshot-placeholder {
            border: 2px solid #f56c6c;
          }
        }

        .step-screenshot-placeholder {
          width: 120px;
          height: 80px;
          border-radius: 6px;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          border: 1px solid var(--el-border-color);

          .step-num {
            font-size: 18px;
            font-weight: 700;
            color: rgba(255, 255, 255, 0.7);
          }

          .step-action-text {
            font-size: 10px;
            color: rgba(255, 255, 255, 0.5);
            margin-top: 2px;
          }
        }

        .step-thumb-status {
          margin-top: 4px;
        }
      }

      .step-info-panel {
        margin-top: 8px;
      }
    }
  }

  // ========== Tab 2: Report ==========
  .report-tab-content {
    .report-header {
      display: flex;
      align-items: center;
      gap: 24px;
      margin-bottom: 24px;

      .score-circle {
        width: 100px;
        height: 100px;
        border-radius: 50%;
        background: conic-gradient(var(--score-color) calc(var(--score, 78) * 1%), var(--el-fill-color) 0);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        position: relative;
        flex-shrink: 0;

        &::before {
          content: '';
          position: absolute;
          inset: 8px;
          border-radius: 50%;
          background: var(--el-bg-color);
        }

        .score-value {
          font-size: 28px;
          font-weight: 700;
          z-index: 1;
          color: var(--score-color);
        }

        .score-label {
          font-size: 11px;
          color: var(--el-text-color-secondary);
          z-index: 1;
        }
      }

      .report-title-area {
        h3 {
          margin: 0 0 6px 0;
          font-size: 18px;
        }

        .report-meta {
          font-size: 13px;
          color: var(--el-text-color-secondary);
          margin: 2px 0;
        }
      }
    }

    .report-summary-grid {
      margin-bottom: 8px;

      .summary-item {
        text-align: center;
        padding: 16px;
        background: var(--el-fill-color-light);
        border-radius: 8px;

        .summary-value {
          font-size: 24px;
          font-weight: 700;
        }

        .summary-label {
          font-size: 13px;
          color: var(--el-text-color-secondary);
          margin-top: 4px;
        }
      }
    }

    .chart-placeholder {
      border: 1px solid var(--el-border-color-lighter);
      border-radius: 8px;
      padding: 16px;

      .chart-title {
        font-size: 14px;
        font-weight: 600;
        margin-bottom: 12px;
      }

      .chart-body {
        display: flex;
        align-items: center;
        justify-content: center;
        min-height: 160px;
      }
    }

    .pie-chart-mock {
      width: 100px;
      height: 100px;
      border-radius: 50%;
      background: conic-gradient(
        #67c23a 0deg var(--pass-deg, 270deg),
        #f56c6c var(--pass-deg, 270deg) calc(var(--pass-deg, 270deg) + 54deg),
        #909399 calc(var(--pass-deg, 270deg) + 54deg) 360deg
      );
      margin-right: 24px;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        inset: 20px;
        border-radius: 50%;
        background: var(--el-bg-color);
      }
    }

    .pie-legend {
      display: flex;
      flex-direction: column;
      gap: 8px;
      font-size: 13px;

      .legend-dot {
        display: inline-block;
        width: 10px;
        height: 10px;
        border-radius: 50%;
        margin-right: 6px;
        vertical-align: middle;
      }
    }

    .trend-bars {
      display: flex;
      align-items: flex-end;
      gap: 8px;
      height: 140px;
      width: 100%;
      justify-content: center;
      padding-top: 20px;

      .trend-bar-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
        flex: 1;
        max-width: 40px;

        .trend-bar {
          width: 100%;
          min-height: 10px;
          border-radius: 4px 4px 0 0;
          position: relative;
          transition: height 0.3s;

          .trend-bar-label {
            position: absolute;
            top: -18px;
            left: 50%;
            transform: translateX(-50%);
            font-size: 10px;
            color: var(--el-text-color-regular);
            white-space: nowrap;
          }
        }

        .trend-date {
          font-size: 11px;
          color: var(--el-text-color-secondary);
        }
      }
    }

    .report-section {
      margin-top: 24px;

      h4 {
        font-size: 15px;
        font-weight: 600;
        margin: 0 0 12px 0;
        display: flex;
        align-items: center;
        gap: 6px;
      }
    }

    .ai-summary-box {
      padding: 16px 20px;
      background: var(--el-fill-color-light);
      border-radius: 8px;
      border-left: 4px solid var(--el-color-primary);
      font-size: 13px;
      line-height: 1.8;
      color: var(--el-text-color-regular);

      :deep(h4) {
        font-size: 14px;
        margin: 0 0 8px 0;
      }

      :deep(h5) {
        font-size: 13px;
      }

      :deep(code) {
        background: var(--el-fill-color);
        padding: 1px 4px;
        border-radius: 3px;
        font-size: 12px;
      }
    }

    .report-actions {
      margin-top: 24px;
      display: flex;
      gap: 8px;
      padding-top: 16px;
      border-top: 1px solid var(--el-border-color-lighter);
    }
  }

  // ========== Tab 3: Logs ==========
  .logs-tab-content {
    .log-actions-bar {
      margin-bottom: 12px;
    }

    .log-case-header {
      display: flex;
      align-items: center;
      gap: 8px;
      width: 100%;

      .log-case-name {
        font-weight: 500;
        flex: 1;
      }

      .log-case-duration {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }

      .log-count {
        font-size: 12px;
        color: var(--el-text-color-placeholder);
        background: var(--el-fill-color);
        padding: 1px 8px;
        border-radius: 10px;
      }
    }

    .log-viewer {
      background: #1e1e2e;
      border-radius: 8px;
      padding: 16px;
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
      font-size: 12px;
      line-height: 1.8;
      overflow-x: auto;
      max-height: 400px;
      overflow-y: auto;
    }

    .log-line {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      padding: 2px 0;

      .log-timestamp {
        color: #6c7086;
        white-space: nowrap;
        flex-shrink: 0;
      }

      .log-level-badge {
        flex-shrink: 0;
        font-family: 'Consolas', 'Monaco', monospace;
        min-width: 52px;
        text-align: center;
      }

      .log-message {
        color: #cdd6f4;
        word-break: break-all;
      }

      &.log-error .log-message {
        color: #f38ba8;
      }

      &.log-warn .log-message {
        color: #f9e2af;
      }

      &.log-pass .log-message {
        color: #a6e3a1;
      }

      &.log-debug .log-message {
        color: #89b4fa;
      }

      &.log-info .log-message {
        color: #cdd6f4;
      }
    }
  }

  // ========== Tab 4: Screenshots ==========
  .screenshots-tab-content {
    .comparison-card {
      border: 1px solid var(--el-border-color-lighter);
      border-radius: 8px;
      margin-bottom: 16px;
      overflow: hidden;

      .comparison-header {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 16px;
        background: var(--el-fill-color-light);
        border-bottom: 1px solid var(--el-border-color-lighter);

        .comparison-case-name {
          font-weight: 600;
          flex: 1;
        }

        .comparison-similarity {
          font-size: 13px;
          color: var(--el-text-color-secondary);
        }
      }

      .comparison-images {
        display: flex;
        gap: 16px;
        padding: 16px;

        .comparison-image-col {
          flex: 1;
          text-align: center;

          .comparison-label {
            font-size: 12px;
            color: var(--el-text-color-secondary);
            margin-bottom: 8px;
          }

          .comparison-image-placeholder {
            height: 160px;
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: center;
            border: 1px solid var(--el-border-color);

            span {
              font-size: 14px;
              color: rgba(255, 255, 255, 0.6);
              font-weight: 600;
            }

            &.diff {
              border-color: #f56c6c;
              border-style: dashed;
            }
          }
        }
      }
    }
  }

  // ========== Tab 5: Replay ==========
  .replay-tab-content {
    .video-player-placeholder {
      background: #1e1e2e;
      border-radius: 8px;
      height: 320px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      margin-bottom: 20px;

      .video-play-btn {
        width: 72px;
        height: 72px;
        border-radius: 50%;
        background: rgba(64, 158, 255, 0.2);
        border: 2px solid var(--el-color-primary);
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.3s;
        color: var(--el-color-primary);

        &:hover {
          background: rgba(64, 158, 255, 0.4);
          transform: scale(1.1);
        }
      }

      .video-player-label {
        color: #cdd6f4;
        font-size: 15px;
        margin-top: 12px;
        font-weight: 500;
      }

      .video-player-sub {
        color: #6c7086;
        font-size: 12px;
        margin-top: 4px;
      }
    }

    .video-grid-items {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
      gap: 12px;
    }

    .video-grid-item {
      border: 1px solid var(--el-border-color-lighter);
      border-radius: 6px;
      overflow: hidden;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        border-color: var(--el-color-primary);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }

      .video-thumb {
        height: 100px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: rgba(255, 255, 255, 0.6);

        .no-video-text {
          font-size: 12px;
          color: rgba(255, 255, 255, 0.3);
        }
      }

      .video-thumb-label {
        padding: 6px 10px;
        font-size: 12px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .video-thumb-badge {
        position: relative;
        margin: 0 10px 8px;
      }
    }

    .step-timeline {
      h4 {
        font-size: 15px;
        font-weight: 600;
        margin-bottom: 16px;
      }

      max-height: 600px;
      overflow-y: auto;

      .timeline-case-title {
        font-weight: 600;
        font-size: 13px;
        margin-bottom: 8px;
      }

      .timeline-step {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 4px 8px;
        border-radius: 4px;
        cursor: pointer;
        font-size: 12px;
        transition: background 0.2s;

        &:hover {
          background: var(--el-fill-color-light);
        }

        .timeline-step-text {
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .timeline-step-time {
          color: var(--el-text-color-secondary);
          flex-shrink: 0;
        }
      }
    }
  }

  // ========== Tab 6: AI Analysis ==========
  .ai-tab-content {
    .ai-section {
      margin-bottom: 28px;

      h3 {
        font-size: 16px;
        font-weight: 600;
        margin: 0 0 12px 0;
        display: flex;
        align-items: center;
        gap: 6px;
      }
    }

    .ai-summary-text {
      padding: 20px 24px;
      background: linear-gradient(135deg, var(--el-color-primary-light-9), var(--el-fill-color-light));
      border-radius: 8px;
      border-left: 4px solid var(--el-color-primary);
      font-size: 13px;
      line-height: 1.8;
      color: var(--el-text-color-regular);

      :deep(h4) { font-size: 14px; margin: 0 0 8px 0; }
      :deep(h5) { font-size: 13px; margin: 8px 0 4px 0; }
      :deep(code) { background: var(--el-fill-color); padding: 1px 4px; border-radius: 3px; font-size: 12px; }
      :deep(strong) { color: var(--el-text-color-primary); }
    }

    .failure-card {
      height: 100%;

      .failure-card-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .failure-card-title {
          font-weight: 600;
          font-size: 14px;
        }
      }

      .failure-card-desc {
        font-size: 13px;
        color: var(--el-text-color-regular);
        margin: 0 0 12px 0;
        line-height: 1.6;
      }

      .failure-card-meta {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        margin-bottom: 12px;
      }

      .failure-card-suggestion {
        display: flex;
        align-items: flex-start;
        gap: 6px;
        padding: 10px 12px;
        background: var(--el-color-info-light-9);
        border-radius: 6px;
        font-size: 12px;
        color: var(--el-text-color-regular);
        line-height: 1.5;

        .el-icon {
          margin-top: 2px;
          flex-shrink: 0;
        }
      }
    }

    .code-value {
      font-family: 'Consolas', 'Monaco', monospace;
      font-size: 12px;
      background: var(--el-fill-color);
      padding: 2px 6px;
      border-radius: 3px;

      &.code-actual {
        background: var(--el-color-danger-light-9);
        color: var(--el-color-danger);
      }
    }

    .ai-actions {
      display: flex;
      gap: 12px;
      padding-top: 20px;
      border-top: 1px solid var(--el-border-color-lighter);
    }
  }

  // ========== Shared ==========
  .error-text {
    color: var(--el-color-danger);
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 12px;
  }
}
</style>
