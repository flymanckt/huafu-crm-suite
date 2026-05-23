<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>客户列表</span>
          <div class="header-actions">
            <!-- 列配置按钮 -->
            <el-button :icon="Setting" circle @click="columnConfig.openDrawer()" title="列配置" />
            <ExcelImportButton
              v-if="activeTab === 'all'"
              module-name="客户"
              :fields="importFields"
              :import-fn="importCustomerRow"
              :export-rows="tableData"
              @done="loadData"
            />
            <el-button v-if="activeTab === 'all'" type="primary" @click="openCreate">
              <el-icon><Plus /></el-icon>新建客户
            </el-button>
          </div>
        </div>
      </template>

      <el-radio-group v-model="activeTab" style="margin-bottom:16px" @change="handleTabChange">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="public">公海池</el-radio-button>
      </el-radio-group>

      <div v-if="activeTab === 'all'" class="search-section">
        <ConfigurableFilterForm
          v-model="query"
          :page-code="pageCode"
          :default-filters="filterFields"
          @search="loadData"
          @reset="resetQuery"
        />

        <!-- ========== 筛选预设区域 ========== -->
        <div v-if="activePresetName" class="preset-bar">
          <el-tag type="primary" closable @close="handleClearPreset">
            预设：{{ activePresetName }}
          </el-tag>
        </div>

        <div v-if="presets.length > 0 || true" class="preset-section">
          <div class="preset-tags">
            <el-tag
              v-for="p in presets"
              :key="p.name"
              type="primary"
              class="preset-tag"
              @click="handleLoadPreset(p)"
            >
              {{ p.name }}
              <el-icon class="preset-tag-close" @click.stop="handleDeletePreset(p)"><Close /></el-icon>
            </el-tag>
          </div>

          <!-- 保存预设下拉 -->
          <el-dropdown @command="handleSaveCommand" trigger="click">
            <el-button link type="primary" class="save-preset-btn">
              保存当前为预设 <el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <div class="preset-save-panel">
                  <el-input
                    v-model="newPresetName"
                    placeholder="输入预设名称"
                    maxlength="20"
                    @keyup.enter="handleSaveNew"
                    style="margin-bottom:8px"
                  />
                  <el-button
                    type="primary"
                    size="small"
                    style="width:100%;margin-bottom:8px"
                    :disabled="!newPresetName.trim() || !canSaveMore"
                    @click="handleSaveNew"
                  >保存为新预设</el-button>
                  <div v-if="existingPresets.length > 0" class="overwrite-hint">
                    <div class="overwrite-title">或覆盖已有预设：</div>
                    <el-tag
                      v-for="p in existingPresets"
                      :key="p.name"
                      size="small"
                      style="margin:2px;cursor:pointer"
                      @click="handleOverwritePreset(p)"
                    >{{ p.name }}</el-tag>
                  </div>
                </div>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <BatchUpdateBar
        resource="customer"
        :fields="batchFields"
        :selected-rows="selectedRows"
        @clear="clearBatchSelection"
        @done="loadData"
      />

      <!-- 表格（动态列） -->
      <el-table
        ref="tableRef"
        :data="tableData"
        v-loading="loading"
        stripe
        border
        class="data-table"
        max-height="calc(100vh - 390px)"
        row-key="id"
        @selection-change="handleSelectionChange"
        style="width:100%"
        :key="tableKey"
      >
        <el-table-column type="selection" width="46" fixed />
        <template v-for="col in visibleColumns" :key="col.key">
          <el-table-column
            v-if="col.key === 'customerCode'"
            :prop="col.key"
            :label="col.label"
            :width="col.width"
          />
          <el-table-column
            v-else-if="col.key === 'customerName'"
            :prop="col.key"
            :label="col.label"
            :min-width="col.width"
            show-overflow-tooltip
          >
            <template #default="{ row }">
              <strong>{{ row.customerName }}</strong>
            </template>
          </el-table-column>
          <el-table-column
            v-else-if="col.key === 'level'"
            :prop="col.key"
            :label="col.label"
            :width="col.width"
          >
            <template #default="{ row }">
              <el-tag v-if="row.level" :type="customerLevelType[row.level]">{{ customerLabel.level(row.level) }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column
            v-else-if="col.key === 'status'"
            :prop="col.key"
            :label="col.label"
            :width="col.width"
          >
            <template #default="{ row }">
              <el-tag v-if="row.status" :type="customerStatusType[row.status]">{{ customerLabel.status(row.status) }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column
            v-else-if="col.key === 'businessType'"
            :prop="col.key"
            :label="col.label"
            :width="col.width"
          >
            <template #default="{ row }">
              <span v-if="hasValue(row.businessType)">{{ customerLabel.businessType(row.businessType) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column
            v-else-if="col.key === 'type'"
            :prop="col.key"
            :label="col.label"
            :width="col.width"
          >
            <template #default="{ row }">
              <span v-if="hasValue(row.type)">{{ customerLabel.type(row.type) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column
            v-else-if="col.key === 'ownerName'"
            :prop="col.key"
            :label="col.label"
            :width="col.width"
          />
          <el-table-column
            v-else-if="col.key === 'mainContactName'"
            :prop="col.key"
            :label="col.label"
            :width="col.width"
          />
          <el-table-column
            v-else-if="col.key === 'mainContactPhone'"
            :prop="col.key"
            :label="col.label"
            :width="col.width"
          />
          <el-table-column
            v-else-if="col.key === 'province'"
            :prop="col.key"
            :label="col.label"
            :width="col.width"
          >
            <template #default="{ row }">
              {{ [row.province, row.city, row.district].filter(Boolean).join('') || '-' }}
            </template>
          </el-table-column>
          <el-table-column
            v-else-if="col.key === 'customerCategory'"
            :prop="col.key"
            :label="col.label"
            :width="col.width"
          >
            <template #default="{ row }">
              <span v-if="hasValue(row.customerCategory)">{{ customerLabel.category(row.customerCategory) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column
            v-else-if="col.key === 'customerSource'"
            :prop="col.key"
            :label="col.label"
            :width="col.width"
          >
            <template #default="{ row }">
              <span v-if="hasValue(row.customerSource)">{{ customerLabel.source(row.customerSource) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column
            v-else-if="col.key === 'mainCustomerGroup'"
            :prop="col.key"
            :label="col.label"
            :width="col.width"
          >
            <template #default="{ row }">
              <span v-if="hasValue(row.mainCustomerGroup)">{{ customerLabel.group(row.mainCustomerGroup) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column
            v-else-if="col.key === 'customerStage'"
            :prop="col.key"
            :label="col.label"
            :width="col.width"
          >
            <template #default="{ row }">
              <span v-if="hasValue(row.customerStage)">{{ customerLabel.stage(row.customerStage) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column
            v-else
            :prop="col.key"
            :label="col.label"
            :width="col.width"
            :align="col.align"
            show-overflow-tooltip
          >
            <template #default="{ row }">
              <DictTag v-if="col.dictCode && hasValue(row[col.key])" :dict-code="col.dictCode" :value="String(row[col.key])" />
              <span v-else>{{ formatCell(row[col.key]) }}</span>
            </template>
          </el-table-column>
        </template>

        <!-- 操作列（固定在最右） -->
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="activeTab === 'all'">
              <el-button link type="primary" @click="$router.push('/customer/detail/' + String(row.id))">详情</el-button>
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-popconfirm title="确定删除该客户？" @confirm="handleDelete(row.id)">
                <template #reference><el-button link type="danger">删除</el-button></template>
              </el-popconfirm>
            </template>
            <template v-else>
              <el-popconfirm title="确定领取该客户？" @confirm="handleClaim(row.id)">
                <template #reference><el-button link type="primary">领取</el-button></template>
              </el-popconfirm>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <EmptyState
        v-if="!loading && tableData.length === 0"
        message="暂无客户数据"
        sub-message="尝试调整筛选条件，或新建客户"
      />

      <el-pagination
        v-if="total > 0"
        style="margin-top:16px;justify-content:flex-end"
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10,20,50]"
        layout="total, sizes, prev, pager, next"
        @page-change="loadData"
        @size-change="loadData"
      />
    </el-card>

    <CustomerForm
      v-if="activeTab === 'all'"
      v-model:visible="formVisible"
      :customer-id="editingId"
      @saved="handleSaved"
    />

    <!-- 列配置 Drawer -->
    <ColumnConfigDrawer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { Plus, Setting, ArrowDown, Close } from '@element-plus/icons-vue'
import { getCustomerPage, createCustomer, deleteCustomer, getPublicPoolPage, claimCustomer } from '@/api/customer'
import CustomerForm from './CustomerForm.vue'
import EmptyState from '@/components/EmptyState.vue'
import DictTag from '@/components/Dict/DictTag.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import ExcelImportButton from '@/components/common/ExcelImportButton.vue'
import { useColumnConfig } from '@/composables/useColumnConfig.js'
import { useFilterPreset } from '@/composables/useFilterPreset.js'
import { ElMessage } from 'element-plus'
import { useDict } from '@/composables/useDict'
import { buildCustomerUpdatePayload, customerLabel, customerLevelType, customerStatusType } from '@/utils/customerFields'

const activeTab = ref('all')
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageCode = 'customer-list'
const { loadDictItems } = useDict()
const initialQuery = () => ({
  current: 1, size: 20,
  customerName: '', level: null, type: null, businessType: null, status: null, ownerName: '',
  customerCategory: null, customerSource: null, customerStage: null, riskLevel: null, salesMerchandiser: ''
})
const query = ref(initialQuery())
const filterFields = [
  { key: 'customerName', label: '客户名称', placement: 'default', width: 180 },
  { key: 'status', label: '状态', type: 'dict', dictCode: 'customer_status', valueType: 'number', placement: 'default', width: 130 },
  { key: 'level', label: '客户等级', type: 'dict', dictCode: 'customer_level', valueType: 'number', placement: 'more' },
  { key: 'type', label: '客户类型', type: 'dict', dictCode: 'customer_type', valueType: 'number', placement: 'more' },
  { key: 'businessType', label: '业务类型', type: 'dict', dictCode: 'biz_type', valueType: 'number', placement: 'more' },
  { key: 'ownerName', label: '负责人', placement: 'more', width: 140 },
  { key: 'customerCategory', label: '客户分类', type: 'dict', dictCode: 'customer_category', valueType: 'number', placement: 'more' },
  { key: 'customerSource', label: '客户来源', type: 'dict', dictCode: 'customer_source', valueType: 'number', placement: 'more' },
  { key: 'customerStage', label: '客户阶段', type: 'dict', dictCode: 'customer_stage', valueType: 'number', placement: 'more' },
  { key: 'riskLevel', label: '风险等级', type: 'dict', dictCode: 'risk_level', valueType: 'number', placement: 'hidden' },
  { key: 'salesMerchandiser', label: '销售跟单', placement: 'hidden' }
]
const formVisible = ref(false)
const editingId = ref(null)
const tableKey = ref(0) // 用于强制刷新表格
const tableRef = ref()
const selectedRows = ref([])
const batchFields = [
  { key: 'status', label: '客户状态', dictCode: 'customer_status', valueType: 'number' },
  { key: 'level', label: '客户等级', dictCode: 'customer_level', valueType: 'number' },
  { key: 'type', label: '客户类型', dictCode: 'customer_type', valueType: 'number' },
  { key: 'businessType', label: '业务类型', dictCode: 'biz_type', valueType: 'number' },
  { key: 'customerCategory', label: '客户分类', dictCode: 'customer_category', valueType: 'number' },
  { key: 'customerSource', label: '客户来源', dictCode: 'customer_source', valueType: 'number' },
  { key: 'customerStage', label: '客户阶段', dictCode: 'customer_stage', valueType: 'number' },
  { key: 'riskLevel', label: '风险等级', dictCode: 'risk_level', valueType: 'number' },
  { key: 'salesMerchandiser', label: '销售跟单' },
  { key: 'remark', label: '备注' }
]
const importFields = [
  { key: 'customerName', label: '客户名称', required: true, example: '浙江云泰纺织有限公司' },
  { key: 'customerShortName', label: '客户简称', required: true, example: '云泰纺织' },
  { key: 'type', label: '客户类型', required: true, type: 'number', valueMap: { 直接客户: 1, 代理: 2, 终端品牌: 3 }, example: '直接客户' },
  { key: 'status', label: '客户状态', type: 'number', valueMap: { 潜在客户: 1, 活跃客户: 2, 非活跃: 3, 流失: 4, 新客户: 5, 重点客户: 6 }, example: '潜在客户' },
  { key: 'businessType', label: '业务类型', required: true, type: 'number', valueMap: { 内销: 1, 外销: 2, 转口: 3 }, example: '内销' },
  { key: 'category', label: '客户分类', type: 'number', valueMap: { 纱线厂: 1, 面料厂: 2, 服装厂: 3, 贸易商: 4 }, example: '纱线厂' },
  { key: 'province', label: '省份', example: '浙江省' },
  { key: 'city', label: '城市', example: '杭州市' },
  { key: 'district', label: '区县', example: '萧山区' },
  { key: 'address', label: '详细地址', example: '示例路1号' },
  { key: 'mainContactName', label: '主联系人', example: '张三' },
  { key: 'mainContactPhone', label: '主联系人电话', example: '13800000000' },
  { key: 'annualRevenue', label: '年营业额', type: 'number', example: '1000' },
  { key: 'countryRegion', label: '国家区域', example: '中国' },
  { key: 'industryPosition', label: '行业地位', example: '区域重点客户' },
  { key: 'remark', label: '备注' }
]

const importCustomerRow = (row) => createCustomer(buildCustomerUpdatePayload({
  ...row,
  status: row.status || 1
}))

// 列配置 composable
const columnConfig = useColumnConfig()
const { columns: allColumns } = columnConfig

// 筛选预设 composable
const filterPreset = useFilterPreset()
const {
  presets,
  activePresetName,
  newPresetName,
  canSaveMore,
  loadPreset,
  savePreset,
  deletePreset,
  clearActivePreset,
} = filterPreset

// 当前筛选项，用于保存预设
const currentFilters = computed(() => ({
  customerName: query.value.customerName,
  level: query.value.level,
  type: query.value.type,
  businessType: query.value.businessType,
  status: query.value.status,
  ownerName: query.value.ownerName,
  customerCategory: query.value.customerCategory,
  customerSource: query.value.customerSource,
  customerStage: query.value.customerStage,
  riskLevel: query.value.riskLevel,
  salesMerchandiser: query.value.salesMerchandiser,
}))

// 可见列（visible=true 且固定列永远在前）
const visibleColumns = computed(() => {
  return allColumns.value.filter(c => c.visible)
})
const visibleDictCodes = computed(() => [...new Set(visibleColumns.value.map(c => c.dictCode).filter(Boolean))])

const formatCell = (value) => {
  if (value === null || value === undefined || value === '') return '-'
  if (Array.isArray(value)) return value.join('、')
  if (typeof value === 'object') return JSON.stringify(value)
  return value
}
const hasValue = (value) => value !== null && value !== undefined && value !== ''

// 已有预设（用于覆盖）
const existingPresets = computed(() => presets.value)

// 列配置变化后强制刷新表格
watch(() => allColumns.value, () => {
  tableKey.value++
}, { deep: true })

watch(visibleDictCodes, (codes) => {
  if (codes.length) loadDictItems(codes)
}, { immediate: true })

const loadData = async () => {
  loading.value = true
  try {
    let res
    if (activeTab.value === 'all') {
      res = await getCustomerPage(query.value)
    } else {
      res = await getPublicPoolPage(query.value)
    }
    // res 是 API 响应拦截器返回的 data 对象，结构为 { current, size, total, records }
    tableData.value = res?.records || []
    // 确保 total 为数字类型，API 返回的 total 可能是字符串
    total.value = res?.total ? Number(res.total) : 0
  } catch (e) {
    console.error('加载客户数据失败:', e)
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  query.value.current = 1
  clearBatchSelection()
  loadData()
}

const resetQuery = () => {
  query.value = initialQuery()
  clearActivePreset()
  loadData()
}

const openCreate = () => {
  editingId.value = null
  formVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  formVisible.value = true
}

const handleSaved = () => {
  formVisible.value = false
  loadData()
}

const handleDelete = async (id) => {
  await deleteCustomer(id)
  ElMessage.success('删除成功')
  loadData()
}

const handleClaim = async (id) => {
  await claimCustomer(id)
  ElMessage.success('领取成功')
  loadData()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const clearBatchSelection = () => {
  selectedRows.value = []
  tableRef.value?.clearSelection?.()
}

// ========== 筛选预设处理 ==========
const handleLoadPreset = async (preset) => {
  const filters = await loadPreset(preset, query.value)
  if (filters) {
    query.value = { current: 1, size: query.value.size, ...filters }
    loadData()
  }
}

const handleSaveNew = async () => {
  await savePreset(currentFilters.value)
  newPresetName.value = ''
}

const handleOverwritePreset = async (preset) => {
  await filterPreset.overwritePreset(preset, currentFilters.value)
}

const handleDeletePreset = async (preset) => {
  await deletePreset(preset)
}

const handleClearPreset = () => {
  clearActivePreset()
  resetQuery()
}

const handleSaveCommand = () => {}

// 初始化
onMounted(async () => {
  await columnConfig.loadConfig()
  await filterPreset.loadPresets()
  loadData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 8px; align-items: center; }
.search-section { margin-bottom: 16px; }
.search-form { margin-bottom: 0; }
.search-form--essential { margin-bottom: 8px; }

/* 折叠区域：默认隐藏 */
.search-extra {
  display: none;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #ebeef5;
}
.search-extra.is-expanded {
  display: block;
}

/* 展开/收起切换按钮 */
.search-toggle {
  text-align: left;
  margin-top: 6px;
}
.toggle-icon {
  margin-left: 2px;
  transition: transform 0.2s;
}
.toggle-icon.is-up {
  transform: rotate(180deg);
}

/* ========== 预设区域 ========== */
.preset-bar {
  margin: 8px 0;
}
.preset-section {
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}
.preset-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.preset-tag {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
.preset-tag .preset-tag-close {
  margin-left: 2px;
  font-size: 10px;
}
.preset-tag:hover .preset-tag-close {
  color: inherit;
}
.save-preset-btn {
  font-size: 13px;
}
.preset-save-panel {
  padding: 10px 12px;
  min-width: 200px;
}
.overwrite-hint {
  border-top: 1px solid #e8e8e8;
  padding-top: 8px;
  margin-top: 4px;
}
.overwrite-title {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
</style>
