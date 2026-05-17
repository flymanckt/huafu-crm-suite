<template>
  <div class="page-container">
    <el-card>
      <template #header><div class="card-header"><span>企微消息日志</span><el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" /></div></template>
      <ConfigurableFilterForm
        v-model="query"
        :page-code="pageCode"
        :default-filters="filterFields"
        @search="loadData"
        @reset="resetQuery"
      />
      <BatchUpdateBar resource="wecom-message-log" :fields="batchFields" :selected-rows="selectedRows" @clear="clearBatchSelection" @done="loadData" />
      <el-table ref="tableRef" :data="tableData" v-loading="loading" stripe border class="data-table" max-height="calc(100vh - 310px)" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" fixed />
        <el-table-column v-if="columnVisible('msgId')" prop="msgId" label="消息ID" width="150" />
        <el-table-column v-if="columnVisible('msgType')" prop="msgType" label="类型" width="80" />
        <el-table-column v-if="columnVisible('fromUser')" prop="fromUser" label="发送人" width="120" />
        <el-table-column v-if="columnVisible('toUser')" prop="toUser" label="接收人" width="120" />
        <el-table-column v-if="columnVisible('content')" prop="content" label="内容" min-width="250" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('parseStatus')" prop="parseStatus" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="{0:'info',1:'success',2:'danger'}[row.parseStatus]" size="small">{{ {0:'未处理',1:'已解析',2:'失败'}[row.parseStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="columnVisible('createdTime')" prop="createdTime" label="时间" width="170" />
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
import request from '@/api/request'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { Setting } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const tableRef = ref()
const selectedRows = ref([])
const pageCode = 'wecom-message-list'
const initialQuery = () => ({ current:1, size:20, parseStatus:null, msgType:'', fromUser:'', toUser:'', content:'' })
const query = ref(initialQuery())
const filterFields = [
  { key: 'parseStatus', label: '处理状态', type: 'dict', dictCode: 'wecom_parse_status', valueType: 'number', placement: 'default' },
  { key: 'msgType', label: '消息类型', placement: 'more' },
  { key: 'fromUser', label: '发送人', placement: 'more' },
  { key: 'toUser', label: '接收人', placement: 'more' },
  { key: 'content', label: '内容', placement: 'hidden', width: 220 }
]
const defaultColumns = [
  { key: 'msgId', label: '消息ID', width: 150, visible: true, fixed: true },
  { key: 'msgType', label: '类型', width: 80, visible: true },
  { key: 'fromUser', label: '发送人', width: 120, visible: true },
  { key: 'toUser', label: '接收人', width: 120, visible: true },
  { key: 'content', label: '内容', width: 250, visible: true },
  { key: 'parseStatus', label: '状态', width: 90, visible: true },
  { key: 'createdTime', label: '时间', width: 170, visible: true }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false
const batchFields = [
  { key: 'parseStatus', label: '处理状态', dictCode: 'wecom_parse_status', valueType: 'number' },
  { key: 'content', label: '内容', type: 'text' }
]

const loadData = async () => {
  loading.value = true
  try {
    const r = await request.get('/wecom/message-log/page', { params: query.value })
    tableData.value = r.records||[]; total.value = r.total||0
  } finally { loading.value = false }
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
