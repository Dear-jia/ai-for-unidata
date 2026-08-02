<template>
  <el-card shadow="never">
    <template #header>
      <div class="toolbar">
        <b>分数线管理</b>
        <div>
          <el-select v-model="filters.schoolId" placeholder="全部学校" clearable filterable style="width: 200px">
            <el-option v-for="s in schoolOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
          <el-select v-model="filters.year" placeholder="全部年份" clearable style="width: 120px">
            <el-option v-for="y in yearOptions" :key="y" :label="`${y}年`" :value="y" />
          </el-select>
          <el-button type="primary" style="margin-left: 10px" @click="openCreate">新增分数线</el-button>
        </div>
      </div>
    </template>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="schoolName" label="学校" min-width="130" />
      <el-table-column prop="year" label="年份" width="70" />
      <el-table-column prop="major" label="专业" min-width="130" />
      <el-table-column prop="lineType" label="线类型" width="90" />
      <el-table-column prop="minScore" label="总分" width="80" />
      <el-table-column label="政治/外语" width="100">
        <template #default="{ row }">{{ row.politicalScore }} / {{ row.foreignScore }}</template>
      </el-table-column>
      <el-table-column label="业务课" width="100">
        <template #default="{ row }">{{ row.majorScore1 }} / {{ row.majorScore2 }}</template>
      </el-table-column>
      <el-table-column label="会员专享" width="90">
        <template #default="{ row }">
          <el-tag :type="row.premium ? 'warning' : 'success'" size="small">{{ row.premium ? '是' : '公开' }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分数线' : '新增分数线'" width="600px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="学校">
          <el-select v-model="form.schoolId" filterable style="width: 100%" placeholder="选择学校">
            <el-option v-for="s in schoolOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="年份"><el-input-number v-model="form.year" :min="2000" :max="2030" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="线类型">
              <el-select v-model="form.lineType" style="width: 100%">
                <el-option v-for="t in lineTypes" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="专业"><el-input v-model="form.major" placeholder="如：计算机科学与技术" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="总分"><el-input-number v-model="form.minScore" :min="0" :max="500" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="政治"><el-input-number v-model="form.politicalScore" :min="0" :max="100" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="外语"><el-input-number v-model="form.foreignScore" :min="0" :max="100" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="业务课一"><el-input-number v-model="form.majorScore1" :min="0" :max="150" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="业务课二"><el-input-number v-model="form.majorScore2" :min="0" :max="150" style="width: 100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
        <el-form-item label="会员专享">
          <el-switch v-model="form.premium" active-text="仅会员可见" inactive-text="公开" />
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
import { adminApi, publicApi } from '../../api'

const lineTypes = ['复试线', '国家线', '校线', '院线']
const filters = reactive({ schoolId: null, year: null })
const schoolOptions = ref([])
const yearOptions = ref([])
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
    const data = await adminApi.scoreLines({
      schoolId: filters.schoolId || undefined,
      year: filters.year || undefined,
      page: p,
      size
    })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, {
    schoolId: null, year: new Date().getFullYear(), major: '', lineType: '复试线',
    minScore: 350, politicalScore: 55, foreignScore: 55, majorScore1: 90, majorScore2: 90,
    remark: '', premium: false
  })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    if (form.id) await adminApi.updateScoreLine(form.id, form)
    else await adminApi.createScoreLine(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除 ${row.schoolName} ${row.year} ${row.major} 分数线？`, '警告', { type: 'warning' })
  await adminApi.deleteScoreLine(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  const [home, schoolData] = await Promise.all([publicApi.home(), adminApi.schools({ page: 1, size: 200 })])
  yearOptions.value = home.years || []
  schoolOptions.value = schoolData.list
  load(1)
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
