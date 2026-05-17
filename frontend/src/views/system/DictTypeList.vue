<template>
  <div>
    <div style="margin-bottom:12px">
      <el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" />
      <el-button type="primary" @click="openCreate">新建类型</el-button>
    </div>
    <ConfigurableFilterForm
      v-model="query"
      :page-code="pageCode"
      :default-filters="filterFields"
      @search="loadData"
      @reset="resetQuery"
    />
    <BatchUpdateBar resource="dict-type" :fields="batchFields" :selected-rows="selectedRows" @clear="clearBatchSelection" @done="loadData" />
    <el-table ref="tableRef" :data="tableData" v-loading="loading" border stripe class="data-table" max-height="calc(100vh - 360px)" row-key="id" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="46" fixed />
      <el-table-column v-if="columnVisible('dictCode')" prop="dictCode" label="编码" width="180" />
      <el-table-column v-if="columnVisible('dictName')" prop="dictName" label="名称" width="180" />
      <el-table-column v-if="columnVisible('dictType')" prop="dictType" label="类型" width="100"><template #default="{ row }">{{ row.dictType === 'SYSTEM' ? '系统' : '自定义' }}</template></el-table-column>
      <el-table-column v-if="columnVisible('description')" prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column v-if="columnVisible('status')" prop="status" label="状态" width="80"><template #default="{ row }"><el-tag :type="row.status ? 'success' : 'danger'" size="small">{{ row.status ? '启用' : '禁用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="emit('select', row)">字典项</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference><el-button link type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top:16px;justify-content:flex-end"
      v-model:current-page="query.current"
      v-model:page-size="query.size"
      :total="total"
      :page-sizes="[10,20,50]"
      layout="total, sizes, prev, pager, next"
      @page-change="loadData" @size-change="loadData"/>

    <el-dialog v-model="formVisible" :title="editingId ? '编辑字典类型' : '新建字典类型'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="编码" required><el-input v-model="form.dictCode" :disabled="!!editingId" /></el-form-item>
        <el-form-item label="名称" required><el-input v-model="form.dictName" /></el-form-item>
        <el-form-item label="类型"><el-radio-group v-model="form.dictType"><el-radio value="SYSTEM">系统</el-radio><el-radio value="CUSTOM">自定义</el-radio></el-radio-group></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
    <ColumnConfigDrawer :page-code="pageCode" :default-columns="defaultColumns" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllDictTypes, getDictTypePage, createDictType, updateDictType, deleteDictType } from '@/api/system/dict'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { ElMessage } from 'element-plus'
import { Setting } from '@element-plus/icons-vue'

const emit = defineEmits(['select'])

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const tableRef = ref()
const selectedRows = ref([])
const pageCode = 'dict-type-list'
const initialQuery = () => ({ current: 1, size: 20, dictCode: '', dictName: '', dictType: '', status: null })
const query = ref(initialQuery())
const filterFields = [
  { key: 'dictCode', label: '编码', placement: 'default' },
  { key: 'dictName', label: '名称', placement: 'default' },
  { key: 'dictType', label: '类型', type: 'dict', dictCode: 'dict_type_category', placement: 'more' },
  { key: 'status', label: '状态', type: 'dict', dictCode: 'dict_status', valueType: 'number', placement: 'more' }
]
const defaultColumns = [
  { key: 'dictCode', label: '编码', width: 180, visible: true, fixed: true },
  { key: 'dictName', label: '名称', width: 180, visible: true },
  { key: 'dictType', label: '类型', width: 100, visible: true },
  { key: 'description', label: '描述', width: 200, visible: true },
  { key: 'status', label: '状态', width: 80, visible: true }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false
const formVisible = ref(false)
const editingId = ref(null)
const form = ref({ dictCode: '', dictName: '', dictType: 'CUSTOM', description: '' })
const batchFields = [
  { key: 'dictName', label: '名称' },
  { key: 'dictType', label: '类型', dictCode: 'dict_type_category' },
  { key: 'status', label: '状态', dictCode: 'dict_status', valueType: 'number' },
  { key: 'sortOrder', label: '排序', type: 'number' },
  { key: 'description', label: '描述' }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDictTypePage(query.value)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

const resetQuery = () => {
  query.value = initialQuery()
  loadData()
}

const openCreate = () => {
  editingId.value = null
  form.value = { dictCode: '', dictName: '', dictType: 'CUSTOM', description: '' }
  formVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  form.value = { ...row }
  formVisible.value = true
}

const handleSave = async () => {
  if (!form.value.dictCode || !form.value.dictName) {
    ElMessage.warning('编码和名称必填')
    return
  }
  try {
    if (editingId.value) {
      await updateDictType(editingId.value, form.value)
    } else {
      await createDictType(form.value)
    }
    ElMessage.success('保存成功')
    formVisible.value = false
    loadData()
  } catch (e) {
    // error already shown by interceptor
  }
}

const handleDelete = async (id) => {
  try {
    await deleteDictType(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // error already shown by interceptor
  }
}

const handleSelectionChange = (rows) => { selectedRows.value = rows }
const clearBatchSelection = () => { selectedRows.value = []; tableRef.value?.clearSelection?.() }

onMounted(loadData)
</script>
