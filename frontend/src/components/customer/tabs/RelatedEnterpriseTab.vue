<template>
  <div class="related-enterprise-tab">
    <el-card shadow="never">
      <template #header>
        <span>捆绑客户列表</span>
      </template>
      <div class="enterprise-list" v-if="relatedList.length">
        <div v-for="item in relatedList" :key="item.id" class="enterprise-item" @click="goToDetail(item.id)">
          <div class="enterprise-info">
            <el-icon class="enterprise-icon"><OfficeBuilding /></el-icon>
            <div class="enterprise-detail">
              <div class="enterprise-name">{{ item.customerName }}</div>
              <div class="enterprise-meta">
                <span>编码：{{ item.customerCode }}</span>
                <DictTag v-if="hasValue(item.level)" dict-code="customer_level" :value="String(item.level)" size="small" />
                <DictTag v-if="hasValue(item.status)" dict-code="customer_status" :value="String(item.status)" size="small" />
              </div>
            </div>
          </div>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
      </div>
      <el-empty v-else description="暂无关联企业" :image-size="60" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { OfficeBuilding, ArrowRight } from '@element-plus/icons-vue'
import { getRelatedEnterpriseListV1 } from '@/api/customer'
import DictTag from '@/components/Dict/DictTag.vue'

const props = defineProps({
  customerId: { type: [String, Number], required: true }
})

const router = useRouter()
const relatedList = ref([])
const hasValue = (value) => value !== null && value !== undefined && value !== ''

const loadData = async () => {
  if (!props.customerId) return
  const rows = await getRelatedEnterpriseListV1(props.customerId)
  relatedList.value = rows.map(row => ({
    ...row,
    id: row.childCustomerId || row.bundleCustomerId || row.id,
    customerName: row.childCustomerName || `客户 ${row.childCustomerId || row.bundleCustomerId || '-'}`,
    customerCode: row.childCustomerCode || '-'
  }))
}

const goToDetail = (id) => {
  if (!id) return
  router.push('/customer/detail/' + id)
}

onMounted(loadData)
watch(() => props.customerId, loadData)
</script>

<style scoped>
.related-enterprise-tab { padding: 8px 0; }

.enterprise-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.enterprise-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.enterprise-item:hover {
  background-color: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary-light-5);
}

.enterprise-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.enterprise-icon {
  font-size: 32px;
  color: var(--el-color-primary);
}

.enterprise-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.enterprise-name {
  font-weight: 600;
  font-size: 14px;
}

.enterprise-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #666;
}

.arrow-icon {
  color: #999;
  transition: transform 0.2s;
}

.enterprise-item:hover .arrow-icon {
  transform: translateX(4px);
  color: var(--el-color-primary);
}
</style>
