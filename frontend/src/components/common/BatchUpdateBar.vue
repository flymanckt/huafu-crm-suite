<template>
  <div class="batch-update-bar">
    <div class="batch-update-summary">
      <span>已选 {{ selectedRows.length }} 条</span>
      <el-button size="small" :disabled="!selectedRows.length" @click="$emit('clear')">清空</el-button>
    </div>
    <el-button type="primary" size="small" :disabled="!selectedRows.length || !fields.length" @click="openDialog">
      批量修改
    </el-button>
  </div>

  <el-dialog v-model="visible" title="批量修改" width="420px" destroy-on-close>
    <el-alert
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom:12px"
      :title="`将同时修改 ${selectedRows.length} 条数据，请确认字段和值。`"
    />
    <el-form label-width="92px">
      <el-form-item label="修改字段">
        <el-select v-model="fieldKey" placeholder="请选择字段" style="width:100%" @change="resetValue">
          <el-option v-for="field in fields" :key="fieldKeyOf(field)" :label="field.label" :value="fieldKeyOf(field)" />
        </el-select>
      </el-form-item>
      <el-form-item label="新值">
        <DictSelect
          v-if="currentField?.dictCode"
          v-model="fieldValue"
          :dict-code="currentField.dictCode"
          :value-type="currentField.valueType || 'string'"
          placeholder="请选择"
          clearable
          style="width:100%"
        />
        <el-select v-else-if="currentField?.options" v-model="fieldValue" placeholder="请选择" clearable style="width:100%">
          <el-option v-for="option in currentField.options" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
        <el-date-picker
          v-else-if="currentField?.type === 'date'"
          v-model="fieldValue"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择日期"
          style="width:100%"
        />
        <el-input-number
          v-else-if="currentField?.type === 'number'"
          v-model="fieldValue"
          :precision="currentField.precision ?? 0"
          style="width:100%"
        />
        <el-input v-else v-model="fieldValue" clearable />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" :disabled="!fieldKey" @click="submit">确认修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { batchUpdate, batchUpdateModuleRecords } from '@/api/batchUpdate'
import DictSelect from '@/components/Dict/DictSelect.vue'

const props = defineProps({
  resource: { type: String, default: '' },
  moduleKey: { type: String, default: '' },
  selectedRows: { type: Array, default: () => [] },
  fields: { type: Array, default: () => [] }
})

const emit = defineEmits(['done', 'clear'])
const visible = ref(false)
const saving = ref(false)
const fieldKey = ref('')
const fieldValue = ref(null)

const fieldKeyOf = (field) => field.key || field.prop
const currentField = computed(() => props.fields.find(field => fieldKeyOf(field) === fieldKey.value))

const openDialog = () => {
  fieldKey.value = fieldKeyOf(props.fields[0] || {}) || ''
  resetValue()
  visible.value = true
}

const resetValue = () => {
  fieldValue.value = currentField.value?.type === 'number' || currentField.value?.valueType === 'number' ? null : ''
}

const submit = async () => {
  const ids = props.selectedRows.map(row => row.id).filter(Boolean)
  if (!ids.length) {
    ElMessage.warning('请选择需要修改的数据')
    return
  }
  saving.value = true
  try {
    const payload = { [fieldKey.value]: fieldValue.value }
    const count = props.moduleKey
      ? await batchUpdateModuleRecords(props.moduleKey, ids, payload)
      : await batchUpdate(props.resource, ids, payload)
    ElMessage.success(`已批量修改 ${count || ids.length} 条数据`)
    visible.value = false
    emit('clear')
    emit('done')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.batch-update-bar {
  min-height: 40px;
  margin: 8px 0 12px;
  padding: 8px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.batch-update-summary {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #606266;
  font-size: 13px;
}
</style>
