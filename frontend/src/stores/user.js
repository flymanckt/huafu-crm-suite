import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value.username || userInfo.value.name || '')

  async function login(loginForm) {
    try {
      const res = await api.post('/admin/login', loginForm)
      if (!res.token) {
        throw new Error('登录接口未返回有效令牌')
      }
      token.value = res.token
      userInfo.value = res.user || { username: loginForm.username, name: loginForm.username }
      localStorage.setItem('token', token.value)
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      return true
    } catch {
      logout()
      return false
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, isLoggedIn, username, login, logout }
})
