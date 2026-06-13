import request from '@/utils/request'

// ===================== Types / Interfaces =====================

export interface FeaturePoint {
  featureName: string
  description: string
  featureType: 'NEW' | 'MODIFIED' | 'REMOVED'
  businessRules: string[]
  relatedModules: string[]
  priority: string
}

export interface ImpactItem {
  affectedModule: string
  impactLevel: 'HIGH' | 'MEDIUM' | 'LOW'
  description: string
  affectedCaseIds: number[]
  suggestion: string
}

export interface GeneratedCase {
  caseName: string
  caseType: string
  priority: string
  preconditions: string
  steps: Array<Record<string, any>>
  expectedResult: string
  sourceFeature: string
}

export interface AnalysisResult {
  documentId: number
  documentName: string
  systemId?: number
  analysisType: string
  features: FeaturePoint[]
  impactAnalysis: ImpactItem[]
  generatedCases: GeneratedCase[]
  createdCaseIds: number[]
  summary: string
  aiAnalysis: string
  analyzedAt: string
}

export interface AnalysisRequest {
  documentId: number
  systemId?: number
  analysisType?: string
  generateCases?: boolean
  casePriority?: string
}

// ===================== Analysis APIs =====================

export function analyzeDocument(data: AnalysisRequest) {
  return request.post('/analysis/analyze', data)
}

export function getAnalysisResult(documentId: number) {
  return request.get(`/analysis/result/${documentId}`)
}

export function generateCasesFromAnalysis(documentId: number, systemId: number) {
  return request.post('/analysis/generate-cases', null, {
    params: { documentId, systemId },
  })
}

// ===================== Reuse Knowledge APIs =====================

export function getDocumentList(params?: {
  page?: number
  size?: number
  categoryId?: number
  docType?: string
}) {
  return request.get('/knowledge/documents', { params })
}

export function getSystemOptions() {
  return request.get('/systems/options')
}
