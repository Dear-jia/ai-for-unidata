<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-title">个人中心</div>
      </template>
      <el-row :gutter="24">
        <el-col :md="12">
          <el-form :model="profile" label-width="80px">
            <el-form-item label="用户名">
              <el-input :model-value="auth.user?.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profile.nickname" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profile.email" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profile.phone" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :md="12">
          <el-form :model="pwd" label-width="80px">
            <el-form-item label="原密码">
              <el-input v-model="pwd.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwd.newPassword" type="password" show-password placeholder="6-32位" />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="pwd.confirm" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" :loading="changing" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../../api'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const saving = ref(false)
const changing = ref(false)

const profile = reactive({ nickname: '', email: '', phone: '' })
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })

Object.assign(profile, {
  nickname: auth.user?.nickname || '',
  email: auth.user?.email || '',
  phone: auth.user?.phone || ''
})

async function saveProfile() {
  saving.value = true
  try {
    const user = await userApi.updateProfile(profile)
    auth.setUser(user)
    ElMessage.success('资料已保存')
  } finally {
    saving.value = false
  }
}

async function changePassword() {
  if (pwd.newPassword.length < 6 || pwd.newPassword.length > 32) {
    return ElMessage.warning('新密码长度需在6-32位之间')
  }
  if (pwd.newPassword !== pwd.confirm) {
    return ElMessage.warning('两次输入的密码不一致')
  }
  changing.value = true
  try {
    await userApi.changePassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
    ElMessage.success('密码已修改')
    pwd.oldPassword = pwd.newPassword = pwd.confirm = ''
  } finally {
    changing.value = false
  }
}
</script>

<style scoped>
.card-title {
  font-size: 16px;
  font-weight: 600;
}
</style>
