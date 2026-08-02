<template>
  <el-card shadow="never">
    <template #header>
      <div class="toolbar">
        <b>学校管理</b>
        <div>
          <el-input v-model="keyword" placeholder="搜索学校名称" clearable style="width: 220px" @keyup.enter="load(1)">
            <template #append><el-button @click="load(1)">搜索</el-button></template>
          </el-input>
          <el-button type="primary" style="margin-left: 10px" @click="openCreate">新增学校</el-button>
        </div>
      </div>
    </template>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="学校名称" min-width="140" />
      <el-table-column prop="province" label="省份" width="90" />
      <el-table-column prop="city" label="城市" width="90" />
      <el-table-column prop="category" label="类型" width="90" />
      <el-table-column prop="dept" label="主管部门" min-width="120" show-overflow-tooltip />
      <el-table-column prop="level" label="层次" min-width="130" />
      <el-table-column label="招生信息" min-width="150">
        <template #default="{ row }">
          <el-link v-if="row.admissionUrl" type="primary" :href="row.admissionUrl" target="_blank">研招网信息页</el-link>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="size" :current-page="page" @current-change="load" />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑学校' : '新增学校'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="学校名称"><el-input v-model="form.name" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="省份"><el-input v-model="form.province" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="城市"><el-input v-model="form.city" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="类型">
              <el-select v-model="form.category" style="width: 100%">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="层次">
              <el-select v-model="form.level" style="width: 100%">
                <el-option v-for="l in levels" :key="l" :label="l" :value="l" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="学校简介">
          <el-input v-model="form.intro" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="主管部门">
          <el-input v-model="form.dept" placeholder="如：教育部 / 北京市" />
        </el-form-item>
        <el-form-item label="招生信息地址">
          <el-input v-model="form.admissionUrl" placeholder="研招网院校信息页或学校官网招生页" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="展示" inactive-text="隐藏" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api'

const categories = ['综合', '理工', '师范', '医药', '财经', '农林', '政法', '艺术', '语言', '民族', '体育', '军事']
const levels = ['985/211/双一流', '双一流', '研究生院']
const keyword = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({})

async function load(p = page.value) {
  page.value = p
  loading.value = true
  try {
    const data = await adminApi.schools({ keyword: keyword.value || undefined, page: p, size })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { name: '', province: '', city: '', category: '综合', level: '', intro: '', dept: '', admissionUrl: '', status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    if (form.id) await adminApi.updateSchool(form.id, form)
    else await adminApi.createSchool(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`删除 ${row.name} 会同时删除其全部分数线数据，确定？`, '警告', { type: 'warning' })
  await adminApi.deleteSchool(row.id)
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
