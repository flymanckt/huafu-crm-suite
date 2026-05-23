<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商机列表</span>
          <div class="header-actions">
            <el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" />
            <ExcelImportButton module-name="商机" :fields="importFields" :import-fn="importOpportunityRow" :export-rows="tableData" @done="loadData" />
            <el-button type="primary" size="small" @click="openCreate">新建商机</el-button>
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
      <BatchUpdateBar resource="opportunity" :fields="batchFields" :selected-rows="selectedRows" @clear="clearBatchSelection" @done="loadData" />
      <el-table ref="tableRef" :data="tableData" v-loading="loading" stripe border class="data-table" max-height="calc(100vh - 330px)" row-key="id" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" fixed />
        <el-table-column v-if="columnVisible('oppNo')" prop="oppNo" label="商机编号" width="150" />
        <el-table-column v-if="columnVisible('opportunityName')" prop="opportunityName" label="商机名称" min-width="180" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('customerName')" prop="customerName" label="客户" min-width="140" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('stage')" prop="stage" label="阶段" width="100">
          <template #default="{ row }">
            <el-tag :type="stageColor[row.stage]" size="small">{{ stageLabel[row.stage] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="columnVisible('estimatedAmount')" prop="estimatedAmount" label="预估金额(万)" width="130" align="right">
          <template #default="{ row }">{{ row.estimatedAmount ?? '-' }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('winProbability')" prop="winProbability" label="赢单率" width="80" align="center">
          <template #default="{ row }">{{ row.winProbability ?? '-' }}%</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('expectedCloseDate')" prop="expectedCloseDate" label="预计成交日" width="110" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push('/opportunity/'+row.id)">详情</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="success" @click="handleAdvance(row)" v-if="row.stage < 5">推进</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total>0" style="margin-top:16px;justify-content:flex-end"
        v-model:current-page="query.current" v-model:page-size="query.size"
        :total="total" layout="total, prev, pager, next" @page-change="loadData" @size-change="loadData" />
    </el-card>
    <el-dialog v-model="showDialog" :title="editingId ? '编辑商机' : '新建商机'" width="680">
      <el-form :model="form" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="商机名称"><el-input v-model="form.opportunityName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户ID"><el-input v-model="form.customerId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="负责人ID"><el-input v-model="form.handlerUserId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="阶段"><DictSelect v-model="form.stage" dict-code="opportunity_stage" value-type="number" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="产品名称"><el-input v-model="form.productName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预计成交日"><el-date-picker v-model="form.expectedCloseDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="数量"><el-input-number v-model="form.quantity" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="单位"><el-input v-model="form.unit" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="赢单率"><el-input-number v-model="form.winProbability" :min="0" :max="100" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预估金额"><el-input-number v-model="form.estimatedAmount" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="来源线索ID"><el-input v-model="form.sourceLeadId" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item></el-col>
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
import { getOpportunityPage, createOpportunity, updateOpportunity, deleteOpportunity, advanceOpportunityStage } from '@/api/opportunity'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import ExcelImportButton from '@/components/common/ExcelImportButton.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const tableRef = ref()
const selectedRows = ref([])
const showDialog = ref(false)
const editingId = ref(null)
const initialQuery = () => ({ current: 1, size: 20, stage: null, customerName: '', opportunityName: '', productName: '' })
const query = ref(initialQuery())
const pageCode = 'opportunity-list'
const filterFields = [
  { key: 'stage', label: '阶段', type: 'dict', dictCode: 'opportunity_stage', valueType: 'number', placement: 'default' },
  { key: 'customerName', label: '客户名称', placement: 'default' },
  { key: 'opportunityName', label: '商机名称', placement: 'more' },
  { key: 'productName', label: '产品', placement: 'more' }
]
const defaultColumns = [
  { key: 'oppNo', label: '商机编号', width: 150, visible: true, fixed: true },
  { key: 'opportunityName', label: '商机名称', width: 180, visible: true, fixed: true },
  { key: 'customerName', label: '客户', width: 140, visible: true },
  { key: 'stage', label: '阶段', width: 100, visible: true },
  { key: 'estimatedAmount', label: '预估金额(万)', width: 130, visible: true, align: 'right' },
  { key: 'winProbability', label: '赢单率', width: 80, visible: true },
  { key: 'expectedCloseDate', label: '预计成交日', width: 110, visible: true }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false

const stageOptions = [
  { label: '初步接触', value: 1 }, { label: '需求确认', value: 2 },
  { label: '方案报价', value: 3 }, { label: '合同谈判', value: 4 }, { label: '成交', value: 5 }
]
const stageLabel = { 1:'初步接触', 2:'需求确认', 3:'方案报价', 4:'合同谈判', 5:'成交' }
const stageColor = { 1:'info', 2:'', 3:'warning', 4:'danger', 5:'success' }
const batchFields = [
  { key: 'stage', label: '阶段', dictCode: 'opportunity_stage', valueType: 'number' },
  { key: 'productName', label: '产品' },
  { key: 'estimatedAmount', label: '预估金额', type: 'number', precision: 2 },
  { key: 'winProbability', label: '赢单率', type: 'number' },
  { key: 'expectedCloseDate', label: '预计成交日', type: 'date' },
  { key: 'remark', label: '备注' }
]
const importFields = [
  { key: 'opportunityName', label: '商机名称', required: true, example: '云泰棉纱年度合作' },
  { key: 'customerId', label: '客户ID', type: 'number' },
  { key: 'handlerUserId', label: '负责人ID', type: 'number' },
  { key: 'stage', label: '阶段', type: 'number', valueMap: { 初步接触: 1, 需求确认: 2, 方案报价: 3, 合同谈判: 4, 成交: 5 }, example: '初步接触' },
  { key: 'productName', label: '产品名称', example: '棉纱' },
  { key: 'quantity', label: '数量', type: 'number' },
  { key: 'unit', label: '单位', example: '吨' },
  { key: 'estimatedAmount', label: '预估金额', type: 'number' },
  { key: 'expectedCloseDate', label: '预计成交日', type: 'date', example: '2026-06-30' },
  { key: 'winProbability', label: '赢单率', type: 'number', example: '30' },
  { key: 'remark', label: '备注' }
]
const userInfo = () => JSON.parse(localStorage.getItem('userInfo') || '{}')
const defaultForm = () => ({ opportunityName: '', customerId: null, handlerUserId: userInfo().id || 1, stage: 1, productName: '', quantity: null, unit: '', estimatedAmount: null, expectedCloseDate: null, winProbability: 0, remark: '', sourceLeadId: null })
const form = ref(defaultForm())
const importOpportunityRow = (row) => createOpportunity({ ...defaultForm(), ...row })

const loadData = async () => {
  loading.value = true
  try { const r = await getOpportunityPage(query.value); tableData.value = r.records||[]; total.value = r.total||0 } finally { loading.value = false }
}

const resetQuery = () => {
  query.value = initialQuery()
  loadData()
}

const handleAdvance = async (row) => {
  const next = row.stage + 1
  await ElMessageBox.confirm(`确定将商机【${row.opportunityName}】推进到【${stageLabel[next]}】阶段？`, '推进确认')
  await advanceOpportunityStage(row.id, next)
  ElMessage.success('阶段推进成功')
  loadData()
}

const openCreate = () => {
  editingId.value = null
  form.value = defaultForm()
  showDialog.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  form.value = { ...defaultForm(), ...row, handlerUserId: row.handlerUserId || userInfo().id || 1 }
  showDialog.value = true
}

const handleSave = async () => {
  if (editingId.value) {
    await updateOpportunity(editingId.value, form.value)
    ElMessage.success('保存成功')
  } else {
    await createOpportunity(form.value)
    ElMessage.success('创建成功')
  }
  showDialog.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除商机【${row.opportunityName || row.oppNo || row.id}】？`, '删除确认', { type: 'warning' })
  await deleteOpportunity(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSelectionChange = (rows) => { selectedRows.value = rows }
const clearBatchSelection = () => { selectedRows.value = []; tableRef.value?.clearSelection?.() }

onMounted(loadData)
</script>

<style scoped>.card-header,.header-actions{display:flex;justify-content:space-between;align-items:center}.header-actions{gap:8px}</style>
