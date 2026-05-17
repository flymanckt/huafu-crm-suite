import request from './request'

export function batchUpdate(resource, ids, fields) {
  return request.put(`/crm/v1/batch-update/${resource}`, { ids, fields })
}

export function batchUpdateModuleRecords(moduleKey, ids, fields) {
  return request.put(`/crm/v1/batch-update/module-records/${moduleKey}`, { ids, fields })
}
