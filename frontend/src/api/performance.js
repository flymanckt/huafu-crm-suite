import request from './request'

export function getVisitRecordPage(params) {
  return request.get('/visit-record/page', { params })
}

export function createVisitRecord(data) {
  return request.post('/visit-record', data)
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

export function getPerformancePage(params) {
  return request.get('/performance/page', { params })
}
