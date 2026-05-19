import request from './request'

export function getVisitRecordPage(params) {
  return request.get('/visit-record/page', { params })
}

export function createVisitRecord(data) {
  return request.post('/visit-record', data)
}

export function updateVisitRecord(id, data) {
  return request.put(`/visit-record/${id}`, data)
}

export function deleteVisitRecord(id) {
  return request.delete(`/visit-record/${id}`)
}

export function getDailyReportPage(params) {
  return request.get('/daily-report/page', { params })
}

export function getDailyReportDetail(id) {
  return request.get(`/daily-report/${id}`)
}

export function createDailyReport(data) {
  return request.post('/daily-report', data)
}

export function updateDailyReport(id, data) {
  return request.put(`/daily-report/${id}`, data)
}

export function deleteDailyReport(id) {
  return request.delete(`/daily-report/${id}`)
}

export function getPerformancePage(params) {
  return request.get('/performance/page', { params })
}
