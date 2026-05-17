<template>
  <div class="filter-shell">
    <el-form :inline="true" :model="modelValue" class="search-form">
      <template v-for="field in defaultFields" :key="field.key">
        <el-form-item :label="field.label">
          <component
            :is="controlOf(field)"
            :model-value="modelValue[field.key]"
            v-bind="controlProps(field)"
            @update:model-value="value => updateField(field.key, value)"
            @keyup.enter="emit('search')"
          />
        </el-form-item>
      </template>
      <el-form-item class="action-item">
        <el-button type="primary" @click="emit('search')">{{ searchText }}</el-button>
        <el-button @click="emit('reset')">{{ resetText }}</el-button>
        <el-button
          v-if="moreFields.length"
          link
          type="primary"
          class="more-toggle"
          @click="expanded = !expanded"
        >
          {{ expanded ? '收起筛选' : '更多筛选' }}
          <el-icon class="toggle-icon" :class="{ 'is-up': expanded }"><ArrowDown /></el-icon>
        </el-button>
        <el-button :icon="Setting" circle title="筛选项配置" @click="filterConfig.openDrawer()" />
      </el-form-item>
    </el-form>

    <el-collapse-transition>
      <el-form v-if="expanded && moreFields.length" :inline="true" :model="modelValue" class="search-form search-form--more">
        <template v-for="field in moreFields" :key="field.key">
          <el-form-item :label="field.label">
            <component
              :is="controlOf(field)"
              :model-value="modelValue[field.key]"
              v-bind="controlProps(field)"
              @update:model-value="value => updateField(field.key, value)"
              @keyup.enter="emit('search')"
            />
          </el-form-item>
        </template>
      </el-form>
    </el-collapse-transition>

    <FilterConfigDrawer :page-code="pageCode" :default-filters="defaultFilters" />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ArrowDown, Setting } from '@element-plus/icons-vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import FilterConfigDrawer from '@/components/FilterConfig/FilterConfigDrawer.vue'
import { useFilterConfig } from '@/composables/useFilterConfig.js'

const props = defineProps({
  pageCode: { type: String, required: true },
  defaultFilters: { type: Array, default: () => [] },
  modelValue: { type: Object, required: true },
  searchText: { type: String, default: '搜索' },
  resetText: { type: String, default: '重置' },
})

const emit = defineEmits(['update:modelValue', 'search', 'reset'])
const expanded = ref(false)
const filterConfig = useFilterConfig({ pageCode: props.pageCode, defaultFilters: props.defaultFilters })

const defaultFields = computed(() => filterConfig.fields.value.filter(field => field.placement === 'default'))
const moreFields = computed(() => filterConfig.fields.value.filter(field => field.placement === 'more'))

const updateField = (key, value) => {
  emit('update:modelValue', { ...props.modelValue, [key]: value })
}

const controlOf = (field) => {
  if (field.type === 'dict') return DictSelect
  if (field.type === 'date' || field.type === 'daterange') return 'el-date-picker'
  if (field.type === 'number') return 'el-input-number'
  if (field.type === 'select') return 'el-select'
  return 'el-input'
}

const controlProps = (field) => {
  const base = {
    clearable: field.type !== 'number',
    disabled: field.disabled,
    placeholder: field.placeholder || (field.type === 'dict' || field.type === 'select' ? '全部' : `请输入${field.label}`),
    style: { width: `${field.width || 160}px` },
  }
  if (field.type === 'dict') {
    return {
      ...base,
      dictCode: field.dictCode,
      valueType: field.valueType,
      filterable: true,
    }
  }
  if (field.type === 'date') {
    return {
      ...base,
      type: 'date',
      valueFormat: 'YYYY-MM-DD',
    }
  }
  if (field.type === 'daterange') {
    return {
      ...base,
      type: 'daterange',
      valueFormat: 'YYYY-MM-DD',
      startPlaceholder: '开始',
      endPlaceholder: '结束',
      style: { width: `${field.width || 260}px` },
    }
  }
  if (field.type === 'number') {
    return {
      ...base,
      min: field.min,
      max: field.max,
      precision: field.precision,
      controlsPosition: 'right',
    }
  }
  if (field.type === 'select') {
    return base
  }
  return base
}
</script>

<style scoped>
.filter-shell {
  margin-bottom: 16px;
}
.search-form {
  margin-bottom: 0;
}
.search-form--more {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #dcdfe6;
}
.action-item :deep(.el-form-item__content) {
  gap: 4px;
}
.more-toggle {
  margin-left: 4px;
}
.toggle-icon {
  margin-left: 2px;
  transition: transform 0.2s;
}
.toggle-icon.is-up {
  transform: rotate(180deg);
}
</style>
