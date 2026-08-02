<template>
  <div class="page-container">
    <el-card shadow="never" v-if="school">
      <div class="school-head">
        <div class="school-name">
          <h1>{{ school.name }}</h1>
          <div class="tags">
            <el-tag type="danger" v-if="(school.level || '').includes('985')">985</el-tag>
            <el-tag type="warning" v-if="(school.level || '').includes('211')">211</el-tag>
            <el-tag type="success" v-if="(school.level || '').includes('双一流')">双一流</el-tag>
          </div>
        </div>
        <div class="school-meta">
          <p class="text-muted">{{ school.province }} · {{ school.category || '综合' }}</p>
          <p v-if="school.dept" class="text-muted">主管部门：{{ school.dept }}</p>
          <div class="school-links">
            <el-button v-if="school.admissionUrl" type="primary" round @click="openUrl(school.admissionUrl)">
              招生简章 / 招生细则（研招网）
            </el-button>
          </div>
        </div>
      </div>
      <el-divider />
      <p class="intro">{{ school.intro || '暂无详细介绍' }}</p>

      <template v-if="sourceYears.length > 0">
        <div class="section-title" style="font-size: 18px">官方复试分数线原文</div>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 14px"
          title="以下为该校在研招网发布的官方复试基本分数线原文，点击前往官方页面查看"
        />
        <div class="source-list">
          <div v-for="(src, i) in scoreSources" :key="i" class="source-item">
            <el-button text type="primary" tag="a" :href="src.sourceUrl" target="_blank">
              {{ src.year }} 年：{{ src.title }}
              <el-icon style="margin-left: 4px"><Link /></el-icon>
            </el-button>
            <div class="source-foot">
              <span class="text-muted">数据来源：研招网（公开信息）</span>
            </div>
          </div>
        </div>
        <el-divider />
      </template>

      <div class="section-title" style="font-size: 18px">历年复试分数线</div>
      <el-alert v-if="hasRealLines" type="success" :closable="false" show-icon style="margin-bottom: 14px"
        title="已收录该校真实复试线（来源：研招网公开信息），未收录的一级学科显示「暂无数据」" />
      <el-alert v-else type="info" :closable="false" show-icon style="margin-bottom: 14px"
        title="该校真实复试线暂未收录，以下工学门类（08 代码）一级学科均显示「暂无数据」，具体以该校官方公布为准" />

      <el-table v-if="scoreLines.length > 0" :data="scoreLines" border stripe style="margin-bottom: 20px">
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column prop="major" label="专业" min-width="220" />
        <el-table-column prop="lineType" label="线类型" width="90" />
        <el-table-column label="总分" width="100">
          <template #default="{ row }">
            <span v-if="!row.locked" class="score-num">{{ row.minScore }}</span>
            <el-tag v-else type="warning" size="small">会员专享</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="政治" width="70">
          <template #default="{ row }">{{ row.locked ? '***' : row.politicalScore }}</template>
        </el-table-column>
        <el-table-column label="外语" width="70">
          <template #default="{ row }">{{ row.locked ? '***' : row.foreignScore }}</template>
        </el-table-column>
        <el-table-column label="业务课" min-width="100">
          <template #default="{ row }">{{ row.locked ? '***' : `${row.majorScore1 ?? '-'} / ${row.majorScore2 ?? '-'}` }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      </el-table>

      <div class="section-title" style="font-size: 16px; margin-top: 8px">
        工学门类（08 代码）一级学科复试线覆盖情况
        <span class="text-muted" style="font-size: 13px; font-weight: 400">仅展示该校已公布的真实复试线，查不到的专业显示「暂无数据」</span>
      </div>
      <el-table :data="engineeringSubjects" border stripe>
        <el-table-column prop="code" label="代码" width="80" align="center" />
        <el-table-column prop="name" label="一级学科" min-width="180" />
        <el-table-column label="2026 年" min-width="130" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.lines[2026]" type="success" size="small">已收录</el-tag>
            <span v-else class="text-muted">暂无数据</span>
          </template>
        </el-table-column>
        <el-table-column label="2025 年" min-width="130" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.lines[2025]" type="success" size="small">已收录</el-tag>
            <span v-else class="text-muted">暂无数据</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="engineeringSubjects.length === 0" description="暂无数据" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Link } from '@element-plus/icons-vue'
import { publicApi } from '../../api'

const route = useRoute()
const school = ref(null)
const scoreLines = ref([])
const scoreSources = ref([])
const hasRealLines = ref(false)
const engineeringSubjects = ref([])

const sourceYears = computed(() => [...new Set(scoreSources.value.map((s) => s.year))].sort((a, b) => b - a))

onMounted(async () => {
  const data = await publicApi.schoolDetail(route.params.id)
  school.value = data.school
  scoreLines.value = data.scoreLines
  scoreSources.value = data.scoreSources || []
  hasRealLines.value = data.hasRealLines || false
  engineeringSubjects.value = data.engineeringSubjects || []
})

function openUrl(url) {
  window.open(url, '_blank')
}

</script>

<style scoped>
.school-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.school-name {
  display: flex;
  align-items: center;
  gap: 14px;
}

.school-name h1 {
  margin: 0;
  font-size: 24px;
}

.tags {
  display: flex;
  gap: 6px;
}

.school-meta {
  text-align: right;
}

.school-meta p {
  margin: 2px 0;
}

.school-links {
  margin-top: 8px;
}

.source-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.source-foot {
  font-size: 13px;
  padding-left: 12px;
}

.intro {
  line-height: 1.8;
  color: #606266;
}

.score-num {
  font-weight: 700;
  color: #f56c6c;
}
</style>
