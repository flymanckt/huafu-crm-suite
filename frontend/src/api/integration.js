import request from '@/api/request'

const BASE = '/crm/v1/integration'

export const connectionTypes = ['REST', 'SOAP', 'WEBHOOK', 'WECOM', 'SAP_RFC', 'SAP_ODATA', 'SAP_IDOC', 'SFTP', 'FTP', 'DATABASE', 'KAFKA', 'RABBITMQ', 'CUSTOM']
export const authTypes = ['NONE', 'BASIC', 'BEARER', 'API_KEY', 'OAUTH2', 'SIGNATURE', 'WECOM_WEBHOOK', 'CUSTOM']
export const directions = ['OUTBOUND', 'INBOUND', 'BIDIRECTIONAL']
export const logStatuses = ['PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'RETRYING']

export function listConnections(params = {}) {
  return request.get(`${BASE}/connections`, { params })
}

export function createConnection(data) {
  return request.post(`${BASE}/connections`, data)
}

export function updateConnection(id, data) {
  return request.put(`${BASE}/connections/${id}`, data)
}

export function deleteConnection(id) {
  return request.delete(`${BASE}/connections/${id}`)
}

export function testConnection(id) {
  return request.post(`${BASE}/connections/${id}/test`)
}

export function listSapRfcConfigs() {
  return request.get(`${BASE}/sap-rfc-configs`)
}

export function createSapRfcConfig(data) {
  return request.post(`${BASE}/sap-rfc-configs`, data)
}

export function updateSapRfcConfig(id, data) {
  return request.put(`${BASE}/sap-rfc-configs/${id}`, data)
}

export function deleteSapRfcConfig(id) {
  return request.delete(`${BASE}/sap-rfc-configs/${id}`)
}

export function testSapRfcConfig(id) {
  return request.post(`${BASE}/sap-rfc-configs/${id}/test`)
}

export function listInterfaces(params = {}) {
  return request.get(`${BASE}/interfaces`, { params })
}

export function createInterface(data) {
  return request.post(`${BASE}/interfaces`, data)
}

export function updateInterface(id, data) {
  return request.put(`${BASE}/interfaces/${id}`, data)
}

export function deleteInterface(id) {
  return request.delete(`${BASE}/interfaces/${id}`)
}

export function listMappings(interfaceId) {
  return request.get(`${BASE}/interfaces/${interfaceId}/mappings`)
}

export function createMapping(data) {
  return request.post(`${BASE}/mappings`, data)
}

export function updateMapping(id, data) {
  return request.put(`${BASE}/mappings/${id}`, data)
}

export function deleteMapping(id) {
  return request.delete(`${BASE}/mappings/${id}`)
}

export function pageLogs(params = {}) {
  return request.get(`${BASE}/logs`, { params })
}

export function createLog(data) {
  return request.post(`${BASE}/logs`, data)
}

export function repushLog(id) {
  return request.post(`${BASE}/logs/${id}/repush`)
}

export function executeLog(id) {
  return request.post(`${BASE}/logs/${id}/execute`)
}

export function executePendingLogs(limit = 20) {
  return request.post(`${BASE}/logs/execute-pending`, null, { params: { limit } })
}
