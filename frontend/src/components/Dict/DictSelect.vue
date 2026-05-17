<template>
  <el-select
    v-model="selectedValue"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled"
    :filterable="filterable"
    :loading="loading"
    :size="size"
    @change="handleChange"
  >
    <el-option
      v-for="item in options"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    >
      <span :style="{ color: item.color || '' }">{{ item.label }}</span>
    </el-option>
  </el-select>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useDict } from '@/composables/useDict'

const props = defineProps({
  modelValue: { type: [String, Number, Boolean], default: '' },
  dictCode: { type: String, required: true },
  valueType: { type: String, default: 'string' },
  placeholder: { type: String, default: '请选择' },
  clearable: { type: Boolean, default: true },
  disabled: { type: Boolean, default: false },
  filterable: { type: Boolean, default: true },
  size: { type: String, default: 'default' }
})

const emit = defineEmits(['update:modelValue', 'change'])

const { getDictItems, dictCache } = useDict()
const loading = ref(false)
const options = computed(() => {
  const items = Array.isArray(dictCache[props.dictCode]) ? dictCache[props.dictCode] : []
  return items.map(item => {
    const rawValue = item.itemCode ?? item.itemValue ?? ''
    const value = normalizeValue(rawValue)
    const name = item.itemName || item.itemValue || item.itemCode || ''
    const showCode = item.showCode !== 0
    return {
      ...item,
      value,
      label: showCode && value !== '' && value !== null && value !== undefined && name ? `${value} - ${name}` : (name || value)
    }
  }).filter(item => item.value !== '' && item.value !== null && item.value !== undefined)
})

const selectedValue = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', normalizeValue(val))
})

const handleChange = (val) => {
  const normalized = normalizeValue(val)
  emit('change', normalized, options.value.find(o => o.value === normalized))
}

function normalizeValue(value) {
  if (value === null || value === undefined || value === '') return value
  if (props.valueType === 'number') {
    const numberValue = Number(value)
    return Number.isNaN(numberValue) ? null : numberValue
  }
  if (props.valueType === 'boolean') {
    return value === true || value === 'true' || value === '1' || value === 1
  }
  return String(value)
}

const loadOptions = () => {
  if (props.dictCode && !dictCache[props.dictCode]) {
    loading.value = true
    getDictItems(props.dictCode).finally(() => { loading.value = false })
  }
}

onMounted(loadOptions)
watch(() => props.dictCode, loadOptions)
</script>
