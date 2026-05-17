import { computed, ref, unref } from 'vue'
import { getFilterPreset, putFilterPreset } from '@/api/customer.js'
import { ElMessage, ElMessageBox } from 'element-plus'

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

export function useFilterPreset(options = {}) {
  const configKey = computed(() => unref(options.pageCode) || 'customer-list')
  const MAX_PRESETS = 10

  const presets = ref([])
  const activePresetName = ref(null)
  const saveDropdownVisible = ref(false)
  const newPresetName = ref('')
  const loading = ref(false)

  const canSaveMore = computed(() => presets.value.length < MAX_PRESETS)

  const loadPresets = async () => {
    try {
      loading.value = true
      const res = parseConfig(await getFilterPreset(configKey.value))
      if (res && Array.isArray(res.presets)) {
        presets.value = res.presets
      } else {
        presets.value = []
      }
    } catch {
      presets.value = []
    } finally {
      loading.value = false
    }
  }

  const savePreset = async (filters) => {
    if (!newPresetName.value.trim()) return false
    if (presets.value.length >= MAX_PRESETS) {
      ElMessage.warning(`预设数量已达上限 (${MAX_PRESETS}/${MAX_PRESETS})，请删除旧预设后再保存`)
      return false
    }
      const name = newPresetName.value.trim()
      const newPreset = { name, filters }
    try {
      const updated = [...presets.value.filter(p => p.name !== name), newPreset]
      await putFilterPreset(configKey.value, { presets: updated })
      presets.value = updated
      activePresetName.value = name
      newPresetName.value = ''
      saveDropdownVisible.value = false
      ElMessage.success(`预设"${name}"已保存`)
      return true
    } catch {
      ElMessage.error('保存预设失败')
      return false
    }
  }

  const overwritePreset = async (preset, filters) => {
    try {
      const updated = presets.value.map(p =>
        p.name === preset.name ? { ...p, filters } : p
      )
      await putFilterPreset(configKey.value, { presets: updated })
      presets.value = updated
      activePresetName.value = preset.name
      saveDropdownVisible.value = false
      newPresetName.value = ''
      ElMessage.success(`预设"${preset.name}"已更新`)
    } catch {
      ElMessage.error('更新预设失败')
    }
  }

  const loadPreset = async (preset, currentFilters) => {
    const hasCurrentFilters = Object.values(currentFilters).some(v => v !== '' && v !== null && v !== undefined)
    if (hasCurrentFilters) {
      try {
        await ElMessageBox.confirm('加载预设将覆盖当前筛选条件，是否继续？', '加载预设', {
          confirmButtonText: '继续',
          cancelButtonText: '取消',
          type: 'info',
        })
      } catch {
        return null
      }
    }
    activePresetName.value = preset.name
    return { ...preset.filters }
  }

  const deletePreset = async (preset) => {
    try {
      await ElMessageBox.confirm(`删除预设"${preset.name}"？该操作不可撤销。`, '删除预设', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
      })
      const updated = presets.value.filter(p => p.name !== preset.name)
      await putFilterPreset(configKey.value, { presets: updated })
      presets.value = updated
      if (activePresetName.value === preset.name) {
        activePresetName.value = null
      }
      ElMessage.success('预设已删除')
    } catch {
      // 用户取消
    }
  }

  const clearActivePreset = () => {
    activePresetName.value = null
  }

  return {
    presets,
    activePresetName,
    saveDropdownVisible,
    newPresetName,
    loading,
    canSaveMore,
    loadPresets,
    savePreset,
    overwritePreset,
    loadPreset,
    deletePreset,
    clearActivePreset,
  }
}
