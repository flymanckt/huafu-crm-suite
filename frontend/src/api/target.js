import request from './request'

export function getTargetPage(params) {
  return request.get('/target/page', { params })
}

export function createTarget(data) {
  return request.post('/target', data)
}

export function getTargetAchievePage(targetId, params) {
  return request.get(`/target/${targetId}/achieve`, { params })
}
