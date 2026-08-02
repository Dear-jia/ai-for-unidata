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
        <div class="section-title" style="font-size: 18px">官方复试分数线（研招网发布）</div>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 14px"
          title="以下为研招网转载的各校官方公布的复试基本分数线原图，点击图片可放大查看"
        />
        <el-radio-group v-model="sourceYear" style="margin-bottom: 14px">
          <el-radio-button v-for="y in sourceYears" :key="y" :value="y">{{ y }} 年</el-radio-button>
        </el-radio-group>
        <div class="source-list">
          <div v-for="(src, i) in currentSources" :key="i" class="source-item">
            <el-image
              :src="localImageUrl(src.imageUrl)"
              :preview-src-list="currentSources.map((s) => localImageUrl(s.imageUrl))"
              :initial-index="i"
              fit="contain"
              class="source-img"
              :preview-teleported="true"
            >
              <template #error>
                <div class="img-error">
                  <p>图片加载失败</p>
                  <el-link type="primary" :href="src.sourceUrl" target="_blank">前往官方原文查看</el-link>
                </div>
              </template>
            </el-image>
            <div class="source-foot">
              <el-link type="primary" :href="src.sourceUrl" target="_blank">查看官方原文</el-link>
            </div>
          </div>
        </div>
        <el-divider />
      </template>

      <div class="section-title" style="font-size: 18px">历年复试分数线</div>
      <el-alert
        type="success"
        :closable="false"
        show-icon
        style="margin-bottom: 14px"
        title="全站分数线数据当前免费开放，会员特权功能开发中"
      />
      <el-table :data="scoreLines" border stripe>
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column prop="major" label="专业" min-width="140" />
        <el-table-column prop="lineType" label="线类型" width="90" />
        <el-table-column label="总分" width="100">
          <template #default="{ row }">
            <span v-if="!row.locked" class="score-num">{{ row.minScore }}</span>
            <el-tag v-else type="warning" size="small">会员专享</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="政治" width="80">
          <template #default="{ row }">
            <span v-if="!row.locked">{{ row.politicalScore }}</span>
            <span v-else class="text-muted">***</span>
          </template>
        </el-table-column>
        <el-table-column label="外语" width="80">
          <template #default="{ row }">
            <span v-if="!row.locked">{{ row.foreignScore }}</span>
            <span v-else class="text-muted">***</span>
          </template>
        </el-table-column>
        <el-table-column label="业务课" min-width="120">
          <template #default="{ row }">
            <span v-if="!row.locked">{{ row.majorScore1 }} / {{ row.majorScore2 }}</span>
            <span v-else class="text-muted">***</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="scoreLines.length === 0" description="暂无分数线数据" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { publicApi } from '../../api'

const route = useRoute()
const school = ref(null)
const scoreLines = ref([])
const scoreSources = ref([])
const sourceYear = ref(null)

const sourceYears = computed(() => [...new Set(scoreSources.value.map((s) => s.year))].sort((a, b) => b - a))
const currentSources = computed(() => scoreSources.value.filter((s) => s.year === sourceYear.value))

onMounted(async () => {
  const data = await publicApi.schoolDetail(route.params.id)
  school.value = data.school
  scoreLines.value = data.scoreLines
  scoreSources.value = data.scoreSources || []
  if (sourceYears.value.length) {
    sourceYear.value = sourceYears.value[0]
  }
})

function openUrl(url) {
  window.open(url, '_blank')
}

function localImageUrl(url) {
  return `/api/public/score-image?url=${encodeURIComponent(url)}`
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
  gap: 12px;
}

.source-img {
  width: 100%;
  min-height: 240px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fafafa;
}

.source-foot {
  margin-top: 6px;
  font-size: 13px;
}

.img-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  color: #909399;
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
