<template>
  <div class="page-container">
    <!-- 顶部返回+标题 -->
    <el-page-header @back="$router.back()" :content="detail.customerName || '客户详情'" style="margin-bottom:16px" />

    <!-- 顶部摘要卡 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="24">
        <el-card shadow="never" class="customer-summary">
          <div class="summary-card">
            <div class="summary-left">
              <div class="customer-name">{{ detail.customerName }}</div>
              <div class="customer-tags">
                <DictTag v-if="hasValue(detail.type)" dict-code="customer_type" :value="String(detail.type)" size="small" />
                <DictTag v-if="hasValue(detail.level)" dict-code="customer_level" :value="String(detail.level)" size="small" />
                <DictTag v-if="hasValue(detail.status)" dict-code="customer_status" :value="String(detail.status)" size="small" />
              </div>
            </div>
            <div class="summary-right">
              <div class="layout-control">
                <span>布局</span>
                <el-radio-group v-model="detailColumns" size="small">
                  <el-radio-button :value="2">2列</el-radio-button>
                  <el-radio-button :value="3">3列</el-radio-button>
                  <el-radio-button :value="4">4列</el-radio-button>
                </el-radio-group>
              </div>
              <el-button @click="handleEdit">编辑基础资料</el-button>
              <el-button type="primary" @click="activeTab = '4'">联系人({{ contacts.length }})</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 移动端：Tab切换 -->
    <div class="mobile-menu-btn">
      <el-button @click="drawerVisible = true" type="primary" plain>
        <el-icon><Menu /></el-icon> 目录
      </el-button>
    </div>

    <!-- 主内容区 -->
    <el-tabs v-model="activeTab" tab-position="left" class="detail-left-tabs el-tabs--left" @tab-change="handleTabChange">
      <!-- Tab1: 总览 -->
      <el-tab-pane name="1">
        <template #label><span class="tab-label"><el-icon><DataAnalysis /></el-icon>总览</span></template>
        <OverviewTab :customer-id="detailId" :detail="detail" @updated="loadData" />
      </el-tab-pane>
      <!-- Tab2: 基础信息 -->
      <el-tab-pane name="2">
        <template #label><span class="tab-label"><el-icon><Document /></el-icon>基础信息</span></template>
        <BasicInfoTab :customer-id="detailId" :detail="detail" @updated="loadData" />
      </el-tab-pane>
      <!-- Tab9: SAP信息 -->
      <el-tab-pane name="9">
        <template #label><span class="tab-label"><el-icon><Grid /></el-icon>SAP信息</span></template>
        <SapInfoTab :customer-id="detailId" />
      </el-tab-pane>
      <!-- Tab3: SAP组织 -->
      <el-tab-pane name="3">
        <template #label><span class="tab-label"><el-icon><Grid /></el-icon>SAP组织</span></template>
        <SapOrgTab :customer-id="detailId" />
      </el-tab-pane>
      <!-- Tab4: 联系人 -->
      <el-tab-pane name="4">
        <template #label><span class="tab-label"><el-icon><User /></el-icon>联系人</span></template>
        <ContactTab :customer-id="detailId" :contacts="contacts" @updated="loadContacts" />
      </el-tab-pane>
      <!-- Tab5: 组织架构图 -->
      <el-tab-pane name="5">
        <template #label><span class="tab-label"><el-icon><Connection /></el-icon>组织架构</span></template>
        <OrgChartTab :customer-id="detailId" :contacts="contacts" />
      </el-tab-pane>
      <!-- Tab6: 关联企业 -->
      <el-tab-pane name="6">
        <template #label><span class="tab-label"><el-icon><OfficeBuilding /></el-icon>关联企业</span></template>
        <RelatedEnterpriseTab :customer-id="detailId" />
      </el-tab-pane>
      <!-- Tab7: 附件 -->
      <el-tab-pane name="7">
        <template #label><span class="tab-label"><el-icon><Paperclip /></el-icon>附件</span></template>
        <AttachmentTab :customer-id="detailId" />
      </el-tab-pane>
      <!-- Tab8: 地址 -->
      <el-tab-pane name="8">
        <template #label><span class="tab-label"><el-icon><Location /></el-icon>地址</span></template>
        <CustomerAddressTab :customer-id="detailId" />
      </el-tab-pane>
    </el-tabs>

    <!-- 移动端：el-drawer 侧边菜单 -->
    <el-drawer v-model="drawerVisible" title="客户详情" direction="ltr" size="260px" class="detail-drawer">
      <el-tabs v-model="activeTab" tab-position="left" class="drawer-tabs el-tabs--left" @tab-change="(key) => { activeTab = key; drawerVisible = false }">
        <el-tab-pane name="1"><template #label><span class="tab-label"><el-icon><DataAnalysis /></el-icon>总览</span></template></el-tab-pane>
        <el-tab-pane name="2"><template #label><span class="tab-label"><el-icon><Document /></el-icon>基础信息</span></template></el-tab-pane>
        <el-tab-pane name="9"><template #label><span class="tab-label"><el-icon><Grid /></el-icon>SAP信息</span></template></el-tab-pane>
        <el-tab-pane name="3"><template #label><span class="tab-label"><el-icon><Grid /></el-icon>SAP组织</span></template></el-tab-pane>
        <el-tab-pane name="4"><template #label><span class="tab-label"><el-icon><User /></el-icon>联系人</span></template></el-tab-pane>
        <el-tab-pane name="5"><template #label><span class="tab-label"><el-icon><Connection /></el-icon>组织架构</span></template></el-tab-pane>
        <el-tab-pane name="6"><template #label><span class="tab-label"><el-icon><OfficeBuilding /></el-icon>关联企业</span></template></el-tab-pane>
        <el-tab-pane name="7"><template #label><span class="tab-label"><el-icon><Paperclip /></el-icon>附件</span></template></el-tab-pane>
        <el-tab-pane name="8"><template #label><span class="tab-label"><el-icon><Location /></el-icon>地址</span></template></el-tab-pane>
      </el-tabs>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, provide, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCustomerDetail, getContactListV1 } from '@/api/customer'
