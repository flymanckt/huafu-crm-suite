<template>
  <div class="transfer-tab">
    <div class="tab-toolbar">
      <el-button type="primary" size="small" @click="showAddDialog">新建交接</el-button>
    </div>

    <el-table :data="transfers" stripe size="small" v-if="transfers.length">
      <el-table-column prop="transferDate" label="交接日期" width="120" formatter="{y}-{m}-{d}" />
      <el-table-column prop="fromUserId" label="交接人" width="100" />
      <el-table-column label="→" width="40" />
      <el-table-column prop="toUserId" label="接交人" width="100" />
      <el-table-column prop="supervisorUserId" label="督交人" width="100" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">{{ statusMap[row.status] || '-' }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="交接原因" show-overflow-tooltip />
      <el-table-column prop="businessSummary" label="业务说明" show-overflow-tooltip />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" link type="primary" size="small" @click="handleConfirm(row.id)">确认</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference><el-button link type="danger" size="small">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-else description="暂无交接记录" />

    <!-- 新建交接对话框 -->
    <el-dialog v-model="dialogVisible" title="新建交接记录" width="600" :close-on-click-modal="false">
      <el-form :model="form" label-width="120px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="交接日期"><el-date-picker v-model="form.transferDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="督交人ID"><el-input-number v-model="form.supervisorUserId" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="业务综合说明"><el-input v-model="form.businessSummary" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="联系人资料快照"><el-input v-model="form.contactInfoSnapshot" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="应收款情况"><el-input v-model="form.receivableInfo" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="目前问题/建议"><el-input v-model="form.cooperationIssues" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="未来合作机会"><el-input v-model="form.futureOpportunities" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="交接原因"><el-input v-model="form.reason" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">提交交接</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { getCustomerTransferList, createCustomerTransfer, confirmCustomerTransfer } from '@/api/customer'
import { ElMessage } from 'element-plus'

const props = defineProps({ customerId: { type: Number, default: null } })
const transfers = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const form = ref({ transferDate: '', fromUserId: null, toUserId: null, supervisorUserId: null,
  businessSummary: '', contactInfoSnapshot: '', receivableInfo: '', cooperationIssues: '', futureOpportunities: '', reason: '' })

const statusMap = { 1: '待确认', 2: '已确认', 3: '已完成' }

const loadData = async () => {
  if (!props.customerId) return
  transfers.value = await getCustomerTransferList(props.customerId)
}

const showAddDialog = () => {
  form.value = { transferDate: '', fromUserId: null, toUserId: null, supervisorUserId: null,
    businessSummary: '', contactInfoSnapshot: '', receivableInfo: '', cooperationIssues: '', futureOpportunities: '', reason: '' }
  dialogVisible.value = true
}

const handleSave = async () => {
  saving.value = true
  try {
    await createCustomerTransfer(props.customerId, form.value)
    ElMessage.success('交接记录已创建')
    dialogVisible.value = false
    await loadData()
  } finally { saving.value = false }
}

const handleConfirm = async (id) => {
  await confirmCustomerTransfer(props.customerId, id)
  ElMessage.success('已确认交接')
  await loadData()
}

const handleDelete = async (id) => {
  // 简单通过确认状态变更模拟删除（实际应调删除接口）
  ElMessage.info('请联系管理员删除')
}

onMounted(loadData)
watch(() => props.customerId, (val) => { if (val) loadData() })
</script>

<style scoped>
.transfer-tab { padding: 8px 0; }
.tab-toolbar { margin-bottom: 12px; text-align: right; }
</style>
