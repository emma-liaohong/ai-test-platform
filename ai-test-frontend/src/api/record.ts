import request from '@/utils/request'

// ===================== Types / Interfaces =====================

/** 录制会话状态 */
export type RecordStatus = 'RECORDING' | 'STOPPED' | 'GENERATED'

/** 操作步骤类型 */
export type StepAction = 'click' | 'input' | 'select' | 'swipe' | 'wait' | 'assert' | 'hover' | 'scroll'

/** 录制操作步骤 */
export interface RecordStep {
  stepOrder: number
  action: StepAction | string
  target: string
  value?: string
  description?: string
  screenshot?: string
  timestamp?: number
}

/** 录制会话 */
export interface RecordSession {
  id?: number
  sessionId?: string
  sessionName: string
  systemId?: number
  systemName?: string
  targetUrl?: string
  status?: RecordStatus
  steps?: RecordStep[]
  stepCount?: number
  videoPath?: string
  durationMs?: number
  generatedCaseId?: number
  createdBy?: number
  startedAt?: string
  stoppedAt?: string
  createdAt?: string
}

/** 查询参数 */
export interface RecordSessionQuery {
  page?: number
  size?: number
  keyword?: string
  systemId?: number | ''
  status?: RecordStatus | ''
}

// ===================== Record Session APIs =====================

export function getSessionList(params?: RecordSessionQuery) {
  return request.get('/record/sessions', { params })
}

export function getSessionDetail(id: number) {
  return request.get(`/record/sessions/${id}`)
}

export function startSession(data: { sessionName: string; systemId?: number; targetUrl?: string }) {
  return request.post('/record/sessions/start', data)
}

export function stopSession(id: number) {
  return request.post(`/record/sessions/${id}/stop`)
}

export function addStep(id: number, step: RecordStep) {
  return request.post(`/record/sessions/${id}/steps`, step)
}

export function deleteSession(id: number) {
  return request.delete(`/record/sessions/${id}`)
}

export function generateCase(id: number) {
  return request.post(`/record/sessions/${id}/generate`)
}

export function getSystemOptions() {
  return request.get('/system/systems/all')
}
