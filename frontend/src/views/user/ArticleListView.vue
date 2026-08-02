<template>
  <div class="page-container">
    <div class="section-title">考研资讯</div>
    <el-card shadow="never" style="margin-bottom: 20px">
      <div class="filter-bar">
        <el-radio-group v-model="query.category" @change="search">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button v-for="c in categories" :key="c" :value="c">{{ c }}</el-radio-button>
        </el-radio-group>
        <el-input v-model="query.keyword" placeholder="搜索标题" clearable style="width: 220px" @keyup.enter="search">
          <template #append>
            <el-button @click="search"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col v-for="a in list" :key="a.id" :xs="24" :sm="12" :md="8" style="margin-bottom: 16px">
        <el-card class="card-hover article-card" shadow="hover" @click="$router.push(`/articles/${a.id}`)">
          <el-tag size="small" effect="plain">{{ a.category }}</el-tag>
          <h3 class="article-title">{{ a.title }}</h3>
          <p class="article-summary">{{ a.summary }}</p>
          <div class="article-meta text-muted">
            <span><el-icon><View /></el-icon> {{ a.views }}</span>
            <span>{{ formatTime(a.createdAt) }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && list.length === 0" description="暂无资讯" />
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

const categories = ['资讯', '政策解读', '备考经验', '院校解读']
const query = reactive({ category: '', keyword: '', page: 1, size: 9 })
const list = ref([])
const total = ref(0)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await publicApi.articles({
      category: query.category || undefined,
      keyword: query.keyword || undefined,
      page: query.page,
      size: query.size
    })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  load()
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
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.article-card {
  height: 100%;
}

.article-title {
  font-size: 16px;
  margin: 12px 0 8px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-summary {
  color: #909399;
  font-size: 13px;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
