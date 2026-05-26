<template>
  <div class="basic-info-tab">
    <!-- 编辑/保存按钮 -->
    <div class="tab-toolbar">
      <el-button v-if="!isEditing" type="primary" size="small" @click="isEditing = true">编辑</el-button>
      <template v-else>
        <el-button type="primary" size="small" @click="handleSave" :loading="saving">保存</el-button>
        <el-button size="small" @click="handleCancel">取消</el-button>
      </template>
    </div>

    <!-- 分组1: 基本信息 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header><span>基本信息</span></template>
      <el-descriptions :column="detailColumns" border v-if="!isEditing">
        <el-descriptions-item label="客户名称">{{ formData.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户名称简称">{{ formData.customerShortName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户编码">{{ formData.customerCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="SAP客户编号">{{ formData.sapCustomerCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户类型"><DictTag v-if="hasValue(formData.type)" dict-code="customer_type" :value="String(formData.type)" size="small" /><span v-else>-</span></el-descriptions-item>
        <el-descriptions-item label="客户状态"><DictTag v-if="hasValue(formData.status)" dict-code="customer_status" :value="String(formData.status)" size="small" /><span v-else>-</span></el-descriptions-item>
        <el-descriptions-item label="客户等级"><DictTag v-if="hasValue(formData.level)" dict-code="customer_level" :value="String(formData.level)" size="small" /><span v-else>-</span></el-descriptions-item>
        <el-descriptions-item label="客户阶段"><DictTag v-if="hasValue(formData.customerStage)" dict-code="customer_stage" :value="String(formData.customerStage)" size="small" /><span v-else>-</span></el-descriptions-item>
        <el-descriptions-item label="英文名称">{{ formData.englishName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="统一社会信用代码">{{ formData.unifiedSocialCreditCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="风险等级"><DictTag v-if="hasValue(formData.riskLevel)" dict-code="risk_level" :value="String(formData.riskLevel)" size="small" /><span v-else>-</span></el-descriptions-item>
      </el-descriptions>
      <el-form v-else :model="formData" label-width="100px" class="edit-form">
        <el-row :gutter="16">
          <el-col :span="formColSpan"><el-form-item label="客户名称" prop="customerName"><el-input v-model="formData.customerName" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="客户名称简称"><el-input v-model="formData.customerShortName" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="客户编码"><el-input v-model="formData.customerCode" disabled /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="SAP客户编号"><el-input v-model="formData.sapCustomerCode" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="客户类型" prop="type"><DictSelect v-model="formData.type" dict-code="customer_type" value-type="number" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="客户状态" prop="status"><DictSelect v-model="formData.status" dict-code="customer_status" value-type="number" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="客户等级"><DictSelect v-model="formData.level" dict-code="customer_level" value-type="number" clearable /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="客户阶段"><DictSelect v-model="formData.customerStage" dict-code="customer_stage" value-type="number" clearable /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="英文名称"><el-input v-model="formData.englishName" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="统一社会信用代码"><el-input v-model="formData.unifiedSocialCreditCode" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="风险等级"><DictSelect v-model="formData.riskLevel" dict-code="risk_level" value-type="number" clearable /></el-form-item></el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 分组2: 业务信息 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header><span>业务信息</span></template>
      <el-descriptions :column="detailColumns" border v-if="!isEditing">
        <el-descriptions-item label="业务类型"><DictTag v-if="hasValue(formData.businessType)" dict-code="biz_type" :value="String(formData.businessType)" size="small" /><span v-else>-</span></el-descriptions-item>
        <el-descriptions-item label="客户分类"><DictTag v-if="hasValue(formData.category)" dict-code="customer_category" :value="String(formData.category)" size="small" /><span v-else>-</span></el-descriptions-item>
        <el-descriptions-item label="客户来源"><DictTag v-if="hasValue(formData.source)" dict-code="customer_source" :value="String(formData.source)" size="small" /><span v-else>-</span></el-descriptions-item>
        <el-descriptions-item label="行业地位">{{ formData.industryPosition || '-' }}</el-descriptions-item>
        <el-descriptions-item label="主要客户群体"><DictTag v-if="hasValue(formData.customerGroup)" dict-code="customer_group" :value="String(formData.customerGroup)" size="small" /><span v-else>-</span></el-descriptions-item>
        <el-descriptions-item label="机台数">{{ formData.machineCount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产能情况">{{ formData.capacityInfo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="年营业额">{{ formData.annualRevenue ? formData.annualRevenue + '万元' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="国家区域">{{ formData.countryRegion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="主营品牌">{{ formData.mainBrand || '-' }}</el-descriptions-item>
        <el-descriptions-item label="年用纱量">{{ formData.annualYarnVolume || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else :model="formData" label-width="100px" class="edit-form">
        <el-row :gutter="16">
          <el-col :span="formColSpan"><el-form-item label="业务类型" prop="businessType"><DictSelect v-model="formData.businessType" dict-code="biz_type" value-type="number" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="客户分类"><DictSelect v-model="formData.category" dict-code="customer_category" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="客户来源"><DictSelect v-model="formData.source" dict-code="customer_source" clearable /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="行业地位"><el-input v-model="formData.industryPosition" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="主要客户群体"><DictSelect v-model="formData.customerGroup" dict-code="customer_group" clearable /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="机台数"><el-input-number v-model="formData.machineCount" :min="0" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="产能情况"><el-input v-model="formData.capacityInfo" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="年营业额"><el-input-number v-model="formData.annualRevenue" :min="0" :precision="2" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="国家区域"><el-input v-model="formData.countryRegion" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="主营品牌"><el-input v-model="formData.mainBrand" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="年用纱量"><el-input v-model="formData.annualYarnVolume" /></el-form-item></el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 分组3: 组织归属 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header><span>组织归属</span></template>
      <el-descriptions :column="detailColumns" border v-if="!isEditing">
        <el-descriptions-item label="归属部门">{{ resolveDeptName(formData.ownerDeptId) }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ resolveUserName(formData.ownerUserId) }}</el-descriptions-item>
        <el-descriptions-item label="销售跟单">{{ formData.salesName || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else :model="formData" label-width="100px" class="edit-form">
        <el-row :gutter="16">
          <el-col :span="formColSpan">
            <el-form-item label="归属部门">
              <el-tree-select
                v-model="formData.ownerDeptId"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'id', children: 'children' }"
                check-strictly
                filterable
                default-expand-all
                clearable
                placeholder="请选择最末级归属部门"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="formColSpan">
            <el-form-item label="负责人">
              <el-select v-model="formData.ownerUserId" clearable filterable style="width:100%" @change="handleOwnerUserChange">
                <el-option v-for="user in userOptions" :key="user.id" :label="user.realName || user.username" :value="user.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="formColSpan"><el-form-item label="销售跟单"><el-input v-model="formData.salesName" /></el-form-item></el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 分组4: 地址与联络 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header><span>地址与联络</span></template>
      <el-descriptions :column="detailColumns" border v-if="!isEditing">
        <el-descriptions-item label="省/市/区">{{ [formData.province, formData.city, formData.district].filter(Boolean).join('/') || '-' }}</el-descriptions-item>
        <el-descriptions-item label="详细地址">{{ formData.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="经度">{{ formData.locationLng || '-' }}</el-descriptions-item>
        <el-descriptions-item label="纬度">{{ formData.locationLat || '-' }}</el-descriptions-item>
        <el-descriptions-item label="主联系人">{{ formData.mainContactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="主联系人电话">{{ formData.mainContactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="主联系人职务">{{ formData.mainContactRole || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else :model="formData" label-width="100px" class="edit-form">
        <el-row :gutter="16">
          <el-col :span="formColSpan"><el-form-item label="省份"><el-input v-model="formData.province" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="城市"><el-input v-model="formData.city" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="区县"><el-input v-model="formData.district" /></el-form-item></el-col>
          <el-col :span="24">
            <el-form-item label="详细地址">
              <div class="address-input-row">
                <el-input v-model="formData.address" placeholder="请输入地址后校验，或打开地图选择" />
                <el-button native-type="button" @click="verifyTypedAddress" :loading="addressVerifying">定位校验</el-button>
                <el-button native-type="button" @click="openAddressPicker">地图选择</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="formColSpan"><el-form-item label="经度"><el-input v-model="formData.locationLng" disabled /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="纬度"><el-input v-model="formData.locationLat" disabled /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="主联系人"><el-input v-model="formData.mainContactName" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="主联系人电话"><el-input v-model="formData.mainContactPhone" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="主联系人职务"><el-input v-model="formData.mainContactRole" /></el-form-item></el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 分组5: 捆绑关系 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header><span>捆绑关系</span></template>
      <el-descriptions :column="detailColumns" border v-if="!isEditing">
        <el-descriptions-item label="捆绑客户/品牌">{{ formData.bundleCustomerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="捆绑客户SAP代码">{{ formData.bundleCustomerSapCode || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else :model="formData" label-width="100px" class="edit-form">
        <el-row :gutter="16">
          <el-col :span="formColSpan">
            <el-form-item label="捆绑客户/品牌">
              <el-select
                v-model="formData.bundleCustomerId"
                filterable
                remote
                clearable
                reserve-keyword
                :remote-method="searchBundleCustomers"
                :loading="bundleCustomerLoading"
                placeholder="搜索并选择客户主数据"
                style="width:100%"
                @change="handleBundleCustomerChange"
                @clear="clearBundleCustomer"
              >
                <el-option
                  v-for="customer in bundleCustomerOptions"
                  :key="customer.id"
                  :label="formatBundleCustomerLabel(customer)"
                  :value="customer.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="formColSpan"><el-form-item label="捆绑客户SAP代码"><el-input v-model="formData.bundleCustomerSapCode" disabled /></el-form-item></el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 分组6: 财务信息 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header><span>财务信息</span></template>
      <el-descriptions :column="detailColumns" border v-if="!isEditing">
        <el-descriptions-item label="资产类型"><DictTag v-if="hasValue(formData.assetType)" dict-code="asset_type" :value="String(formData.assetType)" size="small" /><span v-else>-</span></el-descriptions-item>
        <el-descriptions-item label="税号">{{ formData.taxId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开户行">{{ formData.bankName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="银行账号">{{ formData.bankAccount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发票抬头">{{ formData.invoiceTitle || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else :model="formData" label-width="100px" class="edit-form">
        <el-row :gutter="16">
          <el-col :span="formColSpan"><el-form-item label="资产类型"><DictSelect v-model="formData.assetType" dict-code="asset_type" clearable /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="税号"><el-input v-model="formData.taxId" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="开户行"><el-input v-model="formData.bankName" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="银行账号"><el-input v-model="formData.bankAccount" /></el-form-item></el-col>
          <el-col :span="formColSpan"><el-form-item label="发票抬头"><el-input v-model="formData.invoiceTitle" /></el-form-item></el-col>
        </el-row>
      </el-form>
    </el-card>

    <AddressPicker
      v-model="addressPickerVisible"
      :initial-address="formData.address"
      @select="handleAddressSelect"
    />
  </div>
</template>

<script setup>
import { computed, inject, onMounted, ref, watch } from 'vue'
import { updateCustomer, getCustomerDetail, getCustomerPage } from '@/api/customer'
import { getDeptTree, getUserPage } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import AddressPicker from '@/components/common/AddressPicker.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import DictTag from '@/components/Dict/DictTag.vue'
import { geocodeAddress } from '@/utils/amap'
import {
  buildCustomerUpdatePayload,
  normalizeCustomerForForm
} from '@/utils/customerFields'

const props = defineProps({
  customerId: { type: [String, Number], required: true },
  detail: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['updated'])
const injectedDetailColumns = inject('customerDetailColumns', ref(3))
const detailColumns = computed(() => Number(injectedDetailColumns.value || 3))
const formColSpan = computed(() => 24 / detailColumns.value)

const isEditing = ref(false)
const saving = ref(false)
const addressVerifying = ref(false)
const addressPickerVisible = ref(false)
const bundleCustomerLoading = ref(false)
const bundleCustomerOptions = ref([])
const formData = ref({})
const deptTree = ref([])
const userOptions = ref([])

const flattenDepts = (nodes = []) => nodes.flatMap(node => [node, ...flattenDepts(node.children || [])])
const hasValue = (value) => value !== null && value !== undefined && value !== ''

const resolveDeptName = (id) => {
  if (!id) return '-'
  return flattenDepts(deptTree.value).find(dept => String(dept.id) === String(id))?.deptName || id
}

const resolveUserName = (id) => {
  if (!id) return '-'
  const user = userOptions.value.find(item => String(item.id) === String(id))
  return user?.realName || user?.username || id
}

const handleOwnerUserChange = (id) => {
  const user = userOptions.value.find(item => String(item.id) === String(id))
  formData.value.ownerDeptId = user?.deptId || null
}

const formatBundleCustomerLabel = (customer) => {
  if (!customer) return ''
  const code = customer.customerCode ? `/${customer.customerCode}` : ''
  const sap = customer.sapCustomerCode ? ` SAP:${customer.sapCustomerCode}` : ''
  return `${customer.customerName || customer.customerShortName || customer.id}${code}${sap}`
}

const addBundleOption = (customer) => {
  if (!customer?.id) return
  if (!bundleCustomerOptions.value.some(item => String(item.id) === String(customer.id))) {
    bundleCustomerOptions.value.push(customer)
  }
}

const defaultBundleCustomer = (customer = {}) => ({
  id: customer.bundleCustomerId || customer.id,
  customerName: customer.bundleCustomerName || customer.customerName || customer.customerShortName,
  sapCustomerCode: customer.bundleCustomerSapCode || customer.sapCustomerCode
})

const applyDefaultBundleCustomer = (customer = {}) => {
  if (customer.bundleCustomerId || !customer.id) return
  formData.value.bundleCustomerId = customer.id
  formData.value.bundleCustomerName = customer.customerName || customer.customerShortName || ''
  formData.value.bundleCustomerSapCode = customer.sapCustomerCode || ''
  formData.value.bundleBrand = ''
}

const searchBundleCustomers = async (keyword = '') => {
  bundleCustomerLoading.value = true
  try {
    const res = await getCustomerPage({ current: 1, size: 20, customerName: keyword || '' })
    bundleCustomerOptions.value = res?.records || []
    addBundleOption(defaultBundleCustomer(formData.value))
  } finally {
    bundleCustomerLoading.value = false
  }
}

const handleBundleCustomerChange = (id) => {
  const selected = bundleCustomerOptions.value.find(item => String(item.id) === String(id))
  if (!selected) return
  formData.value.bundleCustomerId = selected.id
  formData.value.bundleCustomerName = selected.customerName || selected.customerShortName || ''
  formData.value.bundleCustomerSapCode = selected.sapCustomerCode || ''
  formData.value.bundleBrand = ''
}

const clearBundleCustomer = () => {
  formData.value.bundleCustomerId = null
  formData.value.bundleCustomerName = ''
  formData.value.bundleCustomerSapCode = ''
  formData.value.bundleBrand = ''
}

const loadOptions = async () => {
  try {
	    const [depts, users] = await Promise.all([
	      getDeptTree(),
	      getUserPage({ current: 1, size: 500 })
	    ])
    deptTree.value = depts || []
    userOptions.value = users?.records || users?.list || users || []
    addBundleOption(defaultBundleCustomer(props.detail))
  } catch (error) {
    deptTree.value = []
    userOptions.value = []
  }
}

const applyLocatedAddress = (data) => {
  if (!data) return
  formData.value.locationLng = data.lng
  formData.value.locationLat = data.lat
  if (data.province) formData.value.province = data.province
  if (data.city) formData.value.city = data.city
  if (data.district) formData.value.district = data.district
}

const handleAddressSelect = (data) => {
  if (data?.address) formData.value.address = data.address
  applyLocatedAddress(data)
  ElMessage.success('已选择地图地址')
}

const openAddressPicker = () => {
  addressPickerVisible.value = true
}

const buildSearchAddress = () => {
  return [formData.value.province, formData.value.city, formData.value.district, formData.value.address]
    .filter(Boolean)
    .join('')
}

const verifyTypedAddress = async () => {
  const address = buildSearchAddress()
  if (!address) {
    ElMessage.warning('请先输入详细地址')
    return
  }
  addressVerifying.value = true
  try {
    const result = await geocodeAddress(address, formData.value.city || '')
    applyLocatedAddress(result)
    ElMessage.success('定位校验成功')
  } finally {
    addressVerifying.value = false
  }
}

watch(() => props.detail, (val) => {
  formData.value = normalizeCustomerForForm(val)
  applyDefaultBundleCustomer(val)
  addBundleOption(defaultBundleCustomer(formData.value))
}, { immediate: true, deep: true })

const handleSave = async () => {
  saving.value = true
  try {
    await updateCustomer(props.customerId, buildCustomerUpdatePayload(formData.value))
    ElMessage.success('保存成功')
    isEditing.value = false
    // P0-1：保存后强制回查，检测丢字段
    await verifySavedData()
    emit('updated')
  } finally {
    saving.value = false
  }
}

/**
 * P0-1：保存后回查，检测关键字段是否在保存后丢失
 * 如果发现丢字段，弹窗警告但不阻塞流程
 */
const verifySavedData = async () => {
  try {
    const refreshed = await getCustomerDetail(props.customerId)
    const original = formData.value
    const lostFields = []

    const keyFields = [
      'customerName', 'customerShortName', 'sapCustomerCode',
      'mainContactName', 'mainContactPhone', 'province', 'city',
      'district', 'address', 'unifiedSocialCreditCode'
    ]
    for (const field of keyFields) {
      if (original[field] !== undefined && original[field] !== null && original[field] !== '') {
        if (refreshed[field] === undefined || refreshed[field] === null || refreshed[field] === '') {
          lostFields.push(field)
        }
      }
    }
    if (lostFields.length > 0) {
      await ElMessageBox.alert(
        `检测到以下字段保存后丢失，请联系管理员排查：\n${lostFields.join('、')}`,
        '数据异常警告',
        { type: 'warning', confirmButtonText: '知道了' }
      )
    }
  } catch (e) {
    console.warn('[BasicInfoTab] 回查失败', e)
  }
}

const handleCancel = () => {
  formData.value = normalizeCustomerForForm(props.detail)
  isEditing.value = false
}

onMounted(loadOptions)
</script>

<style scoped>
.basic-info-tab { padding: 0; }
.tab-toolbar { margin-bottom: 10px; text-align: right; }
.basic-info-tab :deep(.el-card) { margin-bottom: 10px !important; border-radius: 6px; }
.basic-info-tab :deep(.el-card__header) { padding: 9px 14px; font-weight: 600; }
.basic-info-tab :deep(.el-card__body) { padding: 10px 14px; }
.basic-info-tab :deep(.el-descriptions__cell) { padding: 7px 10px; }
.edit-form { padding: 0; }
.edit-form :deep(.el-form-item) { margin-bottom: 10px; }
.address-input-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 8px;
  width: 100%;
}
@media (max-width: 768px) {
  .address-input-row {
    grid-template-columns: 1fr;
  }
}
</style>
