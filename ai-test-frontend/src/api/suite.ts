import request from '@/utils/request'

// ===================== Types / Interfaces =====================

/** 套件类型 */
export type SuiteType = 'smoke' | 'regression' | 'full' | 'custom'

/** 套件状态 */
export type SuiteStatus = 'active' | 'disabled'

/** 执行状态 */
export type ExecutionStatus = 'running' | 'success' | 'failed' | 'cancelled' | 'pending'

/** 触发方式 */
export type TriggerType = 'manual' | 'scheduled' | 'ci'

/** 用例执行状态 */
export type CaseExecStatus = 'passed' | 'failed' | 'skipped' | 'running' | 'pending' | 'error'

/** 测试套件 */
export interface TestSuite {
  id?: number
  suiteName: string
  systemId?: number | null
  systemName?: string
  suiteType: SuiteType
  description?: string
  caseCount?: number
  status?: SuiteStatus
  lastExecutionId?: number
  lastExecutionTime?: string
  passRate?: number
  createdBy?: string
  createdAt?: string
  updatedAt?: string
}

/** 套件关联用例 */
export interface TestSuiteCase {
  id?: number
  suiteId?: number
  caseId: number
  caseName?: string
  caseType?: string
  priority?: string
  sortOrder: number
  addedAt?: string
}

/** 测试执行 */
export interface TestExecution {
  id?: number
  executionName: string
  suiteId?: number
  suiteName?: string
  systemId?: number
  systemName?: string
  triggerType: TriggerType
  status: ExecutionStatus
  totalCases: number
  passedCases: number
  failedCases: number
  skippedCases: number
  passRate: number
  startedAt: string
  finishedAt?: string
  duration?: number
  durationText?: string
  executedBy?: string
  environment?: string
  createdAt?: string
}

/** 执行用例详情 */
export interface TestExecutionDetail {
  id?: number
  executionId?: number
  caseId: number
  caseName: string
  caseType?: string
  priority?: string
  status: CaseExecStatus
  duration?: number
  durationText?: string
  startedAt?: string
  finishedAt?: string
  steps: ExecutionStep[]
  errorMessage?: string
  videoUrl?: string
  hasVideo?: boolean
  logContent?: string
}

/** 执行步骤 */
export interface ExecutionStep {
  stepOrder: number
  stepAction: string
  stepTarget?: string
  stepValue?: string
  stepDescription: string
  status: CaseExecStatus
  duration?: number
  screenshot?: string
  expected?: string
  actual?: string
  errorMessage?: string
  timestamp?: string
}

/** 执行报告 */
export interface ExecutionReport {
  executionId: number
  executionName: string
  suiteName?: string
  score: number
  summary: {
    totalCases: number
    passedCases: number
    failedCases: number
    skippedCases: number
    duration: number
    durationText: string
    passRate: number
    startedAt: string
    finishedAt: string
  }
  moduleStats: ModuleStat[]
  priorityStats: PriorityStat[]
  failedCases: FailedCaseDetail[]
  aiSummary?: string
  trendData?: TrendPoint[]
}

export interface ModuleStat {
  moduleName: string
  total: number
  passed: number
  failed: number
  passRate: number
}

export interface PriorityStat {
  priority: string
  total: number
  passed: number
  failed: number
  passRate: number
}

export interface FailedCaseDetail {
  caseId: number
  caseName: string
  priority: string
  module: string
  errorMessage: string
  duration: number
  durationText: string
}

export interface TrendPoint {
  date: string
  passRate: number
  totalCases: number
}

/** 截图对比结果 */
export interface ScreenshotComparison {
  id: number
  caseId: number
  caseName: string
  baselineUrl: string
  actualUrl: string
  diffUrl?: string
  matchStatus: 'match' | 'diff'
  similarity: number
}

/** 日志条目 */
export interface LogEntry {
  timestamp: string
  level: 'INFO' | 'PASS' | 'WARN' | 'ERROR' | 'DEBUG'
  message: string
}

/** 用例执行日志 */
export interface CaseExecutionLog {
  caseId: number
  caseName: string
  status: CaseExecStatus
  duration: number
  durationText: string
  logs: LogEntry[]
}

/** 套件查询参数 */
export interface SuiteQuery {
  page?: number
  pageSize?: number
  keyword?: string
  systemId?: number | ''
  suiteType?: SuiteType | ''
  status?: SuiteStatus | ''
}

/** 执行查询参数 */
export interface ExecutionQuery {
  page?: number
  pageSize?: number
  keyword?: string
  suiteId?: number | ''
  systemId?: number | ''
  status?: ExecutionStatus | ''
  triggerType?: TriggerType | ''
}

// ===================== Suite CRUD APIs =====================

export function getSuiteList(params?: SuiteQuery) {
  return request.get('/suites', { params })
}

export function getSuiteDetail(id: number) {
  return request.get(`/suites/${id}`)
}

export function createSuite(data: TestSuite) {
  return request.post('/suites', data)
}

export function updateSuite(id: number, data: TestSuite) {
  return request.put(`/suites/${id}`, data)
}

export function deleteSuite(id: number) {
  return request.delete(`/suites/${id}`)
}

// ===================== Suite Case Management =====================

export function getSuiteCases(suiteId: number) {
  return request.get(`/suites/${suiteId}/cases`)
}

export function addCasesToSuite(suiteId: number, caseIds: number[]) {
  return request.post(`/suites/${suiteId}/cases`, { caseIds })
}

export function removeCasesFromSuite(suiteId: number, caseIds: number[]) {
  return request.delete(`/suites/${suiteId}/cases`, { data: { caseIds } })
}

export function reorderSuiteCases(suiteId: number, caseOrders: { caseId: number; sortOrder: number }[]) {
  return request.put(`/suites/${suiteId}/cases/reorder`, { caseOrders })
}

// ===================== Execution APIs =====================

export function executeSuite(suiteId: number) {
  return request.post(`/suites/${suiteId}/execute`)
}

export function getExecutionList(params?: ExecutionQuery) {
  return request.get('/executions', { params })
}

export function getExecutionDetail(id: number) {
  return request.get(`/executions/${id}`)
}

export function getExecutionReport(id: number) {
  return request.get(`/executions/${id}/report`)
}

export function getCaseExecutionLog(executionId: number, caseId: number) {
  return request.get(`/executions/${executionId}/cases/${caseId}/log`)
}

export function cancelExecution(id: number) {
  return request.post(`/executions/${id}/cancel`)
}

// ===================== Screenshot Comparison APIs =====================

export function getScreenshotComparisons(executionId: number) {
  return request.get(`/executions/${executionId}/comparisons`)
}

// ===================== AI Analysis APIs =====================

export function getAiAnalysis(executionId: number) {
  return request.get(`/executions/${executionId}/ai-analysis`)
}

// ===================== System (for dropdowns) =====================

export function getSystemOptions() {
  return request.get('/systems/options')
}
