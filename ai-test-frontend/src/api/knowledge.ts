import request from '@/utils/request'

// ===================== Types / Interfaces =====================

export type CategoryType = 'CASE' | 'REQUIREMENT' | 'DESIGN' | 'GIT' | 'DEFECT' | 'DATABASE' | 'VIDEO'
export type DocType = 'PDF' | 'WORD' | 'MARKDOWN' | 'EXCEL' | 'IMAGE' | 'VIDEO'
export type ParseStatus = 'PENDING' | 'PARSING' | 'DONE' | 'FAILED'
export type ChunkContentType = 'TEXT' | 'TABLE' | 'IMAGE' | 'FLOWCHART'
export type VideoType = 'RECORD' | 'EXECUTION'

export interface KbCategory {
  id?: number
  categoryName: string
  categoryType: CategoryType
  systemId?: number | null
  parentId: number
  sortOrder?: number
  children?: KbCategory[]
  createdAt?: string
}

export interface KbDocument {
  id?: number
  docCode?: string
  docName: string
  categoryId: number
  categoryName?: string
  systemId?: number | null
  systemName?: string
  docType: DocType
  filePath?: string
  fileSize?: number
  content?: string
  metadata?: Record<string, any>
  parseStatus: ParseStatus
  parseResult?: Record<string, any>
  createdBy?: number
  createdAt?: string
  updatedAt?: string
}

export interface KbChunk {
  id?: number
  documentId?: number
  chunkIndex: number
  content: string
  contentType: ChunkContentType
  metadata?: Record<string, any>
  createdAt?: string
}

export interface KbVideo {
  id?: number
  videoName: string
  videoType: VideoType
  systemId?: number | null
  systemName?: string
  caseId?: number
  executionId?: number
  filePath: string
  fileSize?: number
  durationMs?: number
  resolution?: string
  thumbnailPath?: string
  executionStatus?: string
  description?: string
  createdAt?: string
}

export interface DocumentQuery {
  page?: number
  pageSize?: number
  keyword?: string
  categoryId?: number | ''
  systemId?: number | ''
  docType?: DocType | ''
  parseStatus?: ParseStatus | ''
}

// ===================== Category APIs =====================

export function getCategoryTree(params?: { systemId?: number }) {
  return request.get('/knowledge/categories', { params })
}

export function createCategory(data: KbCategory) {
  return request.post('/knowledge/categories', data)
}

export function updateCategory(id: number, data: KbCategory) {
  return request.put(`/knowledge/categories/${id}`, data)
}

export function deleteCategory(id: number) {
  return request.delete(`/knowledge/categories/${id}`)
}

// ===================== Document APIs =====================

export function getDocumentList(params?: DocumentQuery) {
  return request.get('/knowledge/documents', { params })
}

export function getDocumentDetail(id: number) {
  return request.get(`/knowledge/documents/${id}`)
}

export function uploadDocument(formData: FormData) {
  return request.post('/knowledge/documents/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function parseDocument(id: number) {
  return request.post(`/knowledge/documents/${id}/parse`)
}

export function deleteDocument(id: number) {
  return request.delete(`/knowledge/documents/${id}`)
}

export function searchKnowledge(params: { query: string; categoryId?: number; systemId?: number }) {
  return request.get('/knowledge/documents/search', { params })
}

// ===================== Video APIs =====================

export function getVideoList(params?: { page?: number; pageSize?: number; systemId?: number; videoType?: string }) {
  return request.get('/knowledge/videos', { params })
}

export function createVideo(data: KbVideo) {
  return request.post('/knowledge/videos', data)
}

export function deleteVideo(id: number) {
  return request.delete(`/knowledge/videos/${id}`)
}

// ===================== System Options (reuse) =====================

export function getSystemOptions() {
  return request.get('/systems/options')
}
