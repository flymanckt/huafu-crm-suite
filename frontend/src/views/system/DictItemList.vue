<template>
  <div>
    <div style="margin-bottom:12px">
      <el-tag>字典类型：{{ dictType?.dictName }} ({{ dictType?.dictCode }})</el-tag>
      <el-button :icon="Setting" circle title="列配置" style="margin-left:12px" @click="columnConfig.openDrawer()" />
      <el-button type="primary" style="margin-left:12px" @click="openCreate">新建字典项</el-button>
    </div>
    <ConfigurableFilterForm
      v-model="query"
      :page-code="pageCode"
      :default-filters="filterFields"
      @search="loadData"
      @reset="resetQuery"
    />
    <BatchUpdateBar resource="dict-item" :fields="batchFields" :selected-rows="selectedRows" @clear="clearBatchSelection" @done="loadData" />
    <el-table ref="tableRef" :data="filteredTableData" v-loading="loading" border stripe class="data-table" max-height="calc(100vh - 360px)" row-key="id" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="46" fixed />
      <el-table-column v-if="columnVisible('itemCode')" prop="itemCode" label="编码" width="180" />
      <el-table-column v-if="columnVisible('itemName')" prop="itemName" label="名称" width="200" />
      <el-table-column v-if="columnVisible('itemValue')" prop="itemValue" label="值" width="120" />
      <el-table-column v-if="columnVisible('showCode')" prop="showCode" label="前台显示代码" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="row.showCode === 0 ? 'info' : 'success'" size="small">{{ row.showCode === 0 ? '隐藏' : '显示' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="columnVisible('color')" prop="color" label="颜色" width="100"><template #default="{ row }"><el-tag :type="row.color || 'info'" size="small">{{ row.color }}</el-tag></template></el-table-column>
      <el-table-column v-if="columnVisible('sortOrder')" prop="sortOrder" label="排序" width="80" />
      <el-table-column v-if="columnVisible('status')" prop="status" label="状态" width="80"><template #default="{ row }"><el-tag :type="row.status ? 'success' : 'danger'" size="small">{{ row.status ? '启用' : '禁用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="toggleStatus(row)">{{ row.status ? '禁用' : '启用' }}</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference><el-button link type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="formVisible" :title="editingId ? '编辑字典项' : '新建字典项'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="编码" required><el-input v-model="form.itemCode" /></el-form-item>
        <el-form-item label="名称" required><el-input v-model="form.itemName" /></el-form-item>
        <el-form-item label="值"><el-input v-model="form.itemValue" /></el-form-item>
        <el-form-item label="前台代码">
          <el-switch v-model="form.showCode" :active-value="1" :inactive-value="0" active-text="显示" inactive-text="隐藏" />
        </el-form-item>
        <el-form-item label="颜色">
          <DictSelect v-model="form.color" dict-code="dict_color" placeholder="选择颜色" />
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
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
import { computed, ref, watch, onMounted } from 'vue'
import { getDictItems, createDictItem, updateDictItem, deleteDictItem, toggleDictItemStatus } from '@/api/system/dict'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({ dictType: { type: Object, default: null } })

const loading = ref(false)
const tableData = ref([])
const tableRef = ref()
const selectedRows = ref([])
const formVisible = ref(false)
const editingId = ref(null)
const pageCode = 'dict-item-list'
const initialQuery = () => ({ itemCode: '', itemName: '', itemValue: '', showCode: null, status: null })
const query = ref(initialQuery())
const filterFields = [
  { key: 'itemCode', label: '编码', placement: 'default' },
  { key: 'itemName', label: '名称', placement: 'default' },
  { key: 'itemValue', label: '值', placement: 'more' },
  { key: 'showCode', label: '前台显示代码', type: 'dict', dictCode: 'yes_no', valueType: 'number', placement: 'more' },
  { key: 'status', label: '状态', type: 'dict', dictCode: 'dict_status', valueType: 'number', placement: 'more' }
]
const defaultColumns = [
  { key: 'itemCode', label: '编码', width: 180, visible: true, fixed: true },
  { key: 'itemName', label: '名称', width: 200, visible: true },
  { key: 'itemValue', label: '值', width: 120, visible: true },
  { key: 'showCode', label: '前台显示代码', width: 120, visible: true },
  { key: 'color', label: '颜色', width: 100, visible: true },
  { key: 'sortOrder', label: '排序', width: 80, visible: true },
  { key: 'status', label: '状态', width: 80, visible: true }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false
const form = ref({ itemCode: '', itemName: '', itemValue: '', showCode: 1, color: 'info', sortOrder: 0, description: '' })
const filteredTableData = computed(() => tableData.value.filter(row => {
  const q = query.value
  if (q.itemCode && !String(row.itemCode || '').includes(q.itemCode)) return false
  if (q.itemName && !String(row.itemName || '').includes(q.itemName)) return false
  if (q.itemValue && !String(row.itemValue || '').includes(q.itemValue)) return false
  if (q.showCode !== null && q.showCode !== '' && Number(row.showCode) !== Number(q.showCode)) return false
  if (q.status !== null && q.status !== '' && Number(row.status) !== Number(q.status)) return false
  return true
}))
const batchFields = [
  { key: 'itemName', label: '名称' },
  { key: 'itemValue', label: '值' },
  { key: 'showCode', label: '前台显示代码', dictCode: 'yes_no', valueType: 'number' },
  { key: 'color', label: '颜色', dictCode: 'dict_color' },
  { key: 'sortOrder', label: '排序', type: 'number' },
  { key: 'status', label: '状态', dictCode: 'dict_status', valueType: 'number' },
  { key: 'description', label: '描述' }
]

const loadData = async () => {
  if (!props.dictType?.dictCode) return
  loading.value = true
  try {
    const items = await getDictItems(props.dictType.dictCode)
    tableData.value = Array.isArray(items) ? items : []
  } finally { loading.value = false }
}

watch(() => props.dictType, () => { if (props.dictType) loadData() }, { immediate: true })

const resetQuery = () => {
  query.value = initialQuery()
  loadData()
}

const openCreate = () => {
  editingId.value = null
  form.value = { itemCode: '', itemName: '', itemValue: '', showCode: 1, color: 'info', sortOrder: 0, description: '' }
  formVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  form.value = { showCode: 1, ...row }
  formVisible.value = true
}

const handleSave = async () => {
  if (!form.value.itemCode || !form.value.itemName) {
    ElMessage.warning('编码和名称必填')
    return
  }
  try {
    if (editingId.value) {
      await updateDictItem(editingId.value, form.value)
    } else {
      await createDictItem({ ...form.value, dictId: props.dictType.id })
    }
    ElMessage.success('保存成功')
    formVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id) => {
  try {
    await deleteDictItem(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { ElMessage.error('删除失败') }
}

const toggleStatus = async (row) => {
  try {
    await toggleDictItemStatus(row.id, row.status ? 0 : 1)
    ElMessage.success(row.status ? '已禁用' : '已启用')
    loadData()
  } catch (e) { ElMessage.error('操作失败') }
}

const handleSelectionChange = (rows) => { selectedRows.value = rows }
const clearBatchSelection = () => { selectedRows.value = []; tableRef.value?.clearSelection?.() }
</script>
