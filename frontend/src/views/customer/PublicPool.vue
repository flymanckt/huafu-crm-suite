<template>
  <div class="page-container">
    <el-card>
      <template #header><div class="card-header"><span>公海客户</span><el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" /></div></template>
      <ConfigurableFilterForm
        v-model="query"
        :page-code="pageCode"
        :default-filters="filterFields"
        @search="loadData"
        @reset="resetQuery"
      />
      <BatchUpdateBar resource="customer" :fields="batchFields" :selected-rows="selectedRows" @clear="clearBatchSelection" @done="loadData" />
      <el-table ref="tableRef" :data="tableData" v-loading="loading" stripe border class="data-table" max-height="calc(100vh - 330px)" row-key="id" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" fixed />
        <el-table-column v-if="columnVisible('customerCode')" prop="customerCode" label="客户编码" width="130" />
        <el-table-column v-if="columnVisible('customerName')" prop="customerName" label="客户名称" min-width="180" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('type')" prop="type" label="类型" width="90">
          <template #default="{ row }">{{ {1:'直接客户',2:'代理',3:'终端品牌'}[row.type] }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('level')" prop="level" label="级别" width="70">
          <template #default="{ row }">{{ {1:'AAA',2:'AA',3:'A',4:'B',5:'C'}[row.level] }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('publicPoolTime')" prop="publicPoolTime" label="进入公海时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleClaim(row.id)">认领</el-button>
          </template>
        </el-table-column>
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
import { getPublicPoolPage, claimCustomer } from '@/api/customer'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const tableRef = ref()
const selectedRows = ref([])
const pageCode = 'public-pool-list'
const initialQuery = () => ({ current: 1, size: 20, customerName: '', type: null, level: null, ownerName: '' })
const query = ref(initialQuery())
const filterFields = [
  { key: 'customerName', label: '客户名称', placement: 'default' },
  { key: 'type', label: '客户类型', type: 'dict', dictCode: 'customer_type', valueType: 'number', placement: 'more' },
  { key: 'level', label: '客户等级', type: 'dict', dictCode: 'customer_level', valueType: 'number', placement: 'more' },
  { key: 'ownerName', label: '原负责人', placement: 'hidden' }
]
const defaultColumns = [
  { key: 'customerCode', label: '客户编码', width: 130, visible: true, fixed: true },
  { key: 'customerName', label: '客户名称', width: 180, visible: true, fixed: true },
  { key: 'type', label: '类型', width: 90, visible: true },
  { key: 'level', label: '级别', width: 70, visible: true },
  { key: 'publicPoolTime', label: '进入公海时间', width: 170, visible: true }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false
const batchFields = [
  { key: 'level', label: '级别', type: 'number' },
  { key: 'type', label: '类型', type: 'number' },
  { key: 'status', label: '客户状态', type: 'number' },
  { key: 'remark', label: '备注' }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPublicPoolPage(query.value)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

const resetQuery = () => {
  query.value = initialQuery()
  loadData()
}

const handleClaim = async (id) => {
  await claimCustomer(id)
  ElMessage.success('认领成功')
  loadData()
}

const handleSelectionChange = (rows) => { selectedRows.value = rows }
const clearBatchSelection = () => { selectedRows.value = []; tableRef.value?.clearSelection?.() }

onMounted(loadData)
</script>

<style scoped>.card-header{display:flex;justify-content:space-between;align-items:center}</style>
