import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value.username || userInfo.value.name || '')

  const persistLogin = (loginToken, user) => {
    token.value = loginToken
    userInfo.value = user || {}
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    if (userInfo.value.id) localStorage.setItem('userId', String(userInfo.value.id))
    localStorage.setItem('userName', userInfo.value.realName || userInfo.value.username || userInfo.value.name || '')
  }

  async function login(loginForm) {
    try {
      const res = await api.post('/auth/login', loginForm)
      if (!res.token) {
        throw new Error('登录接口未返回有效令牌')
      }
      persistLogin(res.token, res.user || { username: loginForm.username, name: loginForm.username })
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
    localStorage.removeItem('userId')
    localStorage.removeItem('userName')
  }

  return { token, userInfo, isLoggedIn, username, login, logout }
})
