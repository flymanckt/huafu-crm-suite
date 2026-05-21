import { computed, ref, unref, watch } from 'vue'
import { getColumnConfig, putColumnConfig } from '@/api/customer.js'
import { ElMessage } from 'element-plus'

const DEFAULT_CUSTOMER_COLUMNS = [
  { key: 'customerCode', label: '客户编码', width: 120, visible: true, fixed: true },
  { key: 'customerName', label: '客户名称', width: 180, visible: true, fixed: true },
  { key: 'customerShortName', label: '客户简称', width: 140, visible: false },
  { key: 'type', label: '客户类型', width: 100, visible: true },
  { key: 'level', label: '客户等级', width: 100, visible: true },
  { key: 'status', label: '客户状态', width: 100, visible: true },
  { key: 'province', label: '省', width: 90, visible: true },
  { key: 'city', label: '市', width: 100, visible: false },
  { key: 'district', label: '区县', width: 100, visible: false },
  { key: 'address', label: '详细地址', width: 220, visible: false },
  { key: 'mainContactName', label: '主联系人', width: 120, visible: true },
  { key: 'mainContactPhone', label: '主联系人电话', width: 140, visible: false },
  { key: 'mainContactRole', label: '主联系人职务', width: 130, visible: false },
  { key: 'annualRevenue', label: '年营业额', width: 120, visible: false, align: 'right' },
  { key: 'creditLimit', label: '信用额度', width: 120, visible: false, align: 'right' },
  { key: 'taxRate', label: '税率', width: 100, visible: false, align: 'right' },
  { key: 'paymentDays', label: '付款天数', width: 100, visible: false, align: 'right' },
  { key: 'sapCustomerCode', label: 'SAP客户编码', width: 140, visible: false },
  { key: 'remark', label: '备注', width: 200, visible: false },
  { key: 'lastVisitTime', label: '最近拜访时间', width: 170, visible: false },
  { key: 'ownerUserId', label: '负责人ID', width: 110, visible: false },
  { key: 'ownerName', label: '负责人', width: 110, visible: true },
  { key: 'publicPoolTime', label: '进入公海时间', width: 170, visible: false },
  { key: 'createdBy', label: '创建人', width: 110, visible: false },
  { key: 'createdTime', label: '创建时间', width: 170, visible: false },
  { key: 'tenantId', label: '租户ID', width: 100, visible: false },
  { key: 'version', label: '版本', width: 80, visible: false },
  { key: 'customerCategory', label: '客户分类', width: 120, visible: false },
  { key: 'customerSegment', label: '客户品类', width: 120, visible: false },
  { key: 'businessType', label: '业务类型', width: 120, visible: true },
  { key: 'countryRegion', label: '国家区域', width: 120, visible: false },
  { key: 'mainBrand', label: '主要合作品牌', width: 160, visible: false },
  { key: 'annualYarnVolume', label: '年纱线用量', width: 130, visible: false, align: 'right' },
  { key: 'machineCount', label: '机台数', width: 100, visible: false, align: 'right' },
  { key: 'productionCapacity', label: '产能情况', width: 160, visible: false },
  { key: 'industryPosition', label: '行业地位', width: 160, visible: false },
  { key: 'mainCustomerGroup', label: '主要客户群体', width: 140, visible: false },
  { key: 'bundleCustomerName', label: '捆绑客户', width: 160, visible: false },
  { key: 'bundleBrand', label: '捆绑品牌', width: 160, visible: false },
  { key: 'ownerDeptId', label: '归属部门ID', width: 120, visible: false },
  { key: 'salesMerchandiser', label: '销售跟单', width: 120, visible: false },
  { key: 'locationLat', label: '纬度', width: 110, visible: false, align: 'right' },
  { key: 'locationLng', label: '经度', width: 110, visible: false, align: 'right' },
  { key: 'unifiedSocialCreditCode', label: '统一社会信用代码', width: 190, visible: false },
  { key: 'englishName', label: '英文全称', width: 180, visible: false },
  { key: 'assetType', label: '资产类型', width: 120, visible: false },
  { key: 'customerSource', label: '客户来源', width: 120, visible: false },
  { key: 'customerStage', label: '客户阶段', width: 110, visible: false },
  { key: 'competitorShareJson', label: '竞争对手占比', width: 180, visible: false },
  { key: 'cooperationBrandJson', label: '合作品牌明细', width: 180, visible: false },
  { key: 'blacklist', label: '黑名单', width: 90, visible: false },
  { key: 'riskLevel', label: '风险等级', width: 100, visible: false },
  { key: 'taxId', label: '增值税登记号', width: 160, visible: false },
  { key: 'bankName', label: '开户行', width: 160, visible: false },
  { key: 'bankAccount', label: '银行账号', width: 160, visible: false },
  { key: 'invoiceTitle', label: '开票抬头', width: 180, visible: false },
  { key: 'companyCode', label: '公司代码', width: 110, visible: false },
  { key: 'salesGroup', label: '销售组', width: 110, visible: false },
  { key: 'priceList', label: '价格清单', width: 120, visible: false },
  { key: 'currency', label: '货币', width: 90, visible: false },
  { key: 'deliveryFactory', label: '交货工厂', width: 130, visible: false },
  { key: 'accountAssignmentGroup', label: '账户分配组', width: 140, visible: false },
  { key: 'taxClassification', label: '税分类', width: 120, visible: false },
  { key: 'shipToParty', label: '送达方', width: 120, visible: false },
  { key: 'soldToParty', label: '售达方', width: 120, visible: false },
  { key: 'payerParty', label: '付款方', width: 120, visible: false },
  { key: 'countryCode', label: '国家代码', width: 110, visible: false },
]

