<template>
  <div class="contact-tab">
    <div class="tab-toolbar">
      <el-button type="primary" size="small" @click="showAddDialog">新增联系人</el-button>
    </div>

    <!-- 树形表格：无 children 的叶子节点才显示操作列 -->
    <el-table :data="treeData" row-key="id" stripe size="small" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" default-expand-all>
      <el-table-column prop="contactName" label="姓名" width="180">
        <template #default="{ row }">
          <div class="contact-name-cell">
            <span :class="{ 'main-contact-name': row.isMain === 1 }">
              {{ row.contactName }}
              <el-tag v-if="row.isMain === 1" type="danger" size="small" style="margin-left:4px">主</el-tag>
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="position" label="职务" width="120" />
      <el-table-column prop="phone" label="手机" width="130" />
      <el-table-column prop="telephone" label="电话" width="130" />
      <el-table-column prop="email" label="EMAIL" min-width="160" />
      <el-table-column prop="parentContactId" label="上级" width="120">
        <template #default="{ row }">
          {{ getParentName(row.parentContactId) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm :title="row.isMain === 1 ? '主联系人不能直接删除，请先更换' : '确定删除？'" @confirm="handleDelete(row)">
            <template #reference><el-button link type="danger" size="small">删除</el-button></template>
          </el-popconfirm>
          <el-button v-if="row.isMain !== 1 && !row.parentContactId" link type="warning" size="small" @click="handleSetMain(row)">设主</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingContact ? '编辑联系人' : '新增联系人'" width="560">
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.contactName" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="职务"><el-input v-model="form.position" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="电话"><el-input v-model="form.telephone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="EMAIL"><el-input v-model="form.email" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="上级联系人">
          <el-select v-model="form.parentContactId" placeholder="选择上级（选填）" clearable style="width:100%">
            <el-option
              v-for="c in availableParentOptions"
              :key="c.id"
              :label="c.contactName + (c.position ? '（' + c.position + '）' : '')"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="是否主联系人">
          <el-switch v-model="form.isMain" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { createContactV1, updateContactV1, deleteContactV1 } from '@/api/customer'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  customerId: { type: [String, Number], required: true },
  contacts: { type: Array, default: () => [] }
})
const emit = defineEmits(['updated'])

const dialogVisible = ref(false)
const saving = ref(false)
const editingContact = ref(null)
const form = ref({
  contactName: '',
  position: '',
  phone: '',
  telephone: '',
  email: '',
  isMain: 0,
  parentContactId: null
})

// 构建树形结构
const treeData = computed(() => {
  const map = {}
  const roots = []
  // 用 id 做 key
  const list = props.contacts || []
  list.forEach(c => {
    map[c.id] = { ...c, children: [] }
  })
  list.forEach(c => {
    if (c.parentContactId && map[c.parentContactId]) {
      map[c.parentContactId].children.push(map[c.id])
    } else {
      roots.push(map[c.id])
    }
  })
  return roots
})

// 上级选项：排除自己及其后代
const availableParentOptions = computed(() => {
  const editingId = editingContact.value?.id
  if (!editingId) return props.contacts || []
  const descendants = new Set()
  const collectDescendants = (id) => {
    ;(props.contacts || []).filter(c => c.parentContactId === id).forEach(c => {
      descendants.add(c.id)
      collectDescendants(c.id)
    })
  }
  collectDescendants(editingId)
  return (props.contacts || []).filter(c => c.id !== editingId && !descendants.has(c.id))
})

const getParentName = (parentId) => {
  if (!parentId) return '-'
  const c = props.contacts.find(c => c.id === parentId)
  return c ? c.contactName : '-'
}

// 主联系人置顶（树根级别）
const sortedRoots = computed(() => {
  const roots = treeData.value
  const main = roots.filter(c => c.isMain === 1)
  const others = roots.filter(c => c.isMain !== 1)
  return [...main, ...others]
})

const showAddDialog = () => {
  editingContact.value = null
  form.value = { contactName: '', position: '', phone: '', telephone: '', email: '', isMain: 0, parentContactId: null }
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingContact.value = row
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSetMain = async (row) => {
  try {
    await ElMessageBox.confirm('确定将 ' + row.contactName + ' 设为主联系人？', '提示')
    await updateContactV1(props.customerId, row.id, { ...row, isMain: 1 })
    ElMessage.success('设置成功')
    emit('updated')
  } catch (e) {
    // 用户取消
  }
}

const handleSave = async () => {
  if (!form.value.contactName) {
    ElMessage.warning('请填写姓名')
    return
  }
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
    emit('updated')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  if (row.isMain === 1) {
    ElMessage.warning('主联系人不能直接删除，请先更换')
    return
  }
  await deleteContactV1(props.customerId, row.id)
  ElMessage.success('删除成功')
  emit('updated')
}
</script>

<style scoped>
.contact-tab { padding: 8px 0; }
.tab-toolbar { margin-bottom: 12px; text-align: right; }
.contact-name-cell { display: flex; align-items: center; }
.main-contact-name { font-weight: 600; color: var(--el-color-danger); }
</style>
