<template>
  <div class="page-container">
    <div class="section-title">分数线查询</div>
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form inline>
        <el-form-item label="院校">
          <el-select v-model="query.schoolId" placeholder="选择院校" clearable filterable style="width: 220px">
            <el-option v-for="s in schoolOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年份">
          <el-select v-model="query.year" placeholder="全部年份" clearable style="width: 130px">
            <el-option v-for="y in years" :key="y" :label="`${y}年`" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="query.major" placeholder="如：计算机" clearable style="width: 160px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-alert v-if="!auth.isVip" type="warning" :closable="false" show-icon style="margin-bottom: 14px">
      <template #title>
        会员可查看全部历年分数数据，<el-link type="primary" @click="$router.push('/member')">立即开通</el-link>
      </template>
    </el-alert>

    <el-card shadow="never">
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="schoolName" label="院校" min-width="120" />
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column prop="major" label="专业" min-width="140" />
        <el-table-column prop="lineType" label="线类型" width="90" />
        <el-table-column label="总分" width="110">
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
        <el-table-column label="业务课" min-width="110">
          <template #default="{ row }">
            {{ row.locked ? '***' : `${row.majorScore1} / ${row.majorScore2}` }}
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && list.length === 0" description="暂无数据" />
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
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { publicApi } from '../../api'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const query = reactive({ schoolId: null, year: null, major: '', page: 1, size: 15 })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const schoolOptions = ref([])
const years = ref([])

async function load() {
  loading.value = true
  try {
    const data = await publicApi.scoreLines({
      schoolId: query.schoolId || undefined,
      year: query.year || undefined,
      major: query.major || undefined,
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

onMounted(async () => {
  const [home, schoolData] = await Promise.all([
    publicApi.home(),
    publicApi.schools({ page: 1, size: 200 })
  ])
  years.value = home.years || []
  schoolOptions.value = schoolData.list
  load()
})
</script>

<style scoped>
.score-num {
  font-weight: 700;
  color: #f56c6c;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
