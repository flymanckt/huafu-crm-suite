<template>
  <div class="profile-tab" v-loading="loading">
    <el-empty v-if="!profile" description="暂无客户画像，点击编辑按钮添加">
      <el-button type="primary" @click="editMode = true">编辑画像</el-button>
    </el-empty>
    <div v-else>
      <!-- 概述展示 -->
      <el-card shadow="never" style="margin-bottom:16px" v-if="profile.overviewManual || profile.overviewAuto">
        <template #header>客户概述</template>
        <div class="overview-text">{{ profile.overviewManual || profile.overviewAuto || '暂无概述' }}</div>
      </el-card>

      <!-- 画像要素卡片 -->
      <el-row :gutter="16" style="margin-bottom:16px">
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>客户阶段</template>
            <div class="profile-item">{{ profile.customerStage || '-' }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>标签</template>
            <div class="profile-item">{{ profile.tags || '-' }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>行业地位</template>
            <div class="profile-item">{{ profile.industryPosition || '-' }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>主要品牌</template>
            <div class="profile-item">{{ profile.mainBrands || '-' }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>纱线用量</template>
            <div class="profile-item">{{ profile.yarnVolumeSummary || '-' }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>主要客户群体</template>
            <div class="profile-item">{{ profile.mainCustomerGroup || '-' }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>机台/产能</template>
            <div class="profile-item">{{ profile.machineSummary || '-' }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <template #header>竞争对手</template>
            <div class="profile-item">{{ profile.competitorSummary || '-' }}</div>
          </el-card>
        </el-col>
      </el-row>

      <div style="text-align:right">
        <el-button @click="editMode = true">编辑画像</el-button>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editMode" title="编辑客户画像" width="600" :close-on-click-modal="false">
      <el-form :model="form" label-width="130px">
        <el-form-item label="客户阶段"><el-input v-model="form.customerStage" placeholder="如：active, potential, inactive" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="form.tags" placeholder="多个标签用逗号分隔" /></el-form-item>
        <el-form-item label="行业地位"><el-input v-model="form.industryPosition" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="主要品牌"><el-input v-model="form.mainBrands" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="纱线用量说明"><el-input v-model="form.yarnVolumeSummary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="主要客户群体"><el-input v-model="form.mainCustomerGroup" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="机台/产能说明"><el-input v-model="form.machineSummary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="竞争对手说明"><el-input v-model="form.competitorSummary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="其他信息"><el-input v-model="form.otherAssets" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="手动概述(覆盖自动)"><el-input v-model="form.overviewManual" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editMode = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { getCustomerProfile, updateCustomerProfile } from '@/api/customer'
import { ElMessage } from 'element-plus'

const props = defineProps({ customerId: { type: Number, default: null } })
const loading = ref(false)
const saving = ref(false)
const editMode = ref(false)
const profile = ref(null)
const form = ref({})

const loadData = async () => {
  if (!props.customerId) return
  loading.value = true
  try {
    profile.value = await getCustomerProfile(props.customerId)
    if (profile.value) {
      form.value = { ...profile.value }
    }
  } catch { profile.value = null }
  finally { loading.value = false }
}

const handleSave = async () => {
  saving.value = true
  try {
    await updateCustomerProfile(props.customerId, form.value)
    ElMessage.success('保存成功')
    editMode.value = false
    await loadData()
  } finally { saving.value = false }
}

// Re-load when customerId becomes available (after parent finishes loadData)
watch(() => props.customerId, (val) => { if (val) loadData() }, { immediate: true })
</script>

<style scoped>
.profile-tab { padding: 8px 0; }
.overview-text { font-size: 14px; line-height: 1.8; color: #333; }
.profile-item { min-height: 20px; font-size: 13px; color: #666; }
</style>
