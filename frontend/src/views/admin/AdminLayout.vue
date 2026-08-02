<template>
  <el-container class="admin-shell">
    <el-aside width="220px" class="admin-aside">
      <div class="admin-logo">
        <el-icon :size="24" color="#409eff"><DataLine /></el-icon>
        <span>研数管理后台</span>
      </div>
      <el-menu :default-active="activeMenu" router background-color="#1f2d3d" text-color="#aab4c2" active-text-color="#409eff">
        <el-menu-item index="/admin">
          <el-icon><Odometer /></el-icon>
          <span>数据总览</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/schools">
          <el-icon><School /></el-icon>
          <span>学校管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/scorelines">
          <el-icon><DataLine /></el-icon>
          <span>分数线管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/articles">
          <el-icon><Document /></el-icon>
          <span>资讯管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/activities">
          <el-icon><Promotion /></el-icon>
          <span>活动管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-button text @click="$router.push('/')"><el-icon><Back /></el-icon> 返回前台</el-button>
        </div>
        <div class="header-right">
          <span class="text-muted">{{ auth.user?.nickname || auth.user?.username }}</span>
          <el-button type="danger" plain size="small" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => {
  const path = route.path
  if (path === '/admin') return '/admin'
  return path
})

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
}

.admin-aside {
  background: #1f2d3d;
  color: #fff;
}

.admin-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.admin-aside :deep(.el-menu) {
  border-right: none;
}

.admin-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-main {
  background: #f5f7fa;
}
</style>
