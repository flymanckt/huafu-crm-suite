/**
 * 格式化工具集
 * - formatNumber: 千分位/金额格式化
 * - formatDate: 友好日期（今天/昨天/N天前）
 */

// 千分位 + 金额格式化
export function formatNumber(value, options = {}) {
  if (value === null || value === undefined || value === '') return '-'
  const num = Number(value)
  if (isNaN(num)) return value

  const {
    prefix = '',      // 前缀如 ¥
    decimals = 2,     // 小数位
    suffix = '',      // 后缀
    negativeRed = true, // 负数红色（返回对象）
  } = options

  const abs = Math.abs(num)
  const formatted = abs.toLocaleString('zh-CN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  })

  const result = `${prefix}${formatted}${suffix}`
  if (num < 0 && negativeRed) {
    return `<span style="color:#cf1322">-${prefix}${formatted}${suffix}</span>`
  }
  return num < 0 ? `-${result}` : result
}

// 友好日期
export function formatDate(dateStr, mode = 'friendly') {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr

  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const targetDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const diffDays = Math.round((targetDay.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))

  const timeStr = date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  const fullStr = date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })

  if (mode === 'full') return fullStr

  if (diffDays === 0) return `今天 ${timeStr}`
  if (diffDays === -1) return `昨天 ${timeStr}`
  if (diffDays > -7 && diffDays < 0) return `${Math.abs(diffDays)}天前`

  // 跨年显示完整，否则显示 MM-DD
  if (date.getFullYear() !== now.getFullYear()) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  }
  return `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
