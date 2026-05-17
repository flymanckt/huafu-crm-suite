<template>
  <div class="page-container">
    <el-card>
      <template #header><div class="card-header"><span>勤力度评分</span><el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" /></div></template>
      <ConfigurableFilterForm
        v-model="query"
        :page-code="pageCode"
        :default-filters="filterFields"
        @search="loadData"
        @reset="resetQuery"
      />
      <BatchUpdateBar resource="performance" :fields="batchFields" :selected-rows="selectedRows" @clear="clearBatchSelection" @done="loadData" />
      <el-table ref="tableRef" :data="tableData" v-loading="loading" stripe border class="data-table" max-height="calc(100vh - 310px)" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" fixed />
        <el-table-column v-if="columnVisible('userName')" prop="userName" label="姓名" width="100" />
        <el-table-column v-if="columnVisible('visitGroup')" label="拜访" align="center">
          <el-table-column prop="visitCount" label="次数" width="70" align="center" />
          <el-table-column prop="visitTarget" label="目标" width="70" align="center" />
          <el-table-column prop="visitRate" label="达成率" width="80" align="center">
            <template #default="{ row }">{{ row.visitRate }}%</template>
          </el-table-column>
        </el-table-column>
        <el-table-column v-if="columnVisible('reportGroup')" label="日报" align="center">
          <el-table-column prop="reportCount" label="提交" width="70" align="center" />
          <el-table-column prop="reportTarget" label="应交" width="70" align="center" />
          <el-table-column prop="reportRate" label="提交率" width="80" align="center">
            <template #default="{ row }">{{ row.reportRate }}%</template>
          </el-table-column>
        </el-table-column>
        <el-table-column v-if="columnVisible('newCustomerGroup')" label="新客户" align="center">
          <el-table-column prop="newCustomerCount" label="拜访" width="70" align="center" />
          <el-table-column prop="newCustomerTarget" label="目标" width="70" align="center" />
          <el-table-column prop="newCustomerRate" label="达成率" width="80" align="center">
            <template #default="{ row }">{{ row.newCustomerRate }}%</template>
          </el-table-column>
        </el-table-column>
        <el-table-column v-if="columnVisible('compositeScore')" prop="compositeScore" label="综合分" width="80" align="center">
          <template #default="{ row }"><strong>{{ row.compositeScore }}</strong></template>
        </el-table-column>
        <el-table-column v-if="columnVisible('grade')" prop="grade" label="等级" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="gradeColor[row.grade]" size="small" v-if="row.grade">{{ row.grade }}</el-tag>
            <span v-else>-</span>
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
import { getPerformancePage } from '@/api/performance'
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
const now = new Date()
const initialQuery = () => ({ current:1, size:20, year: now.getFullYear(), month: now.getMonth()+1, grade:null, userName:'' })
const query = ref(initialQuery())
const pageCode = 'performance-list'
const filterFields = [
  { key: 'year', label: '年份', type: 'number', min: 2020, max: 2030, width: 130, placement: 'default' },
  { key: 'month', label: '月份', type: 'number', min: 1, max: 12, width: 120, placement: 'default' },
  { key: 'grade', label: '等级', type: 'dict', dictCode: 'performance_grade', valueType: 'number', placement: 'default' },
  { key: 'userName', label: '姓名', placement: 'more' }
]
const defaultColumns = [
  { key: 'userName', label: '姓名', width: 100, visible: true, fixed: true },
  { key: 'visitGroup', label: '拜访', width: 220, visible: true },
  { key: 'reportGroup', label: '日报', width: 220, visible: true },
  { key: 'newCustomerGroup', label: '新客户', width: 220, visible: true },
  { key: 'compositeScore', label: '综合分', width: 80, visible: true },
  { key: 'grade', label: '等级', width: 70, visible: true }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false
const gradeColor = { 1: 'success', 2: 'warning', 3: 'danger', G: 'success', C: 'warning', O: 'danger' }
const batchFields = [
  { key: 'visitTarget', label: '拜访目标', type: 'number' },
  { key: 'reportTarget', label: '日报应交', type: 'number' },
  { key: 'newCustomerTarget', label: '新客户目标', type: 'number' },
  { key: 'grade', label: '等级', dictCode: 'performance_grade', valueType: 'number' }
]

const loadData = async () => {
  loading.value = true
  try { const r = await getPerformancePage(query.value); tableData.value = r.records||[]; total.value = r.total||0 } finally { loading.value = false }
}
const resetQuery = () => {
  query.value = initialQuery()
  loadData()
}
const handleSelectionChange = rows => { selectedRows.value = rows }
const clearBatchSelection = () => tableRef.value?.clearSelection()

onMounted(loadData)
</script>

<style scoped>.card-header{display:flex;justify-content:space-between;align-items:center}</style>
