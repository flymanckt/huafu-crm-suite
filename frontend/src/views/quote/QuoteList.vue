<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报价单管理</span>
          <div class="header-actions">
            <el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" />
            <ExcelImportButton module-name="报价单" :fields="importFields" :import-fn="createQuote" :export-rows="tableData" @done="loadData" />
            <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新建报价单</el-button>
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

      <BatchUpdateBar resource="quote" :fields="batchFields" :selected-rows="selectedRows" @clear="clearBatchSelection" @done="loadData" />
      <el-table ref="tableRef" :data="tableData" v-loading="loading" stripe border class="data-table" max-height="calc(100vh - 360px)" row-key="id" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" fixed />
        <el-table-column v-if="columnVisible('quoteNo')" prop="quoteNo" label="报价单号" width="170" />
        <el-table-column v-if="columnVisible('customerName')" prop="customerName" label="客户名称" min-width="180" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('contactName')" prop="contactName" label="联系人" width="100" />
        <el-table-column v-if="columnVisible('contactPhone')" prop="contactPhone" label="联系电话" width="130" />
        <el-table-column v-if="columnVisible('totalAmount')" prop="totalAmount" label="报价总额" width="120" align="right">
          <template #default="{ row }">{{ row.totalAmount ? '¥' + row.totalAmount.toLocaleString() : '-' }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('status')" prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType[row.status]" size="small">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="columnVisible('validUntil')" prop="validUntil" label="有效期至" width="110">
          <template #default="{ row }">{{ row.validUntil || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('creatorName')" prop="creatorName" label="创建人" width="100" />
        <el-table-column v-if="columnVisible('createdTime')" prop="createdTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" @click="openEdit(row)" v-if="row.status === 1">编辑</el-button>
            <el-button link type="success" @click="handleSend(row)" v-if="row.status === 1">提交</el-button>
            <el-button link type="warning" @click="handleConfirm(row)" v-if="row.status === 2">确认</el-button>
            <el-button link type="danger" @click="handleCancel(row)" v-if="row.status === 2 || row.status === 3">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0" style="margin-top:16px;justify-content:flex-end"
        v-model:current-page="query.current" v-model:page-size="query.size"
        :total="total" :page-sizes="[10,20,50]"
        layout="total, sizes, prev, pager, next"
        @page-change="loadData" @size-change="loadData"
      />
    </el-card>

    <!-- 报价单详情/编辑 -->
    <el-drawer v-model="detailVisible" :title="isEditing ? '编辑报价单' : '报价单详情'" size="800px" destroy-on-close>
      <div v-if="currentQuote">
        <!-- 基本信息 -->
        <el-card shadow="never" style="margin-bottom:16px">
          <el-form :inline="true" :model="detailForm" label-width="90px" :disabled="!isEditing">
            <el-row :gutter="12">
              <el-col :span="12"><el-form-item label="客户名称"><el-input v-model="detailForm.customerName" placeholder="客户名称" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="联系人"><el-input v-model="detailForm.contactName" placeholder="联系人" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="detailForm.contactPhone" placeholder="电话" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="有效期至"><el-date-picker v-model="detailForm.validUntil" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="备注"><el-input v-model="detailForm.remark" type="textarea" :rows="2" /></el-form-item></el-col>
            </el-row>
          </el-form>
        </el-card>

        <!-- 明细行 -->
        <el-card shadow="never" style="margin-bottom:16px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span>报价明细</span>
              <el-button size="small" type="primary" @click="addItem" v-if="isEditing">新增行</el-button>
            </div>
          </template>
          <el-table :data="items" stripe border size="small">
            <el-table-column prop="productName" label="产品名称" width="180">
              <template #default="{ row, $index }">
                <el-input v-model="row.productName" placeholder="产品名称" :disabled="!isEditing" size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="productCode" label="产品编码" width="130">
              <template #default="{ row }">
                <el-input v-model="row.productCode" placeholder="编码" :disabled="!isEditing" size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="0" :precision="2" size="small" :disabled="!isEditing" style="width:80px" />
              </template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="80">
              <template #default="{ row }">
                <el-input v-model="row.unit" placeholder="单位" :disabled="!isEditing" size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="unitPrice" label="单价" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.unitPrice" :min="0" :precision="2" size="small" :disabled="!isEditing" style="width:100px" />
              </template>
            </el-table-column>
            <el-table-column prop="subtotal" label="小计" width="120" align="right">
              <template #default="{ row }">
                <span style="color:#409eff;font-weight:600">
                  ¥{{ ((row.quantity || 0) * (row.unitPrice || 0)).toLocaleString() }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" v-if="isEditing">
              <template #default="{ row, $index }">
                <el-button link type="danger" size="small" @click="removeItem($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div style="text-align:right;padding:12px 0;font-size:16px;color:#303133">
            报价总额：<span style="color:#409eff;font-weight:700;font-size:20px">¥{{ totalAmount.toLocaleString() }}</span>
          </div>
        </el-card>

        <div style="text-align:right">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleSaveDetail" v-if="isEditing">保存报价单</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 新建报价单弹窗（简化引导） -->
    <el-dialog v-model="createVisible" title="新建报价单" width="420px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" label-width="90px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="createForm.customerName" placeholder="客户名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="createForm.contactName" placeholder="联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="createForm.contactPhone" placeholder="电话" />
        </el-form-item>
        <el-form-item label="有效期至" prop="validUntil">
          <el-date-picker v-model="createForm.validUntil" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建并编辑明细</el-button>
      </template>
    </el-dialog>
    <ColumnConfigDrawer :page-code="pageCode" :default-columns="defaultColumns" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Setting } from '@element-plus/icons-vue'
