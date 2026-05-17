<template>
  <div class="address-tab">
    <div class="tab-toolbar">
      <el-button type="primary" size="small" @click="showAddDialog">新增地址</el-button>
    </div>

    <el-row :gutter="16" v-if="addresses.length">
      <el-col :span="8" v-for="addr in addresses" :key="addr.id">
        <el-card shadow="never" class="address-card">
          <template #header>
            <div class="card-title">
              <span>{{ addressTypeMap[addr.addressType] || '地址' }}</span>
              <span class="tag-group">
                <el-tag size="small" v-if="addr.isDefault === 1">默认</el-tag>
                <el-tag size="small" :type="addr.locationVerified === 1 ? 'success' : 'warning'">
                  {{ addr.locationVerified === 1 ? '已定位' : '未定位' }}
                </el-tag>
              </span>
            </div>
          </template>
          <div class="addr-item"><b>联系人：</b>{{ addr.contactName || '-' }}</div>
          <div class="addr-item"><b>电话：</b>{{ addr.phone || '-' }}</div>
          <div class="addr-item"><b>行政区：</b>{{ [addr.country, addr.province, addr.city, addr.district].filter(Boolean).join(' / ') || '-' }}</div>
          <div class="addr-item"><b>详细地址：</b>{{ addr.fullAddress || addr.addressDetail || '-' }}</div>
          <div class="addr-item" v-if="addr.longitude && addr.latitude"><b>坐标：</b>{{ addr.longitude }}, {{ addr.latitude }}</div>
          <div class="addr-item"><b>打卡半径：</b>{{ addr.checkinRadiusMeters || 500 }} 米</div>
          <div class="card-actions">
            <el-button link type="primary" size="small" @click="openEdit(addr)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(addr.id)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-else description="暂无地址，点击新增按钮添加" />

    <el-dialog v-model="dialogVisible" :title="editingAddr ? '编辑地址' : '新增地址'" width="760">
      <el-form :model="form" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="地址类型" required>
              <DictSelect v-model="form.addressType" dict-code="address_type" value-type="number" placeholder="选择类型" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设为默认">
              <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="国家"><el-input v-model="form.country" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="省"><el-input v-model="form.province" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="市"><el-input v-model="form.city" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="区"><el-input v-model="form.district" /></el-form-item></el-col>
          <el-col :span="24">
            <el-form-item label="详细地址" required>
              <el-input v-model="form.addressDetail" placeholder="输入门牌、园区、厂区等详细地址">
                <template #append>
                  <el-button @click="verifyTypedAddress" :loading="verifying">定位校验</el-button>
                  <el-button @click="pickerVisible = true">地图选择</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="完整地址">
              <el-input v-model="form.fullAddress" placeholder="定位后自动生成，可人工修正" />
            </el-form-item>
          </el-col>
          <el-col :span="8"><el-form-item label="经度"><el-input v-model="form.longitude" disabled /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="纬度"><el-input v-model="form.latitude" disabled /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="adcode"><el-input v-model="form.amapAdcode" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="POI名称"><el-input v-model="form.amapPoiName" disabled /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="打卡半径">
              <el-input-number v-model="form.checkinRadiusMeters" :min="50" :max="5000" :step="50" controls-position="right" />
              <span class="meter-unit">米</span>
            </el-form-item>
          </el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.addressRemark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <AddressPicker
      v-model="pickerVisible"
      :initial-address="form.fullAddress || form.addressDetail"
      @select="handleAddressSelected"
    />
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { getCustomerAddressList, createCustomerAddress, updateCustomerAddress, deleteCustomerAddress } from '@/api/customer'
import AddressPicker from '@/components/common/AddressPicker.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import { geocodeAddress } from '@/utils/amap'
import { ElMessage } from 'element-plus'

const props = defineProps({ customerId: { type: [Number, String], default: null } })
const addresses = ref([])
const dialogVisible = ref(false)
const pickerVisible = ref(false)
const saving = ref(false)
const verifying = ref(false)
const editingAddr = ref(null)

const emptyForm = () => ({
  addressType: 1,
  contactName: '',
  phone: '',
  country: '中国',
  province: '',
  city: '',
  district: '',
  addressDetail: '',
  fullAddress: '',
  longitude: null,
  latitude: null,
  amapPoiId: '',
  amapPoiName: '',
  amapAdcode: '',
  amapLevel: '',
  locationSource: 'MANUAL',
  locationVerified: 0,
  checkinRadiusMeters: 500,
  addressRemark: '',
  isDefault: 0
})

const form = ref(emptyForm())

const addressTypeMap = { 1: '注册地', 2: '工厂', 3: '办事处', 4: '仓库', 5: '收货地址', 9: '其他' }

const loadData = async () => {
  if (!props.customerId) return
  addresses.value = await getCustomerAddressList(props.customerId)
}

const showAddDialog = () => {
  editingAddr.value = null
  form.value = emptyForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingAddr.value = row
  form.value = { ...emptyForm(), ...row }
  dialogVisible.value = true
}

const buildSearchAddress = () => {
  return form.value.fullAddress || [form.value.province, form.value.city, form.value.district, form.value.addressDetail].filter(Boolean).join('')
}

const applyLocatedAddress = (data) => {
  form.value.country = data.country || form.value.country || '中国'
  form.value.province = data.province || form.value.province
  form.value.city = data.city || form.value.city
  form.value.district = data.district || form.value.district
  form.value.fullAddress = data.address || form.value.fullAddress || buildSearchAddress()
  form.value.longitude = data.lng
  form.value.latitude = data.lat
  form.value.amapPoiId = data.poiId || ''
  form.value.amapPoiName = data.poiName || ''
  form.value.amapAdcode = data.adcode || ''
  form.value.amapLevel = data.level || data.poiType || ''
  form.value.locationSource = data.source || 'AMAP'
  form.value.locationVerified = 1
}

const stripRegion = (fullAddress, region) => {
  let detail = fullAddress || ''
  ;[region.country, region.province, region.city, region.district].filter(Boolean).forEach(part => {
    if (detail.startsWith(part)) detail = detail.slice(part.length)
  })
  return detail || form.value.addressDetail
}

const verifyTypedAddress = async () => {
  const address = buildSearchAddress()
  if (!address) {
    ElMessage.warning('请先输入详细地址')
    return
  }
  verifying.value = true
  try {
    const result = await geocodeAddress(address, form.value.city || '')
    applyLocatedAddress({ ...result, source: 'AMAP_GEOCODE' })
    ElMessage.success('定位校验成功')
  } finally {
    verifying.value = false
  }
}

const handleAddressSelected = (data) => {
  if (data?.address) {
    form.value.addressDetail = stripRegion(data.address, data)
  }
  applyLocatedAddress(data)
  ElMessage.success('已选择地图地址')
}

const handleSave = async () => {
  saving.value = true
  try {
    if (editingAddr.value) {
      await updateCustomerAddress(props.customerId, editingAddr.value.id, form.value)
    } else {
      await createCustomerAddress(props.customerId, form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id) => {
  await deleteCustomerAddress(props.customerId, id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
watch(() => props.customerId, (val) => { if (val) loadData() })
</script>

<style scoped>
.address-tab { padding: 8px 0; }
.tab-toolbar { margin-bottom: 12px; text-align: right; }
.address-card { margin-bottom: 12px; }
.card-title { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.tag-group { display: inline-flex; gap: 6px; }
.addr-item { font-size: 13px; margin-bottom: 5px; color: #606266; line-height: 1.5; }
.card-actions { margin-top: 8px; text-align: right; }
.meter-unit { margin-left: 8px; color: #909399; font-size: 13px; }
</style>
