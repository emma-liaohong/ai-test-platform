import request from '@/utils/request'

// ===================== Types / Interfaces =====================

/** 缺陷严重等级 */
export type Severity = 'S1' | 'S2' | 'S3' | 'S4'

/** 缺陷优先级 */
export type Priority = 'P0' | 'P1' | 'P2' | 'P3'

/** 缺陷状态 */
export type DefectStatus = 'OPEN' | 'FIXED' | 'VERIFIED' | 'CLOSED' | 'REOPENED'

/** 风险等级 */
export type RiskLevel = 'HIGH' | 'MEDIUM' | 'LOW'

/** 缺陷 */
export interface Defect {
  id?: number
  defectCode?: string
  defectTitle: string
  systemId: number
  systemName?: string
  severity?: Severity
  priority?: Priority
  status?: DefectStatus
  relatedCaseId?: number
  relatedExecutionId?: number
  description?: string
  stepsToReproduce?: string
  expectedResult?: string
  actualResult?: string
  environment?: string
  screenshots?: string
  assignedTo?: number
  assignedToName?: string
  createdBy?: number
  createdAt?: string
  updatedAt?: string
}

/** 风险分析 */
export interface RiskAnalysis {
  id?: number
  systemId: number
  systemName?: string
  analysisType: string
  title: string
  riskLevel?: RiskLevel
  riskItems?: string  // JSON string
  analysisResult?: string
  suggestions?: string
  aiAnalysis?: string
  createdBy?: number
  createdAt?: string
}

/** 风险项 */
export interface RiskItem {
  level: 'HIGH' | 'MEDIUM' | 'LOW'
  title: string
  impact: string
  suggestion: string
}

/** 缺陷统计 */
export interface DefectStatistics {
  totalCount: number
  bySeverity: Record<string, number>
  byStatus: Record<string, number>
  byPriority: Record<string, number>
}

/** 缺陷查询参数 */
export interface DefectQuery {
  page?: number
  size?: number
  keyword?: string
  systemId?: number | ''
  severity?: Severity | ''
  priority?: Priority | ''
  status?: DefectStatus | ''
}

/** 风险分析查询参数 */
export interface RiskAnalysisQuery {
  page?: number
  size?: number
  systemId?: number | ''
  analysisType?: string
}

// ===================== Defect CRUD APIs =====================

export function getDefectList(params?: DefectQuery) {
  return request.get('/defects', { params })
}

export function getDefectDetail(id: number) {
  return request.get(`/defects/${id}`)
}

export function createDefect(data: Defect) {
  return request.post('/defects', data)
}

export function updateDefect(id: number, data: Defect) {
  return request.put(`/defects/${id}`, data)
}

export function deleteDefect(id: number) {
  return request.delete(`/defects/${id}`)
}

export function updateDefectStatus(id: number, status: string) {
  return request.put(`/defects/${id}/status`, { status })
}

export function getDefectStatistics(params?: { systemId?: number }) {
  return request.get('/defects/statistics', { params })
}

// ===================== Risk Analysis APIs =====================

export function getRiskAnalysisList(params?: RiskAnalysisQuery) {
  return request.get('/risk-analysis', { params })
}

export function getRiskAnalysisDetail(id: number) {
  return request.get(`/risk-analysis/${id}`)
}

export function createRiskAnalysis(data: RiskAnalysis) {
  return request.post('/risk-analysis', data)
}

export function deleteRiskAnalysis(id: number) {
  return request.delete(`/risk-analysis/${id}`)
}

// ===================== System (for dropdowns) =====================

export function getSystemOptions() {
  return request.get('/system/systems/all')
}
