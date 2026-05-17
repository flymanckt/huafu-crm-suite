<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>部门管理</span>
          <el-button type="primary" @click="openCreate(null)"><el-icon><Plus /></el-icon>新增部门</el-button>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col :span="10">
          <el-card shadow="never" style="background:#fafafa">
            <el-tree ref="treeRef" :data="treeData" node-key="id" :props="{ label: 'deptName', children: 'children' }"
              highlight-current default-expand-all @node-click="handleNodeClick">
              <template #default="{ node, data }">
                <span class="tree-node">
                  <span>{{ data.deptName }}</span>
                  <span class="tree-actions">
                    <el-button link type="primary" size="small" @click.stop="openCreate(data)">新增</el-button>
                    <el-button link type="primary" size="small" @click.stop="openEdit(data)">编辑</el-button>
                    <el-button link type="danger" size="small" @click.stop="handleDelete(data)" :disabled="data.children?.length > 0">删除</el-button>
                  </span>
                </span>
              </template>
            </el-tree>
          </el-card>
        </el-col>
        <el-col :span="14">
          <el-card shadow="never" v-if="selectedNode">
            <template #header><span>部门详情：{{ selectedNode.deptName }}</span></template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="部门名称">{{ selectedNode.deptName }}</el-descriptions-item>
              <el-descriptions-item label="部门编码">{{ selectedNode.deptCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="负责人">{{ selectedNode.managerName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ selectedNode.managerPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="上级部门">{{ selectedNode.parentName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="排序">{{ selectedNode.sortOrder || 0 }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="selectedNode.status === 1 ? 'success' : 'danger'" size="small">
                  {{ selectedNode.status === 1 ? '正常' : '停用' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="描述" :span="2">{{ selectedNode.description || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
          <el-empty v-else description="点击左侧部门树查看详情" />
        </el-col>
      </el-row>
    </el-card>

    <!-- 部门表单弹窗 -->
    <el-dialog v-model="formVisible" :title="editingId ? '编辑部门' : '新增部门'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级部门">
          <el-tree-select v-model="form.parentId" :data="treeData" :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="顶级部门" clearable check-strictly style="width:100%" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="部门名称" />
        </el-form-item>
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model="form.deptCode" placeholder="唯一编码" />
        </el-form-item>
        <el-form-item label="负责人" prop="managerName">
          <el-input v-model="form.managerName" placeholder="部门负责人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="managerPhone">
          <el-input v-model="form.managerPhone" placeholder="负责人电话" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="部门描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getDeptTree, createDept, updateDept, deleteDept } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const treeRef = ref()
const treeData = ref([])
const selectedNode = ref(null)
const formVisible = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ parentId: null, deptName: '', deptCode: '', managerName: '', managerPhone: '', sortOrder: 0, description: '' })
const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }]
}

const loadTree = async () => {
  treeData.value = await getDeptTree()
}

const handleNodeClick = (data) => {
  selectedNode.value = data
}

const openCreate = (parent) => {
  editingId.value = null
  Object.assign(form, { parentId: parent?.id || null, deptName: '', deptCode: '', managerName: '', managerPhone: '', sortOrder: 0, description: '' })
  formVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, {
    parentId: row.parentId, deptName: row.deptName, deptCode: row.deptCode,
    managerName: row.managerName || '', managerPhone: row.managerPhone || '',
    sortOrder: row.sortOrder || 0, description: row.description || ''
  })
  formVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await updateDept(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createDept(form)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    await loadTree()
  } finally { submitting.value = false }
}

const handleDelete = async (data) => {
  if (data.children?.length > 0) {
    ElMessage.warning('该部门存在下级部门，无法删除')
    return
  }
  await ElMessageBox.confirm('确定删除该部门？', '删除', { type: 'warning' })
  await deleteDept(data.id)
  ElMessage.success('删除成功')
  await loadTree()
  if (selectedNode.value?.id === data.id) selectedNode.value = null
}

onMounted(loadTree)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.tree-node { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.tree-actions { opacity: 0; transition: opacity 0.2s; }
.el-tree-node__content:hover .tree-actions { opacity: 1; }
</style>
