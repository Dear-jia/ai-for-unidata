<template>
  <div>
    <el-row :gutter="16">
      <el-col v-for="card in cards" :key="card.label" :xs="12" :sm="8" :md="4" style="margin-bottom: 16px">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
            <el-icon :size="22"><component :is="card.icon" /></el-icon>
          </div>
          <div>
            <p class="stat-value">{{ stats[card.key] ?? 0 }}</p>
            <p class="text-muted">{{ card.label }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :md="12" style="margin-bottom: 16px">
        <el-card shadow="never">
          <template #header><b>最新注册用户</b></template>
          <el-table :data="recentUsers" size="small" stripe>
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="nickname" label="昵称" />
            <el-table-column label="会员">
              <template #default="{ row }">
                <el-tag v-if="row.vip" type="warning" size="small">VIP</el-tag>
                <el-tag v-else type="info" size="small">FREE</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="注册时间">
              <template #default="{ row }">{{ (row.createdAt || '').replace('T', ' ').slice(0, 16) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :md="12" style="margin-bottom: 16px">
        <el-card shadow="never">
          <template #header><b>最新订单</b></template>
          <el-table :data="recentOrders" size="small" stripe>
            <el-table-column prop="orderNo" label="订单号" min-width="170" />
            <el-table-column label="套餐">
              <template #default="{ row }">{{ { MONTH: '月卡', QUARTER: '季卡', YEAR: '年卡' }[row.plan] }}</template>
            </el-table-column>
            <el-table-column label="金额">
              <template #default="{ row }">¥{{ row.amount }}</template>
            </el-table-column>
            <el-table-column label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 'PAID' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { adminApi } from '../../api'

const stats = reactive({})
const recentUsers = ref([])
const recentOrders = ref([])

const cards = [
  { key: 'users', label: '注册用户', icon: 'User', color: '#409eff', bg: '#ecf5ff' },
  { key: 'vipUsers', label: 'VIP 会员', icon: 'Medal', color: '#e6a23c', bg: '#fdf6ec' },
  { key: 'schools', label: '收录院校', icon: 'School', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'scoreLines', label: '分数线数据', icon: 'DataLine', color: '#f56c6c', bg: '#fef0f0' },
  { key: 'articles', label: '考研资讯', icon: 'Document', color: '#909399', bg: '#f4f4f5' },
  { key: 'orders', label: '订单总数', icon: 'List', color: '#722ed1', bg: '#f9f0ff' }
]

onMounted(async () => {
  const [s, o] = await Promise.all([adminApi.stats(), adminApi.overview()])
  Object.assign(stats, s)
  recentUsers.value = o.recentUsers || []
  recentOrders.value = o.recentOrders || []
})
</script>

<style scoped>
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
}

.stat-card p {
  margin: 0;
  font-size: 12px;
}
</style>
