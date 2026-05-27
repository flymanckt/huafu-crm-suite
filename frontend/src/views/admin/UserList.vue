<template>
  <div class="page-container admin-user-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <div>
            <div class="title">用户管理</div>
            <div class="subtitle">维护登录账号、归属部门、岗位与角色权限</div>
          </div>
          <div class="header-actions">
            <el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" />
            <el-button type="primary" @click="openCreate">
              <el-icon><Plus /></el-icon>
              新增用户
            </el-button>
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

      <el-table
        ref="tableRef"
        :data="tableData"
        v-loading="loading"
        stripe
        border
        class="data-table"
        max-height="calc(100vh - 320px)"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="46" fixed />
        <el-table-column v-if="columnVisible('username')" prop="username" label="用户名" min-width="130" />
        <el-table-column v-if="columnVisible('realName')" prop="realName" label="姓名" min-width="120" />
        <el-table-column v-if="columnVisible('deptName')" prop="deptName" label="部门" min-width="140" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('post')" prop="post" label="岗位" min-width="120" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('roleNames')" prop="roleNames" label="角色" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.roleNames?.join('、') || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('phone')" prop="phone" label="手机号" width="130" />
        <el-table-column v-if="columnVisible('email')" prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('status')" prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="columnVisible('lastLoginTime')" prop="lastLoginTime" label="最后登录" width="170" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleResetPwd(row.id)" :disabled="isCurrentUser(row.id)">重置密码</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)" :disabled="isCurrentUser(row.id)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-popconfirm title="确定删除该用户？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger" :disabled="isCurrentUser(row.id)">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        class="pager"
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @page-change="loadData"
        @size-change="loadData"
      />
    </el-card>

    <el-dialog v-model="formVisible" :title="editingId ? '编辑用户' : '新增用户'" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px" class="user-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" :disabled="!!editingId" placeholder="登录用户名" maxlength="64" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="真实姓名" maxlength="64" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="手机号码" maxlength="32" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="电子邮箱" maxlength="128" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门" prop="deptId">
              <el-tree-select
                v-model="form.deptId"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'id', children: 'children' }"
                node-key="id"
                placeholder="选择部门"
                clearable
                check-strictly
                filterable
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位" prop="post">
              <el-input v-model="form.post" placeholder="岗位/职务" maxlength="64" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio-button :label="1">启用</el-radio-button>
                <el-radio-button :label="2">停用</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="!editingId">
            <el-form-item label="初始密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="至少10位，含大小写/数字/特殊字符" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="角色" prop="roleIds">
              <el-select v-model="form.roleIds" multiple filterable collapse-tags collapse-tags-tooltip placeholder="选择角色" style="width:100%">
                <el-option
                  v-for="role in roleList"
                  :key="role.id"
                  :label="`${role.roleName}（${role.roleKey}）`"
                  :value="role.id"
                  :disabled="role.status === 2"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
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
import { onMounted, reactive, ref } from 'vue'
import { Plus, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createUser,
  deleteUser,
  getDeptTree,
  getRolePage,
  getUserDetail,
  getUserPage,
  resetUserPassword,
  toggleUserStatus,
  updateUser
} from '@/api/admin'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'

const currentUserId = String(localStorage.getItem('userId') || '')
const pageCode = 'admin-user-list'
const initialQuery = () => ({ current: 1, size: 20, username: '', realName: '', status: null, deptId: null })

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const tableRef = ref()
const formRef = ref()
const selectedRows = ref([])
const roleList = ref([])
const deptTree = ref([])
const formVisible = ref(false)
const editingId = ref(null)
const query = ref(initialQuery())

