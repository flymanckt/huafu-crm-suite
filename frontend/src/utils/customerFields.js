const enumValue = (value, codeMap = {}) => {
  if (value === null || value === undefined || value === '') return null
  if (typeof value === 'number') return Number.isFinite(value) ? value : null
  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (trimmed === '') return null
    if (/^-?\d+$/.test(trimmed)) return Number(trimmed)
    return codeMap[trimmed] ?? null
  }
  return null
}

const labelOf = (value, labels) => {
  if (value === null || value === undefined || value === '') return '-'
  return labels[value] || labels[enumValue(value)] || String(value)
}

const putIfPresent = (target, key, value) => {
  if (value !== undefined) target[key] = value
}

const codeFromValue = (value, codes) => {
  const normalized = enumValue(value)
  if (normalized === null) return value ?? null
  return codes[normalized] ?? String(normalized)
}

export const customerTypeCodes = { ENTITY: 1, TRADE: 2, END_BRAND: 3 }
export const customerLevelCodes = { AAA: 1, AA: 2, A: 3, B: 4, C: 5 }
export const customerStatusCodes = { POTENTIAL: 1, ACTIVE: 2, INACTIVE: 3, LOST: 4, NEW: 5, KEY: 6 }
export const businessTypeCodes = { DOMESTIC: 1, EXPORT: 2, ENTRADE: 3 }
export const categoryCodes = { SHANGXIAN: 1, MIANLIAO: 2, FUZHUANG: 3, MAOYISHANG: 4 }
export const sourceCodes = { ZHANHUI: 1, ZHUANJIESHAO: 2, ZIZHUKAIFA: 3, PINGTAI: 4 }
export const customerGroupCodes = { WAIXIAO: 1, NEIXIAOLIUTONG: 2, DIANSHANG: 3 }

export const customerTypeLabels = {
  1: '直接客户',
  2: '代理',
  3: '终端品牌',
  ENTITY: '直接客户',
  TRADE: '代理',
  END_BRAND: '终端品牌'
}

export const customerLevelLabels = {
  1: 'AAA',
  2: 'AA',
  3: 'A',
  4: 'B',
  5: 'C',
  A: 'A',
  B: 'B',
  C: 'C',
  AAA: 'AAA',
  AA: 'AA'
}

export const customerStatusLabels = {
  1: '潜在客户',
  2: '活跃客户',
  3: '非活跃',
  4: '流失',
  5: '新客户',
  6: '重点客户',
  POTENTIAL: '潜在客户',
  ACTIVE: '活跃客户',
  INACTIVE: '非活跃',
  LOST: '流失',
  NEW: '新客户',
  KEY: '重点客户'
}

export const businessTypeLabels = {
  1: '内销',
  2: '外销',
  3: '转口',
  DOMESTIC: '内销',
  EXPORT: '外销',
  ENTRADE: '转口'
}

export const categoryLabels = {
  1: '纱线厂',
  2: '面料厂',
  3: '服装厂',
  4: '贸易商',
  SHANGXIAN: '纱线厂',
  MIANLIAO: '面料厂',
  FUZHUANG: '服装厂',
  MAOYISHANG: '贸易商'
}

export const sourceLabels = {
  1: '展会',
  2: '转介绍',
  3: '自主开发',
  4: '平台',
  ZHANHUI: '展会',
  ZHUANJIESHAO: '转介绍',
  ZIZHUKAIFA: '自主开发',
  PINGTAI: '平台'
}

export const customerGroupLabels = {
  1: '外销',
  2: '内销流通',
  3: '电商',
  WAIXIAO: '外销',
  NEIXIAOLIUTONG: '内销流通',
  DIANSHANG: '电商'
}

export const stageLabels = {
  1: '初筛',
  2: '洽谈',
  3: '试样',
  4: '正式'
}

// 客户阶段代码映射（用于 API → 表单 Integer）
export const stageCodes = {
  INITIAL: 1,
  NEGOTIATING: 2,
  SAMPLING: 3,
  FORMAL: 4
}

// 反向映射：Integer → API 字符串
export const stageValueCodes = {
  1: 'INITIAL',
  2: 'NEGOTIATING',
  3: 'SAMPLING',
  4: 'FORMAL'
}

export const riskLevelLabels = {
  1: '低风险',
  2: '中风险',
  3: '高风险'
}

export const customerStatusType = {
  1: 'info',
  2: 'success',
  3: 'warning',
  4: 'danger',
  5: '',
  6: 'primary',
  POTENTIAL: 'info',
  ACTIVE: 'success',
  INACTIVE: 'warning',
  LOST: 'danger',
  NEW: '',
  KEY: 'primary'
}

export const customerLevelType = {
  1: 'success',
  2: 'warning',
  3: 'info',
  4: 'info',
  A: 'success',
  B: 'warning',
  C: 'info',
  D: 'info'
}

export const customerLabel = {
  type: (value) => labelOf(value, customerTypeLabels),
  level: (value) => labelOf(value, customerLevelLabels),
  status: (value) => labelOf(value, customerStatusLabels),
  businessType: (value) => labelOf(value, businessTypeLabels),
  category: (value) => labelOf(value, categoryLabels),
  source: (value) => labelOf(value, sourceLabels),
  group: (value) => labelOf(value, customerGroupLabels),
  stage: (value) => labelOf(value, stageLabels),
  riskLevel: (value) => labelOf(value, riskLevelLabels)
}

