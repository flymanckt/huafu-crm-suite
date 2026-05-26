<template>
  <div class="overview-tab">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ stats.contactCount }}</div>
          <div class="stat-label">联系人数量</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ detail.machineCount || 0 }}</div>
          <div class="stat-label">机台数</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ detail.yarnUsage || 0 }}</div>
          <div class="stat-label">纱线用量(吨)</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ formatAmount(detail.annualRevenue) }}</div>
          <div class="stat-label">年度销售额(万元)</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表+概述 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :sm="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>销量/毛利趋势</span>
              <el-radio-group v-model="chartPeriod" size="small" @change="loadChartData">
                <el-radio-button value="month">月度</el-radio-button>
                <el-radio-button value="quarter">季度</el-radio-button>
                <el-radio-button value="year">年度</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="never" class="overview-card">
          <template #header>
            <div class="card-header">
              <span>客户整体概述</span>
              <div class="overview-actions">
                <el-button v-if="!overviewEditing" link type="primary" @click="startOverviewEdit">编辑</el-button>
                <template v-else>
                  <el-button link @click="useGeneratedSummary">使用智能草稿</el-button>
                  <el-button link @click="cancelOverviewEdit">取消</el-button>
                  <el-button link type="primary" :loading="overviewSaving" @click="saveOverview">保存</el-button>
                </template>
              </div>
            </div>
          </template>
          <div class="overview-text">
            <el-input
              v-if="overviewEditing"
              v-model="overviewForm.overviewSummary"
              type="textarea"
              :rows="8"
              maxlength="1000"
              show-word-limit
            />
            <p v-else>{{ displaySummary }}</p>
          </div>
          <el-divider />
          <div class="overview-info">
            <div class="info-item">
              <span class="info-label">战略定位：</span>
              <el-input v-if="overviewEditing" v-model="overviewForm.strategyPosition" size="small" maxlength="120" />
              <el-tag v-else size="small">{{ overview.strategyPosition || detail.strategicLevel || '未设置' }}</el-tag>
            </div>
	            <div class="info-item">
	              <span class="info-label">客户来源：</span>
	              <DictTag v-if="hasValue(detail.customerSource ?? detail.source)" dict-code="customer_source" :value="String(detail.customerSource ?? detail.source)" size="small" />
	              <span v-else>-</span>
	            </div>
            <div class="info-item">
              <span class="info-label">最近跟进：</span>
              <span>{{ detail.lastFollowUpDate || '未跟进' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="profile-fields-card">
      <template #header>
        <div class="card-header">
          <span>客户画像要点</span>
        </div>
      </template>
      <el-form v-if="overviewEditing" :model="overviewForm" label-width="150px" class="profile-edit-form">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="行业地位"><el-input v-model="overviewForm.industryPosition" /></el-form-item></el-col>
	          <el-col :span="12"><el-form-item label="主要客户群体"><DictSelect v-model="overviewForm.mainCustomerGroup" dict-code="customer_group" clearable /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="主要合作品牌"><el-input v-model="overviewForm.cooperationBrandJson" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="竞争对手及占比"><el-input v-model="overviewForm.competitorShareJson" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="其他资产/信息"><el-input v-model="overviewForm.otherInfo" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <el-descriptions v-else :column="2" border>
        <el-descriptions-item label="行业地位">{{ detail.industryPosition || '-' }}</el-descriptions-item>
	        <el-descriptions-item label="主要客户群体"><DictTag v-if="hasValue(detail.mainCustomerGroup || detail.customerGroup)" dict-code="customer_group" :value="String(detail.mainCustomerGroup || detail.customerGroup)" size="small" /><span v-else>-</span></el-descriptions-item>
        <el-descriptions-item label="主要合作品牌">{{ formatJsonText(detail.cooperationBrandJson) || detail.mainBrand || '-' }}</el-descriptions-item>
        <el-descriptions-item label="主要合作竞争对手及占比情况">{{ formatJsonText(detail.competitorShareJson) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="其他资产或其他信息">{{ [detail.assetType, detail.remark].filter(Boolean).join('；') || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 纱线用量表格 -->
    <el-card shadow="never" style="margin-top:16px">
      <template #header>
        <div class="card-header">
          <span>纱线用量明细</span>
        </div>
      </template>
      <el-table :data="yarnTableData" stripe size="small">
        <el-table-column prop="month" label="月份" width="100" />
        <el-table-column prop="yarnType" label="纱线品种" min-width="120" />
        <el-table-column prop="quantity" label="用量(吨)" width="100" align="right" />
        <el-table-column prop="unitPrice" label="单价(元)" width="100" align="right" />
        <el-table-column prop="amount" label="金额(万元)" width="100" align="right" />
        <el-table-column prop="grossProfit" label="毛利(万元)" width="100" align="right" />
        <el-table-column prop="grossMargin" label="毛利率" width="80" align="center" />
      </el-table>
      <el-empty v-if="!yarnTableData.length" description="暂无用量数据" :image-size="60" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getContactListV1, getCustomerOverviewV1, updateCustomer, updateCustomerOverviewV1 } from '@/api/customer'
import { buildCustomerUpdatePayload } from '@/utils/customerFields'
import DictSelect from '@/components/Dict/DictSelect.vue'
import DictTag from '@/components/Dict/DictTag.vue'
import { useDict } from '@/composables/useDict'

const props = defineProps({
  customerId: { type: [String, Number], required: true },
  detail: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['updated'])

const chartRef = ref(null)
const chartPeriod = ref('month')
const chartInstance = ref(null)
const yarnTableData = ref([])
const stats = ref({ contactCount: 0 })
const overview = ref({})
const overviewForm = ref({
  overviewSummary: '',
  strategyPosition: '',
  industryPosition: '',
  mainCustomerGroup: null,
  cooperationBrandJson: '',
  competitorShareJson: '',
  otherInfo: ''
})
const overviewEditing = ref(false)
const overviewSaving = ref(false)
const { loadDictItems, getDictLabel } = useDict()
const hasValue = (value) => value !== null && value !== undefined && value !== ''

const formatAmount = (val) => {
  if (!val && val !== 0) return '-'
  return val.toLocaleString()
}

const fieldText = (label, value) => value || value === 0 ? `${label}${value}` : ''

const formatJsonText = (value) => {
  if (!value) return ''
  if (typeof value !== 'string') return String(value)
  try {
    const parsed = JSON.parse(value)
    if (Array.isArray(parsed)) {
      return parsed.map(item => {
        if (typeof item === 'string') return item
        return [item.name, item.brand, item.customer, item.share, item.ratio].filter(Boolean).join(' ')
      }).filter(Boolean).join('；')
    }
    if (typeof parsed === 'object' && parsed !== null) {
      return Object.entries(parsed).map(([key, val]) => `${key}:${val}`).join('；')
    }
  } catch {
    return value
  }
  return value
}

const generatedSummary = computed(() => {
  const d = props.detail || {}
  const area = [d.country, d.province, d.city, d.district].filter(Boolean).join('')
	  const fragments = [
	    `${d.customerName || '该客户'}当前为${dictLabel('customer_type', d.type)}，客户状态为${dictLabel('customer_status', d.status)}，等级为${dictLabel('customer_level', d.level)}`,
	    area ? `客户位于${area}` : '',
	    d.industryPosition ? `行业定位为${d.industryPosition}` : '',
	    d.mainCustomerGroup ? `主要客户群体为${dictLabel('customer_group', d.mainCustomerGroup)}` : '',
    d.mainBrand ? `主要合作品牌为${d.mainBrand}` : '',
    d.cooperationBrandJson ? `合作品牌明细为${formatJsonText(d.cooperationBrandJson)}` : '',
    d.competitorShareJson ? `主要竞争对手及占比为${formatJsonText(d.competitorShareJson)}` : '',
    d.assetType || d.remark ? `其他资产或信息为${[d.assetType, d.remark].filter(Boolean).join('、')}` : '',
	    d.businessType ? `业务类型为${dictLabel('biz_type', d.businessType)}` : '',
	    (d.customerCategory || d.category) ? `客户分类为${dictLabel('customer_category', d.customerCategory || d.category)}` : '',
    fieldText('机台数', d.machineCount),
    fieldText('年度销售额', d.annualRevenue),
    fieldText('年纱线用量', d.annualYarnVolume || d.yarnUsage)
  ].filter(Boolean)

  if (fragments.length <= 1) {
    return '该客户的关键经营、地址、联系人、SAP组织及跟进信息仍需补充，建议先完善基础资料后形成更完整的客户画像。'
  }
  return `${fragments.join('，')}。后续可结合联系人、地址、SAP组织、交易与跟进记录持续完善客户画像和经营策略。`
})

const displaySummary = computed(() => overview.value.overviewSummary || generatedSummary.value)

const loadOverview = async () => {
  if (!props.customerId) return
  overview.value = await getCustomerOverviewV1(props.customerId) || {}
}

const startOverviewEdit = () => {
  overviewForm.value = {
    overviewSummary: displaySummary.value,
    strategyPosition: overview.value.strategyPosition || props.detail.strategicLevel || '',
    industryPosition: props.detail.industryPosition || '',
    mainCustomerGroup: props.detail.mainCustomerGroup || props.detail.customerGroup || null,
    cooperationBrandJson: props.detail.cooperationBrandJson || props.detail.mainBrand || '',
    competitorShareJson: props.detail.competitorShareJson || '',
    otherInfo: props.detail.remark || ''
  }
  overviewEditing.value = true
}

const useGeneratedSummary = () => {
  overviewForm.value.overviewSummary = generatedSummary.value
}

const cancelOverviewEdit = () => {
  overviewEditing.value = false
}

const dictLabel = (code, value) => hasValue(value) ? getDictLabel(code, String(value)) : '-'

const saveOverview = async () => {
  overviewSaving.value = true
  try {
    const { overviewSummary, strategyPosition, industryPosition, mainCustomerGroup, cooperationBrandJson, competitorShareJson, otherInfo } = overviewForm.value
    overview.value = await updateCustomerOverviewV1(props.customerId, { overviewSummary, strategyPosition })
    await updateCustomer(props.customerId, buildCustomerUpdatePayload({
      industryPosition,
      mainCustomerGroup,
      cooperationBrandJson,
      competitorShareJson,
      remark: otherInfo
    }))
    overviewEditing.value = false
    emit('updated')
    ElMessage.success('概述已保存')
  } finally {
    overviewSaving.value = false
  }
}

const loadChartData = () => {
  // 模拟图表数据，实际应从API获取
  const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
  const salesData = [120, 132, 101, 134, 190, 230, 210, 182, 201, 154, 180, 210]
  const profitData = [20, 32, 21, 34, 50, 70, 60, 52, 61, 54, 40, 50]

  if (!chartRef.value) return

  if (!chartInstance.value) {
    chartInstance.value = echarts.init(chartRef.value)
  }

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['销量', '毛利'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: months, boundaryGap: false },
    yAxis: [
      { type: 'value', name: '销量(万元)', axisLabel: { formatter: '{value}' } },
      { type: 'value', name: '毛利(万元)', axisLabel: { formatter: '{value}' } }
    ],
    series: [
      { name: '销量', type: 'bar', data: salesData, itemStyle: { color: '#5470c6' } },
      { name: '毛利', type: 'line', yAxisIndex: 1, data: profitData, itemStyle: { color: '#91cd77' }, smooth: true }
    ]
  }

  chartInstance.value.setOption(option)
}

const loadStats = async () => {
  if (!props.customerId) return
  const contacts = await getContactListV1(props.customerId)
  stats.value.contactCount = contacts?.length || 0
}

onMounted(() => {
  loadDictItems(['customer_type', 'customer_status', 'customer_level', 'customer_group', 'biz_type', 'customer_category', 'customer_source'])
  loadOverview()
  loadStats()
  loadChartData()
  // 模拟纱线用量数据
  yarnTableData.value = [
    { month: '2026-01', yarnType: '32S纯棉', quantity: 120, unitPrice: 32000, amount: 384, grossProfit: 38, grossMargin: '9.9%' },
    { month: '2026-02', yarnType: '40S纯棉', quantity: 95, unitPrice: 35000, amount: 332.5, grossProfit: 35, grossMargin: '10.5%' },
    { month: '2026-03', yarnType: '32S混纺', quantity: 150, unitPrice: 28000, amount: 420, grossProfit: 42, grossMargin: '10.0%' }
  ]
})

watch(() => props.customerId, () => {
  loadOverview()
  loadStats()
})

onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
})
</script>

<style scoped>
.overview-tab { padding: 8px 0; }
.stats-row { margin-bottom: 16px; }
.stat-card { text-align: center; }
.stat-value { font-size: 28px; font-weight: 600; color: var(--el-color-primary); }
.stat-label { font-size: 13px; color: #666; margin-top: 4px; }

.chart-row { margin-bottom: 16px; }
.profile-fields-card { margin-bottom: 16px; }
.profile-edit-form :deep(.el-form-item) { margin-bottom: 10px; }
.chart-container { height: 300px; }

.overview-card { height: 100%; }
.overview-text { min-height: 80px; }
.overview-text p { margin: 0; line-height: 1.6; }
.overview-text .placeholder { color: #999; font-style: italic; }
.overview-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.overview-info { display: flex; flex-direction: column; gap: 10px; }
.info-item { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.info-label { color: #666; min-width: 80px; }

.card-header { display: flex; justify-content: space-between; align-items: center; }

@media (max-width: 768px) {
  .chart-container { height: 250px; }
}
</style>
