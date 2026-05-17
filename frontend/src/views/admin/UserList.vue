<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="header-actions">
            <el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" />
            <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新增用户</el-button>
          </div>
        </div>
      </template>

      <ConfigurableFilterForm
        v-model="query"
        :page-code="pageCode"
        :default-filters="filterFields"
        @search="loadData"
        @reset="resetQuery"
      />

      <BatchUpdateBar resource="user" :fields="batchFields" :selected-rows="selectedRows" @clear="clearBatchSelection" @done="loadData" />
      <el-table ref="tableRef" :data="tableData" v-loading="loading" stripe border class="data-table" max-height="calc(100vh - 360px)" row-key="id" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" fixed />
        <el-table-column v-if="columnVisible('username')" prop="username" label="用户名" width="140" />
        <el-table-column v-if="columnVisible('realName')" prop="realName" label="姓名" width="120" />
        <el-table-column v-if="columnVisible('phone')" prop="phone" label="手机号" width="130" />
        <el-table-column v-if="columnVisible('email')" prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('deptName')" prop="deptName" label="部门" width="140" />
        <el-table-column v-if="columnVisible('roleNames')" prop="roleNames" label="角色" width="160">
          <template #default="{ row }">{{ row.roleNames?.join('、') || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('status')" prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="columnVisible('lastLoginTime')" prop="lastLoginTime" label="最后登录" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleResetPwd(row.id)" :disabled="row.id === currentUserId">重置密码</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)" :disabled="row.id === currentUserId">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-popconfirm title="确定删除该用户？" @confirm="handleDelete(row.id)">
              <template #reference><el-button link type="danger" :disabled="row.id === currentUserId">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0" style="margin-top:16px;justify-content:flex-end"
        v-model:current-page="query.current" v-model:page-size="query.size"
        :total="total" :page-sizes="[10,20,50]"
        layout="total, sizes, prev, pager, next"
        @page-change="loadData" @size-change="loadData"
      />
    </el-card>

    <!-- 用户表单弹窗 -->
    <el-dialog v-model="formVisible" :title="editingId ? '编辑用户' : '新增用户'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!editingId" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="手机号码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="电子邮箱" />
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-tree-select v-model="form.deptId" :data="deptTree" :props="{ label: 'deptName', value: 'id', children: 'children' }" placeholder="选择部门" clearable check-strictly style="width:100%" />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="选择角色" style="width:100%">
            <el-option v-for="r in roleList" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="初始密码" prop="password" v-if="!editingId">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入初始密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
    <ColumnConfigDrawer :page-code="pageCode" :default-columns="defaultColumns" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Setting } from '@element-plus/icons-vue'
import { getUserPage, createUser, updateUser, deleteUser, resetUserPassword, toggleUserStatus, getRolePage, getDeptTree } from '@/api/admin'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

const currentUserId = parseInt(localStorage.getItem('userId') || '0')
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const tableRef = ref()
const selectedRows = ref([])
const pageCode = 'admin-user-list'
const initialQuery = () => ({ current: 1, size: 20, username: '', realName: '', status: null, phone: '', email: '', deptName: '' })
const query = ref(initialQuery())
const filterFields = [
  { key: 'username', label: '用户名', placement: 'default' },
  { key: 'realName', label: '姓名', placement: 'default' },
  { key: 'status', label: '状态', type: 'dict', dictCode: 'user_status', valueType: 'number', placement: 'default', width: 130 },
  { key: 'phone', label: '手机号', placement: 'more' },
  { key: 'email', label: '邮箱', placement: 'more', width: 200 },
  { key: 'deptName', label: '部门', placement: 'hidden' }
]
const defaultColumns = [
  { key: 'username', label: '用户名', width: 140, visible: true, fixed: true },
  { key: 'realName', label: '姓名', width: 120, visible: true },
  { key: 'phone', label: '手机号', width: 130, visible: true },
  { key: 'email', label: '邮箱', width: 180, visible: true },
  { key: 'deptName', label: '部门', width: 140, visible: true },
  { key: 'roleNames', label: '角色', width: 160, visible: true },
  { key: 'status', label: '状态', width: 80, visible: true },
  { key: 'lastLoginTime', label: '最后登录', width: 160, visible: true }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false
const formVisible = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref()
const roleList = ref([])
const deptTree = ref([])
const batchFields = [
  { key: 'status', label: '状态', dictCode: 'user_status', valueType: 'number' },
  { key: 'deptId', label: '部门ID', type: 'number' },
  { key: 'post', label: '岗位' },
  { key: 'phone', label: '手机号' },
  { key: 'email', label: '邮箱' }
]

const form = reactive({ username: '', realName: '', phone: '', email: '', deptId: null, roleIds: [], password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserPage(query.value)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

const resetQuery = () => {
  query.value = initialQuery()
  loadData()
}

const openCreate = () => {
  editingId.value = null
  Object.assign(form, { username: '', realName: '', phone: '', email: '', deptId: null, roleIds: [], password: '' })
  formVisible.value = true
}

const openEdit = async (row) => {
  editingId.value = row.id
  const detail = await getUserDetail(row.id)
  Object.assign(form, {
    username: detail.username, realName: detail.realName, phone: detail.phone || '',
    email: detail.email || '', deptId: detail.deptId, roleIds: detail.roleIds || [], password: ''
  })
  formVisible.value = true
}

const getUserDetail = async (id) => {
  const { getUserDetail: api } = await import('@/api/admin')
  return api(id)
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await updateUser(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    loadData()
  } finally { submitting.value = false }
}

const handleResetPwd = async (id) => {
  await ElMessageBox.confirm('确定重置该用户密码为默认密码？', '重置密码', { type: 'warning' })
  await resetUserPassword(id)
  ElMessage.success('密码已重置为 Huafu@2024')
}

const toggleStatus = async (row) => {
  const action = row.status === 1 ? '停用' : '启用'
  await ElMessageBox.confirm(`确定${action}该用户？`, action, { type: 'warning' })
  await toggleUserStatus(row.id, row.status === 1 ? 2 : 1)
  ElMessage.success(`${action}成功`)
  loadData()
}

const handleDelete = async (id) => {
  await deleteUser(id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSelectionChange = (rows) => { selectedRows.value = rows.filter(row => Number(row.id) !== currentUserId) }
const clearBatchSelection = () => { selectedRows.value = []; tableRef.value?.clearSelection?.() }

const loadRoles = async () => {
  const res = await getRolePage({ current: 1, size: 200 })
  roleList.value = res.records || []
}

const loadDepts = async () => {
  deptTree.value = await getDeptTree()
}

onMounted(() => { loadData(); loadRoles(); loadDepts() })
</script>

<style scoped>
.card-header, .header-actions { display: flex; justify-content: space-between; align-items: center; }
.header-actions { gap: 8px; }
.search-form { margin-bottom: 16px; }
</style>
