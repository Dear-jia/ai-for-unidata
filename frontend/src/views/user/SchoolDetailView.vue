<template>
  <div class="page-container">
    <el-card shadow="never" v-if="school">
      <div class="school-head">
        <div class="school-name">
          <h1>{{ school.name }}</h1>
          <div class="tags">
            <el-tag type="danger" v-if="school.level.includes('985')">985</el-tag>
            <el-tag type="warning" v-if="school.level.includes('211')">211</el-tag>
            <el-tag type="success" v-if="school.level.includes('双一流')">双一流</el-tag>
          </div>
        </div>
        <p class="text-muted">{{ school.province }} · {{ school.city }} · {{ school.category }}</p>
      </div>
      <el-divider />
      <p class="intro">{{ school.intro }}</p>

      <div class="section-title" style="font-size: 18px">历年复试分数线</div>
      <el-alert
        v-if="!auth.isVip"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 14px"
      >
        <template #title>
          以下部分数据为会员专享，<el-link type="primary" @click="$router.push('/member')">开通会员</el-link>后查看完整分数
        </template>
      </el-alert>
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
import { useRoute } from 'vue-router'
import { publicApi } from '../../api'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const auth = useAuthStore()
const school = ref(null)
const scoreLines = ref([])

onMounted(async () => {
  const data = await publicApi.schoolDetail(route.params.id)
  school.value = data.school
  scoreLines.value = data.scoreLines
})
</script>

<style scoped>
.school-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.intro {
  line-height: 1.8;
  color: #606266;
}

.score-num {
  font-weight: 700;
  color: #f56c6c;
}
</style>
