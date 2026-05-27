<template>
  <div class="login-page">
    <div class="login-card">
      <h2>华孚时尚 CRM</h2>
      <el-form :model="form" @submit.prevent="handleLogin" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-button type="primary" style="width:100%" :loading="loading" native-type="submit">登 录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const router = useRouter()
const loading = ref(false)
const form = ref({ username: '', password: '' })

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await request.post('/auth/login', {
      username: form.value.username,
      password: form.value.password
    })
    // res 是 LoginVO { token, user }
    localStorage.setItem('token', res.token)
    const user = res.user || { username: form.value.username }
    const displayName = user.realName || user.username || form.value.username
    localStorage.setItem('userInfo', JSON.stringify(user))
    if (user.id) localStorage.setItem('userId', String(user.id))
    localStorage.setItem('userName', displayName)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    // 错误已由 request interceptor 处理，此处不重复弹窗
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #1d1e2c 0%, #2d3a5c 100%); }
.login-card { background: #fff; padding: 40px; border-radius: 12px; width: 380px; box-shadow: 0 8px 32px rgba(0,0,0,0.15); }
.login-card h2 { text-align: center; margin-bottom: 30px; color: #303133; }
</style>
