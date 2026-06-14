import request from '@/utils/request'

// ===================== Types / Interfaces =====================

/** 测试用例类型 */
export type CaseType = 'PC' | 'APP' | 'API'

/** 优先级 */
export type Priority = 'P0' | 'P1' | 'P2' | 'P3'

/** 用例状态 */
export type CaseStatus = 'draft' | 'ready' | 'archived'

/** 执行步骤动作 */
export type StepAction = 'click' | 'input' | 'select' | 'swipe' | 'wait' | 'assert' | 'hover' | 'scroll'

/** HTTP 方法 */
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | 'HEAD' | 'OPTIONS'

/** 测试用例步骤 */
export interface TestCaseStep {
  id?: number
  caseId?: number
  stepOrder: number
  stepAction: StepAction | string
  stepTarget: string
  stepValue?: string
  stepDescription?: string
  expectedResult?: string
  screenshot?: string
}

/** API 断言规则 */
export interface AssertionRule {
  id?: number
  caseId?: number
  ruleOrder: number
  field: string
  operator: string
  expectedValue: string
  description?: string
}

/** 测试数据表 */
export interface DataTable {
  id?: number
  caseId?: number
  name: string
  description?: string
  columns: DataTableColumn[]
  rows: DataTableRow[]
  createdAt?: string
  updatedAt?: string
}

/** 数据表列定义 */
export interface DataTableColumn {
  key: string
  label: string
  type?: 'string' | 'number' | 'boolean' | 'json'
  defaultValue?: string
}

/** 数据表行 */
export interface DataTableRow {
  id?: number
  tableId?: number
  rowOrder: number
  data: Record<string, any>
  enabled?: boolean
}

/** 测试用例 */
export interface TestCase {
  id?: number
  caseCode?: string
  caseName: string
  caseType: CaseType
  systemId?: number | null
  systemName?: string
  modulePath?: string[]
  priority: Priority
  caseLevel?: string
  tags?: string[]
  preconditions?: string
  expectedResult?: string
  status?: CaseStatus
  createdBy?: string
  createdAt?: string
  updatedAt?: string

  // PC case fields
  steps?: TestCaseStep[]
  naturalLanguageSteps?: string
  playwrightScript?: string

  // API case fields
  apiMethod?: HttpMethod
  apiUrl?: string
  apiHeaders?: string
  apiRequestSchema?: string
  apiResponseSchema?: string
  assertionRules?: AssertionRule[]

  // Data driven
  dataDrivenEnabled?: boolean
  dataTable?: DataTable

  // APP case fields (placeholder)
  appConfig?: Record<string, any>
}

/** 查询参数 */
export interface CaseQuery {
  page?: number
  size?: number
  keyword?: string
  caseType?: CaseType | ''
  systemId?: number | ''
  priority?: Priority | ''
  status?: CaseStatus | ''
}

// ===================== Case CRUD APIs =====================

export function getCaseList(params?: CaseQuery) {
  return request.get('/cases', { params })
}

export function getCaseDetail(id: number) {
  return request.get(`/cases/${id}`)
}

export function createCase(data: TestCase) {
  return request.post('/cases', data)
}

export function updateCase(id: number, data: TestCase) {
  return request.put(`/cases/${id}`, data)
}

export function deleteCase(id: number) {
  return request.delete(`/cases/${id}`)
}

export function batchDeleteCases(ids: number[]) {
  return request.post('/cases/batch-delete', { ids })
}

export function executeCase(id: number) {
  return request.post(`/cases/${id}/execute`)
}

// ===================== System (for dropdowns) =====================

export function getSystemOptions() {
  return request.get('/system/systems/all')
}

// ===================== Data Table APIs =====================

export function getDataTables(caseId: number) {
  return request.get(`/cases/${caseId}/data-tables`)
}

export function getDataTable(tableId: number) {
  return request.get(`/data-tables/${tableId}`)
}

export function createDataTable(caseId: number, data: DataTable) {
  return request.post(`/cases/${caseId}/data-tables`, data)
}

export function updateDataTable(tableId: number, data: DataTable) {
  return request.put(`/data-tables/${tableId}`, data)
}

export function deleteDataTable(tableId: number) {
  return request.delete(`/data-tables/${tableId}`)
}

export function getDataTableRows(tableId: number) {
  return request.get(`/data-tables/${tableId}/rows`)
}

export function addDataTableRow(tableId: number, data: DataTableRow) {
  return request.post(`/data-tables/${tableId}/rows`, data)
}

export function updateDataTableRow(tableId: number, rowId: number, data: DataTableRow) {
  return request.put(`/data-tables/${tableId}/rows/${rowId}`, data)
}

export function deleteDataTableRow(tableId: number, rowId: number) {
  return request.delete(`/data-tables/${tableId}/rows/${rowId}`)
}

export function batchSaveDataTableRows(tableId: number, rows: DataTableRow[]) {
  return request.post(`/data-tables/${tableId}/rows/batch`, { rows })
}
