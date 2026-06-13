import request from '@/utils/request'

// ==================== Types ====================

/** 技能类型 */
export type SkillType = 'BUILTIN' | 'CUSTOM' | 'LEARNED'

/** 技能分类 */
export type SkillCategory = 'TESTING' | 'ANALYSIS' | 'GENERATION' | 'EXECUTION'

/** 执行器类型 */
export type ExecutorType = 'LLM' | 'CODE' | 'API' | 'WORKFLOW'

/** 技能状态 */
export type SkillStatus = 'ACTIVE' | 'DISABLED' | 'TESTING'

/** 经验类型 */
export type ExperienceType = 'SUCCESS_CASE' | 'FAILED_CASE' | 'PATTERN' | 'INSIGHT'

/** AI 技能 */
export interface AiSkill {
  id?: number
  skillCode: string
  skillName: string
  skillType: SkillType
  category?: SkillCategory
  description?: string
  triggerIntent?: string
  triggerKeywords?: string
  inputSchema?: string
  outputSchema?: string
  executorType?: ExecutorType
  executorConfig?: string
  promptTemplate?: string
  scriptContent?: string
  sourceType?: string
  learnedFrom?: number
  version?: number
  confidence?: number
  usageCount?: number
  successCount?: number
  status?: SkillStatus
  createdAt?: string
  updatedAt?: string
}

/** 技能执行日志 */
export interface SkillExecutionLog {
  id?: number
  skillId: number
  skillName?: string
  inputParams?: string
  outputResult?: string
  status?: string
  durationMs?: number
  errorMessage?: string
  userFeedback?: string
  createdAt?: string
}

/** AI 经验 */
export interface AiExperience {
  id?: number
  experienceType: ExperienceType
  title: string
  context?: string
  content?: string
  relatedSkillId?: number
  relatedCaseId?: number
  tags?: string
  isPromoted?: boolean
  promotedSkillId?: number
  createdAt?: string
}

/** 技能查询参数 */
export interface SkillQuery {
  page?: number
  size?: number
  keyword?: string
  skillType?: SkillType | ''
  category?: SkillCategory | ''
  executorType?: ExecutorType | ''
  status?: SkillStatus | ''
}

/** 经验查询参数 */
export interface ExperienceQuery {
  page?: number
  size?: number
  keyword?: string
  experienceType?: ExperienceType | ''
  isPromoted?: boolean | ''
}

// ==================== Skill APIs ====================

/** 获取技能列表 */
export function getSkillList(params?: SkillQuery) {
  return request.get('/skills', { params })
}

/** 获取技能详情 */
export function getSkillDetail(id: number) {
  return request.get(`/skills/${id}`)
}

/** 创建技能 */
export function createSkill(data: AiSkill) {
  return request.post('/skills', data)
}

/** 更新技能 */
export function updateSkill(id: number, data: AiSkill) {
  return request.put(`/skills/${id}`, data)
}

/** 删除技能 */
export function deleteSkill(id: number) {
  return request.delete(`/skills/${id}`)
}

/** 执行技能 */
export function invokeSkill(data: { skillId?: number; skillCode?: string; inputParams: Record<string, any> }) {
  return request.post('/skills/invoke', data)
}

/** 意图匹配 */
export function matchIntent(query: string) {
  return request.get('/skills/intent', { params: { query } })
}

/** 获取所有激活技能 */
export function getActiveSkills() {
  return request.get('/skills/active')
}

/** 获取技能执行日志 */
export function getSkillLogs(skillId: number, params?: { page?: number; size?: number }) {
  return request.get(`/skills/${skillId}/logs`, { params })
}

/** 提交执行反馈 */
export function submitFeedback(logId: number, feedback: string) {
  return request.post(`/skills/logs/${logId}/feedback`, { feedback })
}

// ==================== Experience APIs ====================

/** 获取经验列表 */
export function getExperienceList(params?: ExperienceQuery) {
  return request.get('/experiences', { params })
}

/** 创建经验 */
export function createExperience(data: AiExperience) {
  return request.post('/experiences', data)
}

/** 提升经验为技能 */
export function promoteExperience(id: number) {
  return request.post(`/experiences/${id}/promote`)
}

/** 删除经验 */
export function deleteExperience(id: number) {
  return request.delete(`/experiences/${id}`)
}
