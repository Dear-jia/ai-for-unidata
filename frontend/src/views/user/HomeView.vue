<template>
  <div>
    <!-- Hero -->
    <section class="hero">
      <div class="hero-inner">
        <h1>考研择校，数据先行</h1>
        <p>覆盖全国 900+ 所研究生招生单位，收录 2025-2026 国家线与 34 所自划线院校官方复试线</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="$router.push('/scores')">查询分数线</el-button>
          <el-button size="large" round plain @click="$router.push('/schools')">浏览院校库</el-button>
        </div>
        <div class="hero-stats">
          <div class="stat-item">
            <b>{{ home.stats?.schools || 0 }}</b>
            <span>收录院校</span>
          </div>
          <div class="stat-item">
            <b>{{ home.stats?.scoreLines || 0 }}</b>
            <span>分数线数据</span>
          </div>
          <div class="stat-item">
            <b>{{ home.stats?.articles || 0 }}</b>
            <span>考研资讯</span>
          </div>
          <div class="stat-item">
            <b>{{ home.stats?.users || 0 }}</b>
            <span>注册用户</span>
          </div>
        </div>
      </div>
    </section>

    <div class="page-container">
      <!-- 热门院校 -->
      <div class="section-title">热门院校 <span class="text-muted" style="font-size: 13px; font-weight: 400">更多院校进入院校库查看</span></div>
      <el-row :gutter="16">
        <el-col v-for="s in home.hotSchools || []" :key="s.id" :xs="12" :sm="8" :md="6" style="margin-bottom: 16px">
          <el-card class="card-hover" shadow="hover" @click="$router.push(`/schools/${s.id}`)">
            <div class="school-card">
              <el-tag size="small" type="danger" v-if="s.level.includes('985')">985</el-tag>
              <el-tag size="small" type="warning" v-else-if="s.level.includes('211')">211</el-tag>
              <el-tag size="small" type="success" v-else>普通</el-tag>
              <h3>{{ s.name }}</h3>
              <p class="text-muted">{{ s.province }} · {{ s.city }}</p>
              <p class="intro">{{ s.intro }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 最新资讯 + 活动 -->
      <el-row :gutter="20">
        <el-col :md="16">
          <div class="section-title">考研资讯</div>
          <el-card shadow="never">
            <div
              v-for="a in home.latestArticles || []"
              :key="a.id"
              class="article-item"
              @click="$router.push(`/articles/${a.id}`)"
            >
              <el-tag size="small" effect="plain">{{ a.category }}</el-tag>
              <span class="article-title">{{ a.title }}</span>
              <span class="text-muted article-time">{{ formatTime(a.createdAt) }}</span>
            </div>
          </el-card>
        </el-col>
        <el-col :md="8">
          <div class="section-title">网站活动</div>
          <el-card shadow="never">
            <div
              v-for="act in home.latestActivities || []"
              :key="act.id"
              class="activity-item"
              @click="$router.push('/activities')"
            >
              <div class="activity-badge">活动</div>
              <div>
                <p class="activity-title">{{ act.title }}</p>
                <p class="text-muted">{{ act.endTime ? `截止 ${formatTime(act.endTime).slice(0, 10)}` : '' }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { publicApi } from '../../api'

const home = reactive({})

onMounted(async () => {
  Object.assign(home, await publicApi.home())
})

function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : ''
}
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #1f3b73 0%, #2d5fc4 60%, #409eff 100%);
  color: #fff;
  padding: 64px 16px 72px;
  text-align: center;
}

.hero-inner {
  max-width: 900px;
  margin: 0 auto;
}

.hero h1 {
  font-size: 38px;
  margin: 0 0 12px;
}

.hero p {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 28px;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 56px;
  margin-top: 44px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item b {
  font-size: 28px;
}

.stat-item span {
  font-size: 13px;
  opacity: 0.85;
}

.school-card h3 {
  margin: 8px 0 4px;
  font-size: 17px;
}

.school-card p {
  margin: 2px 0;
  font-size: 13px;
}

.school-card .intro {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: #909399;
  margin-top: 8px;
  line-height: 1.5;
}

.article-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
}

.article-item:last-child {
  border-bottom: none;
}

.article-title {
  flex: 1;
  font-size: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-item:hover .article-title {
  color: #409eff;
}

.article-time {
  font-size: 12px;
}

.activity-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-badge {
  flex-shrink: 0;
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: #ecf5ff;
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.activity-title {
  margin: 0 0 4px;
  font-size: 14px;
  line-height: 1.4;
}
</style>
