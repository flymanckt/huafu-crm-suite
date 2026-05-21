<template>
  <el-dialog
    v-if="asDialog"
    :model-value="visible"
    :title="isEdit ? '编辑客户' : '新建客户'"
    width="800px"
    destroy-on-close
    @close="handleCancel"
  >
    <div class="form-container">
      <el-steps :active="currentStep" finish-status="success" simple class="form-steps">
        <el-step title="基本信息" />
        <el-step title="更多业务信息" />
        <el-step title="快速添加联系人" />
      </el-steps>

      <div class="step-content">
        <!-- Step 1: 基本信息 -->
        <div v-show="currentStep === 0" class="step-panel">
          <el-form ref="step1FormRef" :model="form" :rules="step1Rules" label-width="120px">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="客户名称" prop="customerName">
                  <el-input v-model="form.customerName" placeholder="请输入客户名称" maxlength="255" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="客户名称简称">
                  <el-input v-model="form.customerShortName" placeholder="请输入客户名称简称" maxlength="128" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item v-if="isEdit" label="客户编码" prop="customerCode">
                  <el-input v-model="form.customerCode" placeholder="系统自动生成" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="客户类型" prop="type">
                  <DictSelect v-model="form.type" dict-code="customer_type" value-type="number" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="客户状态" prop="status">
                  <DictSelect v-model="form.status" dict-code="customer_status" value-type="number" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="客户等级">
                  <DictSelect v-model="form.level" dict-code="customer_level" value-type="number" clearable style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="业务类型" prop="businessType">
                  <DictSelect v-model="form.businessType" dict-code="biz_type" value-type="number" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="归属部门">
                  <el-tree-select
                    v-model="form.ownerDeptId"
                    :data="deptTree"
                    :props="{ label: 'deptName', value: 'id', children: 'children' }"
                    check-strictly
                    clearable
                    placeholder="请选择归属部门"
                    style="width:100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="负责人">
                  <el-select v-model="form.ownerUserId" placeholder="请选择负责人" clearable filterable style="width:100%">
                    <el-option v-for="user in userOptions" :key="user.id" :label="user.realName || user.username" :value="user.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="客户分类" prop="category">
                  <DictSelect v-model="form.category" dict-code="customer_category" value-type="number" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="省/市/区">
                  <el-cascader v-model="addressRegion" :options="regionOptions" placeholder="请选择省/市/区" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="详细地址">
                  <div class="address-input-row">
                    <el-input v-model="form.address" placeholder="请输入地址后校验，或打开地图选择" />
                    <el-button native-type="button" @click="verifyTypedAddress" :loading="addressVerifying">定位校验</el-button>
                    <el-button native-type="button" @click="openAddressPicker">地图选择</el-button>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="经度">
                  <el-input v-model="form.locationLng" disabled placeholder="定位后自动填写" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="纬度">
                  <el-input v-model="form.locationLat" disabled placeholder="定位后自动填写" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <!-- Step 2: 更多业务信息 -->
        <div v-show="currentStep === 1" class="step-panel">
          <el-form :model="form" label-width="120px">
            <el-divider content-position="left">业务信息扩展</el-divider>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="客户来源">
                  <DictSelect v-model="form.source" dict-code="customer_source" value-type="number" clearable style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="行业地位">
                  <el-input v-model="form.industryPosition" placeholder="请输入行业地位" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="主要客户群体">
                  <DictSelect v-model="form.customerGroup" dict-code="customer_group" value-type="number" clearable style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="国家区域">
                  <el-input v-model="form.countryRegion" placeholder="请输入国家区域，如中国、香港、海外" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="机台数">
                  <el-input-number v-model="form.machineCount" :min="0" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="产能情况">
                  <el-input v-model="form.capacityInfo" placeholder="请输入产能情况" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">财务信息</el-divider>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="年营业额(万元)">
                  <el-input-number v-model="form.annualRevenue" :precision="2" :min="0" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="信用额度(万元)">
                  <el-input-number v-model="form.creditLimit" :precision="2" :min="0" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="账期(天)">
                  <el-input-number v-model="form.paymentDays" :min="0" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">SAP基础信息</el-divider>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="SAP客户编码">
                  <el-input v-model="form.sapCustomerCode" placeholder="请输入SAP客户编码" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <!-- Step 3: 快速添加联系人 -->
        <div v-show="currentStep === 2" class="step-panel">
          <el-form :model="contactForm" label-width="100px">
            <div v-for="(contact, index) in contacts" :key="index" class="contact-item">
              <el-divider v-if="index > 0" content-position="left">联系人 {{ index + 1 }}</el-divider>
              <el-row :gutter="12">
                <el-col :span="12">
                  <el-form-item label="姓名" :prop="'contacts.' + index + '.contactName'">
                    <el-input v-model="contact.contactName" placeholder="请输入姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="职务">
                    <el-input v-model="contact.position" placeholder="请输入职务" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手机号">
                    <el-input v-model="contact.phone" placeholder="请输入手机号" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="电话">
                    <el-input v-model="contact.telephone" placeholder="请输入电话" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="EMAIL">
                    <el-input v-model="contact.email" placeholder="请输入EMAIL" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="主联系人">
                    <el-switch v-model="contact.isMain" :active-value="1" :inactive-value="0" />
                  </el-form-item>
                </el-col>
              </el-row>
              <div class="contact-actions" v-if="contacts.length > 1">
                <el-button link type="danger" size="small" @click="removeContact(index)">删除</el-button>
              </div>
            </div>

            <div class="add-more-contact">
              <el-button link type="primary" @click="addContact">
                <el-icon><Plus /></el-icon> 添加更多联系人
              </el-button>
            </div>
          </el-form>
        </div>
      </div>

      <div class="step-footer">
        <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="currentStep < 2" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="currentStep === 2" type="primary" @click="handleSubmit" :loading="submitting">完成创建</el-button>
      </div>
    </div>
  </el-dialog>

  <div v-else class="page-container">
    <el-card>
      <template #header>{{ isEdit ? '编辑客户' : '新建客户' }}</template>
      <div class="form-container">
        <el-steps :active="currentStep" finish-status="success" simple class="form-steps">
          <el-step title="基本信息" />
          <el-step title="更多业务信息" />
          <el-step title="快速添加联系人" />
        </el-steps>

        <div class="step-content">
          <!-- Step 1: 基本信息 -->
          <div v-show="currentStep === 0" class="step-panel">
            <el-form ref="step1FormRef" :model="form" :rules="step1Rules" label-width="120px">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="客户名称" prop="customerName">
                    <el-input v-model="form.customerName" placeholder="请输入客户名称" maxlength="255" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="客户名称简称">
                    <el-input v-model="form.customerShortName" placeholder="请输入客户名称简称" maxlength="128" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item v-if="isEdit" label="客户编码" prop="customerCode">
                    <el-input v-model="form.customerCode" placeholder="系统自动生成" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="客户类型" prop="type">
                    <DictSelect v-model="form.type" dict-code="customer_type" value-type="number" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="客户状态" prop="status">
                    <DictSelect v-model="form.status" dict-code="customer_status" value-type="number" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="客户等级">
                    <DictSelect v-model="form.level" dict-code="customer_level" value-type="number" clearable style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="业务类型" prop="businessType">
                    <DictSelect v-model="form.businessType" dict-code="biz_type" value-type="number" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="归属部门">
                    <el-tree-select
                      v-model="form.ownerDeptId"
                      :data="deptTree"
                      :props="{ label: 'deptName', value: 'id', children: 'children' }"
                      check-strictly
                      clearable
                      placeholder="请选择归属部门"
                      style="width:100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="负责人">
                    <el-select v-model="form.ownerUserId" placeholder="请选择负责人" clearable filterable style="width:100%">
                      <el-option v-for="user in userOptions" :key="user.id" :label="user.realName || user.username" :value="user.id" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="客户分类" prop="category">
                    <DictSelect v-model="form.category" dict-code="customer_category" value-type="number" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="省/市/区">
                    <el-cascader v-model="addressRegion" :options="regionOptions" placeholder="请选择省/市/区" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="详细地址">
                    <div class="address-input-row">
                      <el-input v-model="form.address" placeholder="请输入地址后校验，或打开地图选择" />
                      <el-button native-type="button" @click="verifyTypedAddress" :loading="addressVerifying">定位校验</el-button>
                      <el-button native-type="button" @click="openAddressPicker">地图选择</el-button>
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="经度">
                    <el-input v-model="form.locationLng" disabled placeholder="定位后自动填写" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="纬度">
                    <el-input v-model="form.locationLat" disabled placeholder="定位后自动填写" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>

          <!-- Step 2: 更多业务信息 -->
          <div v-show="currentStep === 1" class="step-panel">
            <el-form :model="form" label-width="120px">
              <el-divider content-position="left">业务信息扩展</el-divider>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="客户来源">
                    <DictSelect v-model="form.source" dict-code="customer_source" value-type="number" clearable style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="行业地位">
                    <el-input v-model="form.industryPosition" placeholder="请输入行业地位" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="主要客户群体">
                    <DictSelect v-model="form.customerGroup" dict-code="customer_group" value-type="number" clearable style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="国家区域">
                    <el-input v-model="form.countryRegion" placeholder="请输入国家区域，如中国、香港、海外" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="机台数">
                    <el-input-number v-model="form.machineCount" :min="0" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="产能情况">
                    <el-input v-model="form.capacityInfo" placeholder="请输入产能情况" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-divider content-position="left">财务信息</el-divider>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="年营业额(万元)">
                    <el-input-number v-model="form.annualRevenue" :precision="2" :min="0" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="信用额度(万元)">
                    <el-input-number v-model="form.creditLimit" :precision="2" :min="0" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="账期(天)">
                    <el-input-number v-model="form.paymentDays" :min="0" style="width:100%" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-divider content-position="left">SAP基础信息</el-divider>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="SAP客户编码">
                    <el-input v-model="form.sapCustomerCode" placeholder="请输入SAP客户编码" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>

          <!-- Step 3: 快速添加联系人 -->
          <div v-show="currentStep === 2" class="step-panel">
            <el-form :model="contactForm" label-width="100px">
              <div v-for="(contact, index) in contacts" :key="index" class="contact-item">
                <el-divider v-if="index > 0" content-position="left">联系人 {{ index + 1 }}</el-divider>
                <el-row :gutter="12">
                  <el-col :span="12">
                    <el-form-item label="姓名" :prop="'contacts.' + index + '.contactName'">
                      <el-input v-model="contact.contactName" placeholder="请输入姓名" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="职务">
                      <el-input v-model="contact.position" placeholder="请输入职务" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="手机号">
                      <el-input v-model="contact.phone" placeholder="请输入手机号" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="电话">
                      <el-input v-model="contact.telephone" placeholder="请输入电话" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="EMAIL">
                      <el-input v-model="contact.email" placeholder="请输入EMAIL" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="主联系人">
                      <el-switch v-model="contact.isMain" :active-value="1" :inactive-value="0" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <div class="contact-actions" v-if="contacts.length > 1">
                  <el-button link type="danger" size="small" @click="removeContact(index)">删除</el-button>
                </div>
              </div>

              <div class="add-more-contact">
                <el-button link type="primary" @click="addContact">
                  <el-icon><Plus /></el-icon> 添加更多联系人
                </el-button>
              </div>
            </el-form>
          </div>
        </div>

        <div class="step-footer">
          <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
          <el-button v-if="currentStep < 2" type="primary" @click="nextStep">下一步</el-button>
          <el-button v-if="currentStep === 2" type="primary" @click="handleSubmit" :loading="submitting">完成创建</el-button>
          <el-button @click="handleCancel" style="margin-left:8px">取消</el-button>
        </div>
      </div>
    </el-card>
  </div>

  <AddressPicker
    v-model="addressPickerVisible"
    :initial-address="form.address"
    @select="handleAddressSelect"
  />
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createCustomer, getCustomerDetail, updateCustomer, createContactV1 } from '@/api/customer'
import { getDeptTree, getUserPage } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { buildCustomerUpdatePayload, normalizeCustomerForForm } from '@/utils/customerFields'
import { provinceCityDistrict, parseAddressRegion } from '@/utils/regionData'
import { geocodeAddress } from '@/utils/amap'
import AddressPicker from '@/components/common/AddressPicker.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'

