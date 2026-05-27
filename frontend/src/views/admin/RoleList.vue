<template>
  <div class="page-container admin-role-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <div>
            <div class="title">角色管理</div>
            <div class="subtitle">按角色配置菜单权限、数据范围和启停状态</div>
          </div>
          <div class="header-actions">
            <el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" />
            <el-button type="primary" @click="openCreate">
              <el-icon><Plus /></el-icon>
              新增角色
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

      <BatchUpdateBar resource="role" :fields="batchFields" :selected-rows="selectedRows" @clear="clearBatchSelection" @done="loadData" />

      <el-table
        ref="tableRef"
        :data="tableData"
        v-loading="loading"
        stripe
        border
        class="data-table"
        max-height="calc(100vh - 310px)"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="46" fixed />
        <el-table-column v-if="columnVisible('roleName')" prop="roleName" label="角色名称" min-width="150" />
        <el-table-column v-if="columnVisible('roleKey')" prop="roleKey" label="角色标识" min-width="150" />
        <el-table-column v-if="columnVisible('dataScope')" prop="dataScope" label="数据范围" width="140">
          <template #default="{ row }">{{ scopeMap[row.dataScope] || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('status')" prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="columnVisible('menuIds')" prop="menuIds" label="权限项" width="100">
          <template #default="{ row }">{{ row.menuIds?.length || 0 }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('userCount')" prop="userCount" label="关联用户" width="100" />
        <el-table-column v-if="columnVisible('description')" prop="description" label="描述" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="success" @click="openMenus(row)">权限</el-button>
            <el-popconfirm title="确定删除该角色？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger" :disabled="row.roleKey === 'ROLE_ADMIN'">删除</el-button>
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

    <el-dialog v-model="formVisible" :title="editingId ? '编辑角色' : '新增角色'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="例如：销售经理" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKey">
          <el-input v-model="form.roleKey" placeholder="例如：ROLE_SALES_MANAGER" :disabled="!!editingId" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio-button :label="1">启用</el-radio-button>
            <el-radio-button :label="2">停用</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数据范围" prop="dataScope">
          <DictSelect v-model="form.dataScope" dict-code="data_scope" value-type="number" style="width:100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="256" show-word-limit placeholder="角色职责说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuVisible" title="分配菜单权限" width="680px" destroy-on-close>
      <div class="permission-dialog">
        <div class="permission-help">勾选角色可访问的功能。父级半选也会一并保存，便于菜单正确展示。</div>
        <el-tree
          ref="menuTreeRef"
          :data="menuTree"
          node-key="id"
          :props="{ label: 'menuName', children: 'children' }"
          show-checkbox
          default-expand-all
          class="permission-tree"
        >
          <template #default="{ data }">
            <span class="tree-node">
              <span>{{ data.menuName }}</span>
              <el-tag v-if="data.permission && !data.children?.length" size="small" effect="plain">{{ data.permission }}</el-tag>
            </span>
          </template>
        </el-tree>
      </div>
      <template #footer>
        <el-button @click="menuVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMenus" :loading="menuSaving">保存权限</el-button>
      </template>
    </el-dialog>

    <ColumnConfigDrawer :page-code="pageCode" :default-columns="defaultColumns" />
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { Plus, Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  createRole,
  deleteRole,
  getMenuTree,
  getRoleDetail,
  getRoleMenus,
  getRolePage,
  updateRole,
  updateRoleMenus
} from '@/api/admin'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'

const scopeMap = { 1: '全部数据', 2: '本部门及下级', 3: '本部门', 4: '仅本人' }
const pageCode = 'admin-role-list'
const initialQuery = () => ({ current: 1, size: 20, roleName: '', roleKey: '', status: null, dataScope: null })

const loading = ref(false)
const submitting = ref(false)
const menuSaving = ref(false)
const tableRef = ref()
const formRef = ref()
const menuTreeRef = ref()
const tableData = ref([])
const total = ref(0)
const selectedRows = ref([])
const query = ref(initialQuery())
const formVisible = ref(false)
const menuVisible = ref(false)
const editingId = ref(null)
const menuEditingId = ref(null)
const menuTree = ref([])

