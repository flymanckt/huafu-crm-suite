import request from './request'

export function getTargetPage(params) {
  return request.get('/target/page', { params })
}

export function createTarget(data) {
  return request.post('/target', data)
}

export function getTargetDetail(id) {
  return request.get(`/target/${id}`)
}

export function updateTarget(id, data) {
  return request.put(`/target/${id}`, data)
}

export function deleteTarget(id) {
  return request.delete(`/target/${id}`)
}

export function getTargetAchievePage(targetId, params) {
  return request.get(`/target/${targetId}/achieve`, { params })
}
