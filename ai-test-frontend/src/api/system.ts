import request from '@/utils/request'

export interface System {
  id?: number
  name: string
  code: string
  baseUrl: string
  description?: string
  status?: number
  createdAt?: string
  updatedAt?: string
}

export interface SystemQuery {
  page?: number
  pageSize?: number
  keyword?: string
}

export function getSystemList(params?: SystemQuery) {
  return request.get('/systems', { params })
}

export function getSystemById(id: number) {
  return request.get(`/systems/${id}`)
}

export function createSystem(data: System) {
  return request.post('/systems', data)
}

export function updateSystem(id: number, data: System) {
  return request.put(`/systems/${id}`, data)
}

export function deleteSystem(id: number) {
  return request.delete(`/systems/${id}`)
}
