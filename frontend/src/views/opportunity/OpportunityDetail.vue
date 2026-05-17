<template>
  <div class="page-container">
    <el-page-header @back="$router.back()" :content="detail.opportunityName || '商机详情'" style="margin-bottom:16px" />
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>商机信息</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="商机编号">{{ detail.oppNo }}</el-descriptions-item>
            <el-descriptions-item label="商机名称">{{ detail.opportunityName }}</el-descriptions-item>
            <el-descriptions-item label="客户">
              <router-link v-if="detail.customerId" :to="'/customer/'+detail.customerId" class="link">{{ detail.customerName }}</router-link>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="阶段">
              <el-tag :type="stageColor[detail.stage]">{{ stageLabel[detail.stage] }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="意向产品">{{ detail.productName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="数量">{{ detail.quantity ?? '-' }} {{ detail.unit || '' }}</el-descriptions-item>
            <el-descriptions-item label="预估金额(万元)">{{ detail.estimatedAmount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="赢单概率">{{ detail.winProbability ?? '-' }}%</el-descriptions-item>
            <el-descriptions-item label="预计成交日">{{ detail.expectedCloseDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="竞品">{{ detail.competitorName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="SAP订单号">{{ detail.orderNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>AI解析原文</template>
          <div v-if="detail.rawText" class="raw-text">{{ detail.rawText }}</div>
          <el-empty v-else description="无AI解析原文" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOpportunityDetail } from '@/api/opportunity'

const route = useRoute()
const detail = ref({})
const stageLabel = { 1:'初步接触', 2:'需求确认', 3:'方案报价', 4:'合同谈判', 5:'成交' }
const stageColor = { 1:'info', 2:'', 3:'warning', 4:'danger', 5:'success' }

onMounted(async () => { detail.value = await getOpportunityDetail(route.params.id) })
</script>

<style scoped>.link{color:#409eff;text-decoration:none}.raw-text{font-size:13px;color:#606266;white-space:pre-wrap;line-height:1.6;background:#f5f7fa;padding:12px;border-radius:4px;max-height:400px;overflow-y:auto}</style>
