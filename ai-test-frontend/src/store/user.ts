import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { loginApi, logoutApi, getUserInfoApi } from '@/api/auth'
import router from '@/router'

export interface UserInfo {
  id: number
  username: string
  realName: string
  email: string
  avatar: string
  role: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<UserInfo | null>(null)

  async function login(username: string, password: string) {
    const res: any = await loginApi({ username, password })
    const data = res.data || res
    const tokenValue = data.token
    token.value = tokenValue
    setToken(tokenValue)
    // Store userInfo from login response
    if (data.userInfo) {
      userInfo.value = data.userInfo
    }
    return res
  }

  async function getInfo() {
    const res: any = await getUserInfoApi()
    userInfo.value = res.data || res
    return userInfo.value
  }

  async function logout() {
    try {
      await logoutApi()
    } catch {
      // ignore
    } finally {
      token.value = null
      userInfo.value = null
      removeToken()
      router.push('/login')
    }
  }

  function resetState() {
    token.value = null
    userInfo.value = null
    removeToken()
  }

  return {
    token,
    userInfo,
    login,
    getInfo,
    logout,
    resetState,
  }
})
