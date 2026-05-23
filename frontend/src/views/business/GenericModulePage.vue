<template>
  <div class="module-page">
    <div class="toolbar">
      <div>
        <h2>{{ moduleConfig.title }}</h2>
        <p>{{ moduleConfig.recordName }}台账</p>
      </div>
      <div class="toolbar-actions">
        <el-button :icon="Setting" circle title="列格式" @click="columnConfig.openDrawer()" />
        <ExcelImportButton
          :module-name="moduleConfig.title"
          :fields="importFields"
          :import-fn="importModuleRow"
          :export-rows="tableData"
          :transform-export-row="moduleExportRow"
          @done="loadData"
        />
        <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新增</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <ConfigurableFilterForm
        :model-value="query"
        :page-code="pageCode"
        :default-filters="filterFields"
        search-text="查询"
        @update:model-value="value => Object.assign(query, value)"
        @search="loadData"
        @reset="resetQuery"
      />

      <div class="variant-bar">
        <el-tag
          v-for="preset in presets"
          :key="preset.name"
          :type="preset.name === activePresetName ? 'success' : 'info'"
          class="variant-tag"
          closable
          @click="handleLoadPreset(preset)"
          @close.stop="handleDeletePreset(preset)"
        >
          {{ preset.name }}
        </el-tag>
        <el-dropdown trigger="click">
          <el-button link type="primary">保存个人变式 <el-icon><ArrowDown /></el-icon></el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <div class="variant-save-panel">
                <el-input v-model="newPresetName" placeholder="变式名称" maxlength="20" @keyup.enter="handleSavePreset" />
                <el-button type="primary" size="small" :disabled="!newPresetName.trim() || !canSaveMore" @click="handleSavePreset">保存为新变式</el-button>
                <div v-if="presets.length" class="variant-overwrite">
                  <span>覆盖已有：</span>
                  <el-tag v-for="preset in presets" :key="preset.name" size="small" @click="handleOverwritePreset(preset)">
                    {{ preset.name }}
                  </el-tag>
                </div>
              </div>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <BatchUpdateBar
        :module-key="moduleKey"
        :fields="batchFields"
        :selected-rows="selectedRows"
        @clear="clearBatchSelection"
        @done="loadData"
      />

      <el-table
        ref="tableRef"
        :data="tableData"
        v-loading="loading"
        border
        stripe
        class="data-table"
        max-height="calc(100vh - 360px)"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="46" fixed />
        <el-table-column
          v-for="column in visibleColumns"
          :key="column.key"
          :prop="baseColumnKeys.includes(column.key) ? column.key : undefined"
          :label="column.label"
          :width="column.width"
          :fixed="column.fixed ? 'left' : false"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <el-tag v-if="column.key === 'status'" :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
            <span v-else-if="baseColumnKeys.includes(column.key)">{{ row[column.key] || '-' }}</span>
            <span v-else>{{ payloadOf(row)[column.key] || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        layout="total, sizes, prev, pager, next"
        style="margin-top:16px; justify-content:flex-end"
        @current-change="loadData"
        @size-change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="编号">
              <el-input v-model="form.recordNo" placeholder="不填则可后续维护" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <DictSelect v-model="form.status" dict-code="module_record_status" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="日期">
              <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-input v-model="form.ownerName" />
            </el-form-item>
          </el-col>
          <el-col v-for="field in moduleConfig.fields" :key="field.key" :span="field.type === 'textarea' ? 24 : 12">
            <el-form-item :label="field.label" :prop="`payload.${field.key}`">
              <el-input-number
                v-if="field.type === 'number'"
                v-model="form.payload[field.key]"
                :min="0"
                :precision="2"
                style="width:100%"
              />
              <el-date-picker
                v-else-if="field.type === 'date'"
                v-model="form.payload[field.key]"
                type="date"
                value-format="YYYY-MM-DD"
                style="width:100%"
              />
              <el-input
                v-else-if="field.type === 'textarea'"
                v-model="form.payload[field.key]"
                type="textarea"
                :rows="3"
              />
              <el-input v-else v-model="form.payload[field.key]" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
    <ColumnConfigDrawer :page-code="pageCode" :default-columns="defaultColumns" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Plus, Setting } from '@element-plus/icons-vue'
import { businessModules } from '@/config/businessModules'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import ExcelImportButton from '@/components/common/ExcelImportButton.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import { createModuleRecord, deleteModuleRecord, getModuleRecordPage, updateModuleRecord } from '@/api/moduleRecord'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { useFilterPreset } from '@/composables/useFilterPreset'

const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const tableData = ref([])
const total = ref(0)
const formRef = ref()
const tableRef = ref()
const selectedRows = ref([])

const query = reactive({ current: 1, size: 20, keyword: '', status: '', ownerName: '', recordDate: '' })
const form = reactive({ recordNo: '', status: 'ACTIVE', ownerName: '', recordDate: '', payload: {}, remark: '' })

const moduleKey = computed(() => route.params.moduleKey)
const moduleConfig = computed(() => businessModules[moduleKey.value] || businessModules['exhibition-activity'])
const pageCode = `module-${route.params.moduleKey}`
const baseColumnKeys = ['recordNo', 'title', 'recordDate', 'ownerName', 'status']
const defaultColumns = [
  { key: 'recordNo', label: '编号', width: 150, visible: true, fixed: true },
  { key: 'title', label: moduleConfig.value.recordName, width: 180, visible: true, fixed: true },
  { key: 'recordDate', label: '日期', width: 120, visible: true },
  { key: 'ownerName', label: '负责人', width: 120, visible: true },
  { key: 'status', label: '状态', width: 100, visible: true },
  ...moduleConfig.value.fields.map(field => ({ key: field.key, label: field.label, width: field.type === 'textarea' ? 220 : 150, visible: true }))
]
const filterFields = computed(() => [
  { key: 'keyword', label: '关键词', placement: 'default', width: 220, placeholder: '名称、编号、负责人' },
  { key: 'status', label: '状态', type: 'dict', dictCode: 'module_record_status', placement: 'default', width: 140 },
  { key: 'ownerName', label: '负责人', placement: 'more' },
  { key: 'recordDate', label: '日期', type: 'date', placement: 'more' },
  ...moduleConfig.value.fields.map(field => ({
    key: field.key,
    label: field.label,
    type: field.type === 'date' ? 'date' : field.type === 'number' ? 'number' : 'input',
    placement: 'hidden'
  }))
])
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const visibleColumns = computed(() => columnConfig.columns.value.filter(column => column.visible))
const batchFields = computed(() => [
  { key: 'status', label: '状态', dictCode: 'module_record_status' },
  { key: 'ownerName', label: '负责人' },
  { key: 'recordDate', label: '日期', type: 'date' },
  { key: 'remark', label: '备注' },
  ...moduleConfig.value.fields.map(field => ({
    key: `payload.${field.key}`,
    label: field.label,
    type: field.type === 'date' ? 'date' : field.type === 'number' ? 'number' : 'text'
  }))
])
const importFields = computed(() => [
  { key: 'recordNo', label: '编号', example: '自动生成可留空' },
  { key: 'ownerName', label: '负责人' },
  { key: 'recordDate', label: '日期', type: 'date', example: new Date().toISOString().slice(0, 10) },
  { key: 'status', label: '状态', valueMap: { 草稿: 'DRAFT', 进行中: 'ACTIVE', 已完成: 'DONE' }, example: '进行中' },
  { key: 'remark', label: '备注' },
  ...moduleConfig.value.fields.map(field => ({
    key: field.key,
    label: field.label,
    type: field.type === 'date' ? 'date' : field.type === 'number' ? 'number' : 'text',
    required: !!field.required,
    example: field.required ? `${field.label}示例` : ''
  }))
])
const filterPreset = useFilterPreset({ pageCode })
const { presets, activePresetName, newPresetName, canSaveMore, loadPreset, savePreset, overwritePreset, deletePreset, clearActivePreset } = filterPreset
const dialogTitle = computed(() => `${editingId.value ? '编辑' : '新增'}${moduleConfig.value.recordName}`)

const rules = computed(() => {
  const result = {}
  moduleConfig.value.fields.filter(field => field.required).forEach(field => {
    result[`payload.${field.key}`] = [{ required: true, message: `请输入${field.label}`, trigger: 'blur' }]
  })
  return result
})

const payloadOf = (row) => {
  try {
    return row.payloadJson ? JSON.parse(row.payloadJson) : {}
  } catch (e) {
    return {}
  }
}

const statusLabel = (status) => ({ DRAFT: '草稿', ACTIVE: '进行中', DONE: '已完成' }[status] || status || '-')
const statusType = (status) => ({ DRAFT: 'info', ACTIVE: 'primary', DONE: 'success' }[status] || '')

const resetForm = () => {
  editingId.value = null
  form.recordNo = ''
  form.status = 'ACTIVE'
  form.ownerName = ''
  form.recordDate = new Date().toISOString().slice(0, 10)
  form.remark = ''
  form.payload = {}
  moduleConfig.value.fields.forEach(field => {
    form.payload[field.key] = field.type === 'number' ? null : ''
  })
}

const loadData = async () => {
  loading.value = true
  try {
    const result = await getModuleRecordPage(moduleKey.value, query)
    tableData.value = result.records || []
    total.value = result.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = ''
  query.status = ''
  query.ownerName = ''
  query.recordDate = ''
  query.current = 1
  clearActivePreset()
  clearBatchSelection()
  loadData()
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  resetForm()
  editingId.value = row.id
  form.recordNo = row.recordNo || ''
  form.status = row.status || 'ACTIVE'
  form.ownerName = row.ownerName || ''
  form.recordDate = row.recordDate || ''
  form.remark = row.remark || ''
  form.payload = { ...form.payload, ...payloadOf(row) }
  dialogVisible.value = true
}

const buildRecord = () => {
  const title = form.payload[moduleConfig.value.primaryField] || form.recordNo || moduleConfig.value.title
  return {
    recordNo: form.recordNo,
    title,
    status: form.status,
    ownerName: form.ownerName,
    recordDate: form.recordDate,
    payloadJson: JSON.stringify(form.payload),
    remark: form.remark
  }
}

const importModuleRow = (row) => {
  const payload = {}
  moduleConfig.value.fields.forEach(field => {
    if (row[field.key] !== undefined) payload[field.key] = row[field.key]
  })
  const title = payload[moduleConfig.value.primaryField] || row.recordNo || moduleConfig.value.title
  return createModuleRecord(moduleKey.value, {
    recordNo: row.recordNo || '',
    title,
    status: row.status || 'ACTIVE',
    ownerName: row.ownerName || '',
    recordDate: row.recordDate || new Date().toISOString().slice(0, 10),
    payloadJson: JSON.stringify(payload),
    remark: row.remark || ''
  })
}

const moduleExportRow = (row) => ({
  recordNo: row.recordNo || '',
  ownerName: row.ownerName || '',
  recordDate: row.recordDate || '',
  status: row.status || '',
  remark: row.remark || '',
  ...payloadOf(row)
})

const handleSave = async () => {
  await formRef.value?.validate()
  saving.value = true
  try {
    const record = buildRecord()
    if (editingId.value) {
      await updateModuleRecord(moduleKey.value, editingId.value, record)
    } else {
      await createModuleRecord(moduleKey.value, record)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除「${row.title || row.recordNo}」？`, '删除确认', { type: 'warning' })
  await deleteModuleRecord(moduleKey.value, row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const clearBatchSelection = () => {
  selectedRows.value = []
  tableRef.value?.clearSelection?.()
}

const currentFilters = computed(() => ({ keyword: query.keyword, status: query.status, ownerName: query.ownerName, recordDate: query.recordDate }))

const handleLoadPreset = async (preset) => {
  const filters = await loadPreset(preset, currentFilters.value)
  if (!filters) return
  query.keyword = filters.keyword || ''
  query.status = filters.status || ''
  query.ownerName = filters.ownerName || ''
  query.recordDate = filters.recordDate || ''
  query.current = 1
  loadData()
}

const handleSavePreset = async () => {
  await savePreset(currentFilters.value)
}

const handleOverwritePreset = async (preset) => {
  await overwritePreset(preset, currentFilters.value)
}

const handleDeletePreset = async (preset) => {
  await deletePreset(preset)
}

watch(moduleKey, () => {
  query.current = 1
  clearBatchSelection()
  resetForm()
  loadData()
})

onMounted(async () => {
  resetForm()
  await Promise.all([columnConfig.loadConfig(), filterPreset.loadPresets()])
  loadData()
})
</script>

<style scoped>
.module-page { padding: 16px; }
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.toolbar-actions { display: flex; gap: 8px; align-items: center; }
.toolbar h2 { margin: 0; font-size: 20px; color: #1f2937; }
.toolbar p { margin: 4px 0 0; color: #6b7280; }
.variant-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin: 0 0 12px;
}
.variant-tag { cursor: pointer; }
.variant-save-panel {
  display: grid;
  gap: 8px;
  min-width: 220px;
  padding: 10px 12px;
}
.variant-overwrite {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  border-top: 1px solid #ebeef5;
  padding-top: 8px;
  color: #909399;
  font-size: 12px;
}
</style>
