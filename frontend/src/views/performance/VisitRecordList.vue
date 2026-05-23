<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>拜访记录</span>
          <div class="header-actions">
            <el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" />
            <ExcelImportButton module-name="拜访记录" :fields="importFields" :import-fn="importVisitRow" :export-rows="tableData" @done="loadData" />
            <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新增拜访</el-button>
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
      <BatchUpdateBar resource="visit-record" :fields="batchFields" :selected-rows="selectedRows" @clear="clearBatchSelection" @done="loadData" />
      <el-table ref="tableRef" :data="tableData" v-loading="loading" stripe border class="data-table" max-height="calc(100vh - 340px)" row-key="id" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" fixed />
        <el-table-column v-if="columnVisible('visitNo')" prop="visitNo" label="编号" width="150" />
        <el-table-column v-if="columnVisible('visitDate')" prop="visitDate" label="拜访日期" width="110" />
        <el-table-column v-if="columnVisible('visitType')" prop="visitType" label="类型" width="90">
          <template #default="{ row }">{{ {1:'上门',2:'电话',3:'微信',4:'展会'}[row.visitType] }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('customerName')" prop="customerName" label="客户" min-width="140" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('contactName')" prop="contactName" label="联系人" width="100" />
        <el-table-column v-if="columnVisible('visitPurpose')" prop="visitPurpose" label="拜访目的" min-width="150" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('visitContent')" prop="visitContent" label="拜访内容" min-width="200" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('locationName')" prop="locationName" label="地点" width="120" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('checkinResult')" prop="checkinResult" label="地址校验" width="110">
          <template #default="{ row }">
            <el-tag :type="checkinTagType[row.checkinResult] || 'info'" size="small">{{ checkinResultText[row.checkinResult] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="columnVisible('checkinDistanceMeters')" prop="checkinDistanceMeters" label="距离" width="90">
          <template #default="{ row }">{{ row.checkinDistanceMeters != null ? `${row.checkinDistanceMeters}m` : '-' }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('isNewCustomer')" prop="isNewCustomer" label="新客户" width="70" align="center">
          <template #default="{ row }"><el-tag v-if="row.isNewCustomer===1" type="success" size="small">是</el-tag><span v-else>否</span></template>
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

    <el-dialog v-model="showDialog" :title="editingId ? '编辑拜访记录' : '新增拜访记录'" width="680">
      <el-form :model="form" label-width="100px">
        <el-form-item label="拜访日期"><el-date-picker v-model="form.visitDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="拜访类型"><DictSelect v-model="form.visitType" dict-code="visit_type" value-type="number" /></el-form-item>
        <el-form-item label="客户ID"><el-input v-model="form.customerId" @change="loadCustomerAddresses" placeholder="输入客户ID后可选择客户地址校验" /></el-form-item>
        <el-form-item label="客户名称"><el-input v-model="form.customerName" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
        <el-form-item label="拜访目的"><el-input v-model="form.visitPurpose" /></el-form-item>
        <el-form-item label="拜访内容"><el-input v-model="form.visitContent" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="校验地址">
          <el-select v-model="form.customerAddressId" clearable placeholder="选择客户主数据地址" style="width:100%" :disabled="!customerAddresses.length">
            <el-option
              v-for="addr in customerAddresses"
              :key="addr.id"
              :label="formatAddressOption(addr)"
              :value="addr.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="打卡位置">
          <el-input v-model="form.checkinAddress" placeholder="浏览器定位后自动填写，可人工修正">
            <template #append><el-button @click="locateForCheckin" :loading="locating">定位</el-button></template>
          </el-input>
        </el-form-item>
        <el-form-item label="坐标">
          <el-input :model-value="formatCheckinLocation()" disabled />
        </el-form-item>
        <el-form-item label="下次拜访"><el-date-picker v-model="form.nextVisitPlan" type="date" value-format="YYYY-MM-DD" /></el-form-item>
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
import { getVisitRecordPage, createVisitRecord, updateVisitRecord, deleteVisitRecord } from '@/api/performance'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import ExcelImportButton from '@/components/common/ExcelImportButton.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { getCustomerAddressList } from '@/api/customer'
import { regeocode } from '@/utils/amap'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const tableRef = ref()
const selectedRows = ref([])
const initialQuery = () => ({ current:1, size:20, visitDateRange:null, visitType:null, customerName:'', contactName:'', checkinResult:null, isNewCustomer:null })
const query = ref(initialQuery())
const pageCode = 'visit-record-list'
const filterFields = [
  { key: 'visitDateRange', label: '拜访日期', type: 'daterange', width: 260, placement: 'default' },
  { key: 'visitType', label: '拜访类型', type: 'dict', dictCode: 'visit_type', valueType: 'number', placement: 'default' },
  { key: 'customerName', label: '客户', placement: 'more' },
  { key: 'contactName', label: '联系人', placement: 'more' },
  { key: 'checkinResult', label: '地址校验', type: 'dict', dictCode: 'checkin_result', placement: 'more' },
  { key: 'isNewCustomer', label: '新客户', type: 'dict', dictCode: 'yes_no', valueType: 'number', placement: 'more' }
]
const defaultColumns = [
  { key: 'visitNo', label: '编号', width: 150, visible: true, fixed: true },
  { key: 'visitDate', label: '拜访日期', width: 110, visible: true },
  { key: 'visitType', label: '类型', width: 90, visible: true },
  { key: 'customerName', label: '客户', width: 140, visible: true },
  { key: 'contactName', label: '联系人', width: 100, visible: true },
  { key: 'visitPurpose', label: '拜访目的', width: 150, visible: true },
  { key: 'visitContent', label: '拜访内容', width: 200, visible: true },
  { key: 'locationName', label: '地点', width: 120, visible: true },
  { key: 'checkinResult', label: '地址校验', width: 110, visible: true },
  { key: 'checkinDistanceMeters', label: '距离', width: 90, visible: true },
  { key: 'isNewCustomer', label: '新客户', width: 70, visible: true }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false
const showDialog = ref(false)
const editingId = ref(null)
const today = new Date().toISOString().slice(0,10)
const locating = ref(false)
const customerAddresses = ref([])
const userInfo = () => JSON.parse(localStorage.getItem('userInfo') || '{}')
const defaultForm = () => ({
  userId: 1,
  visitDate: today,
  visitType: 1,
  customerId: null,
  customerName: '',
  contactName: '',
  visitPurpose: '',
  visitContent: '',
  customerAddressId: null,
  checkinLongitude: null,
  checkinLatitude: null,
  checkinAddress: '',
  longitude: null,
  latitude: null,
  locationName: '',
  nextVisitPlan: null,
  isNewCustomer: 0,
  remark: ''
})
const form = ref(defaultForm())
const importFields = [
  { key: 'visitDate', label: '拜访日期', type: 'date', required: true, example: today },
  { key: 'visitType', label: '拜访类型', type: 'number', valueMap: { 上门: 1, 电话: 2, 微信: 3, 展会: 4 }, example: '上门' },
  { key: 'customerId', label: '客户ID', type: 'number' },
  { key: 'customerName', label: '客户名称', required: true, example: '浙江云泰纺织有限公司' },
  { key: 'contactName', label: '联系人', example: '张三' },
  { key: 'visitPurpose', label: '拜访目的', example: '年度合作沟通' },
  { key: 'visitContent', label: '拜访内容' },
  { key: 'checkinAddress', label: '打卡地址' },
  { key: 'nextVisitPlan', label: '下次拜访', type: 'date' },
  { key: 'isNewCustomer', label: '新客户', type: 'number', valueMap: { 是: 1, 否: 0 } },
  { key: 'remark', label: '备注' }
]
const importVisitRow = (row) => createVisitRecord({ ...defaultForm(), userId: userInfo().id || 1, ...row })

const checkinResultText = { MATCHED: '通过', MISMATCHED: '超距', NO_ADDRESS: '无地址', UNCHECKED: '未校验' }
const checkinTagType = { MATCHED: 'success', MISMATCHED: 'danger', NO_ADDRESS: 'warning', UNCHECKED: 'info' }
const batchFields = [
  { key: 'visitType', label: '拜访类型', dictCode: 'visit_type', valueType: 'number' },
  { key: 'visitPurpose', label: '拜访目的' },
  { key: 'visitContent', label: '拜访内容' },
  { key: 'nextVisitPlan', label: '下次拜访', type: 'date' },
  { key: 'isNewCustomer', label: '新客户', dictCode: 'yes_no', valueType: 'number' },
  { key: 'remark', label: '备注' }
]

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...query.value }
    if (params.visitDateRange && params.visitDateRange.length === 2) {
      params.visitDateStart = params.visitDateRange[0]
      params.visitDateEnd = params.visitDateRange[1]
    }
    delete params.visitDateRange
    const r = await getVisitRecordPage(params)
    tableData.value = r.records||[]; total.value = r.total||0
  } finally { loading.value = false }
}

