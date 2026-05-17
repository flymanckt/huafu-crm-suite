import request from './request'

const BASE = '/crm/v1/module-records'

export function getModuleRecordPage(moduleKey, params) {
  return request.get(`${BASE}/${moduleKey}`, { params })
}

export function createModuleRecord(moduleKey, data) {
  return request.post(`${BASE}/${moduleKey}`, data)
}

export function updateModuleRecord(moduleKey, id, data) {
  return request.put(`${BASE}/${moduleKey}/${id}`, data)
}

export function deleteModuleRecord(moduleKey, id) {
  return request.delete(`${BASE}/${moduleKey}/${id}`)
}
