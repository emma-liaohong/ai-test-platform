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
  size?: number
  keyword?: string
}

export function getSystemList(params?: SystemQuery) {
  // Map frontend params to backend params
  const backendParams = {
    current: params?.page || 1,
    size: params?.size || 10,
    name: params?.keyword || undefined,
  };
  return request.get('/system/systems', { params: backendParams });
}

export function getSystemById(id: number) {
  return request.get(`/system/systems/${id}`)
}

export function createSystem(data: System) {
  return request.post('/system/systems', data)
}

export function updateSystem(id: number, data: System) {
  return request.put(`/system/systems/${id}`, data)
}

export function deleteSystem(id: number) {
  return request.delete(`/system/systems/${id}`)
}
