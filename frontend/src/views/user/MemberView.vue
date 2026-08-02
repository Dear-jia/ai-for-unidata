<template>
  <div class="page-container">
    <div class="section-title">会员中心</div>

    <el-card shadow="never" style="margin-bottom: 20px">
      <div class="vip-status">
        <el-icon :size="44" :color="auth.isVip ? '#e6a23c' : '#909399'"><Medal /></el-icon>
        <div>
          <h3>{{ auth.isVip ? '尊贵的 VIP 会员' : '当前为普通用户' }}</h3>
          <p class="text-muted">
            <template v-if="auth.isVip">会员有效期至 {{ formatTime(auth.user?.membershipExpireAt) }}</template>
            <template v-else>开通会员即可解锁全部院校历年分数线数据</template>
          </p>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col v-for="p in plans" :key="p.plan" :xs="24" :sm="8" style="margin-bottom: 16px">
        <el-card shadow="hover" class="plan-card" :class="{ hot: p.hot }">
          <template #header>
            <div class="plan-header">
              <span>{{ p.name }}</span>
              <el-tag v-if="p.hot" type="danger" size="small">最受欢迎</el-tag>
            </div>
          </template>
          <div class="plan-body">
            <p class="price plan-price">¥{{ p.amount }}<span class="text-muted"> / {{ p.duration }}</span></p>
            <ul class="plan-features">
              <li v-for="f in p.features" :key="f">{{ f }}</li>
            </ul>
            <el-button
              type="primary"
              round
              style="width: 100%"
              :loading="paying === p.plan"
              @click="buy(p)"
            >
              立即开通
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header><div class="card-title">我的订单</div></template>
      <el-table :data="orders" border stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column label="套餐" width="100">
          <template #default="{ row }">{{ planName(row.plan) }}</template>
        </el-table-column>
        <el-table-column prop="months" label="时长(月)" width="90" />
        <el-table-column label="金额" width="90">
          <template #default="{ row }"><span class="price">¥{{ row.amount }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PAID' ? 'success' : 'warning'" size="small">
              {{ row.status === 'PAID' ? '已支付' : '待支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="150">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="orders.length === 0" description="暂无订单" :image-size="80" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { memberApi } from '../../api'
import { useAuthStore } from '../../stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()
const orders = ref([])
const paying = ref('')

const plans = [
  { plan: 'MONTH', name: '月度会员', amount: 19, duration: '1个月', hot: false, features: ['全站分数线查看', '历年数据回溯', '会员专属标识'] },
  { plan: 'QUARTER', name: '季度会员', amount: 49, duration: '3个月', hot: true, features: ['月度会员全部权益', '备考资料优先推送', '新活动优先体验'] },
  { plan: 'YEAR', name: '年度会员', amount: 168, duration: '12个月', hot: false, features: ['季度会员全部权益', '全年不限次数查询', '专属客服支持'] }
]

function planName(p) {
  return { MONTH: '月度会员', QUARTER: '季度会员', YEAR: '年度会员' }[p] || p
}

function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : ''
}

async function buy(plan) {
  if (!auth.isLogin) {
    ElMessage.warning('请先登录')
    return router.push({ name: 'Login', query: { redirect: '/member' } })
  }
  paying.value = plan.plan
  try {
    const order = await memberApi.createOrder({ plan: plan.plan })
    // 演示环境：直接模拟支付成功
    await memberApi.activateOrder(order.orderNo)
    await auth.refreshUser()
    ElMessage.success('会员开通成功！')
    loadOrders()
  } finally {
    paying.value = ''
  }
}

async function loadOrders() {
  if (!auth.isLogin) return
  orders.value = await memberApi.myOrders()
}

onMounted(loadOrders)
</script>

<style scoped>
.vip-status {
  display: flex;
  align-items: center;
  gap: 18px;
}

.vip-status h3 {
  margin: 0 0 6px;
  font-size: 18px;
}

.vip-status p {
  margin: 0;
}

.plan-card {
  height: 100%;
}

.plan-card.hot {
  border: 2px solid #f56c6c;
}

.plan-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
}

.plan-price {
  font-size: 28px;
  margin: 0 0 12px;
}

.plan-price span {
  font-size: 13px;
  font-weight: 400;
}

.plan-features {
  list-style: none;
  padding: 0;
  margin: 0 0 18px;
  color: #606266;
  font-size: 14px;
}

.plan-features li {
  padding: 6px 0;
}

.plan-features li::before {
  content: '✓';
  color: #67c23a;
  font-weight: 700;
  margin-right: 8px;
}

.card-title {
  font-weight: 600;
}
</style>
