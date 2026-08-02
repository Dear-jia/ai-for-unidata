<template>
  <div class="page-container">
    <div class="section-title">院校库</div>
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form inline>
        <el-form-item label="院校名称">
          <el-input v-model="query.name" placeholder="如：清华" clearable style="width: 160px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="省份">
          <el-select v-model="query.province" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.category" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="层次">
          <el-select v-model="query.level" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="l in levels" :key="l" :label="l" :value="l" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16">
      <el-col v-for="s in list" :key="s.id" :xs="12" :sm="8" :md="6" style="margin-bottom: 16px">
        <el-card class="card-hover" shadow="hover" @click="$router.push(`/schools/${s.id}`)">
          <div class="school-card">
            <div class="badges">
              <el-tag size="small" type="danger" v-if="s.level.includes('985')">985</el-tag>
              <el-tag size="small" type="warning" v-else-if="s.level.includes('211')">211</el-tag>
              <el-tag size="small" type="success" v-else>普通</el-tag>
            </div>
            <h3>{{ s.name }}</h3>
            <p class="text-muted">{{ s.province }} · {{ s.city }} · {{ s.category }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && list.length === 0" description="没有找到匹配的院校" />
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

const provinces = ['北京', '上海', '湖北', '浙江', '广东', '四川', '江苏', '陕西', '湖南', '天津']
const categories = ['综合', '理工', '师范', '医药', '财经', '农林', '政法', '艺术', '语言', '民族']
const levels = ['985/211/双一流', '211/双一流', '双一流', '普通本科']

const query = reactive({ name: '', province: '', category: '', level: '', page: 1, size: 12 })
const list = ref([])
const total = ref(0)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await publicApi.schools(query)
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

onMounted(load)
</script>

<style scoped>
.school-card h3 {
  margin: 8px 0 4px;
  font-size: 17px;
}

.school-card p {
  margin: 0;
  font-size: 13px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
