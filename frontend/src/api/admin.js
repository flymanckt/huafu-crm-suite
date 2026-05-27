import request from './request'

// 用户管理
export function getUserPage(params) {
  return request.get('/admin/user/page', { params })
}
export function getUserDetail(id) {
  return request.get(`/admin/user/${id}`)
}
export function createUser(data) {
  return request.post('/admin/user', data)
}
export function updateUser(id, data) {
  return request.put(`/admin/user/${id}`, data)
}
export function deleteUser(id) {
  return request.delete(`/admin/user/${id}`)
}
export function resetUserPassword(id) {
  return request.put(`/admin/user/${id}/reset-password`)
}
export function toggleUserStatus(id, status) {
  return request.put(`/admin/user/${id}/status`, null, { params: { status } })
}

// 角色管理
export function getRolePage(params) {
  return request.get('/admin/role/page', { params })
}
export function getRoleDetail(id) {
  return request.get(`/admin/role/${id}`)
}
export function createRole(data) {
  return request.post('/admin/role', data)
}
export function updateRole(id, data) {
  return request.put(`/admin/role/${id}`, data)
}
export function deleteRole(id) {
  return request.delete(`/admin/role/${id}`)
}
export function getRoleMenus(roleId) {
  return request.get(`/admin/role/${roleId}/menus`)
}
export function getMenuTree() {
  return request.get('/admin/role/menus/tree')
}
export function updateRoleMenus(roleId, menuIds) {
  return request.put(`/admin/role/${roleId}/menus`, menuIds)
}

// 部门管理
export function getDeptTree() {
  return request.get('/admin/dept/tree')
}
export function getDeptDetail(id) {
  return request.get(`/admin/dept/${id}`)
}
export function createDept(data) {
  return request.post('/admin/dept', data)
}
export function updateDept(id, data) {
  return request.put(`/admin/dept/${id}`, data)
}
export function deleteDept(id) {
  return request.delete(`/admin/dept/${id}`)
}
