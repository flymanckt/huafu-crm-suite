import request from './request'

export function getQuotePage(params) {
  return request.get('/quote/page', { params })
}
export function getQuoteDetail(id) {
  return request.get(`/quote/${id}`)
}
export function createQuote(data) {
  return request.post('/quote', data)
}
export function updateQuote(id, data) {
  return request.put(`/quote/${id}`, data)
}
export function deleteQuote(id) {
  return request.delete(`/quote/${id}`)
}
export function sendQuote(id) {
  return request.post(`/quote/${id}/send`)
}
export function confirmQuote(id) {
  return request.post(`/quote/${id}/confirm`)
}
export function cancelQuote(id) {
  return request.post(`/quote/${id}/cancel`)
}
export function addQuoteItem(quoteId, data) {
  return request.post(`/quote/${quoteId}/item`, data)
}
export function updateQuoteItem(itemId, data) {
  return request.put(`/quote/item/${itemId}`, data)
}
export function deleteQuoteItem(itemId) {
  return request.delete(`/quote/item/${itemId}`)
}
export function getQuoteItems(quoteId) {
  return request.get(`/quote/${quoteId}/items`)
}
