<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>丢单记录</span>
          <div class="header-actions">
            <el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" />
            <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新增丢单</el-button>
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
      <BatchUpdateBar resource="lost-order" :fields="batchFields" :selected-rows="selectedRows" @clear="clearBatchSelection" @done="loadData" />
      <el-table ref="tableRef" :data="tableData" v-loading="loading" stripe border class="data-table" max-height="calc(100vh - 330px)" row-key="id" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" fixed />
        <el-table-column v-if="columnVisible('lostNo')" prop="lostNo" label="丢单编号" width="150" />
        <el-table-column v-if="columnVisible('customerId')" prop="customerId" label="客户ID" width="90" />
        <el-table-column v-if="columnVisible('lostType')" prop="lostType" label="丢单原因" width="90">
          <template #default="{ row }">{{ lostTypeMap[row.lostType] || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('competitorName')" prop="competitorName" label="竞品" width="120" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('competitorPrice')" prop="competitorPrice" label="竞品价格" width="110" align="right" />
        <el-table-column v-if="columnVisible('ourPrice')" prop="ourPrice" label="我方报价" width="110" align="right" />
        <el-table-column v-if="columnVisible('marginDiff')" prop="marginDiff" label="毛利差" width="90" align="right" />
        <el-table-column v-if="columnVisible('recoveryPossible')" prop="recoveryPossible" label="挽回可能" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.recoveryPossible===1" type="warning" size="small">可能</el-tag>
            <el-tag v-else-if="row.recoveryPossible===2" type="danger" size="small">不可能</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column v-if="columnVisible('reasonDetail')" prop="reasonDetail" label="详细原因" min-width="200" show-overflow-tooltip />
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

    <el-dialog v-model="showDialog" :title="editingId ? '编辑丢单记录' : '新增丢单记录'" width="680">
      <el-form :model="form" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="商机ID"><el-input v-model="form.opportunityId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户ID"><el-input v-model="form.customerId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="丢单原因"><DictSelect v-model="form.lostType" dict-code="lost_type" value-type="number" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="挽回可能"><DictSelect v-model="form.recoveryPossible" dict-code="recovery_possible" value-type="number" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="竞品名称"><el-input v-model="form.competitorName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="跟进日期"><el-date-picker v-model="form.followUpDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="竞品价格"><el-input-number v-model="form.competitorPrice" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="我方报价"><el-input-number v-model="form.ourPrice" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="竞品折扣"><el-input-number v-model="form.competitorDiscount" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="我方折扣"><el-input-number v-model="form.ourDiscount" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="毛利差"><el-input-number v-model="form.marginDiff" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="经办人ID"><el-input v-model="form.handlerUserId" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="详细原因"><el-input v-model="form.reasonDetail" type="textarea" :rows="3" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
    <ColumnConfigDrawer :page-code="pageCode" :default-columns="defaultColumns" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Setting } from '@element-plus/icons-vue'
import { getLostOrderPage, createLostOrder, updateLostOrder, deleteLostOrder } from '@/api/opportunity'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const tableRef = ref()
const selectedRows = ref([])
const initialQuery = () => ({ current: 1, size: 20, lostType: null, customerName: '', competitorName: '', recoveryPossible: null })
const query = ref(initialQuery())
const pageCode = 'lost-order-list'
const filterFields = [
  { key: 'lostType', label: '丢单原因', type: 'dict', dictCode: 'lost_type', valueType: 'number', placement: 'default' },
  { key: 'customerName', label: '客户', placement: 'more' },
  { key: 'competitorName', label: '竞品', placement: 'more' },
  { key: 'recoveryPossible', label: '挽回可能', type: 'dict', dictCode: 'recovery_possible', valueType: 'number', placement: 'more' }
]
const defaultColumns = [
  { key: 'lostNo', label: '丢单编号', width: 150, visible: true, fixed: true },
  { key: 'customerId', label: '客户ID', width: 90, visible: true },
  { key: 'lostType', label: '丢单原因', width: 90, visible: true },
  { key: 'competitorName', label: '竞品', width: 120, visible: true },
  { key: 'competitorPrice', label: '竞品价格', width: 110, visible: true, align: 'right' },
  { key: 'ourPrice', label: '我方报价', width: 110, visible: true, align: 'right' },
  { key: 'marginDiff', label: '毛利差', width: 90, visible: true, align: 'right' },
  { key: 'recoveryPossible', label: '挽回可能', width: 90, visible: true },
  { key: 'reasonDetail', label: '详细原因', width: 200, visible: true }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false
const lostTypeMap = { 1:'价格', 2:'交期', 3:'质量', 4:'现货', 5:'其他' }
const showDialog = ref(false)
const editingId = ref(null)
const defaultForm = () => ({ opportunityId: null, customerId: null, lostType: 1, competitorName: '', competitorPrice: null, ourPrice: null, marginDiff: null, competitorDiscount: null, ourDiscount: null, reasonDetail: '', recoveryPossible: null, followUpDate: null, handlerUserId: null })
const form = ref(defaultForm())
const batchFields = [
  { key: 'lostType', label: '丢单原因', dictCode: 'lost_type', valueType: 'number' },
  { key: 'recoveryPossible', label: '挽回可能', dictCode: 'recovery_possible', valueType: 'number' },
  { key: 'followUpDate', label: '跟进日期', type: 'date' },
  { key: 'competitorName', label: '竞品' },
  { key: 'reasonDetail', label: '详细原因' }
]

const loadData = async () => {
  loading.value = true
  try { const r = await getLostOrderPage(query.value); tableData.value = r.records||[]; total.value = r.total||0 } finally { loading.value = false }
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
    await updateLostOrder(editingId.value, form.value)
    ElMessage.success('保存成功')
  } else {
    await createLostOrder(form.value)
    ElMessage.success('创建成功')
  }
  showDialog.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除丢单记录【${row.lostNo || row.id}】？`, '删除确认', { type: 'warning' })
  await deleteLostOrder(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSelectionChange = (rows) => { selectedRows.value = rows }
const clearBatchSelection = () => { selectedRows.value = []; tableRef.value?.clearSelection?.() }

onMounted(loadData)
</script>

<style scoped>.card-header,.header-actions{display:flex;justify-content:space-between;align-items:center}.header-actions{gap:8px}</style>
