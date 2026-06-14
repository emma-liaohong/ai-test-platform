import request from '@/utils/request'

export interface DashboardStats {
  caseCount: number
  suiteCount: number
  defectCount: number
  skillCount: number
  conversationCount: number
}

export function getDashboardStats() {
  return request.get<any, { code: number; data: DashboardStats }>('/dashboard/stats')
}
