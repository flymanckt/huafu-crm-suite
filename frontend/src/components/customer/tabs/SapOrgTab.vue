<template>
  <div class="sap-org-tab">
    <div class="tab-toolbar">
      <el-button v-if="!hasEditingRow" type="primary" size="small" @click="handleAdd">新增SAP组织</el-button>
      <el-button v-if="hasEditingRow" type="success" size="small" @click="handleSaveRow" :loading="saving">保存</el-button>
      <el-button v-if="hasEditingRow" size="small" @click="handleCancelEdit">取消</el-button>
    </div>

    <el-table :data="tableData" stripe border size="small" class="sap-table">
      <el-table-column prop="sapCode" label="SAP编号" width="150">
        <template #default="{ row }">
          <el-select v-if="row._editing" v-model="row.sapCode" size="small" filterable clearable placeholder="选择SAP编号" style="width:100%">
            <el-option v-for="item in sapInfos" :key="item.id" :label="item.sapCode" :value="item.sapCode" />
          </el-select>
          <span v-else>{{ row.sapCode || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="companyCode" label="公司代码" width="150">
        <template #default="{ row }">
          <DictSelect v-if="row._editing" v-model="row.companyCode" dict-code="company_code" size="small" style="width:100%" />
          <span v-else>{{ row.companyCode }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="salesOrg" label="销售组织" width="150">
        <template #default="{ row }">
          <DictSelect v-if="row._editing" v-model="row.salesOrg" dict-code="sales_org" size="small" style="width:100%" />
          <span v-else>{{ row.salesOrg }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="salesOffice" label="销售办公室" width="150">
        <template #default="{ row }">
          <DictSelect v-if="row._editing" v-model="row.salesOffice" dict-code="sales_office" size="small" style="width:100%" />
          <span v-else>{{ row.salesOffice }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="salesGroup" label="销售组" width="150">
        <template #default="{ row }">
          <DictSelect v-if="row._editing" v-model="row.salesGroup" dict-code="sales_group" size="small" style="width:100%" />
          <span v-else>{{ row.salesGroup }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="priceList" label="价格清单" width="150">
        <template #default="{ row }">
          <DictSelect v-if="row._editing" v-model="row.priceList" dict-code="price_list" size="small" style="width:100%" />
          <span v-else>{{ row.priceList }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="currency" label="货币" width="150">
        <template #default="{ row }">
          <DictSelect v-if="row._editing" v-model="row.currency" dict-code="currency" size="small" style="width:100%" />
          <span v-else>{{ row.currency }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="deliveryPlant" label="交货工厂" width="150">
        <template #default="{ row }">
          <DictSelect v-if="row._editing" v-model="row.deliveryPlant" dict-code="delivery_plant" size="small" style="width:100%" />
          <span v-else>{{ row.deliveryPlant }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="paymentTerms" label="付款条件" width="150">
        <template #default="{ row }">
          <DictSelect v-if="row._editing" v-model="row.paymentTerms" dict-code="payment_terms" size="small" style="width:100%" />
          <span v-else>{{ row.paymentTerms }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="taxClassification" label="税分类" width="150">
        <template #default="{ row }">
          <DictSelect v-if="row._editing" v-model="row.taxClassification" dict-code="tax_classification" size="small" style="width:100%" />
          <span v-else>{{ row.taxClassification }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row, $index }">
          <template v-if="!row._editing">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row, $index)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!tableData.length" description="暂无SAP组织数据" :image-size="60" />
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createSapOrgV1, deleteSapOrgV1, getSapInfoListV1, getSapOrgListV1, updateSapOrgV1 } from '@/api/customer'
import DictSelect from '@/components/Dict/DictSelect.vue'

const props = defineProps({
  customerId: { type: [String, Number], required: true }
})

const tableData = ref([])
const sapInfos = ref([])
const saving = ref(false)
const editingRow = ref(null)
const hasEditingRow = computed(() => tableData.value.some(r => r._editing))

const loadData = async () => {
  if (!props.customerId) return
  const [orgs, infos] = await Promise.all([
    getSapOrgListV1(props.customerId),
    getSapInfoListV1(props.customerId)
  ])
  tableData.value = orgs || []
  sapInfos.value = infos || []
}

const handleAdd = () => {
  if (hasEditingRow.value) return
  tableData.value.push({
    _editing: true,
    _isNew: true,
    sapCode: sapInfos.value.find(item => item.isDefault === 1)?.sapCode || '',
    companyCode: '',
    salesOrg: '',
    salesOffice: '',
    salesGroup: '',
    priceList: '',
    currency: '',
    deliveryPlant: '',
    paymentTerms: '',
    taxClassification: ''
  })
}

const handleEdit = (row) => {
  row._editing = true
  editingRow.value = { ...row }
}

const handleSaveRow = async () => {
  const row = tableData.value.find(r => r._editing)
  if (row) {
    if (!row.companyCode) {
      ElMessage.warning('请填写公司代码')
      return
    }
    saving.value = true
    const payload = { ...row }
    delete payload._editing
    delete payload._isNew
    try {
      if (row._isNew) {
        await createSapOrgV1(props.customerId, payload)
      } else {
        await updateSapOrgV1(props.customerId, row.id, payload)
      }
      await loadData()
    } finally {
      saving.value = false
    }
    ElMessage.success('保存成功')
  }
}

const handleCancelEdit = () => {
  if (tableData.value.some(r => r._isNew)) {
    tableData.value = tableData.value.filter(r => !r._isNew)
  } else if (editingRow.value) {
    const idx = tableData.value.findIndex(r => r.id === editingRow.value.id)
    if (idx >= 0) tableData.value[idx] = editingRow.value
  }
  tableData.value.forEach(r => { r._editing = false })
  editingRow.value = null
}

const handleDelete = async (row, index) => {
  await deleteSapOrgV1(props.customerId, row.id)
  await loadData()
  ElMessage.success('删除成功')
}

onMounted(loadData)
watch(() => props.customerId, loadData)
</script>

<style scoped>
.sap-org-tab { padding: 8px 0; }
.tab-toolbar { margin-bottom: 12px; text-align: right; }
.sap-table { margin-bottom: 12px; }
</style>