const props = defineProps({
  visible: { type: Boolean, default: undefined },
  customerId: { type: [String, Number], default: null }
})
const emit = defineEmits(['update:visible', 'saved', 'cancel'])

const route = useRoute()
const router = useRouter()
const asDialog = computed(() => props.visible !== undefined)
const currentId = computed(() => props.customerId || route.params.id || null)
const isEdit = computed(() => !!currentId.value)
const submitting = ref(false)
const currentStep = ref(0)
const step1FormRef = ref()
const deptTree = ref([])
const userOptions = ref([])

const defaultForm = () => ({
  // === 基础信息 ===
  customerCode: '',
  customerName: '',
  customerShortName: '',
  englishName: '',
  unifiedSocialCreditCode: '',
  // === 客户类型/状态/等级 ===
  type: null,
  level: null,
  status: 1,
  customerStage: null,
  businessType: null,
  // === 分类与来源 ===
  category: null,
  source: null,
  customerGroup: null,
  customerSegment: '',
  customerSource: null,
  // === 地址与地区 ===
  province: '',
  city: '',
  district: '',
  address: '',
  countryRegion: '',
  // === 主要联系信息 ===
  mainContactName: '',
  mainContactPhone: '',
  mainContactRole: '',
  // === 归属 ===
  ownerDeptId: null,
  ownerUserId: null,
  // === 业务信息 ===
  mainBrand: '',
  annualYarnVolume: null,
  machineCount: null,
  capacityInfo: '',
  industryPosition: '',
  // === 财务 ===
  annualRevenue: null,
  creditLimit: null,
  taxRate: null,
  paymentDays: null,
  // === SAP ===
  sapCustomerCode: '',
  companyCode: '',
  priceList: '',
  currency: '',
  deliveryFactory: '',
  accountAssignmentGroup: '',
  taxClassification: '',
  shipToParty: '',
  soldToParty: '',
  payerParty: '',
  // === 风险 ===
  riskLevel: null,
  competitorShareJson: '',
  cooperationBrandJson: '',
  bundleCustomerName: '',
  bundleBrand: '',
  bundleCustomerId: null,
  bundleCustomerSapCode: '',
  // === 银行/税务 ===
  taxId: '',
  bankName: '',
  bankAccount: '',
  invoiceTitle: '',
  // === 位置 ===
  locationLat: null,
  locationLng: null,
  // === 其他 ===
  salesName: '',
  assetType: '',
  remark: ''
})

