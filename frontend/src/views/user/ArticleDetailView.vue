<template>
  <div class="page-container">
    <el-card shadow="never" v-if="article">
      <div class="article-head">
        <el-tag effect="plain">{{ article.category }}</el-tag>
        <h1>{{ article.title }}</h1>
        <p class="text-muted">
          <el-icon><View /></el-icon> {{ article.views }} 次浏览 · {{ formatTime(article.createdAt) }}
        </p>
      </div>
      <el-divider />
      <div class="markdown-content" v-html="article.content"></div>
      <el-divider />
      <div class="back-row">
        <el-button @click="$router.back()"><el-icon><ArrowLeft /></el-icon> 返回列表</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { publicApi } from '../../api'

const route = useRoute()
const article = ref(null)

function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : ''
}

onMounted(async () => {
  article.value = await publicApi.articleDetail(route.params.id)
})
</script>

<style scoped>
.article-head {
  text-align: center;
  padding: 10px 0;
}

.article-head h1 {
  margin: 14px 0 10px;
  font-size: 26px;
}

.back-row {
  text-align: center;
}
</style>
