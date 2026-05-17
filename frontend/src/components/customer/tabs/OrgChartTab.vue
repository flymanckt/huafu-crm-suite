<template>
  <div class="org-chart-tab">
    <div class="tab-toolbar">
      <el-button size="small" @click="expandAll">展开全部</el-button>
      <el-button size="small" @click="collapseAll">折叠全部</el-button>
    </div>

    <el-card shadow="never">
      <el-tree
        ref="treeRef"
        :data="treeData"
        :props="treeProps"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
        draggable
        @node-click="handleNodeClick"
        @node-drop="handleDrop"
      >
        <template #default="{ node, data }">
          <span class="tree-node">
            <span class="node-icon">
              <el-icon v-if="data.isMain === 1" color="#f56c6c"><Star /></el-icon>
              <el-icon v-else><User /></el-icon>
            </span>
            <span class="node-label">{{ node.label }}</span>
            <el-tag v-if="data.isMain === 1" type="danger" size="small" style="margin-left:8px">主</el-tag>
            <span class="node-position">{{ data.position }}</span>
          </span>
        </template>
      </el-tree>
    </el-card>

    <el-empty v-if="!treeData.length" description="暂无组织架构数据" :image-size="60" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { Star, User } from '@element-plus/icons-vue'
import { getContactList } from '@/api/customer'

const props = defineProps({
  customerId: { type: [String, Number], required: true },
  contacts: { type: Array, default: () => [] }
})

const treeRef = ref(null)
const localContacts = ref([])
const treeProps = {
  label: 'label',
  children: 'children'
}

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

const expandAll = () => {
  const nodes = treeRef.value?.store.nodesMap || {}
  Object.values(nodes).forEach(n => n.expand())
}

const collapseAll = () => {
  const nodes = treeRef.value?.store.nodesMap || {}
  Object.values(nodes).forEach(n => n.collapse())
}

const handleNodeClick = (data) => {
  console.log('点击节点:', data)
}

const handleDrop = (draggingNode, dropNode, dropType) => {
  console.log('拖拽:', draggingNode.data, dropNode.data, dropType)
  // 实际应调用API更新parentContactId
}
</script>

<style scoped>
.org-chart-tab { padding: 8px 0; }
.tab-toolbar { margin-bottom: 12px; }

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
}
.node-icon {
  display: flex;
  align-items: center;
}
.node-label {
  font-weight: 500;
}
.node-position {
  color: #999;
  font-size: 12px;
  margin-left: 8px;
}
</style>
