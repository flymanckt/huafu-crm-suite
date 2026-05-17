import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const MAX_TABS = 10
const PROTECTED_TABS = ['/dashboard']

export const useTabStore = defineStore('tabs', () => {
  const tabs = ref([
    { path: '/dashboard', title: '工作台', protected: true }
  ])
  const activeTab = ref('/dashboard')

  const canAddMore = computed(() => tabs.value.length < MAX_TABS)

  function openTab(path, title) {
    // 已打开 → 直接跳转
    const existing = tabs.value.find(t => t.path === path)
    if (existing) {
      if (title && existing.title !== title) existing.title = title
      activeTab.value = path
      return true
    }
    // 新建
    if (tabs.value.length >= MAX_TABS) {
      return false
    }
    tabs.value.push({
      path,
      title,
      protected: PROTECTED_TABS.includes(path)
    })
    activeTab.value = path
    return true
  }

  function closeTab(path) {
    const idx = tabs.value.findIndex(t => t.path === path)
    if (idx === -1) return
    const tab = tabs.value[idx]
    if (tab.protected) return

    tabs.value.splice(idx, 1)

    // 关闭当前 tab → 切换到隔壁
    if (activeTab.value === path) {
      const next = tabs.value[Math.min(idx, tabs.value.length - 1)]
      activeTab.value = next?.path || '/dashboard'
    }
  }

  function closeOtherTabs(path) {
    tabs.value = tabs.value.filter(t => t.protected || t.path === path)
    activeTab.value = path
  }

  function closeRightTabs(path) {
    const idx = tabs.value.findIndex(t => t.path === path)
    tabs.value = tabs.value.filter((t, i) => i <= idx || t.protected)
    activeTab.value = path
  }

  function closeAllTabs() {
    tabs.value = tabs.value.filter(t => t.protected)
    activeTab.value = tabs.value[0]?.path || '/dashboard'
  }

  function switchTab(path) {
    if (tabs.value.find(t => t.path === path)) {
      activeTab.value = path
    }
  }

  function updateTabTitle(path, title) {
    const tab = tabs.value.find(t => t.path === path)
    if (tab) tab.title = title
  }

  return {
    tabs,
    activeTab,
    canAddMore,
    openTab,
    closeTab,
    closeOtherTabs,
    closeRightTabs,
    closeAllTabs,
    switchTab,
    updateTabTitle
  }
})
