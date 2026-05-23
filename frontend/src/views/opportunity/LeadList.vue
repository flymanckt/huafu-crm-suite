<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>线索与商情</span>
          <div class="header-actions">
            <el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" />
            <ExcelImportButton module-name="线索" :fields="importFields" :import-fn="createLead" :export-rows="tableData" @done="loadData" />
            <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>录入线索</el-button>
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
      <BatchUpdateBar resource="lead" :fields="batchFields" :selected-rows="selectedRows" @clear="clearBatchSelection" @done="loadData" />
      <el-table ref="tableRef" :data="tableData" v-loading="loading" stripe border class="data-table" max-height="calc(100vh - 340px)" row-key="id" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" fixed />
        <el-table-column v-if="columnVisible('leadNo')" prop="leadNo" label="线索编号" width="150" />
        <el-table-column v-if="columnVisible('leadType')" prop="leadType" label="类型" width="80">
          <template #default="{ row }">{{ row.leadType===1?'商情':'线索' }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('customerName')" prop="customerName" label="客户名称" min-width="150" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('contactName')" prop="contactName" label="联系人" width="100" />
        <el-table-column v-if="columnVisible('productName')" prop="productName" label="意向产品" min-width="120" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('competitorName')" prop="competitorName" label="竞品" width="120" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('status')" prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="{1:'info',2:'warning',3:'success',4:'danger'}[row.status]" size="small">
              {{ {1:'新线索',2:'跟进中',3:'已转化',4:'无效'}[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="columnVisible('source')" prop="source" label="来源" width="90">
          <template #default="{ row }">{{ {1:'日报AI',2:'展会',3:'主动开发',4:'转介绍'}[row.source] || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('aiParsed')" label="AI解析" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.source === 1 || row.aiParsed === 1 || row.aiParsed === true" type="success" size="small">AI解析</el-tag>
            <el-tag v-else type="info" size="small">人工录入</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="columnVisible('createdTime')" prop="createdTime" label="创建时间" width="170" />
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

    <el-dialog v-model="showDialog" :title="editingId ? '编辑线索' : '录入线索'" width="620">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="类型"><DictSelect v-model="form.leadType" dict-code="lead_type" value-type="number" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="来源"><DictSelect v-model="form.source" dict-code="lead_source" value-type="number" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户名称"><el-input v-model="form.customerName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户ID"><el-input v-model="form.customerId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="省份"><el-input v-model="form.province" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="城市"><el-input v-model="form.city" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="意向产品"><el-input v-model="form.productName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="经办人ID"><el-input v-model="form.handlerUserId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="竞品名称"><el-input v-model="form.competitorName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="竞品价格"><el-input-number v-model="form.competitorPrice" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="竞品折扣"><el-input-number v-model="form.competitorDiscount" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="毛利率"><el-input-number v-model="form.marginRate" :precision="2" style="width:100%" /></el-form-item></el-col>
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
import { Plus, Setting } from '@element-plus/icons-vue'
import { getLeadPage, createLead, updateLead, deleteLead } from '@/api/opportunity'
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
const initialQuery = () => ({ current: 1, size: 20, leadType: null, status: null, customerName: '', productName: '', source: null })
const query = ref(initialQuery())
const pageCode = 'lead-list'
const filterFields = [
  { key: 'leadType', label: '类型', type: 'dict', dictCode: 'lead_type', valueType: 'number', placement: 'default' },
  { key: 'status', label: '状态', type: 'dict', dictCode: 'lead_status', valueType: 'number', placement: 'default' },
  { key: 'customerName', label: '客户名称', placement: 'more' },
  { key: 'productName', label: '意向产品', placement: 'more' },
  { key: 'source', label: '来源', type: 'dict', dictCode: 'lead_source', valueType: 'number', placement: 'more' }
]
const defaultColumns = [
  { key: 'leadNo', label: '线索编号', width: 150, visible: true, fixed: true },
  { key: 'leadType', label: '类型', width: 80, visible: true },
  { key: 'customerName', label: '客户名称', width: 150, visible: true },
  { key: 'contactName', label: '联系人', width: 100, visible: true },
  { key: 'productName', label: '意向产品', width: 120, visible: true },
  { key: 'competitorName', label: '竞品', width: 120, visible: true },
  { key: 'status', label: '状态', width: 90, visible: true },
  { key: 'source', label: '来源', width: 90, visible: true },
  { key: 'aiParsed', label: 'AI解析', width: 90, visible: true },
  { key: 'createdTime', label: '创建时间', width: 170, visible: true }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false
const showDialog = ref(false)
const editingId = ref(null)
const userInfo = () => JSON.parse(localStorage.getItem('userInfo') || '{}')
const defaultForm = () => ({ leadType: 2, customerId: null, customerName: '', contactName: '', contactPhone: '', province: '', city: '', productName: '', competitorName: '', competitorPrice: null, competitorDiscount: null, marginRate: null, source: 3, creatorUserId: userInfo().id || 1, handlerUserId: null, remark: '' })
const form = ref(defaultForm())
const batchFields = [
  { key: 'leadType', label: '类型', dictCode: 'lead_type', valueType: 'number' },
  { key: 'status', label: '状态', dictCode: 'lead_status', valueType: 'number' },
  { key: 'source', label: '来源', dictCode: 'lead_source', valueType: 'number' },
  { key: 'productName', label: '意向产品' },
  { key: 'competitorName', label: '竞品名称' },
  { key: 'remark', label: '备注' }
]
const importFields = [
  { key: 'leadType', label: '类型', type: 'number', valueMap: { 商情: 1, 线索: 2 }, example: '线索' },
  { key: 'customerName', label: '客户名称', required: true, example: '浙江云泰纺织有限公司' },
  { key: 'contactName', label: '联系人', example: '张三' },
  { key: 'contactPhone', label: '联系电话', example: '13800000000' },
  { key: 'province', label: '省份', example: '浙江省' },
  { key: 'city', label: '城市', example: '杭州市' },
  { key: 'productName', label: '意向产品', example: '棉纱' },
  { key: 'competitorName', label: '竞品名称' },
  { key: 'source', label: '来源', type: 'number', valueMap: { 日报AI: 1, 展会: 2, 主动开发: 3, 转介绍: 4 }, example: '主动开发' },
  { key: 'remark', label: '备注' }
]

const loadData = async () => {
  loading.value = true
  try { const r = await getLeadPage(query.value); tableData.value = r.records||[]; total.value = r.total||0 } finally { loading.value = false }
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
  form.value = { ...defaultForm(), ...row, creatorUserId: row.creatorUserId || userInfo().id || 1 }
  showDialog.value = true
}

const handleSave = async () => {
  if (editingId.value) {
    await updateLead(editingId.value, form.value)
    ElMessage.success('保存成功')
  } else {
    await createLead(form.value)
    ElMessage.success('录入成功')
  }
  showDialog.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除线索【${row.leadNo || row.customerName || row.id}】？`, '删除确认', { type: 'warning' })
  await deleteLead(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSelectionChange = (rows) => { selectedRows.value = rows }
const clearBatchSelection = () => { selectedRows.value = []; tableRef.value?.clearSelection?.() }

onMounted(loadData)
</script>

<style scoped>.card-header,.header-actions{display:flex;justify-content:space-between;align-items:center}.header-actions{gap:8px}</style>
