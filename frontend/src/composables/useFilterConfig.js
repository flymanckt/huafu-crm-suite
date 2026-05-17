import { computed, ref, unref, watch } from 'vue'
import { getFilterPreset, putFilterPreset } from '@/api/customer.js'
import { ElMessage } from 'element-plus'

const stores = new Map()

const parseConfig = (value) => {
  if (!value) return null
  if (typeof value === 'string') {
    try {
      return JSON.parse(value)
    } catch {
      return null
    }
  }
  return value
}

const normalizePlacement = (field) => {
  if (['default', 'more', 'hidden'].includes(field.placement)) return field.placement
  if (field.visible === false) return 'hidden'
  if (field.more === true) return 'more'
  return 'default'
}

const normalizeFields = (fields = []) => fields.map(field => ({
  key: field.key,
  label: field.label || field.key,
  type: field.type || (field.dictCode ? 'dict' : 'input'),
  placement: normalizePlacement(field),
  placeholder: field.placeholder,
  width: field.width || 160,
  dictCode: field.dictCode,
  valueType: field.valueType,
  options: field.options,
  min: field.min,
  max: field.max,
  precision: field.precision,
  disabled: field.disabled === true,
}))

const mergeFields = (savedFields = [], defaultFields = []) => {
  const defaults = normalizeFields(defaultFields)
  const defaultMap = new Map(defaults.map(field => [field.key, field]))
  const merged = normalizeFields(savedFields)
    .filter(field => defaultMap.has(field.key))
    .map(field => ({ ...defaultMap.get(field.key), ...field }))
  defaults.forEach(field => {
    if (!merged.find(item => item.key === field.key)) merged.push({ ...field })
  })
  return merged
}

export function useFilterConfig(options = {}) {
  const pageCode = computed(() => unref(options.pageCode) || 'customer-list')
  const configKey = computed(() => `${pageCode.value}__filter_fields`)
  const defaults = computed(() => normalizeFields(unref(options.defaultFilters) || []))
  const storeKey = configKey.value

  if (stores.has(storeKey)) return stores.get(storeKey)

  const fields = ref(JSON.parse(JSON.stringify(defaults.value)))
  const drawerVisible = ref(false)
  const pendingFields = ref([])
  const loading = ref(false)

  const loadConfig = async () => {
    try {
      loading.value = true
      const res = parseConfig(await getFilterPreset(configKey.value))
      if (res && Array.isArray(res.fields)) {
        fields.value = mergeFields(res.fields, defaults.value)
      } else {
        fields.value = JSON.parse(JSON.stringify(defaults.value))
      }
    } catch {
      fields.value = JSON.parse(JSON.stringify(defaults.value))
    } finally {
      loading.value = false
    }
  }

  const saveConfig = async () => {
    const visibleFields = pendingFields.value.filter(field => field.placement !== 'hidden')
    if (!visibleFields.length) {
      ElMessage.warning('至少保留1个筛选项')
      return false
    }
    try {
      await putFilterPreset(configKey.value, { fields: pendingFields.value })
      fields.value = JSON.parse(JSON.stringify(pendingFields.value))
      drawerVisible.value = false
      ElMessage.success('筛选项配置已更新')
      return true
    } catch {
      ElMessage.error('保存筛选项配置失败')
      return false
    }
  }

  const openDrawer = () => {
    pendingFields.value = JSON.parse(JSON.stringify(fields.value))
    drawerVisible.value = true
  }

  const closeDrawer = () => {
    drawerVisible.value = false
  }

  const resetConfig = () => {
    pendingFields.value = JSON.parse(JSON.stringify(defaults.value))
  }

  const fieldPlacement = (key) => fields.value.find(field => field.key === key)?.placement || 'hidden'

  watch(defaults, (value) => {
    fields.value = mergeFields(fields.value, value)
  }, { deep: true })

  loadConfig()

  const api = {
    fields,
    drawerVisible,
    pendingFields,
    loading,
    loadConfig,
    saveConfig,
    openDrawer,
    closeDrawer,
    resetConfig,
    fieldPlacement,
  }
  stores.set(storeKey, api)
  return api
}
