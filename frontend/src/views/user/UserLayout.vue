<template>
  <div>
    <header class="site-header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <el-icon :size="26" color="#409eff"><DataLine /></el-icon>
          <span>研数 <b>UniData</b></span>
        </router-link>
        <nav class="nav">
          <router-link to="/" class="nav-link" active-class="active" exact-active-class="active">首页</router-link>
          <router-link to="/schools" class="nav-link" active-class="active">院校库</router-link>
          <router-link to="/scores" class="nav-link" active-class="active">分数线</router-link>
          <router-link to="/articles" class="nav-link" active-class="active">考研资讯</router-link>
          <router-link to="/activities" class="nav-link" active-class="active">网站活动</router-link>
        </nav>
        <div class="header-actions">
          <template v-if="auth.isLogin">
            <router-link to="/member">
              <el-tag v-if="auth.isVip" type="warning" effect="dark" round>VIP会员</el-tag>
              <el-tag v-else type="info" effect="plain" round>开通会员</el-tag>
            </router-link>
            <el-dropdown @command="onCommand">
              <span class="user-entry">
                <el-avatar :size="30" class="avatar">{{ avatarText }}</el-avatar>
                <span class="nickname">{{ auth.user?.nickname || auth.user?.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="auth.isAdmin" command="admin">管理后台</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button text @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" round @click="$router.push('/register')">免费注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <main>
      <router-view />
    </main>

    <footer class="site-footer">
      <div class="footer-inner">
        <p class="text-muted">研数 UniData · 考研院校分数线数据平台</p>
        <p class="text-muted">数据仅供备考参考，请以各高校官方发布为准</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const avatarText = computed(() => {
  const name = auth.user?.nickname || auth.user?.username || 'U'
  return name.slice(0, 1).toUpperCase()
})

async function onCommand(cmd) {
  if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'admin') router.push('/admin')
  else if (cmd === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    auth.logout()
    router.push('/')
  }
}

onMounted(() => {
  if (auth.isLogin) {
    auth.refreshUser().catch(() => {})
  }
})
</script>

<style scoped>
.site-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 28px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.logo b {
  color: #409eff;
}

.nav {
  display: flex;
  gap: 4px;
  flex: 1;
}

.nav-link {
  padding: 6px 14px;
  border-radius: 20px;
  color: #606266;
  font-size: 15px;
  transition: all 0.2s;
}

.nav-link:hover {
  color: #409eff;
  background: #ecf5ff;
}

.nav-link.active {
  color: #409eff;
  background: #ecf5ff;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #303133;
  outline: none;
}

.avatar {
  background: #409eff;
  color: #fff;
}

.site-footer {
  background: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 24px 0;
  text-align: center;
  font-size: 13px;
}

.footer-inner p {
  margin: 4px 0;
}
</style>
