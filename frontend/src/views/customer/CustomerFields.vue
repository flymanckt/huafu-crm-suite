<template>
  <el-form-item label="客户编码" prop="customerCode">
    <el-input v-model="form.customerCode" disabled placeholder="系统自动生成，无需填写" />
  </el-form-item>
  <el-form-item label="客户名称" prop="customerName">
    <el-input v-model="form.customerName" placeholder="请输入客户全称" />
  </el-form-item>
  <el-form-item label="客户简称"><el-input v-model="form.customerShortName" /></el-form-item>
  <el-form-item label="英文名称"><el-input v-model="form.englishName" placeholder="请输入英文名称" /></el-form-item>
  <el-form-item label="统一社会信用代码"><el-input v-model="form.unifiedSocialCreditCode" placeholder="请输入统一社会信用代码" /></el-form-item>
  <el-form-item label="客户类型" prop="type">
    <DictSelect v-model="form.type" dict-code="customer_type" value-type="number" placeholder="请选择" />
  </el-form-item>
  <el-form-item label="客户级别">
    <DictSelect v-model="form.level" dict-code="customer_level" value-type="number" placeholder="请选择" clearable />
  </el-form-item>
  <el-form-item label="客户状态">
    <DictSelect v-model="form.status" dict-code="customer_status" value-type="number" placeholder="请选择" />
  </el-form-item>
  <el-form-item label="客户阶段">
    <DictSelect v-model="form.customerStage" dict-code="customer_stage" value-type="number" placeholder="请选择" clearable />
  </el-form-item>
  <el-form-item label="业务类型">
    <DictSelect v-model="form.businessType" dict-code="biz_type" value-type="number" placeholder="请选择" clearable />
  </el-form-item>
  <el-form-item label="客户分类">
    <DictSelect v-model="form.category" dict-code="customer_category" value-type="number" placeholder="请选择" clearable />
  </el-form-item>
  <el-form-item label="客户来源">
    <DictSelect v-model="form.source" dict-code="customer_source" value-type="number" placeholder="请选择" clearable />
  </el-form-item>
  <el-form-item label="主要客户群体">
    <DictSelect v-model="form.customerGroup" dict-code="customer_group" value-type="number" placeholder="请选择" clearable />
  </el-form-item>
  <el-form-item label="客户细分组"><el-input v-model="form.customerSegment" placeholder="请输入客户细分组" /></el-form-item>
  <el-form-item label="国家区域"><el-input v-model="form.countryRegion" placeholder="请输入国家区域" /></el-form-item>
  <el-form-item label="主要品牌"><el-input v-model="form.mainBrand" placeholder="请输入主要品牌" /></el-form-item>
  <el-form-item label="年用纱量(吨)"><el-input-number v-model="form.annualYarnVolume" :precision="2" :min="0" /></el-form-item>
  <el-form-item label="机台数"><el-input-number v-model="form.machineCount" :min="0" /></el-form-item>
  <el-form-item label="产能情况"><el-input v-model="form.capacityInfo" placeholder="请输入产能情况" /></el-form-item>
  <el-form-item label="行业地位"><el-input v-model="form.industryPosition" placeholder="请输入行业地位" /></el-form-item>
  <el-form-item label="业务员"><el-input v-model="form.salesName" placeholder="请输入业务员姓名" /></el-form-item>
  <el-form-item label="资产类型">
    <DictSelect v-model="form.assetType" dict-code="asset_type" value-type="number" placeholder="请选择" clearable />
  </el-form-item>
  <el-form-item label="是否黑名单">
    <DictSelect v-model="form.blacklist" dict-code="yes_no" value-type="number" placeholder="请选择" clearable />
  </el-form-item>
  <el-form-item label="风险等级">
    <DictSelect v-model="form.riskLevel" dict-code="risk_level" value-type="number" placeholder="请选择" clearable />
  </el-form-item>
  <el-form-item label="省份"><el-input v-model="form.province" /></el-form-item>
  <el-form-item label="城市"><el-input v-model="form.city" /></el-form-item>
  <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
  <el-divider content-position="left">联系人信息</el-divider>
  <el-form-item label="主联系人"><el-input v-model="form.mainContactName" /></el-form-item>
  <el-form-item label="联系电话"><el-input v-model="form.mainContactPhone" /></el-form-item>
  <el-form-item label="联系人角色">
    <DictSelect v-model="form.mainContactRole" dict-code="contact_role" value-type="number" clearable />
  </el-form-item>
  <el-divider content-position="left">商务信息</el-divider>
  <el-form-item label="年营业额(万元)"><el-input-number v-model="form.annualRevenue" :precision="2" :min="0" /></el-form-item>
  <el-form-item label="信用额度(万元)"><el-input-number v-model="form.creditLimit" :precision="2" :min="0" /></el-form-item>
  <el-form-item label="账期(天)"><el-input-number v-model="form.paymentDays" :min="0" /></el-form-item>
  <el-form-item label="SAP客户编码"><el-input v-model="form.sapCustomerCode" /></el-form-item>
  <el-divider content-position="left">税务银行信息</el-divider>
  <el-form-item label="税务登记号"><el-input v-model="form.taxId" placeholder="请输入税务登记号" /></el-form-item>
  <el-form-item label="开户银行"><el-input v-model="form.bankName" placeholder="请输入开户银行" /></el-form-item>
  <el-form-item label="银行账号"><el-input v-model="form.bankAccount" placeholder="请输入银行账号" /></el-form-item>
  <el-form-item label="发票抬头"><el-input v-model="form.invoiceTitle" placeholder="请输入发票抬头" /></el-form-item>
  <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
</template>

<script setup>
import DictSelect from '@/components/Dict/DictSelect.vue'

defineProps({
  form: { type: Object, required: true },
  isEdit: { type: Boolean, default: false }
})
</script>