const resetQuery = () => {
  query.value = initialQuery()
  loadData()
}

const openCreate = () => {
  editingId.value = null
  customerAddresses.value = []
  form.value = { ...defaultForm(), userId: userInfo().id || 1 }
  showDialog.value = true
}

const openEdit = async (row) => {
  editingId.value = row.id
  form.value = { ...defaultForm(), ...row, userId: row.userId || userInfo().id || 1 }
  showDialog.value = true
  await loadCustomerAddresses()
}

const handleSave = async () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  form.value.userId = userInfo.id || form.value.userId || 1
  form.value.longitude = form.value.checkinLongitude
  form.value.latitude = form.value.checkinLatitude
  form.value.locationName = form.value.checkinAddress
  if (editingId.value) {
    await updateVisitRecord(editingId.value, form.value)
    ElMessage.success('保存成功')
  } else {
    await createVisitRecord(form.value)
    ElMessage.success('记录成功')
  }
  showDialog.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除拜访记录【${row.visitNo || row.id}】？`, '删除确认', { type: 'warning' })
  await deleteVisitRecord(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const loadCustomerAddresses = async () => {
  customerAddresses.value = []
  if (!form.value.customerId) return
  customerAddresses.value = await getCustomerAddressList(form.value.customerId)
}

const formatAddressOption = (addr) => {
  const name = { 1:'注册地', 2:'工厂', 3:'办事处', 4:'仓库', 5:'收货地址', 9:'其他' }[addr.addressType] || '地址'
  const status = addr.locationVerified === 1 ? '已定位' : '未定位'
  return `${name} · ${status} · ${addr.fullAddress || addr.addressDetail || '-'}`
}

const formatCheckinLocation = () => {
  if (!form.value.checkinLongitude || !form.value.checkinLatitude) return '未定位'
  return `${form.value.checkinLongitude}, ${form.value.checkinLatitude}`
}

const locateForCheckin = async () => {
  if (!navigator.geolocation) {
    ElMessage.warning('当前浏览器不支持定位')
    return
  }
  locating.value = true
  try {
    const position = await new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject, { enableHighAccuracy: true, timeout: 10000 })
    })
    const lng = Number(position.coords.longitude.toFixed(8))
    const lat = Number(position.coords.latitude.toFixed(8))
    form.value.checkinLongitude = lng
    form.value.checkinLatitude = lat
    try {
      const addr = await regeocode(lng, lat)
      form.value.checkinAddress = addr.address
    } catch (error) {
      form.value.checkinAddress = `${lng},${lat}`
    }
    ElMessage.success('定位成功')
  } finally {
    locating.value = false
  }
}

const handleSelectionChange = (rows) => { selectedRows.value = rows }
const clearBatchSelection = () => { selectedRows.value = []; tableRef.value?.clearSelection?.() }

onMounted(loadData)
</script>

<style scoped>.card-header,.header-actions{display:flex;justify-content:space-between;align-items:center}.header-actions{gap:8px}</style>
