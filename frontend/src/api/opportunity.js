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

export function deleteLead(id) {
  return request.delete(`/lead/${id}`)
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

export function deleteOpportunity(id) {
  return request.delete(`/opportunity/${id}`)
}

export function advanceOpportunityStage(id, newStage) {
  return request.put(`/opportunity/${id}/stage-simple`, null, { params: { stage: newStage } })
}

export function getLostOrderPage(params) {
  return request.get('/lost-order/page', { params })
}

export function getLostOrderDetail(id) {
  return request.get(`/lost-order/${id}`)
}

export function createLostOrder(data) {
  return request.post('/lost-order', data)
}

export function updateLostOrder(id, data) {
  return request.put(`/lost-order/${id}`, data)
}

export function deleteLostOrder(id) {
  return request.delete(`/lost-order/${id}`)
}

// 商机阶段推进（支持跳阶段/丢单）
export function updateOpportunityStage(id, stage, data) {
  return request.put(`/opportunity/${id}/stage`, data, { params: { stage } })
}
