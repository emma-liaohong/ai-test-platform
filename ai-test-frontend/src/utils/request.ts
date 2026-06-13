import axios, { type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

// Request interceptor
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    // If response has a code field, check for errors
    if (res.code !== undefined) {
      if (res.code !== 200 && res.code !== 0) {
        ElMessage.error(res.message || '请求失败')
        // 401: Token expired or invalid
        if (res.code === 401) {
          removeToken()
          router.push('/login')
        }
        return Promise.reject(new Error(res.message || '请求失败'))
      }
      return res
    }
    // If no code field, return data directly
    return res
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          removeToken()
          router.push('/login')
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default service
