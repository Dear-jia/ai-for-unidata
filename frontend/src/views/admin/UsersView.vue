<template>
  <el-card shadow="never">
    <template #header>
      <div class="toolbar">
        <b>用户管理</b>
        <el-input v-model="keyword" placeholder="搜索用户名/昵称" clearable style="width: 240px" @keyup.enter="load(1)">
          <template #append><el-button @click="load(1)">搜索</el-button></template>
        </el-input>
      </div>
    </template>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" min-width="100" />
      <el-table-column prop="nickname" label="昵称" min-width="100" />
      <el-table-column prop="email" label="邮箱" min-width="140" show-overflow-tooltip />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column label="角色" width="90">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="会员" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.vip" type="warning" size="small">VIP 至 {{ (row.membershipExpireAt || '').slice(0, 10) }}</el-tag>
          <el-tag v-else type="info" size="small">FREE</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="openReset(row)">重置密码</el-button>
          <el-button size="small" type="danger" :disabled="row.role === 'ADMIN'" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="size" :current-page="page" @current-change="load" />
    </div>

    <el-dialog v-model="editVisible" title="编辑用户" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="正常" inactive-text="禁用" />
        </el-form-item>
        <el-form-item label="会员类型">
          <el-select v-model="form.membershipType" style="width: 100%">
            <el-option label="免费用户" value="FREE" />
            <el-option label="VIP 会员" value="VIP" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.membershipType === 'VIP'" label="到期时间">
          <el-date-picker v-model="form.membershipExpireAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api'

const keyword = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)
const editVisible = ref(false)
const saving = ref(false)
const form = reactive({})

async function load(p = page.value) {
  page.value = p
  loading.value = true
  try {
    const data = await adminApi.users({ keyword: keyword.value || undefined, page: p, size })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  Object.assign(form, row)
  editVisible.value = true
}

async function save() {
  saving.value = true
  try {
    await adminApi.updateUser(form.id, form)
    ElMessage.success('保存成功')
    editVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function openReset(row) {
  const { value } = await ElMessageBox.prompt(`为 ${row.username} 设置新密码（6-32位）`, '重置密码', {
    inputPattern: /^.{6,32}$/,
    inputErrorMessage: '密码长度需在6-32位之间'
  })
  await adminApi.resetPassword(row.id, value)
  ElMessage.success('密码已重置')
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除用户 ${row.username}？此操作不可恢复`, '警告', { type: 'warning' })
  await adminApi.deleteUser(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(() => load(1))
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
