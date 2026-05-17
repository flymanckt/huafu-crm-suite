import request from './request'

// ========== 现有 API (保持兼容) ==========
export function getCustomerPage(params) {
  return request.get('/customer/page', { params })
}

export function getCustomerDetail(id) {
  return request.get(`/customer/${id}`)
}

export function createCustomer(data) {
  return request.post('/customer', data)
}

export function updateCustomer(id, data) {
  return request.put(`/customer/${id}`, data)
}

export function deleteCustomer(id) {
  return request.delete(`/customer/${id}`)
}

export function getPublicPoolPage(params) {
  return request.get('/customer/public-pool', { params })
}

export function claimCustomer(id) {
  return request.post(`/customer/claim/${id}`)
}

export function getContactList(customerId) {
  return request.get('/customer-contact/list', { params: { customerId } })
}

export function createContact(data) {
  return request.post('/customer-contact', data)
}

export function updateContact(id, data) {
  return request.put(`/customer-contact/${id}`, data)
}

export function deleteContact(id) {
  return request.delete(`/customer-contact/${id}`)
}

// 客户生命周期
export function transferCustomer(id, data) {
  return request.put(`/customer/${id}/transfer`, data)
}
export function freezeCustomer(id, data) {
  return request.put(`/customer/${id}/freeze`, data)
}
export function lossCustomer(id, data) {
  return request.put(`/customer/${id}/loss`, data)
}

// ========== P0 新增接口 ==========

// 客户画像
export function getCustomerProfile(customerId) {
  return request.get(`/customer/${customerId}/profile`)
}

export function updateCustomerProfile(customerId, data) {
  return request.put(`/customer/${customerId}/profile`, data)
}

// 地址
export function getCustomerAddressList(customerId) {
  return request.get(`/customer/${customerId}/address`)
}

export function createCustomerAddress(customerId, data) {
  return request.post(`/customer/${customerId}/address`, data)
}

export function updateCustomerAddress(customerId, id, data) {
  return request.put(`/customer/${customerId}/address/${id}`, data)
}

export function deleteCustomerAddress(customerId, id) {
  return request.delete(`/customer/${customerId}/address/${id}`)
}

// 交接记录
export function getCustomerTransferList(customerId) {
  return request.get(`/customer/${customerId}/transfer`)
}

export function createCustomerTransfer(customerId, data) {
  return request.post(`/customer/${customerId}/transfer`, data)
}

export function confirmCustomerTransfer(customerId, id) {
  return request.put(`/customer/${customerId}/transfer/${id}/confirm`)
}

// ========== CRM v1 新 API (新网关) ==========

const CRM_V1 = '/crm/v1'

export function getCustomerPageV1(params) {
  return getCustomerPage(params)
}

export function getCustomerDetailV1(id) {
  return getCustomerDetail(id)
}

export function createCustomerV1(data) {
  return createCustomer(data)
}

export function updateCustomerV1(id, data) {
  return updateCustomer(id, data)
}

export function deleteCustomerV1(id) {
  return deleteCustomer(id)
}

// 联系人
export function getContactListV1(customerId) {
  return request.get(`${CRM_V1}/customers/${customerId}/contacts`)
}

export function createContactV1(customerId, data) {
  return request.post(`${CRM_V1}/customers/${customerId}/contacts`, data)
}

export function updateContactV1(customerId, id, data) {
  return request.put(`${CRM_V1}/customers/${customerId}/contacts/${id}`, data)
}

export function deleteContactV1(customerId, id) {
  return request.delete(`${CRM_V1}/customers/${customerId}/contacts/${id}`)
}

// 联系人树
export function getContactTree(customerId) {
  return request.get(`${CRM_V1}/customers/${customerId}/contacts/tree`)
}

// SAP组织
export function getSapInfoListV1(customerId) {
  return request.get(`${CRM_V1}/customers/${customerId}/sap-infos`)
}

export function createSapInfoV1(customerId, data) {
  return request.post(`${CRM_V1}/customers/${customerId}/sap-infos`, data)
}

export function updateSapInfoV1(customerId, id, data) {
  return request.put(`${CRM_V1}/customers/${customerId}/sap-infos/${id}`, data)
}

export function deleteSapInfoV1(customerId, id) {
  return request.delete(`${CRM_V1}/customers/${customerId}/sap-infos/${id}`)
}

export function getSapOrgListV1(customerId) {
  return request.get(`${CRM_V1}/customers/${customerId}/sap-orgs`)
}

export function createSapOrgV1(customerId, data) {
  return request.post(`${CRM_V1}/customers/${customerId}/sap-orgs`, data)
}

export function updateSapOrgV1(customerId, id, data) {
  return request.put(`${CRM_V1}/customers/${customerId}/sap-orgs/${id}`, data)
}

export function deleteSapOrgV1(customerId, id) {
  return request.delete(`${CRM_V1}/customers/${customerId}/sap-orgs/${id}`)
}

// 附件
export function getAttachmentListV1(customerId) {
  return request.get(`${CRM_V1}/customers/${customerId}/attachments`)
}

export function uploadAttachmentV1(customerId, data) {
  return request.post(`${CRM_V1}/customers/${customerId}/attachments`, data)
}

export function deleteAttachmentV1(customerId, id) {
  return request.delete(`${CRM_V1}/customers/${customerId}/attachments/${id}`)
}

// 关联企业
export function getRelatedEnterpriseListV1(customerId) {
  return request.get(`${CRM_V1}/customers/${customerId}/bundles`)
}

// 统计概览
export function getCustomerOverviewV1(customerId) {
  return request.get(`${CRM_V1}/customers/${customerId}/overview`)
}

export function updateCustomerOverviewV1(customerId, data) {
  return request.put(`${CRM_V1}/customers/${customerId}/overview`, data)
}

// 销量毛利趋势
export function getSalesTrendV1(customerId, params) {
  return request.get(`${CRM_V1}/customers/${customerId}/sales-trend`, { params })
}

// 纱线用量
export function getYarnUsageV1(customerId, params) {
  return request.get(`${CRM_V1}/customers/${customerId}/yarn-usage`, { params })
}

// ========== 用户配置 API ==========
export function getColumnConfig(key) {
  return request.get(`${CRM_V1}/user-configs/columns/${key}`)
}

export function putColumnConfig(key, data) {
  return request.put(`${CRM_V1}/user-configs/columns/${key}`, { columnConfigs: data })
}

export function getFilterPreset(key) {
  return request.get(`${CRM_V1}/user-configs/filters/${key}`)
}

export function putFilterPreset(key, data) {
  return request.put(`${CRM_V1}/user-configs/filters/${key}`, { filterConfigs: data })
}