import { DataAnalysis, Document, Grid, User, Connection, OfficeBuilding, Paperclip, Menu, Location } from '@element-plus/icons-vue'
import DictTag from '@/components/Dict/DictTag.vue'
import OverviewTab from '@/components/customer/tabs/OverviewTab.vue'
import BasicInfoTab from '@/components/customer/tabs/BasicInfoTab.vue'
import SapInfoTab from '@/components/customer/tabs/SapInfoTab.vue'
import SapOrgTab from '@/components/customer/tabs/SapOrgTab.vue'
import ContactTab from '@/components/customer/tabs/ContactTab.vue'
import OrgChartTab from '@/components/customer/tabs/OrgChartTab.vue'
import RelatedEnterpriseTab from '@/components/customer/tabs/RelatedEnterpriseTab.vue'
import AttachmentTab from '@/components/customer/tabs/AttachmentTab.vue'
import CustomerAddressTab from '@/components/customer/CustomerAddressTab.vue'

const route = useRoute()
const router = useRouter()
const detailId = String(route.params.id)
const detail = ref({})
const contacts = ref([])
const activeTab = ref('1')
const drawerVisible = ref(false)
const detailColumns = ref(Number(localStorage.getItem('customerDetailColumns') || 3))
const hasValue = (value) => value !== null && value !== undefined && value !== ''

const loadData = async () => {
  detail.value = await getCustomerDetail(detailId)
}

const loadContacts = async () => {
  contacts.value = await getContactListV1(detailId)
}

const handleTabChange = (key) => {
  activeTab.value = key
}

const handleEdit = () => {
  router.push('/customer/edit/' + detailId)
}

// Provide detail data to child tabs
provide('customerDetail', detail)
provide('reloadDetail', loadData)
provide('customerDetailColumns', detailColumns)

watch(detailColumns, (value) => {
  const normalized = [2, 3, 4].includes(Number(value)) ? Number(value) : 3
  detailColumns.value = normalized
  localStorage.setItem('customerDetailColumns', String(normalized))
}, { immediate: true })

onMounted(() => { loadData(); loadContacts() })
</script>

<style scoped>
.page-container {
  padding: 16px;
  max-width: 1400px;
  margin: 0 auto;
}
.customer-summary {
  border-radius: 8px;
}
.summary-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
}
.summary-left { flex: 1; }
.customer-name {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 8px;
}
.customer-tags { display: flex; gap: 6px; margin-bottom: 8px; }
.summary-right { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.layout-control {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 6px;
  color: #606266;
  font-size: 13px;
}

/* 主体区 - 顶部Tab样式 */
.detail-top-tabs {
  background: #fff;
  border-radius: 4px;
  padding: 0;
}
.detail-left-tabs {
  display: flex;
  gap: 0;
  min-height: 600px;
}
/* 强制 el-tabs 为水平布局，覆盖 el-tabs--top 默认的 column */
.detail-left-tabs :deep(.el-tabs) {
  flex-direction: row;
}
.detail-left-tabs :deep(.el-tabs__header) {
  width: 160px;
  margin-right: 0;
  background: #fafafa;
  border-radius: 4px 0 0 4px;
  border-right: 1px solid #e8e8e8;
}
.detail-left-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}
.detail-left-tabs :deep(.el-tabs__content) {
  flex: 1;
  background: #fff;
  border-radius: 0 4px 4px 0;
  padding: 12px 14px;
  min-height: 500px;
  overflow: auto;
}
.detail-left-tabs :deep(.el-tabs__item) {
  height: 50px;
  line-height: 50px;
  padding: 0 20px;
  width: 100%;
}
.detail-left-tabs :deep(.el-tabs__item.is-active) {
  color: var(--el-color-primary);
  font-weight: 600;
  background: #fff;
  border-right: 2px solid var(--el-color-primary);
}
.detail-left-tabs :deep(.el-tabs__nav) {
  height: 100%;
}
.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

/* 移动端 */
.mobile-menu-btn {
  display: none;
  margin-bottom: 12px;
}
@media (max-width: 768px) {
  .mobile-menu-btn { display: block; }
  .detail-content { padding: 12px; }
  .detail-left-tabs { display: none; }
  .summary-card { flex-direction: column; }
  .summary-right { width: 100%; flex-wrap: wrap; }
  .summary-right .el-button { flex: 1; }
  .layout-control { width: 100%; margin-right: 0; justify-content: space-between; }
}

/* Drawer tabs */
.drawer-tabs :deep(.el-tabs__header) {
  height: 100%;
}
.drawer-tabs :deep(.el-tabs__nav-wrap) {
  height: 100%;
}
.drawer-tabs :deep(.el-tabs__item) {
  height: 50px;
  line-height: 50px;
}
</style>
