<template>
  <div class="org-chart-tab">
    <div class="tab-toolbar">
      <div>
        <div class="toolbar-title">组织架构图</div>
        <div class="toolbar-subtitle">按联系人上下级关系生成，可在联系人页签维护上级关系</div>
      </div>
      <el-radio-group v-model="scaleMode" size="small">
        <el-radio-button v-for="item in scaleOptions" :key="item.value" :value="item.value">
          {{ item.label }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="treeData.length" class="org-chart-shell">
      <div class="org-chart-canvas" :class="`scale-${scaleMode}`">
        <ul class="org-root-list">
          <OrgChartNode
            v-for="root in treeData"
            :key="root.id"
            :node="root"
            @select="handleNodeClick"
          />
        </ul>
      </div>
    </div>

    <el-empty v-if="!treeData.length" description="暂无组织架构数据" :image-size="60" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { getContactList } from '@/api/customer'
import OrgChartNode from '@/components/customer/OrgChartNode.vue'

const props = defineProps({
  customerId: { type: [String, Number], required: true },
  contacts: { type: Array, default: () => [] }
})

const localContacts = ref([])
const scaleMode = ref('standard')
const scaleOptions = [
  { label: '标准', value: 'standard' },
  { label: '紧凑', value: 'compact' },
  { label: '宽松', value: 'wide' }
]

// 如果外部传入了 contacts（组织架构图组件外部已经加载），直接用；否则自己加载
const contactSource = computed(() => props.contacts.length > 0 ? props.contacts : localContacts.value)

const loadData = async () => {
  if (!props.customerId) return
  const list = await getContactList(props.customerId)
  localContacts.value = list || []
}

onMounted(() => {
  if (!props.contacts.length) {
    loadData()
  }
})

watch(() => props.customerId, (val) => {
  if (val && !props.contacts.length) loadData()
})

// 构建树形数据
const treeData = computed(() => {
  if (!contactSource.value.length) return []

  const map = {}
  const roots = []

  // 初始化所有节点
  contactSource.value.forEach(c => {
    map[c.id] = {
      id: c.id,
      label: c.contactName,
      position: c.position || '',
      department: c.department || c.deptName || '',
      phone: c.mobile || c.phone || c.telephone || '',
      isMain: c.isMain,
      children: []
    }
  })

  // 建立父子关系（parentContactId 为上级）
  contactSource.value.forEach(c => {
    if (c.parentContactId && map[c.parentContactId]) {
      map[c.parentContactId].children.push(map[c.id])
    } else {
      roots.push(map[c.id])
    }
  })

  return roots
})

const handleNodeClick = () => {}
</script>

<style scoped>
.org-chart-tab {
  padding: 0;
}
.tab-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
  padding: 4px 2px 12px;
  border-bottom: 1px solid #edf1f7;
}
.toolbar-title {
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 700;
}
.toolbar-subtitle {
  margin-top: 3px;
  color: #7a8798;
  font-size: 12px;
}
.org-chart-shell {
  min-height: 520px;
  overflow: auto;
  padding: 24px;
  border: 1px solid #e1e8f2;
  border-radius: 6px;
  background:
    linear-gradient(#f4f7fb 1px, transparent 1px),
    linear-gradient(90deg, #f4f7fb 1px, transparent 1px),
    #ffffff;
  background-size: 28px 28px;
}
.org-chart-canvas {
  min-width: max-content;
  padding: 18px 28px 34px;
  transform-origin: top center;
}
.org-chart-canvas.scale-compact {
  transform: scale(0.88);
}
.org-chart-canvas.scale-wide {
  transform: scale(1.08);
}
.org-root-list {
  display: flex;
  justify-content: center;
  gap: 18px;
  margin: 0;
  padding: 0;
  list-style: none;
}
@media (max-width: 768px) {
  .tab-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
  .org-chart-shell {
    padding: 14px;
  }
}
</style>
