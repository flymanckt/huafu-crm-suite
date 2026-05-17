<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>丢单记录</span>
          <el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" />
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
        <el-table-column v-if="columnVisible('customerName')" prop="customerName" label="客户" min-width="140" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('lostType')" prop="lostType" label="丢单原因" width="90">
          <template #default="{ row }">{{ lostTypeMap[row.lostType] }}</template>
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
      </el-table>
      <el-pagination v-if="total>0" style="margin-top:16px;justify-content:flex-end"
        v-model:current-page="query.current" v-model:page-size="query.size"
        :total="total" layout="total, prev, pager, next" @page-change="loadData" @size-change="loadData" />
    </el-card>
    <ColumnConfigDrawer :page-code="pageCode" :default-columns="defaultColumns" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getLostOrderPage } from '@/api/opportunity'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { Setting } from '@element-plus/icons-vue'

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
  { key: 'customerName', label: '客户', width: 140, visible: true, fixed: true },
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

const handleSelectionChange = (rows) => { selectedRows.value = rows }
const clearBatchSelection = () => { selectedRows.value = []; tableRef.value?.clearSelection?.() }

onMounted(loadData)
</script>

<style scoped>.card-header{display:flex;justify-content:space-between;align-items:center}</style>
