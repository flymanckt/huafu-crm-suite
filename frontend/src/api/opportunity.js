import request from './request'

export function getLeadPage(params) {
  return request.get('/lead/page', { params })
}

export function createLead(data) {
  return request.post('/lead', data)
}

export function updateLead(id, data) {
  return request.put(`/lead/${id}`, data)
}

export function getOpportunityPage(params) {
  return request.get('/opportunity/page', { params })
}

export function getOpportunityDetail(id) {
  return request.get(`/opportunity/${id}`)
}

export function createOpportunity(data) {
  return request.post('/opportunity', data)
}

export function updateOpportunity(id, data) {
  return request.put(`/opportunity/${id}`, data)
}

export function advanceOpportunityStage(id, newStage) {
  return request.put(`/opportunity/${id}/advance-stage`, null, { params: { newStage } })
}

export function getLostOrderPage(params) {
  return request.get('/lost-order/page', { params })
}

export function createLostOrder(data) {
  return request.post('/lost-order', data)
}

// 商机阶段推进（支持跳阶段/丢单）
export function updateOpportunityStage(id, stage, data) {
  return request.put(`/opportunity/${id}/stage`, data, { params: { stage } })
}