export function normalizeCustomerForForm(data = {}) {
  const normalizedStage = enumValue(data.customerStage ?? data.stage, stageCodes)
  const normalizedCategory = data.customerCategory ?? data.category ?? null
  const normalizedSource = data.customerSource ?? data.source ?? null
  const normalizedCustomerGroup = data.mainCustomerGroup ?? data.customerGroup ?? null

  return {
    ...data,
    // 枚举字段：API字符串 → 表单Integer
    type: enumValue(data.type, customerTypeCodes),
    level: enumValue(data.level, customerLevelCodes),
    status: enumValue(data.status, customerStatusCodes),
    businessType: enumValue(data.businessType, businessTypeCodes),
    customerStage: normalizedStage,
    stage: normalizedStage,
    customerCategory: normalizedCategory,
    category: normalizedCategory,
    customerSource: normalizedSource,
    source: normalizedSource,
    mainCustomerGroup: normalizedCustomerGroup,
    customerGroup: normalizedCustomerGroup,
    riskLevel: enumValue(data.riskLevel),
    countryRegion: data.countryRegion ?? data.region ?? '',
    // 字段名归一
    productionCapacity: data.productionCapacity ?? data.capacityInfo ?? '',
    capacityInfo: data.productionCapacity ?? data.capacityInfo ?? '',
    salesMerchandiser: data.salesMerchandiser ?? data.salesName ?? '',
    salesName: data.salesMerchandiser ?? data.salesName ?? '',
    competitorShare: data.competitorShareJson ?? data.competitorShare ?? '',
    bundleCustomerName: data.bundleCustomerName ?? '',
    bundleBrand: data.bundleBrand ?? '',
    bundleCustomerId: data.bundleCustomerId ?? null,
    bundleCustomerSapCode: data.bundleCustomerSapCode ?? '',
  }
}

// 按 DTO 字段实际类型分组
// Integer 枚举：直接透传表单 Integer，不做字符串转换
const INTEGER_ENUM_FIELDS = [
  'type', 'level', 'status', 'businessType', 'customerStage',
  'machineCount', 'riskLevel',
]
export function buildCustomerUpdatePayload(source = {}) {
  const payload = {}

  // ① 直接透传字段（String/Number原样，undefined/null跳过）
  const directFields = [
    'customerCode', 'customerName', 'customerShortName',
    'province', 'city', 'district', 'address',
    'mainContactName', 'mainContactPhone', 'mainContactRole',
    'annualRevenue', 'creditLimit', 'taxRate', 'paymentDays',
    'sapCustomerCode', 'remark',
    'customerSegment', 'countryRegion', 'mainBrand', 'annualYarnVolume',
    'productionCapacity', 'industryPosition', 'salesMerchandiser',
    'bundleCustomerName', 'bundleBrand', 'bundleCustomerId', 'bundleCustomerSapCode',
    'locationLat', 'locationLng', 'unifiedSocialCreditCode',
    'englishName', 'assetType',
    'competitorShareJson', 'cooperationBrandJson',
    'taxId', 'bankName', 'bankAccount', 'invoiceTitle',
    'companyCode', 'priceList', 'currency',
    'deliveryFactory', 'accountAssignmentGroup', 'taxClassification',
    'shipToParty', 'soldToParty', 'payerParty',
    'ownerUserId', 'ownerDeptId',
  ]
  directFields.forEach((key) => putIfPresent(payload, key, source[key]))

  // ② Integer 枚举：直接保留数字，不过滤 null（让后端接收 null）
  INTEGER_ENUM_FIELDS.forEach((field) => {
    const val = source[field]
    // null/undefined/空字符串 → 不发该字段
    if (val !== null && val !== undefined && val !== '') {
      payload[field] = Number(val) || val
    }
  })

  // ③ 字段名归一（表单别名 → API标准名）
  if (source.capacityInfo != null) putIfPresent(payload, 'productionCapacity', source.capacityInfo)
  if (source.salesName != null) putIfPresent(payload, 'salesMerchandiser', source.salesName)
  if (source.stage != null) putIfPresent(payload, 'customerStage', Number(source.stage) || source.stage)
  if (source.category != null && source.category !== '') {
    putIfPresent(payload, 'customerCategory', source.category)
  }
  if (source.source != null && source.source !== '') {
    putIfPresent(payload, 'customerSource', source.source)
  }
  if (source.customerGroup != null && source.customerGroup !== '') {
    putIfPresent(payload, 'mainCustomerGroup', source.customerGroup)
  }
  if (source.competitorShare != null) putIfPresent(payload, 'competitorShareJson', source.competitorShare)

  // ⑥ contacts 数组处理（前端扩展字段，用于联系人管理）
  if (Array.isArray(source.contacts) && source.contacts.length > 0) {
    payload.contacts = source.contacts.map(c => ({
      name: c.name,
      phone: c.phone,
      role: c.role,
      isPrimary: c.isPrimary ?? false,
    }))
  }

  // ⑦ 清理 undefined
  Object.keys(payload).forEach((key) => {
    if (payload[key] === undefined) delete payload[key]
  })
  return payload
}
