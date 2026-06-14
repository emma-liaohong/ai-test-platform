import request from '@/utils/request'

export interface User {
  id?: number
  username: string
  password?: string
  nickname: string
  email?: string
  phone?: string
  avatar?: string
  roles?: string[]
  status?: number
  createdAt?: string
  updatedAt?: string
}

export interface UserQuery {
  page?: number
  size?: number
  keyword?: string
  status?: number
}

export function getUserList(params?: UserQuery) {
  return request.get('/system/users', { params })
}

export function getUserById(id: number) {
  return request.get(`/system/users/${id}`)
}

export function createUser(data: User) {
  return request.post('/system/users', data)
}

export function updateUser(id: number, data: User) {
  return request.put(`/system/users/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete(`/system/users/${id}`)
}

export function resetUserPassword(id: number, password: string) {
  return request.post(`/system/users/${id}/reset-password`, { password })
}
