<template>
  <el-tag
    :class="customClass"
    :style="customStyle"
    :size="size"
    :effect="effect"
    :hit="hit"
    :disable-transitions="disableTransitions"
    :type="elementPlusType"
  >
    {{ displayText }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'
import { useDict } from '@/composables/useDict'

const props = defineProps({
  dictCode: { type: String, required: true },
  value: { type: String, required: true },
  size: { type: String, default: 'default' },
  effect: { type: String, default: 'light' },
  hit: { type: Boolean, default: false },
  disableTransitions: { type: Boolean, default: false }
})

const { getDictLabel, getDictColor } = useDict()

const displayText = computed(() => getDictLabel(props.dictCode, props.value))

// 设计文档定义的多彩色 Map（key = 后端返回的 itemCode）
const COLOR_MAP = {
  // 客户等级
  customer_level: {
    'A': { bg: '#fff0f6', color: '#c41d7f', border: '#ffadd2' }, // A级（核心）VIP 粉
    'B': { bg: '#fff7e6', color: '#d46b08', border: '#ffd591' }, // B级（重要）橙
    'C': { bg: '#f0f0f0', color: '#595959', border: '#d9d9d9' }, // C级（普通）灰
    'D': { bg: '#e6f4ff', color: '#1677ff', border: '#91caff' }, // D级（潜在）蓝
  },
  // 客户状态
  customer_status: {
    'POTENTIAL': { bg: '#f0f0f0', color: '#595959', border: '#d9d9d9' }, // 潜在客户 灰
    'ACTIVE':    { bg: '#f6ffed', color: '#389e0d', border: '#b7eb8f' }, // 活跃客户 绿
    'INACTIVE':  { bg: '#fff2e8', color: '#fa8c16', border: '#ffbb96' }, // 非活跃 橙
    'LOST':      { bg: '#fff1f0', color: '#cf1322', border: '#ffa39e' }, // 流失客户 红
  },
}

const customStyle = computed(() => {
  const map = COLOR_MAP[props.dictCode]
  if (map && map[props.value]) {
    const { bg, color, border } = map[props.value]
    return {
      backgroundColor: bg,
      color,
      borderColor: border,
      fontWeight: '500',
      borderRadius: '4px',
    }
  }
  return {}
})

// 如果走自定义色，type 不用（light effect 下 type 不生效）
const customClass = computed(() => {
  const map = COLOR_MAP[props.dictCode]
  if (map && map[props.value]) {
    return 'dict-tag--custom'
  }
  return ''
})

const elementPlusType = computed(() => {
  const map = COLOR_MAP[props.dictCode]
  if (map && map[props.value]) return '' // '' 表示不用 EP 内置 type
  const colorMap = { primary: 'primary', success: 'success', warning: 'warning', danger: 'danger', info: 'info' }
  return colorMap[getDictColor(props.dictCode, props.value)] || 'info'
})
</script>

<style scoped>
:deep(.dict-tag--custom) {
  border-style: solid;
  font-weight: 500;
}
</style>
