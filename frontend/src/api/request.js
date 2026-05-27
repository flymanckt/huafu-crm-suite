import axios from 'axios'
import { ElMessage } from 'element-plus'

const clearLoginSession = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('userId')
  localStorage.removeItem('userName')
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  // 关键：阻止 axios 默认 JSON.parse 把大整数转成 JS Number 导致精度丢失
  // BigInt JSON 序列化支持（PostgreSQL int8 → Java Long → JSON "123" → 保持字符串）
  transformResponse: [
    (data) => {
      if (!data || data === '') return data
      try {
        // 使用 BigInt 感知的 JSON 解析器，保持所有超过 MAX_SAFE_INTEGER 的数字为字符串
        return JSON.parse(data, (key, value) => {
          if (typeof value === 'string') {
            // 检测是否是高精度数字字符串（19位+，用于 id 类字段）
            if (/^\d{16,}$/.test(value)) {
              return value // 保持为字符串，不用 Number()
            }
          }
          return value
        })
      } catch (e) {
        return data
      }
    }
  ]
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    // Gateway HTTP-level 401
    if (response.status === 401) {
      clearLoginSession()
      window.location.href = '/login'
      return Promise.reject(new Error('未登录或登录已过期'))
    }
    // Business code error
    if (res.code !== 200) {
      // 401 business code → also logout
      if (res.code === 401) {
        clearLoginSession()
        window.location.href = '/login'
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  error => {
    if (error.response && error.response.status === 401) {
      clearLoginSession()
      window.location.href = '/login'
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