import { getQuotePage, getQuoteDetail, createQuote, updateQuote, deleteQuote, sendQuote, confirmQuote, cancelQuote, getQuoteItems, addQuoteItem, updateQuoteItem, deleteQuoteItem } from '@/api/quote'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import ExcelImportButton from '@/components/common/ExcelImportButton.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

const statusMap = { 1: '草稿', 2: '已提交', 3: '已确认', 4: '已拒绝' }
const statusType = { 1: 'info', 2: 'primary', 3: 'success', 4: 'danger' }
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageCode = 'quote-list'
const filterFields = [
  { key: 'customerName', label: '客户', placement: 'default' },
  { key: 'status', label: '状态', type: 'dict', dictCode: 'quote_status', valueType: 'number', placement: 'default', width: 130 },
  { key: 'quoteNo', label: '报价单号', placement: 'more' },
  { key: 'contactName', label: '联系人', placement: 'more' },
  { key: 'creatorName', label: '创建人', placement: 'hidden' },
  { key: 'validUntil', label: '有效期至', type: 'date', placement: 'hidden' }
]
const defaultColumns = [
  { key: 'quoteNo', label: '报价单号', width: 170, visible: true, fixed: true },
  { key: 'customerName', label: '客户名称', width: 180, visible: true, fixed: true },
  { key: 'contactName', label: '联系人', width: 100, visible: true },
  { key: 'contactPhone', label: '联系电话', width: 130, visible: true },
  { key: 'totalAmount', label: '报价总额', width: 120, visible: true, align: 'right' },
  { key: 'status', label: '状态', width: 90, visible: true },
  { key: 'validUntil', label: '有效期至', width: 110, visible: true },
  { key: 'creatorName', label: '创建人', width: 100, visible: true },
  { key: 'createdTime', label: '创建时间', width: 160, visible: true }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false
const tableRef = ref()
const selectedRows = ref([])
const initialQuery = () => ({ current: 1, size: 20, customerName: '', status: null, quoteNo: '', contactName: '', creatorName: '', validUntil: '' })
const query = ref(initialQuery())
const detailVisible = ref(false)
const createVisible = ref(false)
const batchFields = [
  { key: 'status', label: '状态', dictCode: 'quote_status', valueType: 'number' },
  { key: 'quoteDate', label: '报价日期', type: 'date' },
  { key: 'validDate', label: '有效期至', type: 'date' },
  { key: 'discountRate', label: '折扣率', type: 'number', precision: 4 },
  { key: 'paymentTerms', label: '付款条款' },
  { key: 'deliveryTerms', label: '交付条款' },
  { key: 'remark', label: '备注' }
]
const importFields = [
  { key: 'customerName', label: '客户名称', required: true, example: '浙江云泰纺织有限公司' },
  { key: 'contactName', label: '联系人', example: '张三' },
  { key: 'contactPhone', label: '联系电话', example: '13800000000' },
  { key: 'validUntil', label: '有效期至', type: 'date', example: '2026-06-30' },
  { key: 'remark', label: '备注' }
]
const isEditing = ref(false)
const currentQuote = ref(null)
const currentQuoteId = ref(null)
const items = ref([])
const createFormRef = ref()
const createForm = reactive({ customerName: '', contactName: '', contactPhone: '', validUntil: '', remark: '' })

const totalAmount = computed(() => {
  return items.value.reduce((sum, item) => sum + (item.quantity || 0) * (item.unitPrice || 0), 0)
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getQuotePage(query.value)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

const resetQuery = () => {
  query.value = initialQuery()
  loadData()
}

const openCreate = () => {
  createForm.customerName = ''
  createForm.contactName = ''
  createForm.contactPhone = ''
  createForm.validUntil = ''
  createForm.remark = ''
  createVisible.value = true
}

const handleCreate = async () => {
  await createFormRef.value?.validate()
  const result = await createQuote({ ...createForm })
  createVisible.value = false
  ElMessage.success('报价单已创建')
  await openDetail({ id: result.id, status: 1 })
}

const openDetail = async (row) => {
  currentQuoteId.value = row.id
  const detail = await getQuoteDetail(row.id)
  currentQuote.value = detail
  detailForm.customerName = detail.customerName
  detailForm.contactName = detail.contactName || ''
  detailForm.contactPhone = detail.contactPhone || ''
  detailForm.validUntil = detail.validUntil || ''
  detailForm.remark = detail.remark || ''
  isEditing.value = row.status === 1
  // 加载明细
  items.value = await getQuoteItems(row.id)
  detailVisible.value = true
}

const openEdit = async (row) => {
  await openDetail(row)
  isEditing.value = true
}

const detailForm = reactive({ customerName: '', contactName: '', contactPhone: '', validUntil: '', remark: '' })

const addItem = () => {
  items.value.push({ id: null, productName: '', productCode: '', quantity: 1, unit: '件', unitPrice: 0 })
}

const removeItem = (index) => {
  const item = items.value[index]
  if (item.id) {
    deleteQuoteItem(item.id)
  }
  items.value.splice(index, 1)
}

const handleSaveDetail = async () => {
  // 保存基本信息
  await updateQuote(currentQuoteId.value, { ...detailForm, totalAmount: totalAmount.value })
  // 逐行保存明细
  for (const item of items.value) {
    if (item.id) {
      await updateQuoteItem(item.id, item)
    } else if (item.productName) {
      await addQuoteItem(currentQuoteId.value, item)
    }
  }
  ElMessage.success('保存成功')
  detailVisible.value = false
  loadData()
}

const handleSend = async (row) => {
  await ElMessageBox.confirm('确认提交该报价单？', '提交报价', { type: 'info' })
  await sendQuote(row.id)
  ElMessage.success('报价单已提交')
  loadData()
}

const handleConfirm = async (row) => {
  await ElMessageBox.confirm('确认该报价单？', '确认报价', { type: 'success' })
  await confirmQuote(row.id)
  ElMessage.success('报价单已确认')
  loadData()
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确定取消该报价单？', '取消报价', { type: 'warning' })
  await cancelQuote(row.id)
  ElMessage.success('报价单已取消')
  loadData()
}

const handleSelectionChange = (rows) => { selectedRows.value = rows }
const clearBatchSelection = () => { selectedRows.value = []; tableRef.value?.clearSelection?.() }

onMounted(loadData)
</script>

<style scoped>
.card-header, .header-actions { display: flex; justify-content: space-between; align-items: center; }
.header-actions { gap: 8px; }
.search-form { margin-bottom: 16px; }
</style>
