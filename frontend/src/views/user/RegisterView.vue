<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-logo" @click="$router.push('/')">
        <el-icon :size="30" color="#409eff"><DataLine /></el-icon>
        <h2>注册新账号</h2>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" label-position="top">
        <el-form-item prop="username" label="用户名">
          <el-input v-model="form.username" placeholder="3-20位字母、数字或下划线" />
        </el-form-item>
        <el-form-item prop="nickname" label="昵称">
          <el-input v-model="form.nickname" placeholder="选填" />
        </el-form-item>
        <el-form-item prop="email" label="邮箱">
          <el-input v-model="form.email" placeholder="选填" />
        </el-form-item>
        <el-form-item prop="phone" label="手机号">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item prop="password" label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="6-32位" />
        </el-form-item>
        <el-form-item prop="confirm" label="确认密码">
          <el-input v-model="form.confirm" type="password" show-password placeholder="再次输入密码" />
        </el-form-item>
        <el-button type="primary" class="auth-btn" size="large" :loading="loading" @click="submit">注 册</el-button>
      </el-form>
      <div class="auth-footer">
        <span class="text-muted">已有账号？</span>
        <el-link type="primary" @click="$router.push('/login')">去登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', nickname: '', email: '', phone: '', password: '', confirm: '' })
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{3,20}$/, message: '3-20位字母、数字或下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度需在6-32位之间', trigger: 'blur' }
  ],
  confirm: [
    {
      validator: (rule, value, cb) => (value === form.password ? cb() : cb(new Error('两次输入的密码不一致'))),
      trigger: 'blur'
    }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const { confirm, ...payload } = form
    await auth.register(payload)
    ElMessage.success('注册成功，欢迎加入 UniData！')
    router.push('/')
  } finally {
    loading.value = false
  }
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
  width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 36px 36px 28px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.25);
}

.auth-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
  cursor: pointer;
}

.auth-logo h2 {
  margin: 0;
  font-size: 20px;
}

.auth-btn {
  width: 100%;
}

.auth-footer {
  margin-top: 14px;
  text-align: center;
  font-size: 14px;
}
</style>