const stores = new Map()

const parseConfig = (value) => {
  if (!value) return null
  if (typeof value === 'string') {
    try {
      return JSON.parse(value)
    } catch {
      return null
    }
  }
  return value
}

const normalizeColumns = (columns = []) => columns.map(col => ({
  key: col.key,
  label: col.label || col.key,
  width: col.width || 120,
  visible: col.visible !== false,
  fixed: col.fixed === true,
  align: col.align,
  formatter: col.formatter,
}))

const mergeColumns = (savedColumns = [], defaultColumns = []) => {
  const defaults = normalizeColumns(defaultColumns)
  const defaultMap = new Map(defaults.map(col => [col.key, col]))
  const merged = normalizeColumns(savedColumns)
    .filter(col => defaultMap.has(col.key))
    .map(col => ({ ...defaultMap.get(col.key), ...col, fixed: defaultMap.get(col.key).fixed }))
  defaults.forEach(col => {
    if (!merged.find(item => item.key === col.key)) merged.push({ ...col })
  })
  return merged
}

export function useColumnConfig(options = {}) {
  const configKey = computed(() => unref(options.pageCode) || 'customer-list')
  const defaults = computed(() => {
    const configured = unref(options.defaultColumns)
    return normalizeColumns(Array.isArray(configured) && configured.length ? configured : DEFAULT_CUSTOMER_COLUMNS)
  })
  const storeKey = configKey.value

  if (stores.has(storeKey)) return stores.get(storeKey)

  // 初始化为默认列，避免 Drawer 首次打开时显示空白
  const columns = ref(JSON.parse(JSON.stringify(defaults.value)))
  const drawerVisible = ref(false)
  const pendingColumns = ref([])
  const loading = ref(false)

  const fixedColumns = computed(() => columns.value.filter(c => c.fixed))
  const optionalColumns = computed(() => columns.value.filter(c => !c.fixed))

  const loadConfig = async () => {
    try {
      loading.value = true
      const res = await getColumnConfig(configKey.value)
      const config = parseConfig(res)
      if (config && Array.isArray(config.columns)) {
        columns.value = mergeColumns(config.columns, defaults.value)
      } else {
        columns.value = JSON.parse(JSON.stringify(defaults.value))
      }
    } catch {
      columns.value = JSON.parse(JSON.stringify(defaults.value))
    } finally {
      loading.value = false
    }
  }

  const saveConfig = async () => {
    const checkedOptional = pendingColumns.value.filter(c => !c.fixed && c.visible)
    if (checkedOptional.length === 0) {
      ElMessage.warning('至少保留1个业务列，方便查看客户信息')
      return false
    }
    const ordered = [
      ...pendingColumns.value.filter(c => c.fixed),
      ...pendingColumns.value.filter(c => !c.fixed),
    ]
    try {
      await putColumnConfig(configKey.value, { columns: ordered })
      columns.value = JSON.parse(JSON.stringify(ordered))
      drawerVisible.value = false
      ElMessage.success('列配置已更新')
      return true
    } catch {
      ElMessage.error('保存列配置失败')
      return false
    }
  }

  const openDrawer = () => {
    pendingColumns.value = JSON.parse(JSON.stringify(columns.value))
    drawerVisible.value = true
  }

  const closeDrawer = () => {
    drawerVisible.value = false
  }

  const toggleVisible = (key) => {
    const col = pendingColumns.value.find(c => c.key === key)
    if (!col || col.fixed) return
    if (col.visible) {
      const otherChecked = pendingColumns.value.filter(c => !c.fixed && c.visible && c.key !== key)
      if (otherChecked.length === 0) {
        ElMessage.warning('至少保留1个业务列')
        return
      }
    }
    col.visible = !col.visible
  }

  const onDragEnd = () => {
    const fixed = pendingColumns.value.filter(c => c.fixed)
    const optional = pendingColumns.value.filter(c => !c.fixed)
    pendingColumns.value = [...fixed, ...optional]
  }

  const updateWidth = (key, width) => {
    const col = pendingColumns.value.find(c => c.key === key)
    if (col) col.width = width
  }

  const resetConfig = () => {
    pendingColumns.value = JSON.parse(JSON.stringify(defaults.value))
  }

  const checkAllOptional = (checked) => {
    pendingColumns.value.forEach(c => {
      if (!c.fixed) c.visible = checked
    })
    if (checked) {
      const checkedOptional = pendingColumns.value.filter(c => !c.fixed && c.visible)
      if (checkedOptional.length === 0) {
        ElMessage.warning('至少保留1个业务列')
      }
    }
  }

  const allOptionalChecked = computed(() => {
    return pendingColumns.value.filter(c => !c.fixed).every(c => c.visible)
  })

  watch(defaults, (value) => {
    if (!columns.value.length) {
      columns.value = JSON.parse(JSON.stringify(value))
    }
  }, { deep: true })

  loadConfig()

  const api = {
    columns,
    drawerVisible,
    pendingColumns,
    loading,
    fixedColumns,
    optionalColumns,
    loadConfig,
    saveConfig,
    openDrawer,
    closeDrawer,
    toggleVisible,
    onDragEnd,
    updateWidth,
    resetConfig,
    checkAllOptional,
    allOptionalChecked,
  }
  stores.set(storeKey, api)
  return api
}
