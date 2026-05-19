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
      <el-descriptions :column="2" border v-if="!isEditing">
        <el-descriptions-item label="客户名称">{{ formData.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户编码">{{ formData.customerCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户类型">{{ customerLabel.type(formData.type) }}</el-descriptions-item>
        <el-descriptions-item label="客户状态"><el-tag :type="customerStatusType[formData.status]">{{ customerLabel.status(formData.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="客户等级">{{ customerLabel.level(formData.level) }}</el-descriptions-item>
        <el-descriptions-item label="客户阶段">{{ customerLabel.stage(formData.customerStage) }}</el-descriptions-item>
        <el-descriptions-item label="英文名称">{{ formData.englishName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="统一社会信用代码">{{ formData.unifiedSocialCreditCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="黑名单"><el-tag :type="formData.blacklist === 1 ? 'danger' : 'info'">{{ formData.blacklist === 1 ? '是' : '否' }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="风险等级"><el-tag :type="riskLevelTagType[formData.riskLevel]">{{ customerLabel.riskLevel(formData.riskLevel) }}</el-tag></el-descriptions-item>
      </el-descriptions>
      <el-form v-else :model="formData" label-width="100px" class="edit-form">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="客户名称" prop="customerName"><el-input v-model="formData.customerName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户编码"><el-input v-model="formData.customerCode" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户类型" prop="type"><DictSelect v-model="formData.type" dict-code="customer_type" value-type="number" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户状态" prop="status"><DictSelect v-model="formData.status" dict-code="customer_status" value-type="number" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户等级"><DictSelect v-model="formData.level" dict-code="customer_level" value-type="number" clearable /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户阶段"><DictSelect v-model="formData.customerStage" dict-code="customer_stage" value-type="number" clearable /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="英文名称"><el-input v-model="formData.englishName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="统一社会信用代码"><el-input v-model="formData.unifiedSocialCreditCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="黑名单"><DictSelect v-model="formData.blacklist" dict-code="yes_no" value-type="number" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="风险等级"><DictSelect v-model="formData.riskLevel" dict-code="risk_level" value-type="number" clearable /></el-form-item></el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 分组2: 业务信息 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header><span>业务信息</span></template>
      <el-descriptions :column="2" border v-if="!isEditing">
        <el-descriptions-item label="业务类型">{{ customerLabel.businessType(formData.businessType) }}</el-descriptions-item>
        <el-descriptions-item label="客户分类">{{ customerLabel.category(formData.category) }}</el-descriptions-item>
        <el-descriptions-item label="客户来源">{{ customerLabel.source(formData.source) }}</el-descriptions-item>
        <el-descriptions-item label="行业地位">{{ formData.industryPosition || '-' }}</el-descriptions-item>
        <el-descriptions-item label="主要客户群体">{{ customerLabel.group(formData.customerGroup) }}</el-descriptions-item>
        <el-descriptions-item label="机台数">{{ formData.machineCount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产能情况">{{ formData.capacityInfo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="年营业额">{{ formData.annualRevenue ? formData.annualRevenue + '万元' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="国家/地区">{{ formData.countryRegion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="主营品牌">{{ formData.mainBrand || '-' }}</el-descriptions-item>
        <el-descriptions-item label="年用纱量">{{ formData.annualYarnVolume || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else :model="formData" label-width="100px" class="edit-form">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="业务类型" prop="businessType"><DictSelect v-model="formData.businessType" dict-code="biz_type" value-type="number" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户分类"><DictSelect v-model="formData.category" dict-code="customer_category" value-type="number" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户来源"><DictSelect v-model="formData.source" dict-code="customer_source" value-type="number" clearable /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="行业地位"><el-input v-model="formData.industryPosition" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="主要客户群体"><DictSelect v-model="formData.customerGroup" dict-code="customer_group" value-type="number" clearable /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="机台数"><el-input-number v-model="formData.machineCount" :min="0" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="产能情况"><el-input v-model="formData.capacityInfo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="年营业额"><el-input-number v-model="formData.annualRevenue" :min="0" :precision="2" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="国家/地区"><el-input v-model="formData.countryRegion" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="主营品牌"><el-input v-model="formData.mainBrand" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="年用纱量"><el-input v-model="formData.annualYarnVolume" /></el-form-item></el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 分组3: 组织归属 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header><span>组织归属</span></template>
      <el-descriptions :column="2" border v-if="!isEditing">
        <el-descriptions-item label="归属部门">{{ resolveDeptName(formData.ownerDeptId) }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ resolveUserName(formData.ownerUserId) }}</el-descriptions-item>
        <el-descriptions-item label="销售跟单">{{ formData.salesName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="销售组">{{ formData.salesGroup || '-' }}</el-descriptions-item>
        <el-descriptions-item label="国家区域">{{ regionMap[formData.region] || formData.region || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else :model="formData" label-width="100px" class="edit-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="归属部门">
              <el-tree-select
                v-model="formData.ownerDeptId"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'id', children: 'children' }"
                check-strictly
                clearable
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-select v-model="formData.ownerUserId" clearable filterable style="width:100%">
                <el-option v-for="user in userOptions" :key="user.id" :label="user.realName || user.username" :value="user.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="销售跟单"><el-input v-model="formData.salesName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="销售组"><el-input v-model="formData.salesGroup" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="国家区域"><el-input v-model="formData.region" /></el-form-item></el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 分组4: 地址与联络 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header><span>地址与联络</span></template>
      <el-descriptions :column="2" border v-if="!isEditing">
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
          <el-col :span="12"><el-form-item label="省份"><el-input v-model="formData.province" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="城市"><el-input v-model="formData.city" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="区县"><el-input v-model="formData.district" /></el-form-item></el-col>
          <el-col :span="24">
            <el-form-item label="详细地址">
              <div class="address-input-row">
                <el-input v-model="formData.address" placeholder="请输入地址后校验，或打开地图选择" />
                <el-button native-type="button" @click="verifyTypedAddress" :loading="addressVerifying">定位校验</el-button>
                <el-button native-type="button" @click="openAddressPicker">地图选择</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="经度"><el-input v-model="formData.locationLng" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="纬度"><el-input v-model="formData.locationLat" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="主联系人"><el-input v-model="formData.mainContactName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="主联系人电话"><el-input v-model="formData.mainContactPhone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="主联系人职务"><el-input v-model="formData.mainContactRole" /></el-form-item></el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 分组5: 捆绑关系 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header><span>捆绑关系</span></template>
      <el-descriptions :column="2" border v-if="!isEditing">
        <el-descriptions-item label="捆绑客户">{{ formData.bundleCustomerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="捆绑品牌">{{ formData.bundleBrand || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else :model="formData" label-width="100px" class="edit-form">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="捆绑客户"><el-input v-model="formData.bundleCustomerName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="捆绑品牌"><el-input v-model="formData.bundleBrand" /></el-form-item></el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 分组6: 财务信息 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header><span>财务信息</span></template>
      <el-descriptions :column="2" border v-if="!isEditing">
        <el-descriptions-item label="资产类型">{{ formData.assetType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="税号">{{ formData.taxId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开户行">{{ formData.bankName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="银行账号">{{ formData.bankAccount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发票抬头">{{ formData.invoiceTitle || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else :model="formData" label-width="100px" class="edit-form">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="资产类型"><el-input v-model="formData.assetType" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="税号"><el-input v-model="formData.taxId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="开户行"><el-input v-model="formData.bankName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="银行账号"><el-input v-model="formData.bankAccount" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="发票抬头"><el-input v-model="formData.invoiceTitle" /></el-form-item></el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 分组7: SAP信息 -->
    <el-card shadow="never">
      <template #header><span>SAP信息</span></template>
      <el-descriptions :column="2" border v-if="!isEditing">
        <el-descriptions-item label="SAP客户编码">{{ formData.sapCustomerCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="账户组">{{ formData.sapAccountGroup || '-' }}</el-descriptions-item>
        <el-descriptions-item label="国家代码">{{ formData.sapCountryCode || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else :model="formData" label-width="100px" class="edit-form">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="SAP客户编码"><el-input v-model="formData.sapCustomerCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="账户组"><el-input v-model="formData.sapAccountGroup" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="国家代码"><el-input v-model="formData.sapCountryCode" disabled /></el-form-item></el-col>
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
import { onMounted, ref, watch } from 'vue'
import { updateCustomer, getCustomerDetail } from '@/api/customer'
import { getDeptTree, getUserPage } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import AddressPicker from '@/components/common/AddressPicker.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import { geocodeAddress } from '@/utils/amap'
import {
  buildCustomerUpdatePayload,
  customerLabel,
  customerStatusType,
  normalizeCustomerForForm
} from '@/utils/customerFields'

const props = defineProps({
  customerId: { type: [String, Number], required: true },
  detail: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['updated'])

const isEditing = ref(false)
const saving = ref(false)
const addressVerifying = ref(false)
const addressPickerVisible = ref(false)
const formData = ref({})
const deptTree = ref([])
const userOptions = ref([])

const regionMap = { 1: '华东', 2: '华南', 3: '华北', 4: '海外' }
const riskLevelTagType = { 1: 'success', 2: 'warning', 3: 'danger' }

const flattenDepts = (nodes = []) => nodes.flatMap(node => [node, ...flattenDepts(node.children || [])])

const resolveDeptName = (id) => {
  if (!id) return '-'
  return flattenDepts(deptTree.value).find(dept => String(dept.id) === String(id))?.deptName || id
}

const resolveUserName = (id) => {
  if (!id) return '-'
  const user = userOptions.value.find(item => String(item.id) === String(id))
  return user?.realName || user?.username || id
}

const loadOptions = async () => {
  try {
    const [depts, users] = await Promise.all([
      getDeptTree(),
      getUserPage({ pageNum: 1, pageSize: 200 })
    ])
    deptTree.value = depts || []
    userOptions.value = users?.records || users?.list || users || []
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
.basic-info-tab { padding: 8px 0; }
.tab-toolbar { margin-bottom: 16px; text-align: right; }
.edit-form { padding: 8px 0; }
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
