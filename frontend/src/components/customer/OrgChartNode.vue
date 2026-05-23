<template>
  <li class="org-node-wrap">
    <div
      class="org-node"
      :class="{ 'is-main': node.isMain === 1 }"
      @click="$emit('select', node)"
    >
      <div class="node-header">
        <span class="node-name">{{ node.label || '-' }}</span>
        <el-tag v-if="node.isMain === 1" type="danger" size="small">主联系人</el-tag>
      </div>
      <div class="node-title">{{ node.position || '未维护职务' }}</div>
      <div class="node-meta">
        <span v-if="node.department">{{ node.department }}</span>
        <span v-if="node.phone">{{ node.phone }}</span>
      </div>
    </div>
    <ul v-if="node.children && node.children.length" class="org-children">
      <OrgChartNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        @select="$emit('select', $event)"
      />
    </ul>
  </li>
</template>

<script setup>
defineProps({
  node: { type: Object, required: true }
})

defineEmits(['select'])
</script>

<style scoped>
.org-node-wrap {
  position: relative;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: 0 14px;
}
.org-node {
  width: 210px;
  min-height: 92px;
  padding: 12px 14px;
  border: 1px solid #b8c7da;
  border-top: 4px solid #4f7db8;
  border-radius: 3px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 6px 16px rgba(33, 80, 130, 0.12);
  cursor: pointer;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}
.org-node:hover {
  border-color: #337ecc;
  box-shadow: 0 8px 22px rgba(33, 80, 130, 0.18);
  transform: translateY(-1px);
}
.org-node.is-main {
  border-top-color: #d94b4b;
}
.node-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}
.node-name {
  min-width: 0;
  color: #1f2d3d;
  font-size: 15px;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.node-title {
  color: #45556c;
  font-size: 13px;
  line-height: 20px;
}
.node-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 7px;
  color: #7a8798;
  font-size: 12px;
  line-height: 18px;
}
.org-children {
  position: relative;
  display: flex;
  justify-content: center;
  gap: 0;
  margin: 28px 0 0;
  padding: 28px 0 0;
  list-style: none;
}
.org-children::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  height: 28px;
  border-left: 1px solid #9eb3ca;
}
.org-children > .org-node-wrap::before,
.org-children > .org-node-wrap::after {
  content: '';
  position: absolute;
  top: -28px;
  width: 50%;
  height: 28px;
  border-top: 1px solid #9eb3ca;
}
.org-children > .org-node-wrap::before {
  right: 50%;
}
.org-children > .org-node-wrap::after {
  left: 50%;
}
.org-children > .org-node-wrap:first-child::before,
.org-children > .org-node-wrap:last-child::after {
  border-top: 0;
}
.org-children > .org-node-wrap:only-child::before,
.org-children > .org-node-wrap:only-child::after {
  display: none;
}
.org-children > .org-node-wrap::after,
.org-children > .org-node-wrap::before {
  border-color: #9eb3ca;
}
.org-children > .org-node-wrap:not(:only-child)::before,
.org-children > .org-node-wrap:not(:only-child)::after {
  border-top-style: solid;
}
.org-children > .org-node-wrap > .org-node::before {
  content: '';
  position: absolute;
  top: -28px;
  left: 50%;
  height: 28px;
  border-left: 1px solid #9eb3ca;
}
</style>
