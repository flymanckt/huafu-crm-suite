import request from '@/api/request'

const BASE = '/crm/v1/system/configs'

export function getAllConfigs() {
  return request.get(BASE)
}

export function getConfigsByGroup(group) {
  return request.get(`${BASE}/group/${group}`)
}

export function getConfigValue(configKey) {
  return request.get(`${BASE}/value/${configKey}`)
}

export function getConfigGroups() {
  return request.get(`${BASE}/groups`)
}

export function createConfig(data) {
  return request.post(BASE, data)
}

export function updateConfig(data) {
  return request.put(BASE, data)
}

export function deleteConfig(id) {
  return request.delete(`${BASE}/${id}`)
}