const form = ref(defaultForm())
const addressRegion = ref([])
const contacts = ref([{ contactName: '', position: '', phone: '', telephone: '', email: '', isMain: 1 }])
const addressPickerVisible = ref(false)
const addressVerifying = ref(false)

const step1Rules = {
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择客户类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择客户状态', trigger: 'change' }],
  businessType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  category: [{ required: true, message: '请选择客户分类', trigger: 'change' }]
}

const regionOptions = provinceCityDistrict.map(province => ({
  value: province.p,
  label: province.p,
  children: province.c.map(city => ({
    value: city.n,
    label: city.n,
    children: city.d.map(district => ({ value: district, label: district }))
  }))
}))

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

const loadDetail = async () => {
  form.value = defaultForm()
  if (currentId.value) {
    const res = await getCustomerDetail(currentId.value)
    // API 返回 {code, message, data} 结构，取 data 下真正的客户对象
    const data = res.data || res
    form.value = {
      ...form.value,
      ...normalizeCustomerForForm(data)
    }
    // 地址
    if (data.province) addressRegion.value = [data.province, data.city, data.district].filter(Boolean)
    if (data.paymentDays != null) form.value.paymentDays = Number(data.paymentDays)
    if (data.machineCount != null) form.value.machineCount = Number(data.machineCount)
  }
}

