<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-logo" @click="$router.push('/')">
        <el-icon :size="30" color="#409eff"><DataLine /></el-icon>
        <h2>研数 UniData</h2>
      </div>
        <p class="text-muted auth-sub">全站分数线数据当前免费开放，登录后可收藏与管理个人数据</p>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="密码" :prefix-icon="Lock" />
        </el-form-item>
        <el-button type="primary" class="auth-btn" size="large" :loading="loading" @click="submit">登 录</el-button>
      </el-form>
      <div class="auth-footer">
        <span class="text-muted">还没有账号？</span>
        <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
      </div>
      <el-divider>演示账号</el-divider>
      <div class="demo-accounts">
        <el-button size="small" round @click="fill('admin', 'admin123')">管理员 admin</el-button>
        <el-button size="small" round @click="fill('demo', 'demo123')">会员 demo</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const user = await auth.login(form)
    ElMessage.success(`欢迎回来，${user.nickname || user.username}`)
    router.push(user.role === 'ADMIN' ? '/admin' : (route.query.redirect || '/'))
  } finally {
    loading.value = false
  }
}

function fill(u, p) {
  form.username = u
  form.password = p
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f3b73 0%, #2d5fc4 60%, #409eff 100%);
  padding: 20px;
}

.auth-card {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px 28px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.25);
}

.auth-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
}

.auth-logo h2 {
  margin: 0;
  font-size: 22px;
}

.auth-sub {
  text-align: center;
  margin: 8px 0 24px;
  font-size: 13px;
}

.auth-btn {
  width: 100%;
}

.auth-footer {
  margin-top: 16px;
  text-align: center;
  font-size: 14px;
}

.demo-accounts {
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style>
