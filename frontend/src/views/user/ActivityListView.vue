<template>
  <div class="page-container">
    <div class="section-title">网站活动</div>
    <el-row :gutter="16">
      <el-col v-for="a in list" :key="a.id" :xs="24" :sm="12" style="margin-bottom: 16px">
        <el-card shadow="hover" class="activity-card">
          <div class="activity-head">
            <el-tag type="danger" effect="dark" round>进行中</el-tag>
            <h3>{{ a.title }}</h3>
          </div>
          <p class="activity-content">{{ a.content }}</p>
          <p class="text-muted activity-time">
            {{ formatTime(a.startTime) }} 至 {{ formatTime(a.endTime) }}
          </p>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && list.length === 0" description="暂无活动" />
    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="query.size"
        :current-page="query.page"
        @current-change="onPage"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { publicApi } from '../../api'

const query = reactive({ page: 1, size: 8 })
const list = ref([])
const total = ref(0)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await publicApi.activities(query)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function onPage(p) {
  query.page = p
  load()
}

function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : ''
}

onMounted(load)
</script>

<style scoped>
.activity-card {
  height: 100%;
}

.activity-head h3 {
  margin: 12px 0 10px;
  font-size: 17px;
}

.activity-content {
  color: #606266;
  line-height: 1.8;
  min-height: 72px;
}

.activity-time {
  font-size: 12px;
  border-top: 1px dashed #e4e7ed;
  padding-top: 10px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
