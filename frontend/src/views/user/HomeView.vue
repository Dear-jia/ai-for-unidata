<template>
  <div>
    <!-- Hero -->
    <section class="hero">
      <div class="hero-inner">
        <h1>考研择校，数据先行</h1>
        <p>覆盖全国 148 所双一流建设高校，查询各院校工学门类（08 代码）一级学科真实复试线</p>
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
            <span>分数线记录</span>
          </div>
          <div class="stat-item">
            <b>{{ home.stats?.nationalLines || 0 }}</b>
            <span>国家线条目</span>
          </div>
          <div class="stat-item">
            <b>{{ home.stats?.articles || 0 }}</b>
            <span>考研资讯</span>
          </div>
        </div>
      </div>
    </section>

    <div class="page-container">
      <!-- 国家线速览 + 34 所自划线院校 -->
      <el-row :gutter="20">
        <el-col :md="16">
          <div class="section-title">
            2026 国家线速览
            <span class="text-muted" style="font-size: 13px; font-weight: 400">A 类 / B 类考生进入复试基本要求</span>
          </div>
          <el-card shadow="never">
            <el-table :data="(home.national2026 || []).slice(0, 8)" size="small" border>
              <el-table-column prop="discipline" label="学科门类 / 类别" min-width="150" />
              <el-table-column prop="subjects" label="适用学科专业" min-width="170" show-overflow-tooltip />
              <el-table-column label="A类总分" width="90" align="center">
                <template #default="{ row }"><b class="score-num">{{ row.totalA }}</b></template>
              </el-table-column>
              <el-table-column label="A类单科(100)" width="100" align="center">
                <template #default="{ row }">{{ row.oneA }}</template>
              </el-table-column>
              <el-table-column label="B类总分" width="90" align="center">
                <template #default="{ row }"><b class="score-num">{{ row.totalB }}</b></template>
              </el-table-column>
            </el-table>
            <div class="more-link">
              <el-link type="primary" @click="$router.push('/scores')">查看完整国家线（2025-2026）→</el-link>
            </div>
          </el-card>
        </el-col>
        <el-col :md="8">
          <div class="section-title">
            34 所自划线院校
            <span class="text-muted" style="font-size: 13px; font-weight: 400">官方复试线原文</span>
          </div>
          <el-card shadow="never">
            <div class="tag-cloud">
              <el-tag
                v-for="s in home.selfLineSchools || []"
                :key="s.id"
                class="tag-item"
                type="warning"
                effect="plain"
                @click="$router.push(`/schools/${s.id}`)"
              >
                {{ s.name }}
              </el-tag>
            </div>
            <div class="more-link">
              <el-link type="primary" @click="$router.push('/schools')">查看全部院校 →</el-link>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 最新分数线记录 -->
      <div class="section-title">
        最新分数线记录
        <span class="text-muted" style="font-size: 13px; font-weight: 400">点击行查看院校详情</span>
      </div>
      <el-card shadow="never">
        <el-table :data="home.latestScoreLines || []" size="small" border stripe @row-click="goSchool" class="clickable-table">
          <el-table-column prop="schoolName" label="院校" min-width="150" />
          <el-table-column prop="year" label="年份" width="70" align="center" />
          <el-table-column prop="major" label="专业" min-width="190" />
          <el-table-column prop="lineType" label="线类型" width="90" align="center" />
          <el-table-column label="总分" width="80" align="center">
            <template #default="{ row }"><b class="score-num">{{ row.minScore }}</b></template>
          </el-table-column>
          <el-table-column label="政治" width="60" align="center">
            <template #default="{ row }">{{ row.politicalScore }}</template>
          </el-table-column>
          <el-table-column label="外语" width="60" align="center">
            <template #default="{ row }">{{ row.foreignScore }}</template>
          </el-table-column>
        </el-table>
        <div class="more-link">
          <el-link type="primary" @click="$router.push('/scores')">前往分数线查询 →</el-link>
        </div>
      </el-card>

      <!-- 热门院校 -->
      <div class="section-title">
        热门院校
        <span class="text-muted" style="font-size: 13px; font-weight: 400">更多院校进入院校库查看</span>
      </div>
      <el-row :gutter="16">
        <el-col v-for="s in home.hotSchools || []" :key="s.id" :xs="12" :sm="8" :md="6" style="margin-bottom: 16px">
          <el-card class="card-hover" shadow="hover" @click="$router.push(`/schools/${s.id}`)">
            <div class="school-card">
              <el-tag size="small" type="danger" v-if="(s.level || '').includes('985')">985</el-tag>
              <el-tag size="small" type="warning" v-else-if="(s.level || '').includes('211')">211</el-tag>
              <el-tag size="small" type="success" v-else-if="(s.level || '').includes('双一流')">双一流</el-tag>
              <h3>{{ s.name }}</h3>
              <p class="text-muted">{{ s.province }} · {{ s.category || '综合' }}</p>
              <p class="intro">{{ s.intro || '暂无详细介绍' }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 按省份浏览 -->
      <div class="section-title">
        按省份浏览院校
        <span class="text-muted" style="font-size: 13px; font-weight: 400">点击省份快速筛选</span>
      </div>
      <el-card shadow="never">
        <div class="tag-cloud">
          <el-tag
            v-for="p in home.provinceStats || []"
            :key="p.province"
            class="tag-item province-tag"
            type="info"
            effect="plain"
            @click="$router.push({ path: '/schools', query: { province: p.province } })"
          >
            {{ p.province }}（{{ p.count }}）
          </el-tag>
        </div>
      </el-card>

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
import { useRouter } from 'vue-router'
import { publicApi } from '../../api'

const router = useRouter()
const home = reactive({})

onMounted(async () => {
  Object.assign(home, await publicApi.home())
})

function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : ''
}

function goSchool(row) {
  if (row.schoolId) {
    router.push(`/schools/${row.schoolId}`)
  }
}
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #1f3b73 0%, #2d5fc4 60%, #409eff 100%);
  color: #fff;
  padding: 56px 16px 64px;
  text-align: center;
}

.hero-inner {
  max-width: 900px;
  margin: 0 auto;
}

.hero h1 {
  font-size: 36px;
  margin: 0 0 12px;
}

.hero p {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 26px;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 48px;
  margin-top: 40px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item b {
  font-size: 26px;
}

.stat-item span {
  font-size: 13px;
  opacity: 0.85;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  cursor: pointer;
}

.province-tag {
  font-size: 13px;
}

.more-link {
  margin-top: 12px;
  text-align: right;
}

.clickable-table :deep(.el-table__row) {
  cursor: pointer;
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

.score-num {
  font-weight: 700;
  color: #f56c6c;
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
