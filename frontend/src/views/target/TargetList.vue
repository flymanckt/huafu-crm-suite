<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>目标管理</span>
          <div class="header-actions">
            <el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" />
            <ExcelImportButton module-name="目标" :fields="importFields" :import-fn="importTargetRow" :export-rows="tableData" @done="loadData" />
            <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新增目标</el-button>
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
      <BatchUpdateBar resource="target" :fields="batchFields" :selected-rows="selectedRows" @clear="clearBatchSelection" @done="loadData" />
      <el-table ref="tableRef" :data="tableData" v-loading="loading" stripe border class="data-table" max-height="calc(100vh - 340px)" row-key="id" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" fixed />
        <el-table-column v-if="columnVisible('targetNo')" prop="targetNo" label="目标编号" width="150" />
        <el-table-column v-if="columnVisible('targetYear')" prop="targetYear" label="年" width="70" />
        <el-table-column v-if="columnVisible('targetMonth')" prop="targetMonth" label="月" width="60" />
        <el-table-column v-if="columnVisible('productCategory')" prop="productCategory" label="产品品类" min-width="120" />
        <el-table-column v-if="columnVisible('metricType')" prop="metricType" label="指标" width="90">
          <template #default="{ row }">{{ metricMap[row.metricType] }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('targetValue')" prop="targetValue" label="目标值" width="120" align="right" />
        <el-table-column v-if="columnVisible('achieveValue')" label="达成值" width="120" align="right">
          <template #default="{ row }">{{ row.achieveValue ?? '-' }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('achieveRate')" label="达成率" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.achieveRate != null" :style="{color: row.achieveRate >= 100 ? '#67c23a' : '#f56c6c'}">
              {{ row.achieveRate }}%
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total>0" style="margin-top:16px;justify-content:flex-end"
        v-model:current-page="query.current" v-model:page-size="query.size"
        :total="total" layout="total, prev, pager, next" @page-change="loadData" @size-change="loadData" />
    </el-card>

    <el-dialog v-model="showDialog" :title="editingId ? '编辑目标' : '新增目标'" width="560">
      <el-form :model="form" label-width="100px">
        <el-form-item label="年份"><el-input-number v-model="form.targetYear" :min="2020" :max="2030" /></el-form-item>
        <el-form-item label="月份"><el-input-number v-model="form.targetMonth" :min="1" :max="12" /></el-form-item>
        <el-form-item label="部门ID"><el-input v-model="form.deptId" /></el-form-item>
        <el-form-item label="负责人ID"><el-input v-model="form.userId" /></el-form-item>
        <el-form-item label="产品品类"><el-input v-model="form.productCategory" /></el-form-item>
        <el-form-item label="指标类型">
          <DictSelect v-model="form.metricType" dict-code="target_metric_type" value-type="number" />
        </el-form-item>
        <el-form-item label="目标值"><el-input-number v-model="form.targetValue" :precision="2" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog=false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
    <ColumnConfigDrawer :page-code="pageCode" :default-columns="defaultColumns" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Setting } from '@element-plus/icons-vue'
import { getTargetPage, createTarget, updateTarget, deleteTarget } from '@/api/target'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import ExcelImportButton from '@/components/common/ExcelImportButton.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const tableRef = ref()
const selectedRows = ref([])
const now = new Date()
const initialQuery = () => ({ current:1, size:20, year: now.getFullYear(), month: now.getMonth()+1, metricType:null, productCategory:'' })
const query = ref(initialQuery())
const pageCode = 'target-list'
const filterFields = [
  { key: 'year', label: '年份', type: 'number', min: 2020, max: 2030, width: 130, placement: 'default' },
  { key: 'month', label: '月份', type: 'number', min: 1, max: 12, width: 120, placement: 'default' },
  { key: 'metricType', label: '指标类型', type: 'dict', dictCode: 'target_metric_type', valueType: 'number', placement: 'default' },
  { key: 'productCategory', label: '产品品类', placement: 'more' }
]
const defaultColumns = [
  { key: 'targetNo', label: '目标编号', width: 150, visible: true, fixed: true },
  { key: 'targetYear', label: '年', width: 70, visible: true },
  { key: 'targetMonth', label: '月', width: 60, visible: true },
  { key: 'productCategory', label: '产品品类', width: 120, visible: true },
  { key: 'metricType', label: '指标', width: 90, visible: true },
  { key: 'targetValue', label: '目标值', width: 120, visible: true, align: 'right' },
  { key: 'achieveValue', label: '达成值', width: 120, visible: true, align: 'right' },
  { key: 'achieveRate', label: '达成率', width: 100, visible: true }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false
const showDialog = ref(false)
const editingId = ref(null)
const defaultForm = () => ({ targetYear: now.getFullYear(), targetMonth: now.getMonth()+1, deptId: null, userId: null, productCategory:'', metricType:1, targetValue:null })
const form = ref(defaultForm())
const metricMap = { 1:'销售额', 2:'毛利', 3:'客户数', 4:'拜访次数' }
const importFields = [
  { key: 'targetYear', label: '年份', type: 'number', required: true, example: now.getFullYear() },
  { key: 'targetMonth', label: '月份', type: 'number', required: true, example: now.getMonth() + 1 },
  { key: 'deptId', label: '部门ID', type: 'number' },
  { key: 'userId', label: '负责人ID', type: 'number' },
  { key: 'productCategory', label: '产品品类', example: '棉纱' },
  { key: 'metricType', label: '指标类型', type: 'number', valueMap: { 销售额: 1, 毛利: 2, 客户数: 3, 拜访次数: 4 }, example: '销售额' },
  { key: 'targetValue', label: '目标值', type: 'number', required: true, example: '1000' }
]
const importTargetRow = (row) => createTarget({ ...defaultForm(), ...row })
const batchFields = [
  { key: 'targetYear', label: '年份', type: 'number' },
  { key: 'targetMonth', label: '月份', type: 'number' },
  { key: 'productCategory', label: '产品品类' },
  { key: 'metricType', label: '指标类型', dictCode: 'target_metric_type', valueType: 'number' },
  { key: 'targetValue', label: '目标值', type: 'number', precision: 2 }
]

const loadData = async () => {
  loading.value = true
  try { const r = await getTargetPage(query.value); tableData.value = r.records||[]; total.value = r.total||0 } finally { loading.value = false }
}

const resetQuery = () => {
  query.value = initialQuery()
  loadData()
}

const openCreate = () => {
  editingId.value = null
  form.value = defaultForm()
  showDialog.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  form.value = { ...defaultForm(), ...row }
  showDialog.value = true
}

const handleSave = async () => {
  if (editingId.value) {
    await updateTarget(editingId.value, form.value)
    ElMessage.success('保存成功')
  } else {
    await createTarget(form.value)
    ElMessage.success('创建成功')
  }
  showDialog.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除目标【${row.targetNo || row.id}】？`, '删除确认', { type: 'warning' })
  await deleteTarget(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSelectionChange = (rows) => { selectedRows.value = rows }
const clearBatchSelection = () => { selectedRows.value = []; tableRef.value?.clearSelection?.() }

onMounted(loadData)
</script>

<style scoped>.card-header,.header-actions{display:flex;justify-content:space-between;align-items:center}.header-actions{gap:8px}</style>
