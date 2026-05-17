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
      token.value = res.token || 'mock-token-huafu'
      userInfo.value = res.user || { username: loginForm.username, name: loginForm.username }
      localStorage.setItem('token', token.value)
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      return true
    } catch (e) {
      // Mock login for demo
      if (loginForm.username && loginForm.password) {
        token.value = 'mock-token-huafu'
        userInfo.value = { username: loginForm.username, name: loginForm.username }
        localStorage.setItem('token', token.value)
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        return true
      }
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
