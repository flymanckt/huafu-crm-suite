<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header"><span>数据字典管理</span></div>
      </template>

      <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
        <el-tab-pane label="字典类型" name="types">
          <dict-type-list ref="typeListRef" @select="onTypeSelect" />
        </el-tab-pane>
        <el-tab-pane label="字典项" name="items" :disabled="!selectedType">
          <dict-item-list ref="itemListRef" :dict-type="selectedType" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import DictTypeList from './DictTypeList.vue'
import DictItemList from './DictItemList.vue'

const activeTab = ref('types')
const selectedType = ref(null)

const onTypeSelect = (type) => {
  selectedType.value = type
  activeTab.value = 'items'
}

const handleTabChange = (tab) => {
  if (tab === 'types') {
    selectedType.value = null
  }
}
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>