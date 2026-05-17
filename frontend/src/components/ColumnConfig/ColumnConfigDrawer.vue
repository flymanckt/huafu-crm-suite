<template>
  <el-drawer
    v-model="visible"
    title="列配置"
    direction="rtl"
    :size="320"
    :show-close="true"
    @close="handleClose"
  >
    <template #header>
      <div class="drawer-header">
        <span class="drawer-title">列配置</span>
      </div>
    </template>

    <div class="drawer-body">
      <!-- 批量操作栏 -->
      <div class="batch-actions">
        <el-checkbox
          v-model="allChecked"
          @change="handleCheckAll"
          :indeterminate="isIndeterminate"
        >全选可选列</el-checkbox>
        <el-button link type="primary" @click="handleReset">重置</el-button>
      </div>

      <el-divider />

      <!-- 固定列区域 -->
      <div class="section-title fixed-label">固定列</div>
      <div class="fixed-columns">
        <div v-for="col in fixedColumns" :key="col.key" class="column-item column-item--fixed">
          <el-checkbox :model-value="true" disabled />
          <span class="column-label">{{ col.label }}</span>
        </div>
      </div>

      <el-divider />

      <!-- 可选列区域：拖拽排序 -->
      <div class="section-title">可选择列</div>
      <draggable
        v-model="localOptional"
        item-key="key"
        handle=".drag-handle"
        ghost-class="ghost"
        @end="onDragEnd"
        class="optional-columns"
      >
        <template #item="{ element }">
          <div class="column-item" :class="{ 'column-item--hidden': !element.visible }">
            <el-icon class="drag-handle"><Rank /></el-icon>
            <el-checkbox
              :model-value="element.visible"
              @change="() => handleToggleVisible(element.key)"
            />
            <span class="column-label">{{ element.label }}</span>
            <div class="width-slider">
              <el-slider
                :model-value="element.width"
                :min="80"
                :max="300"
                :step="10"
                :show-tooltip="true"
                :format-tooltip="formatTooltip"
                @input="(val) => updateWidthLocal(element.key, val)"
                size="small"
              />
              <span class="width-value">{{ element.width }}px</span>
            </div>
          </div>
        </template>
      </draggable>
    </div>

    <!-- Footer -->
    <template #footer>
      <div class="drawer-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleApply">应用此配置</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import draggable from 'vuedraggable'
import { Rank } from '@element-plus/icons-vue'
import { useColumnConfig } from '@/composables/useColumnConfig.js'

const props = defineProps({
  pageCode: { type: String, default: 'customer-list' },
  defaultColumns: { type: Array, default: () => [] }
})

const {
  drawerVisible,
  pendingColumns,
  fixedColumns,
  optionalColumns,
  saveConfig,
  closeDrawer,
  toggleVisible,
  onDragEnd: configOnDragEnd,
  updateWidth,
  resetConfig,
  checkAllOptional,
  allOptionalChecked,
} = useColumnConfig({ pageCode: props.pageCode, defaultColumns: props.defaultColumns })

const visible = computed({
  get: () => drawerVisible.value,
  set: (val) => { if (!val) closeDrawer() }
})

  // 固定列（本地副本）
  const localFixed = ref([])
  // 可选列（用于拖拽）
  const localOptional = ref([])

watch(drawerVisible, (val) => {
  if (val) {
    localFixed.value = JSON.parse(JSON.stringify(fixedColumns.value))
    localOptional.value = JSON.parse(JSON.stringify(optionalColumns.value))
  }
})

const allChecked = computed({
  get: () => allOptionalChecked.value,
  set: (val) => handleCheckAll(val)
})

const isIndeterminate = computed(() => {
  const optional = localOptional.value
  const checked = optional.filter(c => c.visible).length
  return checked > 0 && checked < optional.length
})

const handleCheckAll = (checked) => {
  localOptional.value.forEach(c => { c.visible = checked })
}

const handleReset = () => {
  resetConfig()
  // 重新同步
  const defaults = JSON.parse(JSON.stringify(fixedColumns.value.concat(optionalColumns.value)))
  localFixed.value = defaults.filter(c => c.fixed)
  localOptional.value = defaults.filter(c => !c.fixed)
}

const formatTooltip = (val) => `${val}px`

// 触发 toggle 时同步到 localOptional
const handleToggleVisible = (key) => {
  const col = localOptional.value.find(c => c.key === key)
  if (!col) return
  if (col.visible) {
    const others = localOptional.value.filter(c => c.visible && c.key !== key)
    if (others.length === 0) return // 至少保留1个
  }
  col.visible = !col.visible
}

const updateWidthLocal = (key, width) => {
  const col = localOptional.value.find(c => c.key === key)
  if (col) {
    col.width = width
    updateWidth(key, width)
  }
}

const onDragEnd = () => {
  configOnDragEnd()
}

const handleApply = async () => {
  // 同步回 pendingColumns
  pendingColumns.value = [...localFixed.value, ...localOptional.value]
  await saveConfig()
}

const handleClose = () => {
  closeDrawer()
}
</script>

<style scoped>
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.drawer-title {
  font-weight: 600;
  font-size: 16px;
}
.drawer-body {
  padding: 0 16px;
}
.batch-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}
.section-title {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}
.fixed-label {
  margin-top: 4px;
}
.fixed-columns {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.optional-columns {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 200px;
}
.column-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 4px;
  background: #fafafa;
  transition: opacity 0.2s;
}
.column-item--fixed {
  opacity: 0.7;
}
.column-item--hidden {
  opacity: 0.5;
}
.drag-handle {
  cursor: grab;
  color: #bfbfbf;
  font-size: 14px;
}
.drag-handle:active {
  cursor: grabbing;
}
.column-label {
  flex: 0 0 80px;
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.column-item--fixed .column-label {
  color: #999;
}
.width-slider {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}
.width-value {
  font-size: 11px;
  color: #999;
  flex: 0 0 32px;
  text-align: right;
}
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid #e8e8e8;
}
:deep(.ghost) {
  opacity: 0.5;
  background: #e6f4ff;
  border: 1px dashed #91caff;
}
:deep(.el-overlay) {
  position: fixed !important;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2000;
}
:deep(.el-drawer) {
  position: fixed !important;
  top: 0;
  right: 0;
  bottom: 0;
  height: 100vh !important;
  width: 320px !important;
  z-index: 2001;
}
</style>