watch(() => form.value.address, (address) => {
  if (!address || addressRegion.value.length) return
  const region = parseAddressRegion(address)
  if (region) {
    addressRegion.value = [region.province, region.city, region.district].filter(Boolean)
  }
})

const openAddressPicker = () => {
  addressPickerVisible.value = true
}

const handleAddressSelect = (data) => {
  if (!data) return
  form.value.address = data.address || form.value.address
  form.value.locationLng = data.lng ?? form.value.locationLng
  form.value.locationLat = data.lat ?? form.value.locationLat
  if (data.province || data.city || data.district) {
    addressRegion.value = [data.province, data.city, data.district].filter(Boolean)
    form.value.province = data.province || ''
    form.value.city = data.city || ''
    form.value.district = data.district || ''
  }
}

const buildSearchAddress = () => {
  return [addressRegion.value?.[0], addressRegion.value?.[1], addressRegion.value?.[2], form.value.address]
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
    const result = await geocodeAddress(address, addressRegion.value?.[1] || form.value.city || '')
    handleAddressSelect(result)
    ElMessage.success('定位校验成功')
  } catch (error) {
    ElMessage.error(error.message || '定位不到该地址，请填写更准确的地址')
  } finally {
    addressVerifying.value = false
  }
}

const nextStep = async () => {
  if (currentStep.value === 0) {
    await step1FormRef.value?.validate()
  }
  currentStep.value++
}

