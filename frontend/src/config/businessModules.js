export const businessModules = {
  'exhibition-activity': {
    title: '展会活动',
    recordName: '活动',
    primaryField: 'activityName',
    fields: [
      { key: 'activityName', label: '展会名称', required: true },
      { key: 'city', label: '城市' },
      { key: 'venue', label: '展馆/展位' },
      { key: 'startDate', label: '开始日期', type: 'date' },
      { key: 'endDate', label: '结束日期', type: 'date' },
      { key: 'owner', label: '负责人' },
      { key: 'goal', label: '目标', type: 'textarea' },
      { key: 'summary', label: '总结', type: 'textarea' }
    ]
  },
  'exhibition-material': {
    title: '参展物资清单',
    recordName: '物资',
    primaryField: 'materialName',
    fields: [
      { key: 'materialName', label: '物资名称', required: true },
      { key: 'category', label: '类别' },
      { key: 'quantity', label: '数量', type: 'number' },
      { key: 'unit', label: '单位' },
      { key: 'keeper', label: '保管人' },
      { key: 'status', label: '准备状态' },
      { key: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  'exhibition-sample': {
    title: '展会样品库',
    recordName: '样品',
    primaryField: 'sampleName',
    fields: [
      { key: 'sampleName', label: '样品名称', required: true },
      { key: 'barcode', label: '样品条码' },
      { key: 'productCategory', label: '产品品类' },
      { key: 'composition', label: '成分/工艺' },
      { key: 'stockQty', label: '库存数量', type: 'number' },
      { key: 'location', label: '存放位置' },
      { key: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  'exhibition-talk': {
    title: '展会洽谈记录',
    recordName: '洽谈',
    primaryField: 'customerName',
    fields: [
      { key: 'customerName', label: '客户/品牌', required: true },
      { key: 'contactName', label: '联系人' },
      { key: 'phone', label: '手机' },
      { key: 'sampleBarcode', label: '样品条码' },
      { key: 'demand', label: '需求描述', type: 'textarea' },
      { key: 'nextAction', label: '下一步行动', type: 'textarea' },
      { key: 'followOwner', label: '跟进人' }
    ]
  },
  'product-master': {
    title: '产品档案',
    recordName: '产品',
    primaryField: 'productName',
    fields: [
      { key: 'productName', label: '产品名称', required: true },
      { key: 'productCode', label: '产品编码' },
      { key: 'productType', label: '产品类型' },
      { key: 'yarnType', label: '纱线类型' },
      { key: 'composition', label: '成分' },
      { key: 'season', label: '季节/版本' },
      { key: 'sourceSystem', label: '来源系统' },
      { key: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  'sample-inventory': {
    title: '样品库存',
    recordName: '库存',
    primaryField: 'sampleName',
    fields: [
      { key: 'sampleName', label: '样品名称', required: true },
      { key: 'sampleType', label: '样品类型' },
      { key: 'warehouse', label: '仓库' },
      { key: 'stockQty', label: '库存数量', type: 'number' },
      { key: 'availableQty', label: '可用数量', type: 'number' },
      { key: 'safetyQty', label: '安全库存', type: 'number' },
      { key: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  'sample-outbound': {
    title: '样品出库单',
    recordName: '出库单',
    primaryField: 'customerName',
    fields: [
      { key: 'customerName', label: '客户/品牌', required: true },
      { key: 'sampleName', label: '样品名称' },
      { key: 'quantity', label: '出库数量', type: 'number' },
      { key: 'applicant', label: '申请人' },
      { key: 'deliveryPlan', label: '关联派送计划' },
      { key: 'reason', label: '出库原因', type: 'textarea' },
      { key: 'followResult', label: '结果跟进', type: 'textarea' }
    ]
  },
  'color-card-plan': {
    title: '色卡派送计划',
    recordName: '派送计划',
    primaryField: 'customerName',
    fields: [
      { key: 'department', label: '部门' },
      { key: 'customerName', label: '派送客户/品牌', required: true },
      { key: 'plannedQty', label: '计划派送数量', type: 'number' },
      { key: 'actualQty', label: '实际派送数量', type: 'number' },
      { key: 'receiverRole', label: '派送对象' },
      { key: 'reason', label: '派送理由', type: 'textarea' },
      { key: 'planDate', label: '计划派送时间', type: 'date' },
      { key: 'actualDate', label: '实际派送日期', type: 'date' },
      { key: 'sender', label: '派送人' },
      { key: 'followResult', label: '结果跟进', type: 'textarea' }
    ]
  },
  'color-card-ledger': {
    title: '色卡派送总表',
    recordName: '色卡版本',
    primaryField: 'versionName',
    fields: [
      { key: 'versionName', label: '版本', required: true },
      { key: 'releaseDate', label: '推出时间', type: 'date' },
      { key: 'makeQty', label: '制作数量', type: 'number' },
      { key: 'totalCost', label: '合计费用', type: 'number' },
      { key: 'plannedQty', label: '计划派送数量', type: 'number' },
      { key: 'deliveredQty', label: '累计派送数量', type: 'number' },
      { key: 'stockQty', label: '系统库存', type: 'number' },
      { key: 'order2025', label: '25年接单', type: 'number' },
      { key: 'order2024', label: '24年接单', type: 'number' }
    ]
  },
  'sales-order': {
    title: '销售订单',
    recordName: '订单',
    primaryField: 'customerName',
    fields: [
      { key: 'customerName', label: '客户', required: true },
      { key: 'orderNo', label: '订单号' },
      { key: 'productName', label: '产品' },
      { key: 'productCategory', label: '产品品类' },
      { key: 'quantityTon', label: '数量（吨）', type: 'number' },
      { key: 'amount', label: '金额', type: 'number' },
      { key: 'grossMarginRate', label: '毛利率', type: 'number' },
      { key: 'orderDate', label: '下单日期', type: 'date' }
    ]
  },
  'shipment': {
    title: '发货单',
    recordName: '发货',
    primaryField: 'customerName',
    fields: [
      { key: 'customerName', label: '客户', required: true },
      { key: 'shipmentNo', label: '发货单号' },
      { key: 'orderNo', label: '订单号' },
      { key: 'productName', label: '产品' },
      { key: 'quantityTon', label: '发货数量（吨）', type: 'number' },
      { key: 'shipmentDate', label: '发货日期', type: 'date' },
      { key: 'factory', label: '交货工厂' },
      { key: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  'custom-report': {
    title: '自定义报表',
    recordName: '报表',
    primaryField: 'reportName',
    fields: [
      { key: 'reportName', label: '报表名称', required: true },
      { key: 'reportType', label: '报表类型' },
      { key: 'dataScope', label: '数据范围' },
      { key: 'frequency', label: '输出频率' },
      { key: 'owner', label: '负责人' },
      { key: 'description', label: '说明', type: 'textarea' }
    ]
  },
  'work-daily-report': {
    title: '工作日报',
    recordName: '日报',
    primaryField: 'reporter',
    fields: [
      { key: 'reporter', label: '提交人', required: true },
      { key: 'department', label: '部门' },
      { key: 'visitCustomer', label: '拜访客户' },
      { key: 'opportunityInfo', label: '商机信息', type: 'textarea' },
      { key: 'marketInfo', label: '商情信息', type: 'textarea' },
      { key: 'lostInfo', label: '丢单信息', type: 'textarea' },
      { key: 'tomorrowPlan', label: '明日计划', type: 'textarea' }
    ]
  }
}

export const businessModuleGroups = [
  {
    title: '展会管理',
    children: ['exhibition-activity', 'exhibition-material', 'exhibition-sample', 'exhibition-talk']
  },
  {
    title: '产品与样品',
    children: ['product-master', 'sample-inventory', 'sample-outbound']
  },
  {
    title: '色卡派送',
    children: ['color-card-plan', 'color-card-ledger']
  },
  {
    title: '订单管理',
    children: ['sales-order', 'shipment']
  },
  {
    title: '报表与日报',
    children: ['custom-report', 'work-daily-report']
  }
]
