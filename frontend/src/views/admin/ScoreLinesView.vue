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
          <el-button type="warning" plain style="margin-left: 6px" @click="importVisible = true">批量导入</el-button>
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
        <el-form-item label="专业">
          <el-select v-model="form.major" filterable allow-create default-first-option style="width: 100%"
            placeholder="选择或输入工学一级学科，如：计算机科学与技术（0812）">
            <el-option v-for="d in engineeringSubjects" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
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
    <el-dialog v-model="importVisible" title="批量导入真实复试线" width="720px">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px"
        title="粘贴 CSV 文本（可从 Excel 复制），来源请填写学校官网 / 省教育考试院 / 研招网等官方渠道；平台不再使用国家线充数" />
      <div class="import-demo">
        <p class="text-muted" style="margin: 0">格式示例（首行为表头）：</p>
        <pre>schoolName,year,major,total,politicalScore,foreignScore,majorScore1,majorScore2,lineType,remark,source
清华大学,2026,计算机科学与技术（0812）,350,55,90,110,110,复试线,计算机系复试线,清华大学研究生院官网
清华大学,2025,软件工程（0835）,340,50,85,100,100,复试线,,清华大学研究生院官网</pre>
      </div>
      <el-input v-model="importCsv" type="textarea" :rows="8" placeholder="在此粘贴 CSV 数据…" />
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="doImport">导入</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi, publicApi } from '../../api'

const lineTypes = ['复试线', '国家线', '校线', '院线']
const engineeringSubjects = [
  '工学门类（0800）', '力学（0801）', '机械工程（0802）', '光学工程（0803）',
  '仪器科学与技术（0804）', '材料科学与工程（0805）', '冶金工程（0806）',
  '动力工程及工程热物理（0807）', '电气工程（0808）', '电子科学与技术（0809）',
  '信息与通信工程（0810）', '控制科学与工程（0811）', '计算机科学与技术（0812）',
  '建筑学（0813）', '土木工程（0814）', '水利工程（0815）', '测绘科学与技术（0816）',
  '化学工程与技术（0817）', '地质资源与地质工程（0818）', '矿业工程（0819）',
  '石油与天然气工程（0820）', '纺织科学与工程（0821）', '轻工技术与工程（0822）',
  '交通运输工程（0823）', '船舶与海洋工程（0824）', '航空宇航科学与技术（0825）',
  '兵器科学与技术（0826）', '核科学与技术（0827）', '农业工程（0828）',
  '林业工程（0829）', '环境科学与工程（0830）', '生物医学工程（0831）',
  '食品科学与工程（0832）', '城乡规划学（0833）', '软件工程（0835）',
  '生物工程（0836）', '安全科学与工程（0837）', '公安技术（0838）', '网络空间安全（0839）'
]
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
const importVisible = ref(false)
const importCsv = ref('')
const importing = ref(false)

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

async function doImport() {
  if (!importCsv.value.trim()) {
    ElMessage.warning('请先粘贴 CSV 数据')
    return
  }
  importing.value = true
  try {
    const res = await adminApi.importScoreLines({ csv: importCsv.value })
    ElMessage.success(`导入成功 ${res.success} 条`)
    if (res.errors && res.errors.length) {
      ElMessageBox.alert(`以下行未导入：\n${res.errors.slice(0, 20).join('\n')}`, '部分行失败', { type: 'warning' })
    }
    importVisible.value = false
    importCsv.value = ''
    load()
  } finally {
    importing.value = false
  }
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

.import-demo {
  background: #f7f8fa;
  border-radius: 6px;
  padding: 8px 12px;
  margin-bottom: 10px;
}

.import-demo pre {
  margin: 4px 0 0;
  font-size: 12px;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
