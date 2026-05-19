<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>日报列表</span>
          <div class="header-actions">
            <el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" />
            <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新增日报</el-button>
          </div>
        </div>
      </template>
      <ConfigurableFilterForm
        v-model="query"
        :page-code="pageCode"
        :default-filters="filterFields"
        @search="loadData"
        @reset="resetQuery"
      />
      <BatchUpdateBar resource="daily-report" :fields="batchFields" :selected-rows="selectedRows" @clear="clearBatchSelection" @done="loadData" />
      <el-table ref="tableRef" :data="tableData" v-loading="loading" stripe border class="data-table" max-height="calc(100vh - 340px)" row-key="id" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" fixed />
        <el-table-column v-if="columnVisible('reportNo')" prop="reportNo" label="日报编号" width="150" />
        <el-table-column v-if="columnVisible('userName')" prop="userName" label="提交人" width="100" />
        <el-table-column v-if="columnVisible('reportDate')" prop="reportDate" label="日报日期" width="110" />
        <el-table-column v-if="columnVisible('contentText')" prop="contentText" label="内容" min-width="250" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('parseStatus')" prop="parseStatus" label="解析状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="parseStatusType[row.parseStatus]" size="small">{{ parseStatusLabel[row.parseStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="columnVisible('aiExtract')" label="AI提取" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.商机Count">商机{{ row.商机Count }}</span>
            <span v-if="row.商情Count"> 商情{{ row.商情Count }}</span>
            <span v-if="row.丢单Count"> 丢单{{ row.丢单Count }}</span>
            <span v-if="!row.商机Count && !row.商情Count && !row.丢单Count">-</span>
          </template>
        </el-table-column>
        <el-table-column v-if="columnVisible('parseError')" prop="parseError" label="错误信息" width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total>0" style="margin-top:16px;justify-content:flex-end"
        v-model:current-page="query.current" v-model:page-size="query.size"
        :total="total" layout="total, prev, pager, next" @page-change="loadData" @size-change="loadData" />
    </el-card>

    <el-dialog v-model="showDialog" :title="editingId ? '编辑日报' : '新增日报'" width="660">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="日报日期"><el-date-picker v-model="form.reportDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="提交人ID"><el-input v-model="form.userId" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="日报内容"><el-input v-model="form.contentText" type="textarea" :rows="8" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 日报详情对话框 -->
    <el-dialog v-model="showDetail" title="日报详情" width="700">
      <el-descriptions :column="2" border size="small" v-if="currentReport">
        <el-descriptions-item label="编号">{{ currentReport.reportNo }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ currentReport.reportDate }}</el-descriptions-item>
        <el-descriptions-item label="解析状态">
          <el-tag :type="parseStatusType[currentReport.parseStatus]" size="small">{{ parseStatusLabel[currentReport.parseStatus] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="解析时间">{{ currentReport.parseTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">日报原文</el-divider>
      <div class="raw-text" v-if="currentReport">{{ currentReport.contentText }}</div>
      <el-divider content-position="left" v-if="currentReport && currentReport.parsedJson">AI解析结果</el-divider>
      <pre class="parsed-json" v-if="currentReport && currentReport.parsedJson">{{ formatJson(currentReport.parsedJson) }}</pre>
    </el-dialog>
    <ColumnConfigDrawer :page-code="pageCode" :default-columns="defaultColumns" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDailyReportPage, getDailyReportDetail, createDailyReport, updateDailyReport, deleteDailyReport } from '@/api/performance'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { Plus, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const tableRef = ref()
const selectedRows = ref([])
const initialQuery = () => ({ current:1, size:20, dateRange:null, parseStatus:null, userName:'', contentText:'' })
const query = ref(initialQuery())
const pageCode = 'daily-report-list'
const filterFields = [
  { key: 'dateRange', label: '日期范围', type: 'daterange', width: 260, placement: 'default' },
  { key: 'parseStatus', label: '解析状态', type: 'dict', dictCode: 'daily_report_parse_status', valueType: 'number', placement: 'default' },
  { key: 'userName', label: '提交人', placement: 'more' },
  { key: 'contentText', label: '内容', placement: 'more', width: 220 }
]
const defaultColumns = [
  { key: 'reportNo', label: '日报编号', width: 150, visible: true, fixed: true },
  { key: 'userName', label: '提交人', width: 100, visible: true },
  { key: 'reportDate', label: '日报日期', width: 110, visible: true },
  { key: 'contentText', label: '内容', width: 250, visible: true },
  { key: 'parseStatus', label: '解析状态', width: 90, visible: true },
  { key: 'aiExtract', label: 'AI提取', width: 120, visible: true },
  { key: 'parseError', label: '错误信息', width: 150, visible: true }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false
const showDetail = ref(false)
const currentReport = ref(null)
const showDialog = ref(false)
const editingId = ref(null)
const today = new Date().toISOString().slice(0, 10)
const userInfo = () => JSON.parse(localStorage.getItem('userInfo') || '{}')
const defaultForm = () => ({ userId: userInfo().id || 1, reportDate: today, contentText: '' })
const form = ref(defaultForm())

const parseStatusLabel = { 0:'未解析', 1:'解析中', 2:'成功', 3:'失败' }
const parseStatusType = { 0:'info', 1:'warning', 2:'success', 3:'danger' }
const batchFields = [
  { key: 'parseStatus', label: '解析状态', dictCode: 'daily_report_parse_status', valueType: 'number' },
  { key: 'reportDate', label: '日报日期', type: 'date' },
  { key: 'parseError', label: '错误信息' }
]

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...query.value }
    if (params.dateRange && params.dateRange.length === 2) {
      params.startDate = params.dateRange[0]
      params.endDate = params.dateRange[1]
    }
    delete params.dateRange
    const r = await getDailyReportPage(params)
    tableData.value = r.records||[]; total.value = r.total||0
  } finally { loading.value = false }
}

const resetQuery = () => {
  query.value = initialQuery()
  loadData()
}

const viewDetail = async (row) => {
  currentReport.value = await getDailyReportDetail(row.id)
  showDetail.value = true
}

const openCreate = () => {
  editingId.value = null
  form.value = defaultForm()
  showDialog.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  form.value = { ...defaultForm(), ...row, userId: row.userId || userInfo().id || 1 }
  showDialog.value = true
}

const handleSave = async () => {
  if (editingId.value) {
    await updateDailyReport(editingId.value, form.value)
    ElMessage.success('保存成功')
  } else {
    await createDailyReport(form.value)
    ElMessage.success('创建成功')
  }
  showDialog.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除日报【${row.reportNo || row.id}】？`, '删除确认', { type: 'warning' })
  await deleteDailyReport(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const formatJson = (j) => {
  try { return JSON.stringify(typeof j === 'string' ? JSON.parse(j) : j, null, 2) } catch { return j }
}

const handleSelectionChange = (rows) => { selectedRows.value = rows }
const clearBatchSelection = () => { selectedRows.value = []; tableRef.value?.clearSelection?.() }

onMounted(loadData)
</script>

<style scoped>.card-header,.header-actions{display:flex;justify-content:space-between;align-items:center}.header-actions{gap:8px}.raw-text{font-size:13px;color:#606266;white-space:pre-wrap;line-height:1.6;background:#f5f7fa;padding:12px;border-radius:4px;max-height:200px;overflow-y:auto}.parsed-json{font-size:12px;background:#1d1e2c;color:#a0e9a0;padding:12px;border-radius:4px;max-height:300px;overflow:auto}</style>
