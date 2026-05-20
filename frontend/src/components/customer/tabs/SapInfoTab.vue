<template>
  <div class="sap-info-tab">
    <div class="tab-toolbar">
      <el-button v-if="!hasEditingRow" type="primary" size="small" @click="handleAdd">新增SAP信息</el-button>
      <el-button v-if="hasEditingRow" type="success" size="small" @click="handleSaveRow" :loading="saving">保存</el-button>
      <el-button v-if="hasEditingRow" size="small" @click="handleCancelEdit">取消</el-button>
    </div>

    <el-table :data="tableData" stripe border size="small" class="sap-table">
      <el-table-column prop="sapCode" label="SAP编号" width="150">
        <template #default="{ row }">
          <el-input v-if="row._editing" v-model="row.sapCode" size="small" />
          <span v-else>{{ row.sapCode }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="isDefault" label="默认" width="80" align="center">
        <template #default="{ row }">
          <el-switch v-if="row._editing" v-model="row.isDefault" :active-value="1" :inactive-value="0" />
          <el-tag v-else-if="row.isDefault === 1" size="small">默认</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="accountGroup" label="账户组" width="120">
        <template #default="{ row }">
          <DictSelect v-if="row._editing" v-model="row.accountGroup" dict-code="sap_account_group" size="small" style="width:100%" />
          <span v-else>{{ row.accountGroup }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="countryCode" label="国家代码" width="100">
        <template #default="{ row }"><el-input v-if="row._editing" v-model="row.countryCode" size="small" /><span v-else>{{ row.countryCode }}</span></template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="180" show-overflow-tooltip>
        <template #default="{ row }"><el-input v-if="row._editing" v-model="row.description" size="small" /><span v-else>{{ row.description }}</span></template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <template v-if="!row._editing">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!tableData.length" description="暂无SAP信息" :image-size="60" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createSapInfoV1, deleteSapInfoV1, getSapInfoListV1, updateSapInfoV1 } from '@/api/customer'
import DictSelect from '@/components/Dict/DictSelect.vue'

const props = defineProps({ customerId: { type: [String, Number], required: true } })
const tableData = ref([])
const saving = ref(false)
const editingRow = ref(null)
const hasEditingRow = computed(() => tableData.value.some(row => row._editing))

const loadData = async () => {
  if (!props.customerId) return
  tableData.value = await getSapInfoListV1(props.customerId)
}

const emptyRow = () => ({
  _editing: true,
  _isNew: true,
  sapCode: '',
  isDefault: tableData.value.length ? 0 : 1,
  accountGroup: '',
  countryCode: '',
  description: ''
})

const handleAdd = () => {
  if (hasEditingRow.value) return
  tableData.value.push(emptyRow())
}

const handleEdit = (row) => {
  row._editing = true
  editingRow.value = { ...row }
}

const handleSaveRow = async () => {
  const row = tableData.value.find(item => item._editing)
  if (!row) return
  if (!row.sapCode) {
    ElMessage.warning('请填写SAP编号')
    return
  }
  saving.value = true
  const payload = { ...row }
  delete payload._editing
  delete payload._isNew
  try {
    if (row._isNew) {
      await createSapInfoV1(props.customerId, payload)
    } else {
      await updateSapInfoV1(props.customerId, row.id, payload)
    }
    await loadData()
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

const handleCancelEdit = () => {
  if (tableData.value.some(row => row._isNew)) {
    tableData.value = tableData.value.filter(row => !row._isNew)
  } else if (editingRow.value) {
    const idx = tableData.value.findIndex(row => row.id === editingRow.value.id)
    if (idx >= 0) tableData.value[idx] = editingRow.value
  }
  tableData.value.forEach(row => { row._editing = false })
  editingRow.value = null
}

const handleDelete = async (row) => {
  await deleteSapInfoV1(props.customerId, row.id)
  await loadData()
  ElMessage.success('删除成功')
}

onMounted(loadData)
watch(() => props.customerId, loadData)
</script>

<style scoped>
.sap-info-tab { padding: 8px 0; }
.tab-toolbar { margin-bottom: 12px; text-align: right; }
.sap-table { margin-bottom: 12px; }
</style>
