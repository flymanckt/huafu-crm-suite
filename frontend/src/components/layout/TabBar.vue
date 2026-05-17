<template>
  <div class="tab-bar">
    <div class="tab-list" ref="tabListRef">
      <div
        v-for="tab in tabStore.tabs"
        :key="tab.path"
        :class="['tab-item', { active: tab.path === tabStore.activeTab, protected: tab.protected }]"
        @click="handleTabClick(tab)"
        @contextmenu.prevent="showContextMenu($event, tab)"
      >
        <span class="tab-title">{{ tab.title }}</span>
        <span v-if="!tab.protected" class="tab-close" @click.stop="handleClose(tab)">×</span>
      </div>
    </div>

    <!-- 右键菜单 -->
    <div
      v-if="contextMenu.visible"
      class="context-menu"
      :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
      @click.stop
    >
      <div class="context-item" @click="handleRefresh">刷新</div>
      <div class="context-item" @click="handleCloseOthers">关闭其他</div>
      <div class="context-item" @click="handleCloseRight">关闭右侧</div>
      <div class="context-item" @click="handleCloseAll">关闭全部</div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount } from 'vue'
import { useTabStore } from '@/stores/tabStore'
import { useRouter } from 'vue-router'

const tabStore = useTabStore()
const router = useRouter()
const tabListRef = ref(null)

const contextMenu = reactive({
  visible: false,
  x: 0,
  y: 0,
  tab: null
})

function handleTabClick(tab) {
  tabStore.switchTab(tab.path)
  router.push(tab.path)
}

function handleClose(tab) {
  const wasActive = tab.path === tabStore.activeTab
  tabStore.closeTab(tab.path)
  if (wasActive) {
    router.push(tabStore.activeTab)
  }
}

function showContextMenu(e, tab) {
  contextMenu.visible = true
  contextMenu.x = e.clientX
  contextMenu.y = e.clientY
  contextMenu.tab = tab
}

function hideContextMenu() {
  contextMenu.visible = false
}

function handleRefresh() {
  if (contextMenu.tab) {
    tabStore.switchTab(contextMenu.tab.path)
    router.replace(contextMenu.tab.path)
  }
  hideContextMenu()
}

function handleCloseOthers() {
  if (contextMenu.tab) {
    tabStore.closeOtherTabs(contextMenu.tab.path)
    router.push(tabStore.activeTab)
  }
  hideContextMenu()
}

function handleCloseRight() {
  if (contextMenu.tab) {
    tabStore.closeRightTabs(contextMenu.tab.path)
    router.push(tabStore.activeTab)
  }
  hideContextMenu()
}

function handleCloseAll() {
  tabStore.closeAllTabs()
  router.push(tabStore.activeTab)
  hideContextMenu()
}

// 点击其他地方关闭右键菜单
function handleDocumentClick() {
  hideContextMenu()
}

onMounted(() => {
  document.addEventListener('click', handleDocumentClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick)
})
</script>

<style scoped>
.tab-bar {
  position: relative;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  height: 40px;
  display: flex;
  align-items: center;
  padding: 0 12px;
}

.tab-list {
  display: flex;
  align-items: center;
  overflow-x: auto;
  gap: 2px;
  flex: 1;
  height: 100%;
}

.tab-list::-webkit-scrollbar {
  height: 0;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  height: 32px;
  background: transparent;
  border-radius: 4px 4px 0 0;
  cursor: pointer;
  color: #606266;
  font-size: 13px;
  white-space: nowrap;
  border: 1px solid transparent;
  border-bottom: none;
  transition: background 0.2s;
}

.tab-item:hover {
  background: #e4e7ed;
  color: #303133;
}

.tab-item.active {
  background: #fff;
  color: #409eff;
  border-color: #e4e7ed;
  border-bottom: 1px solid #fff;
}

.tab-item.protected .tab-title {
  font-weight: 500;
}

.tab-close {
  font-size: 14px;
  line-height: 1;
  color: #c0c4cc;
  padding: 2px;
  border-radius: 2px;
}

.tab-close:hover {
  background: #f56c6c;
  color: #fff;
}

.context-menu {
  position: fixed;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 9999;
  min-width: 120px;
}

.context-item {
  padding: 8px 16px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
}

.context-item:hover {
  background: #f5f7fa;
  color: #409eff;
}
</style>
