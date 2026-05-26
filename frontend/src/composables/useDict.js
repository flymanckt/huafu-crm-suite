import { ref, reactive } from 'vue'
import { getBatchDicts, getDictItems } from '@/api/system/dict'

// 全局字典缓存
const dictCache = reactive({})
const dictLoaded = ref(new Set())

export function useDict() {
  // 批量加载字典
  const loadDictItems = async (codes) => {
    const unloadedCodes = codes.filter(c => !dictLoaded.value.has(c))
    if (unloadedCodes.length === 0) return

    try {
      const res = await getBatchDicts(unloadedCodes)
      // 兼容不同响应格式
      const data = res || {}
      Object.assign(dictCache, data)
      unloadedCodes.forEach(c => dictLoaded.value.add(c))
    } catch (error) {
      console.error(`加载字典失败: ${unloadedCodes.join(',')}`, error)
    }
  }

  // 加载单个字典
  const fetchDictItems = async (code) => {
    if (dictCache[code]) return dictCache[code]
    try {
      const res = await getDictItems(code)
      const items = Array.isArray(res) ? res : (res || [])
      dictCache[code] = items
      dictLoaded.value.add(code)
      return items
    } catch (error) {
      console.error(`加载字典[${code}]失败:`, error)
      return []
    }
  }

  // 获取字典项显示文本
  const getDictLabel = (code, itemCode) => {
    const items = dictCache[code] || []
    const normalized = String(itemCode ?? '')
    return items.find(i => String(i.itemCode) === normalized)?.itemName || itemCode
  }

  // 获取字典项颜色
  const getDictColor = (code, itemCode) => {
    const items = dictCache[code] || []
    const normalized = String(itemCode ?? '')
    return items.find(i => String(i.itemCode) === normalized)?.color || ''
  }

  return {
    dictCache,
    dictLoaded,
    loadDictItems,
    getDictItems: fetchDictItems,
    getDictLabel,
    getDictColor
  }
}
