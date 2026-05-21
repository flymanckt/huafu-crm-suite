<template>
  <div>
    <div class="tab-toolbar">
      <el-button type="primary" @click="openEdit" v-if="!editMode">编辑基础资料</el-button>
    </div>

    <!-- P0-2: 阅读模式重构：基础信息卡片头部摘要 + 可折叠卡片组 -->
    <div class="read-mode" v-loading="loading" v-if="!editMode">
      <!-- 基础信息摘要卡片（始终展示） -->
      <el-card shadow="never" body-style="padding: 0" class="summary-card">
        <div class="summary-header">
          <div class="summary-title">
            <span class="customer-name">{{ detail.customerName || '-' }}</span>
            <el-tag size="small" :type="customerStatusType[detail.status]" style="margin-left:8px">{{ customerLabel.status(detail.status) }}</el-tag>
          </div>
          <div class="summary-meta">
            <span class="meta-item">编码：{{ detail.customerCode || '-' }}</span>
            <span class="meta-divider">|</span>
            <span class="meta-item">{{ customerLabel.type(detail.type) }}</span>
            <span class="meta-divider">|</span>
            <span class="meta-item">{{ customerLabel.level(detail.level) }}</span>
            <span class="meta-divider" v-if="detail.customerShortName">|</span>
            <span class="meta-item" v-if="detail.customerShortName">简称：{{ detail.customerShortName }}</span>
          </div>
        </div>
      </el-card>

      <!-- 业务信息可折叠卡片 -->
      <el-card shadow="never" class="info-card" style="margin-top:12px">
        <template #header>
          <div class="card-header" @click="collapse.business = !collapse.business">
            <span class="card-title">业务信息</span>
            <el-icon class="collapse-icon" :class="{ 'is-collapsed': collapse.business }"><ArrowUp /></el-icon>
          </div>
        </template>
        <div v-show="!collapse.business" class="card-body">
          <el-row :gutter="16">
            <el-col :span="12"><div class="field-item"><label>主要合作品牌</label><value>{{ detail.mainBrand || '-' }}</value></div></el-col>
            <el-col :span="12"><div class="field-item"><label>年纱线用量(吨)</label><value>{{ detail.annualYarnVolume ?? '-' }}</value></div></el-col>
            <el-col :span="12"><div class="field-item"><label>机台数</label><value>{{ detail.machineCount ?? '-' }}</value></div></el-col>
            <el-col :span="12"><div class="field-item"><label>产能情况</label><value>{{ detail.productionCapacity || '-' }}</value></div></el-col>
            <el-col :span="12"><div class="field-item"><label>行业地位</label><value>{{ detail.industryPosition || '-' }}</value></div></el-col>
            <el-col :span="12"><div class="field-item"><label>主要客户群体</label><value>{{ detail.mainCustomerGroup || '-' }}</value></div></el-col>
            <el-col :span="12"><div class="field-item"><label>国家区域</label><value>{{ detail.countryRegion || '-' }}</value></div></el-col>
            <el-col :span="12"><div class="field-item"><label>主要竞争对手</label><value>{{ detail.competitorShareJson || '-' }}</value></div></el-col>
          </el-row>
        </div>
      </el-card>

      <!-- 归属信息可折叠卡片 -->
      <el-card shadow="never" class="info-card" style="margin-top:12px">
        <template #header>
          <div class="card-header" @click="collapse.ownership = !collapse.ownership">
            <span class="card-title">归属信息</span>
            <el-icon class="collapse-icon" :class="{ 'is-collapsed': collapse.ownership }"><ArrowUp /></el-icon>
          </div>
        </template>
        <div v-show="!collapse.ownership" class="card-body">
          <el-row :gutter="16">
            <el-col :span="8"><div class="field-item"><label>负责人</label><value>{{ resolveOwnerUser(detail.ownerUserId) }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>销售跟单</label><value>{{ detail.salesMerchandiser || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>归属部门</label><value>{{ resolveOwnerDept(detail.ownerDeptId) }}</value></div></el-col>
            <el-col :span="12"><div class="field-item"><label>主联系人</label><value>{{ detail.mainContactName || '-' }}</value></div></el-col>
            <el-col :span="12"><div class="field-item"><label>主联系人电话</label><value>{{ detail.mainContactPhone || '-' }}</value></div></el-col>
            <el-col :span="12"><div class="field-item"><label>省市区</label><value>{{ [detail.province, detail.city, detail.district].filter(Boolean).join('') || '-' }}</value></div></el-col>
            <el-col :span="12"><div class="field-item"><label>详细地址</label><value>{{ detail.address || '-' }}</value></div></el-col>
          </el-row>
        </div>
      </el-card>

      <!-- 财务信息可折叠卡片 -->
      <el-card shadow="never" class="info-card" style="margin-top:12px">
        <template #header>
          <div class="card-header" @click="collapse.finance = !collapse.finance">
            <span class="card-title">财务信息</span>
            <el-icon class="collapse-icon" :class="{ 'is-collapsed': collapse.finance }"><ArrowUp /></el-icon>
          </div>
        </template>
        <div v-show="!collapse.finance" class="card-body">
          <el-row :gutter="16">
            <el-col :span="8"><div class="field-item"><label>开户行</label><value>{{ detail.bankName || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>账号</label><value>{{ detail.bankAccount || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>开票抬头</label><value>{{ detail.invoiceTitle || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>税号</label><value>{{ detail.taxId || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>账期(天)</label><value>{{ detail.paymentDays ?? '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>信用额度(万元)</label><value>{{ detail.creditLimit ?? '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>年营业额(万元)</label><value>{{ detail.annualRevenue ?? '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>风险等级</label><value>{{ riskLevelMap[detail.riskLevel] || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>是否黑名单</label><value>{{ detail.blacklist === 1 ? '是' : '否' }}</value></div></el-col>
          </el-row>
        </div>
      </el-card>

      <!-- SAP信息可折叠卡片 -->
      <el-card shadow="never" class="info-card" style="margin-top:12px">
        <template #header>
          <div class="card-header" @click="collapse.sap = !collapse.sap">
            <span class="card-title">SAP信息</span>
            <el-icon class="collapse-icon" :class="{ 'is-collapsed': collapse.sap }"><ArrowUp /></el-icon>
          </div>
        </template>
        <div v-show="!collapse.sap" class="card-body">
          <el-row :gutter="16">
            <el-col :span="8"><div class="field-item"><label>SAP客户编码</label><value>{{ detail.sapCustomerCode || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>公司代码</label><value>{{ detail.companyCode || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>销售组</label><value>{{ detail.salesGroup || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>价格清单</label><value>{{ detail.priceList || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>货币</label><value>{{ detail.currency || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>交货工厂</label><value>{{ detail.deliveryFactory || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>送达方</label><value>{{ detail.shipToParty || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>售达方</label><value>{{ detail.soldToParty || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>付款方</label><value>{{ detail.payerParty || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>统一社会信用代码</label><value>{{ detail.unifiedSocialCreditCode || '-' }}</value></div></el-col>
            <el-col :span="8"><div class="field-item"><label>资产类型</label><value>{{ detail.assetType || '-' }}</value></div></el-col>
          </el-row>
        </div>
      </el-card>

      <!-- 备注 -->
      <div v-if="detail.remark" class="remark-block">
        <div class="remark-label">备注</div>
        <div class="remark-content">{{ detail.remark }}</div>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editMode" title="编辑客户基础资料" width="900" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="130px" style="max-height:60vh;overflow-y:auto">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="客户名称"><el-input v-model="editForm.customerName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户简称"><el-input v-model="editForm.customerShortName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="英文全称"><el-input v-model="editForm.englishName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户分类"><el-input v-model="editForm.customerCategory" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户品类"><el-input v-model="editForm.customerSegment" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户来源"><el-input v-model="editForm.customerSource" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="国家区域"><el-input v-model="editForm.countryRegion" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="主要合作品牌"><el-input v-model="editForm.mainBrand" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="产能情况"><el-input v-model="editForm.productionCapacity" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="行业地位"><el-input v-model="editForm.industryPosition" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="主要客户群体"><el-input v-model="editForm.mainCustomerGroup" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="主要竞争对手"><el-input v-model="editForm.competitorShare" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="主联系人"><el-input v-model="editForm.mainContactName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="主联系人电话"><el-input v-model="editForm.mainContactPhone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="省"><el-input v-model="editForm.province" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="市"><el-input v-model="editForm.city" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="区"><el-input v-model="editForm.district" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="捆绑客户"><el-input v-model="editForm.bundleCustomerName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="捆绑品牌"><el-input v-model="editForm.bundleBrand" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="详细地址"><el-input v-model="editForm.address" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="开户行"><el-input v-model="editForm.bankName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="账号"><el-input v-model="editForm.bankAccount" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="开票抬头"><el-input v-model="editForm.invoiceTitle" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="税号"><el-input v-model="editForm.taxId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="统一社会信用代码"><el-input v-model="editForm.unifiedSocialCreditCode" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input type="textarea" v-model="editForm.remark" :rows="3" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editMode = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ArrowUp } from '@element-plus/icons-vue'
import { updateCustomer } from '@/api/customer'
import { ElMessage } from 'element-plus'
import { buildCustomerUpdatePayload, customerLabel, customerStatusType } from '@/utils/customerFields'

const props = defineProps({ customerId: Number, detail: Object })
const emit = defineEmits(['updated'])
const loading = ref(false)
const editMode = ref(false)
const saving = ref(false)
const editForm = ref({})

// 折叠状态：默认全部展开（collapse.x = false 表示展开）
const collapse = reactive({ business: false, ownership: false, finance: false, sap: false })

// P0-3: ID字段映射（前端兜底）
// 后端若未返回 displayName/deptName，前端用中文占位
const resolveOwnerUser = (id) => {
  if (!id) return '-'
  // 优先用 detail 里可能存在的 displayName 字段
  if (props.detail.displayName) return props.detail.displayName
  return '管理员'
}

const resolveOwnerDept = (id) => {
  if (!id) return '-'
  if (props.detail.deptName) return props.detail.deptName
  return '华孚时尚'
}

const openEdit = () => {
  editForm.value = {
    customerName: props.detail.customerName || '',
    customerShortName: props.detail.customerShortName || '',
    englishName: props.detail.englishName || '',
    customerCategory: props.detail.customerCategory || '',
    customerSegment: props.detail.customerSegment || '',
    customerSource: props.detail.customerSource || '',
    countryRegion: props.detail.countryRegion || '',
    mainBrand: props.detail.mainBrand || '',
    productionCapacity: props.detail.productionCapacity || '',
    industryPosition: props.detail.industryPosition || '',
    mainCustomerGroup: props.detail.mainCustomerGroup || '',
    competitorShare: props.detail.competitorShareJson || props.detail.competitorShare || '',
    mainContactName: props.detail.mainContactName || '',
    mainContactPhone: props.detail.mainContactPhone || '',
    province: props.detail.province || '',
    city: props.detail.city || '',
    district: props.detail.district || '',
    bundleCustomerName: props.detail.bundleCustomerName || '',
    bundleBrand: props.detail.bundleBrand || '',
    address: props.detail.address || '',
    bankName: props.detail.bankName || '',
    bankAccount: props.detail.bankAccount || '',
    invoiceTitle: props.detail.invoiceTitle || '',
    taxId: props.detail.taxId || '',
    unifiedSocialCreditCode: props.detail.unifiedSocialCreditCode || '',
    remark: props.detail.remark || '',
  }
  editMode.value = true
}

const handleSave = async () => {
  if (!editForm.value.customerName || !editForm.value.customerName.trim()) {
    ElMessage.warning('请填写客户名称')
    return
  }
  saving.value = true
  try {
    await updateCustomer(props.customerId, buildCustomerUpdatePayload(editForm.value))
    ElMessage.success('保存成功')
    editMode.value = false
    emit('updated')
  } catch (e) {
    console.error('保存失败', e)
  } finally {
    saving.value = false
  }
}

const riskLevelMap = { 1: '低', 2: '中', 3: '高' }
</script>

<style scoped>
.tab-toolbar { text-align: right; margin-bottom: 12px; }

/* 阅读模式 */
.read-mode {}

/* 摘要卡片 */
.summary-card {
  border-left: 3px solid var(--el-color-primary);
}
.summary-header {
  padding: 16px 20px;
}
.summary-title {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.customer-name {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
}
.summary-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
}
.meta-divider { color: #ddd; }

/* 可折叠信息卡片 */
.info-card {}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  user-select: none;
}
.card-title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}
.collapse-icon {
  transition: transform 0.25s ease;
  color: #909399;
}
.collapse-icon.is-collapsed {
  transform: rotate(180deg);
}
.card-body {
  padding-top: 4px;
}

/* 字段行：label 80px + 右对齐，value 左对齐 */
.field-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
}
.field-item label {
  width: 80px;
  flex-shrink: 0;
  text-align: right;
  padding-right: 12px;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}
.field-item value {
  flex: 1;
  font-size: 13px;
  color: #303133;
  line-height: 1.5;
  word-break: break-all;
}

/* 备注区 */
.remark-block {
  margin-top: 12px;
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 4px;
}
.remark-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}
.remark-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
</style>