const prevStep = () => {
  currentStep.value--
}

const addContact = () => {
  contacts.value.push({ contactName: '', position: '', phone: '', telephone: '', email: '', isMain: 0 })
}

const removeContact = (index) => {
  contacts.value.splice(index, 1)
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    if (form.value.paymentDays != null) form.value.paymentDays = Number(form.value.paymentDays)
    if (form.value.machineCount != null) form.value.machineCount = Number(form.value.machineCount)

    // 处理地址
    if (addressRegion.value.length >= 3) {
      form.value.province = addressRegion.value[0]
      form.value.city = addressRegion.value[1]
      form.value.district = addressRegion.value[2]
    }

    let customerId = currentId.value
    const payload = buildCustomerUpdatePayload(form.value)
    if (isEdit.value) {
      await updateCustomer(customerId, payload)
    } else {
      const res = await createCustomer(payload)
      customerId = res?.id || currentId.value
    }

    // 创建联系人
    const validContacts = contacts.value.filter(c => c.contactName)
    for (const contact of validContacts) {
      if (contact.contactName) {
        await createContactV1(customerId, { ...contact, customerId })
      }
    }

    ElMessage.success('保存成功')
    emit('saved')
    emit('update:visible', false)
    if (!asDialog.value) router.push('/customer')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
  emit('update:visible', false)
  if (!asDialog.value) router.back()
}

watch(() => props.visible, (val) => { if (val) loadDetail() })
watch(() => props.customerId, () => { if (props.visible) loadDetail() })
onMounted(() => {
  loadOptions()
  if (!asDialog.value) loadDetail()
})
</script>

<style scoped>
.form-container {
  padding: 20px 0;
}
.form-steps {
  margin-bottom: 24px;
}
.step-content {
  min-height: 300px;
}
.step-panel {
  padding: 16px 0;
}
.step-footer {
  margin-top: 24px;
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.contact-item {
  margin-bottom: 16px;
}
.contact-actions {
  text-align: right;
  margin-top: 8px;
}
.add-more-contact {
  text-align: center;
  padding: 16px 0;
}
.address-input-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 8px;
  width: 100%;
}

@media (max-width: 768px) {
  .form-steps {
    font-size: 12px;
  }
  .address-input-row {
    grid-template-columns: 1fr;
  }
}
</style>