const filterFields = [
  { key: 'username', label: '用户名', placement: 'default' },
  { key: 'realName', label: '姓名', placement: 'default' },
  { key: 'status', label: '状态', type: 'dict', dictCode: 'user_status', valueType: 'number', placement: 'default', width: 130 },
  { key: 'deptId', label: '部门ID', placement: 'hidden' }
]
const defaultColumns = [
  { key: 'username', label: '用户名', width: 130, visible: true, fixed: true },
  { key: 'realName', label: '姓名', width: 120, visible: true },
  { key: 'deptName', label: '部门', width: 140, visible: true },
  { key: 'post', label: '岗位', width: 120, visible: true },
  { key: 'roleNames', label: '角色', width: 180, visible: true },
  { key: 'phone', label: '手机号', width: 130, visible: true },
  { key: 'email', label: '邮箱', width: 180, visible: true },
  { key: 'status', label: '状态', width: 90, visible: true },
  { key: 'lastLoginTime', label: '最后登录', width: 170, visible: true }
]
const batchFields = [
  { key: 'status', label: '状态', dictCode: 'user_status', valueType: 'number' },
  { key: 'post', label: '岗位' },
  { key: 'phone', label: '手机号' },
  { key: 'email', label: '邮箱' }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false

const form = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  deptId: null,
  post: '',
  roleIds: [],
  status: 1,
  password: ''
})

const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{10,}$/
const validatePassword = (rule, value, callback) => {
  if (editingId.value) {
    callback()
    return
  }
  if (!value) {
    callback(new Error('请输入初始密码'))
    return
  }
  if (!passwordPattern.test(value)) {
    callback(new Error('至少10位，且包含大小写字母、数字和特殊字符'))
    return
  }
  callback()
}
const validateRole = (rule, value, callback) => {
  if (!value || value.length === 0) {
    callback(new Error('请至少选择一个角色'))
    return
  }
  callback()
}
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleIds: [{ validator: validateRole, trigger: 'change' }],
  password: [{ validator: validatePassword, trigger: 'blur' }]
}

const isCurrentUser = id => String(id) === currentUserId

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserPage(query.value)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.value = initialQuery()
  loadData()
}

const resetForm = () => {
  Object.assign(form, {
    username: '',
    realName: '',
    phone: '',
    email: '',
    deptId: null,
    post: '',
    roleIds: [],
    status: 1,
    password: ''
  })
}

const openCreate = () => {
  editingId.value = null
  resetForm()
  formVisible.value = true
}

const openEdit = async (row) => {
  editingId.value = row.id
  const detail = await getUserDetail(row.id)
  Object.assign(form, {
    username: detail.username || '',
    realName: detail.realName || '',
    phone: detail.phone || '',
    email: detail.email || '',
    deptId: detail.deptId || null,
    post: detail.post || '',
    roleIds: detail.roleIds || [],
    status: detail.status ?? 1,
    password: ''
  })
  formVisible.value = true
}

const buildPayload = () => ({
  username: form.username,
  realName: form.realName,
  phone: form.phone,
  email: form.email,
  deptId: form.deptId,
  post: form.post,
  roleIds: [...form.roleIds],
  status: form.status,
  ...(editingId.value ? {} : { password: form.password })
})

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await updateUser(editingId.value, buildPayload())
      ElMessage.success('用户已更新')
    } else {
      await createUser(buildPayload())
      ElMessage.success('用户已创建')
    }
    formVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const handleResetPwd = async (id) => {
  await ElMessageBox.confirm('确定重置该用户密码？系统将生成一个随机临时密码。', '重置密码', { type: 'warning' })
  const newPassword = await resetUserPassword(id)
  ElMessageBox.alert(`临时密码：${newPassword}`, '密码已重置', { confirmButtonText: '我已记录' })
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
  ElMessage.success('用户已删除')
  loadData()
}

const handleSelectionChange = rows => {
  selectedRows.value = rows.filter(row => !isCurrentUser(row.id))
}
const clearBatchSelection = () => {
  selectedRows.value = []
  tableRef.value?.clearSelection?.()
}

const loadRoles = async () => {
  const res = await getRolePage({ current: 1, size: 500 })
  roleList.value = res.records || []
}

const loadDepts = async () => {
  deptTree.value = await getDeptTree()
}

onMounted(() => {
  loadData()
  loadRoles()
  loadDepts()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}
.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.user-form :deep(.el-form-item) {
  margin-bottom: 18px;
}
</style>
