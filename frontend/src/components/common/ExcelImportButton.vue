<template>
  <div class="excel-import">
    <el-dropdown split-button size="small" @click="triggerUpload" @command="handleCommand">
      Excel导入
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="template">下载XLSX模板</el-dropdown-item>
          <el-dropdown-item command="export" divided>导出当前数据</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
    <input
      ref="fileInputRef"
      class="file-input"
      type="file"
      accept=".xlsx"
      @change="handleFileChange"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import readXlsxFile from 'read-excel-file'
import writeXlsxFile from 'write-excel-file'

const props = defineProps({
  fields: { type: Array, required: true },
  importFn: { type: Function, required: true },
  moduleName: { type: String, default: '数据' },
  templateName: { type: String, default: '' },
  extraPayload: { type: Object, default: () => ({}) },
  exportRows: { type: Array, default: () => [] },
  exportFields: { type: Array, default: () => [] },
  exportName: { type: String, default: '' },
  transformExportRow: { type: Function, default: null }
})
const emit = defineEmits(['done'])

const fileInputRef = ref()

function triggerUpload() {
  fileInputRef.value?.click()
}

function handleCommand(command) {
  if (command === 'template') void downloadTemplate()
  if (command === 'export') void exportCurrentRows()
}

async function handleFileChange(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  try {
    const rows = await readRows(file)
    const payloads = rows.map((row, index) => mapRow(row, index + 2)).filter(Boolean)
    if (payloads.length === 0) {
      ElMessage.warning('没有可导入的数据')
      return
    }
    await ElMessageBox.confirm(`确认导入 ${payloads.length} 条${props.moduleName}数据？`, 'Excel批量导入', {
      type: 'warning',
      confirmButtonText: '开始导入',
      cancelButtonText: '取消'
    })
    const result = await importRows(payloads)
    const message = `导入完成：成功 ${result.success} 条，失败 ${result.failed} 条`
    if (result.failed) ElMessage.warning(message)
    else ElMessage.success(message)
    emit('done', result)
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error.message || '导入失败')
  }
}

async function readRows(file) {
  if (!file.name.toLowerCase().endsWith('.xlsx')) {
    throw new Error('请使用安全的 .xlsx 格式导入，旧版 .xls 请先另存为 .xlsx')
  }
  const sheetRows = await readXlsxFile(file)
  const headers = (sheetRows[0] || []).map(cellToPlainValue)
  return sheetRows.slice(1).map(row => {
    const data = {}
    headers.forEach((header, index) => {
      if (!header) return
      data[header] = cellToPlainValue(row[index])
    })
    return data
  }).filter(row => Object.values(row).some(value => !isBlank(value)))
}

function mapRow(row, rowNumber) {
  const payload = { ...props.extraPayload }
  for (const field of props.fields) {
    const raw = row[field.label] ?? row[field.key] ?? ''
    if (field.required && isBlank(raw)) {
      throw new Error(`第 ${rowNumber} 行缺少必填字段：${field.label}`)
    }
    if (isBlank(raw)) continue
    payload[field.key] = normalizeValue(raw, field)
  }
  return Object.keys(payload).length ? payload : null
}

function normalizeValue(value, field) {
  if (field.valueMap && Object.prototype.hasOwnProperty.call(field.valueMap, value)) {
    return field.valueMap[value]
  }
  if (field.valueMap && Object.prototype.hasOwnProperty.call(field.valueMap, String(value).trim())) {
    return field.valueMap[String(value).trim()]
  }
  if (field.type === 'number') {
    const number = Number(value)
    return Number.isFinite(number) ? number : value
  }
  if (field.type === 'date') {
    if (value instanceof Date) return formatDate(value)
    return String(value).trim()
  }
  return typeof value === 'string' ? value.trim() : value
}

async function importRows(rows) {
  let success = 0
  const errors = []
  for (let i = 0; i < rows.length; i++) {
    try {
      await props.importFn(rows[i], i)
      success++
    } catch (error) {
      errors.push({ row: i + 2, message: error.message || '保存失败' })
    }
  }
  if (errors.length) {
    console.warn(`[${props.moduleName}] Excel导入失败明细`, errors)
  }
  return { success, failed: errors.length, errors }
}

async function downloadTemplate() {
  const headers = props.fields.map(field => field.label)
  const example = props.fields.map(field => field.example ?? '')
  await writeWorkbook([headers, example], `${props.templateName || props.moduleName}导入模板.xlsx`)
}

async function exportCurrentRows() {
  const rows = props.exportRows || []
  if (!rows.length) {
    ElMessage.warning('当前没有可导出的数据')
    return
  }
  const fields = props.exportFields.length ? props.exportFields : props.fields
  const headers = fields.map(field => field.label)
  const data = rows.map(row => {
    const source = props.transformExportRow ? props.transformExportRow(row) : row
    return fields.map(field => exportValue(source, field))
  })
  await writeWorkbook([headers, ...data], `${props.exportName || props.moduleName}导出.xlsx`)
}

async function writeWorkbook(rows, fileName) {
  const data = rows.map((row, rowIndex) => row.map(value => ({
    value: value ?? '',
    fontWeight: rowIndex === 0 ? 'bold' : undefined
  })))
  await writeXlsxFile(data, { fileName })
}

function exportValue(row, field) {
  const value = row?.[field.key]
  if (value === null || value === undefined) return ''
  if (field.valueMap) {
    const matched = Object.entries(field.valueMap).find(([, mapped]) => String(mapped) === String(value))
    if (matched) return matched[0]
  }
  if (typeof value === 'object') return JSON.stringify(value)
  return value
}

function cellToPlainValue(value) {
  if (value === null || value === undefined) return ''
  if (value instanceof Date) return value
  if (typeof value !== 'object') return value
  if (Array.isArray(value.richText)) return value.richText.map(item => item.text || '').join('')
  if (value.text) return value.text
  if (value.result !== undefined) return cellToPlainValue(value.result)
  if (value.hyperlink && value.text) return value.text
  return String(value)
}

function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function isBlank(value) {
  return value === null || value === undefined || String(value).trim() === ''
}
</script>

<style scoped>
.excel-import { display: inline-flex; }
.file-input { display: none; }
</style>
