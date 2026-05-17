<template>
  <el-drawer
    v-model="visible"
    title="筛选项配置"
    direction="rtl"
    :size="360"
    :show-close="true"
    @close="handleClose"
  >
    <div class="drawer-body">
      <div class="batch-actions">
        <span class="hint">设置每个筛选项在列表中的展示位置</span>
        <el-button link type="primary" @click="handleReset">重置</el-button>
      </div>

      <el-divider />

      <draggable
        v-model="localFields"
        item-key="key"
        handle=".drag-handle"
        ghost-class="ghost"
        class="filter-list"
      >
        <template #item="{ element }">
          <div class="filter-item" :class="{ 'filter-item--hidden': element.placement === 'hidden' }">
            <el-icon class="drag-handle"><Rank /></el-icon>
            <div class="filter-main">
              <span class="filter-label">{{ element.label }}</span>
              <el-radio-group v-model="element.placement" size="small">
                <el-radio-button value="default">默认</el-radio-button>
                <el-radio-button value="more">更多</el-radio-button>
                <el-radio-button value="hidden">隐藏</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </template>
      </draggable>
    </div>

    <template #footer>
      <div class="drawer-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleApply">应用此配置</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import draggable from 'vuedraggable'
import { Rank } from '@element-plus/icons-vue'
import { useFilterConfig } from '@/composables/useFilterConfig.js'

const props = defineProps({
  pageCode: { type: String, required: true },
  defaultFilters: { type: Array, default: () => [] }
})

const filterConfig = useFilterConfig({ pageCode: props.pageCode, defaultFilters: props.defaultFilters })
const localFields = ref([])

const visible = computed({
  get: () => filterConfig.drawerVisible.value,
  set: (val) => { if (!val) filterConfig.closeDrawer() }
})

watch(filterConfig.drawerVisible, (val) => {
  if (val) localFields.value = JSON.parse(JSON.stringify(filterConfig.pendingFields.value))
})

const handleReset = () => {
  filterConfig.resetConfig()
  localFields.value = JSON.parse(JSON.stringify(filterConfig.pendingFields.value))
}

const handleApply = async () => {
  filterConfig.pendingFields.value = JSON.parse(JSON.stringify(localFields.value))
  await filterConfig.saveConfig()
}

const handleClose = () => {
  filterConfig.closeDrawer()
}
</script>

<style scoped>
.drawer-body {
  padding: 0 16px;
}
.batch-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
}
.hint {
  color: #606266;
  font-size: 13px;
}
.filter-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border-radius: 4px;
  background: #fafafa;
}
.filter-item--hidden {
  opacity: 0.55;
}
.drag-handle {
  color: #bfbfbf;
  cursor: grab;
}
.drag-handle:active {
  cursor: grabbing;
}
.filter-main {
  min-width: 0;
  flex: 1;
}
.filter-label {
  display: block;
  margin-bottom: 8px;
  color: #303133;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