const filterFields = [
  { key: 'roleName', label: '角色名称', placement: 'default' },
  { key: 'roleKey', label: '角色标识', placement: 'default' },
  { key: 'status', label: '状态', type: 'dict', dictCode: 'dict_status', valueType: 'number', placement: 'more' },
  { key: 'dataScope', label: '数据范围', type: 'dict', dictCode: 'data_scope', valueType: 'number', placement: 'more' }
]
const defaultColumns = [
  { key: 'roleName', label: '角色名称', width: 150, visible: true, fixed: true },
  { key: 'roleKey', label: '角色标识', width: 150, visible: true },
  { key: 'dataScope', label: '数据范围', width: 140, visible: true },
  { key: 'status', label: '状态', width: 90, visible: true },
  { key: 'menuIds', label: '权限项', width: 100, visible: true },
  { key: 'userCount', label: '关联用户', width: 100, visible: true },
  { key: 'description', label: '描述', width: 220, visible: true }
]
const batchFields = [
  { key: 'status', label: '状态', dictCode: 'dict_status', valueType: 'number' },
  { key: 'dataScope', label: '数据范围', dictCode: 'data_scope', valueType: 'number' },
  { key: 'description', label: '描述' }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false

const form = reactive({
  roleName: '',
  roleKey: '',
  description: '',
  status: 1,
  dataScope: 4
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [
    { required: true, message: '请输入角色标识', trigger: 'blur' },
    { pattern: /^ROLE_[A-Z0-9_]+$/, message: '建议使用 ROLE_ 开头的大写标识', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRolePage(query.value)
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
  Object.assign(form, { roleName: '', roleKey: '', description: '', status: 1, dataScope: 4 })
}

const openCreate = () => {
  editingId.value = null
  resetForm()
  formVisible.value = true
}

const openEdit = async (row) => {
  editingId.value = row.id
  const detail = await getRoleDetail(row.id)
  Object.assign(form, {
    roleName: detail.roleName || '',
    roleKey: detail.roleKey || '',
    description: detail.description || '',
    status: detail.status ?? 1,
    dataScope: detail.dataScope ?? 4
  })
  formVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = {
      roleName: form.roleName,
      roleKey: form.roleKey,
      description: form.description,
      status: form.status,
      dataScope: form.dataScope
    }
    if (editingId.value) {
      await updateRole(editingId.value, payload)
      ElMessage.success('角色已更新')
    } else {
      await createRole({ ...payload, menuIds: [] })
      ElMessage.success('角色已创建，请继续分配权限')
    }
    formVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const loadMenuTree = async () => {
  if (menuTree.value.length) return
  menuTree.value = await getMenuTree()
}

const openMenus = async (row) => {
  menuEditingId.value = row.id
  await loadMenuTree()
  menuVisible.value = true
  const existing = await getRoleMenus(row.id)
  await nextTick()
  menuTreeRef.value?.setCheckedKeys(existing || [], false)
}

const handleSaveMenus = async () => {
  menuSaving.value = true
  try {
    const checked = menuTreeRef.value?.getCheckedKeys(false) || []
    const halfChecked = menuTreeRef.value?.getHalfCheckedKeys() || []
    const menuIds = Array.from(new Set([...checked, ...halfChecked]))
    await updateRoleMenus(menuEditingId.value, menuIds)
    ElMessage.success('菜单权限已保存')
    menuVisible.value = false
    loadData()
  } finally {
    menuSaving.value = false
  }
}

const handleDelete = async (id) => {
  await deleteRole(id)
  ElMessage.success('角色已删除')
  loadData()
}

const handleSelectionChange = rows => { selectedRows.value = rows }
const clearBatchSelection = () => {
  selectedRows.value = []
  tableRef.value?.clearSelection?.()
}

onMounted(loadData)
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
.permission-dialog {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fff;
}
.permission-help {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  color: #606266;
  font-size: 13px;
}
.permission-tree {
  max-height: 440px;
  overflow-y: auto;
  padding: 10px 12px;
}
.tree-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
</style>
