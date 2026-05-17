<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商机列表</span>
          <div class="header-actions">
            <el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" />
            <el-button type="primary" size="small" @click="$router.push('/opportunity/new')">新建商机</el-button>
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
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push('/opportunity/'+row.id)">详情</el-button>
            <el-button link type="success" @click="handleAdvance(row)" v-if="row.stage < 5">推进</el-button>
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
import { getOpportunityPage, advanceOpportunityStage } from '@/api/opportunity'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const tableRef = ref()
const selectedRows = ref([])
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

const handleSelectionChange = (rows) => { selectedRows.value = rows }
const clearBatchSelection = () => { selectedRows.value = []; tableRef.value?.clearSelection?.() }

onMounted(loadData)
</script>

<style scoped>.card-header,.header-actions{display:flex;justify-content:space-between;align-items:center}.header-actions{gap:8px}</style>
