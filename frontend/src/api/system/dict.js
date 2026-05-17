import request from '@/api/request'

// 字典类型（无 s）：/api/crm/v1/dict-types
const DT_BASE = '/crm/v1/dict-types'
// 字典项（不带dict前缀）：/api/crm/v1/items
const DI_BASE = '/crm/v1/items'
// 字典批量查询（带s）：/api/crm/v1/dicts
const DICT_BASE = '/crm/v1/dicts'

// ========== 字典类型 ==========
export function createDictType(data) {
  return request.post(`${DT_BASE}`, data)
}
export function updateDictType(id, data) {
  return request.put(`${DT_BASE}/${id}`, data)
}
export function deleteDictType(id) {
  return request.delete(`${DT_BASE}/${id}`)
}
export function getDictTypePage(params) {
  return request.get(`${DT_BASE}`, { params })
}
export function getAllDictTypes() {
  return request.get(`${DT_BASE}/all`)
}

// ========== 字典项（独立路径） ==========
export function getDictItems(dictCode) {
  return request.get(`${DICT_BASE}/codes/${dictCode}`)
}
export function getBatchDicts(codes) {
  return request.get(`${DICT_BASE}/batch-codes`, { params: { codes: codes.join(',') } })
}
export function createDictItem(data) {
  return request.post(`${DI_BASE}`, data)
}
export function updateDictItem(id, data) {
  return request.put(`${DI_BASE}/${id}`, data)
}
export function deleteDictItem(id) {
  return request.delete(`${DI_BASE}/${id}`)
}
export function toggleDictItemStatus(id, status) {
  return request.put(`${DI_BASE}/${id}/status`, null, { params: { status } })
}
