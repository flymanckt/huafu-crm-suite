<template>
  <div class="contact-tab">
    <div class="tab-toolbar">
      <el-button type="primary" size="small" @click="showAddDialog">新增联系人</el-button>
    </div>

    <!-- 角色 Tab 分类 -->
    <el-tabs v-model="activeRole" class="role-tabs" v-if="contacts.length">
      <el-tab-pane label="全部" name="all">
        <el-table :data="contacts" stripe size="small">
          <el-table-column prop="contactName" label="姓名" width="100" />
          <el-table-column prop="phone" label="手机" width="130" />
          <el-table-column prop="telephone" label="座机" width="130" />
          <el-table-column prop="position" label="职位" width="120" />
          <el-table-column prop="roleType" label="角色" width="80">
            <template #default="{ row }">{{ roleTypeMap[row.roleType] || '-' }}</template>
          </el-table-column>
          <el-table-column prop="decisionLevel" label="决策权" width="80">
            <template #default="{ row }">{{ decisionMap[row.decisionLevel] || '-' }}</template>
          </el-table-column>
          <el-table-column prop="isMain" label="主联系人" width="80">
            <template #default="{ row }">{{ row.isMain === 1 ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column prop="isActive" label="在职" width="70">
            <template #default="{ row }">{{ row.isActive === 1 ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column prop="parentContactId" label="上级" width="100">
            <template #default="{ row }">{{ getParentName(row.parentContactId) }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
                <template #reference><el-button link type="danger" size="small">删除</el-button></template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane v-for="role in roleList" :key="role.value" :label="role.label + '(' + getRoleCount(role.value) + ')'" :name="role.value">
        <el-table :data="getContactsByRole(role.value)" stripe size="small">
          <el-table-column prop="contactName" label="姓名" width="100" />
          <el-table-column prop="phone" label="手机" width="130" />
          <el-table-column prop="telephone" label="座机" width="130" />
          <el-table-column prop="position" label="职位" width="120" />
          <el-table-column prop="decisionLevel" label="决策权" width="80">
            <template #default="{ row }">{{ decisionMap[row.decisionLevel] || '-' }}</template>
          </el-table-column>
          <el-table-column prop="isMain" label="主联系人" width="80">
            <template #default="{ row }">{{ row.isMain === 1 ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column prop="isActive" label="在职" width="70">
            <template #default="{ row }">{{ row.isActive === 1 ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
                <template #reference><el-button link type="danger" size="small">删除</el-button></template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 联系人架构图 -->
    <el-card shadow="never" style="margin-top:16px" v-if="contacts.length">
      <template #header>
        <span>联系人架构图</span>
        <el-button link type="primary" size="small" style="float:right" @click="showOrgChart = !showOrgChart">
          {{ showOrgChart ? '收起' : '展开' }}
        </el-button>
      </template>
      <el-tree v-if="showOrgChart" :data="orgTreeData" :props="{ label: 'label', children: 'children' }" default-expand-all node-key="id" style="max-height:300px;overflow:auto" />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingContact ? '编辑联系人' : '新增联系人'" width="560">
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名" required><el-input v-model="form.contactName" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="座机"><el-input v-model="form.telephone" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="微信"><el-input v-model="form.wechat" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="职位"><el-input v-model="form.position" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="角色">
            <DictSelect v-model="form.roleType" dict-code="contact_role" value-type="number" clearable />
          </el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="决策权">
            <DictSelect v-model="form.decisionLevel" dict-code="contact_decision_level" value-type="number" clearable />
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="是否主联系人">
            <DictSelect v-model="form.isMain" dict-code="yes_no" value-type="number" />
          </el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="上级联系人">
            <el-select v-model="form.parentContactId" clearable placeholder="无">
              <el-option v-for="c in contacts.filter(c => c.id !== editingContact?.id)" :key="c.id" :label="c.contactName" :value="c.id" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="是否在职">
            <DictSelect v-model="form.isActive" dict-code="contact_status" value-type="number" />
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { getContactListV1, createContactV1, updateContactV1, deleteContactV1 } from '@/api/customer'
import { ElMessage } from 'element-plus'
import DictSelect from '@/components/Dict/DictSelect.vue'

const props = defineProps({ customerId: { type: Number, default: null } })
const contacts = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const editingContact = ref(null)
const activeRole = ref('all')
const showOrgChart = ref(false)
const form = ref({ contactName: '', phone: '', telephone: '', wechat: '', email: '',
  position: '', roleType: null, decisionLevel: null, isMain: 0, parentContactId: null, isActive: 1, remark: '' })

const roleList = [
  { label: '老板', value: 1 }, { label: '采购', value: 2 }, { label: '营业', value: 3 },
  { label: '开发', value: 4 }, { label: '生产', value: 5 }, { label: '财务', value: 6 }, { label: '品质', value: 7 }
]
const roleTypeMap = { 1: '老板', 2: '采购', 3: '营业', 4: '开发', 5: '生产', 6: '财务', 7: '品质' }
const decisionMap = { 1: '拍板', 2: '否决', 3: '参与', 4: '知情' }

const getContactsByRole = (role) => contacts.value.filter(c => c.roleType === role)
const getRoleCount = (role) => contacts.value.filter(c => c.roleType === role).length

const getParentName = (parentId) => {
  if (!parentId) return '-'
  const parent = contacts.value.find(c => c.id === parentId)
  return parent ? parent.contactName : '-'
}

// 构建树形数据
const orgTreeData = computed(() => {
  const map = {}
  const roots = []
  contacts.value.forEach(c => { map[c.id] = { id: c.id, label: `${c.contactName}(${roleTypeMap[c.roleType] || ''})`, children: [] } })
  contacts.value.forEach(c => {
    if (c.parentContactId && map[c.parentContactId]) {
      map[c.parentContactId].children.push(map[c.id])
    } else {
      roots.push(map[c.id])
    }
  })
  return roots
})

const showAddDialog = () => {
  editingContact.value = null
  form.value = { contactName: '', phone: '', telephone: '', wechat: '', email: '', position: '',
    roleType: null, decisionLevel: null, isMain: 0, parentContactId: null, isActive: 1, remark: '' }
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingContact.value = row
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.contactName) { ElMessage.warning('请填写姓名'); return }
  saving.value = true
  try {
    const data = { ...form.value, customerId: props.customerId }
    if (editingContact.value) {
      await updateContactV1(props.customerId, editingContact.value.id, data)
    } else {
      await createContactV1(props.customerId, data)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadData()
  } finally { saving.value = false }
}

const handleDelete = async (id) => {
  await deleteContactV1(props.customerId, id)
  ElMessage.success('删除成功')
  await loadData()
}

const loadData = async () => {
  if (!props.customerId) return
  contacts.value = await getContactListV1(props.customerId)
}

onMounted(loadData)
watch(() => props.customerId, (val) => { if (val) loadData() })
</script>

<style scoped>
.contact-tab { padding: 8px 0; }
.tab-toolbar { margin-bottom: 12px; text-align: right; }
.role-tabs { margin-bottom: 12px; }
</style>
