<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <div class="header-actions">
            <el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" />
            <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新增角色</el-button>
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
      <el-table ref="tableRef" :data="tableData" v-loading="loading" stripe border class="data-table" max-height="calc(100vh - 300px)" row-key="id" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" fixed />
        <el-table-column v-if="columnVisible('roleName')" prop="roleName" label="角色名称" width="160" />
        <el-table-column v-if="columnVisible('roleCode')" prop="roleCode" label="角色编码" width="140" />
        <el-table-column v-if="columnVisible('description')" prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('dataScope')" prop="dataScope" label="数据范围" width="100">
          <template #default="{ row }">{{ scopeMap[row.dataScope] || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="columnVisible('status')" prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="columnVisible('userCount')" prop="userCount" label="关联用户" width="100" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="success" @click="openMenus(row)">分配菜单</el-button>
            <el-popconfirm title="确定删除该角色？" @confirm="handleDelete(row.id)">
              <template #reference><el-button link type="danger">删除</el-button></template>
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

    <!-- 角色表单弹窗 -->
    <el-dialog v-model="formVisible" :title="editingId ? '编辑角色' : '新增角色'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="唯一编码，如 admin" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="角色描述" />
        </el-form-item>
        <el-form-item label="数据范围" prop="dataScope">
          <DictSelect v-model="form.dataScope" dict-code="data_scope" value-type="number" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 菜单分配弹窗 -->
    <el-dialog v-model="menuVisible" title="分配菜单权限" width="400px" destroy-on-close>
      <el-tree ref="menuTreeRef" :data="menuTree" node-key="id" :props="{ label: 'menuName', children: 'children' }"
        show-checkbox default-expand-all style="max-height:400px;overflow-y:auto" />
      <template #footer>
        <el-button @click="menuVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMenus" :loading="menuSaving">保存</el-button>
      </template>
    </el-dialog>
    <ColumnConfigDrawer :page-code="pageCode" :default-columns="defaultColumns" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Setting } from '@element-plus/icons-vue'
import { getRolePage, createRole, updateRole, deleteRole, getRoleMenus, updateRoleMenus } from '@/api/admin'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { ElMessage } from 'element-plus'

const scopeMap = { 1: '全部数据', 2: '本部门及下级', 3: '本部门', 4: '仅本人' }
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const tableRef = ref()
const selectedRows = ref([])
const pageCode = 'admin-role-list'
const initialQuery = () => ({ current: 1, size: 20, roleName: '', roleCode: '', status: null, dataScope: null })
const query = ref(initialQuery())
const filterFields = [
  { key: 'roleName', label: '角色名称', placement: 'default' },
  { key: 'roleCode', label: '角色编码', placement: 'default' },
  { key: 'status', label: '状态', type: 'dict', dictCode: 'dict_status', valueType: 'number', placement: 'more' },
  { key: 'dataScope', label: '数据范围', type: 'dict', dictCode: 'data_scope', valueType: 'number', placement: 'more' }
]
const defaultColumns = [
  { key: 'roleName', label: '角色名称', width: 160, visible: true, fixed: true },
  { key: 'roleCode', label: '角色编码', width: 140, visible: true },
  { key: 'description', label: '描述', width: 200, visible: true },
  { key: 'dataScope', label: '数据范围', width: 100, visible: true },
  { key: 'status', label: '状态', width: 80, visible: true },
  { key: 'userCount', label: '关联用户', width: 100, visible: true }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false
const formVisible = ref(false)
const menuVisible = ref(false)
const editingId = ref(null)
const menuEditingId = ref(null)
const submitting = ref(false)
const menuSaving = ref(false)
const formRef = ref()
const menuTreeRef = ref()
const form = reactive({ roleName: '', roleCode: '', description: '', dataScope: 1 })
const batchFields = [
  { key: 'description', label: '描述' }
]
const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const menuTree = ref([])

// 简化版菜单树数据（实际应从后端加载）
const defaultMenuTree = [
  { id: 1, menuName: '工作台', children: [] },
  { id: 2, menuName: '客户管理', children: [
    { id: 21, menuName: '客户列表' }, { id: 22, menuName: '公海客户' }
  ]},
  { id: 3, menuName: '商机管理', children: [
    { id: 31, menuName: '线索商情' }, { id: 32, menuName: '商机列表' }, { id: 33, menuName: '丢单记录' }
  ]},
  { id: 4, menuName: '目标管理', children: [] },
  { id: 5, menuName: '勤力度', children: [
    { id: 51, menuName: '拜访记录' }, { id: 52, menuName: '日报列表' }, { id: 53, menuName: '勤力度评分' }
  ]},
  { id: 6, menuName: '报价管理', children: [] },
  { id: 7, menuName: 'AI解析', children: [] },
  { id: 8, menuName: '系统管理', children: [
    { id: 81, menuName: '用户管理' }, { id: 82, menuName: '角色管理' }, { id: 83, menuName: '部门管理' }
  ]}
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRolePage(query.value)
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
  Object.assign(form, { roleName: '', roleCode: '', description: '', dataScope: 1 })
  formVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, { roleName: row.roleName, roleCode: row.roleCode, description: row.description || '', dataScope: row.dataScope || 1 })
  formVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await updateRole(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createRole(form)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    loadData()
  } finally { submitting.value = false }
}

const openMenus = async (row) => {
  menuEditingId.value = row.id
  menuTree.value = defaultMenuTree
  menuVisible.value = true
  // 加载已有菜单
  try {
    const existing = await getRoleMenus(row.id)
    if (existing && existing.length > 0) {
      setTimeout(() => {
        menuTreeRef.value?.setCheckedKeys(existing)
      }, 100)
    }
  } catch (e) { /* ignore */ }
}

const handleSaveMenus = async () => {
  const checked = menuTreeRef.value?.getCheckedKeys(true) || []
  await updateRoleMenus(menuEditingId.value, checked)
  ElMessage.success('菜单权限保存成功')
  menuVisible.value = false
}

const handleDelete = async (id) => {
  await deleteRole(id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSelectionChange = (rows) => { selectedRows.value = rows }
const clearBatchSelection = () => { selectedRows.value = []; tableRef.value?.clearSelection?.() }

onMounted(loadData)
</script>

<style scoped>
.card-header, .header-actions { display: flex; justify-content: space-between; align-items: center; }
.header-actions { gap: 8px; }
</style>
