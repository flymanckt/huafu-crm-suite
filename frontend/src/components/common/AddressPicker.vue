<template>
  <el-dialog v-model="visible" title="地址定位" width="900px" destroy-on-close append-to-body @closed="teardownMap">
    <div class="picker-wrap">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="输入地址搜索（如：杭州市文三路）" @keyup.enter="doSearch" style="flex:1">
          <template #append><el-button @click="doSearch">搜索</el-button></template>
        </el-input>
      </div>

      <div class="content">
        <!-- 搜索结果列表 -->
        <div v-if="results.length > 0" class="results-list">
          <div v-for="(r, i) in results" :key="i" class="result-item" @click="selectResult(r)">
            <div class="result-name">{{ r.name }}</div>
            <div class="result-addr">{{ r.pname }} {{ r.cityname }} {{ r.adname }} {{ r.address }}</div>
          </div>
        </div>

        <!-- 地图 -->
        <div ref="mapContainer" class="map-container">
          <div v-if="mapError" class="map-error">{{ mapError }}</div>
        </div>
      </div>

      <!-- 选中信息 -->
      <div v-if="selectedAddress" class="selected-info">
        <el-icon><LocationFilled /></el-icon>
        <span>{{ selectedAddress }}</span>
        <el-button type="primary" size="small" style="margin-left:12px" @click="confirm">确认选择</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick, computed } from 'vue'
import { LocationFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { searchPlace, regeocode, getAMapInstance } from '@/utils/amap'

const props = defineProps({
  modelValue: Boolean,
  initialAddress: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue', 'select'])

const visible = computed({
  get: () => props.modelValue,
  set: v => emit('update:modelValue', v)
})

const mapContainer = ref()
const keyword = ref('')
const results = ref([])
const selectedLocation = ref(null)
const selectedAddress = ref('')
const selectedRegion = ref({ province: '', city: '', district: '' })
const selectedPoi = ref({})
const mapError = ref('')

let mapInstance = null
let marker = null
let geocoder = null

async function initMap() {
  await nextTick()
  mapError.value = ''
  const AMap = await getAMapInstance()
  mapInstance = new AMap.Map(mapContainer.value, {
    zoom: 14,
    center: [120.2, 30.3],
    viewMode: '2D'
  })
  geocoder = new AMap.Geocoder()
  mapInstance.on('click', async ({ lnglat }) => {
    marker && mapInstance.remove(marker)
    marker = new AMap.Marker({ position: lnglat, icon: new AMap.Icon({ size: [32, 32], image: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-default.png', imageSize: [32, 32] }) })
    mapInstance.add(marker)
    selectedLocation.value = { lng: lnglat.lng, lat: lnglat.lat }
    const r = await regeocode(lnglat.lng, lnglat.lat)
    selectedRegion.value = r
    selectedPoi.value = { adcode: r.adcode || '', source: 'AMAP_REGEOCODE' }
    selectedAddress.value = r.address || keyword.value
  })
}

function teardownMap() {
  if (mapInstance) {
    mapInstance.destroy()
  }
  mapInstance = null
  marker = null
  geocoder = null
}

async function doSearch() {
  if (!keyword.value.trim()) return
  try {
    results.value = await searchPlace(keyword.value)
    if (!results.value.length) ElMessage.warning('未搜索到匹配地址')
  } catch (e) {
    results.value = []
    ElMessage.error(e.message || '地址搜索失败')
  }
}

async function selectResult(r) {
  if (!r.location) return
  selectedLocation.value = r.location
  selectedAddress.value = [r.pname, r.cityname, r.adname, r.address].filter(Boolean).join('')
  selectedRegion.value = { country: '中国', province: r.pname, city: r.cityname, district: r.adname, adcode: r.adcode || '' }
  selectedPoi.value = { id: r.id, name: r.name, adcode: r.adcode, type: r.type, source: 'AMAP_POI' }

  if (mapInstance) {
    const AMap = await getAMapInstance()
    mapInstance.setCenter([r.location.lng, r.location.lat])
    mapInstance.setZoom(15)
    marker && mapInstance.remove(marker)
    marker = new AMap.Marker({ position: [r.location.lng, r.location.lat] })
    mapInstance.add(marker)
  }
}

function confirm() {
  if (!selectedLocation.value) {
    ElMessage.warning('请先在搜索结果或地图上选择地址')
    return
  }
  emit('select', {
    address: selectedAddress.value,
    lng: selectedLocation.value.lng,
    lat: selectedLocation.value.lat,
    country: selectedRegion.value.country || '中国',
    province: selectedRegion.value.province,
    city: selectedRegion.value.city,
    district: selectedRegion.value.district,
    adcode: selectedRegion.value.adcode || selectedPoi.value.adcode || '',
    poiId: selectedPoi.value.id || '',
    poiName: selectedPoi.value.name || '',
    poiType: selectedPoi.value.type || '',
    source: selectedPoi.value.source || 'AMAP'
  })
  visible.value = false
}

watch(visible, async (v) => {
  if (v) {
    results.value = []
    selectedLocation.value = null
    selectedAddress.value = ''
    selectedPoi.value = {}
    keyword.value = props.initialAddress || ''
    await nextTick()
    try {
      teardownMap()
      await initMap()
    } catch (error) {
      mapError.value = '地图加载失败，请检查高德 JS Key 或安全密钥配置；仍可通过左侧搜索结果选择地址。'
      ElMessage.error(error.message || '地图加载失败')
    }
    if (keyword.value) doSearch()
  }
})
</script>

<style scoped>
.picker-wrap { display:flex; flex-direction:column; gap:12px; height:560px; }
.search-bar { display:flex; gap:8px; }
.content { display:flex; flex:1; gap:12px; min-height:0; }
.results-list { width:260px; overflow-y:auto; border:1px solid #e8e8e8; border-radius:4px; padding:4px; }
.result-item { padding:8px 10px; cursor:pointer; border-radius:4px; }
.result-item:hover { background:#f5f7fa; }
.result-name { font-weight:500; color:#333; }
.result-addr { font-size:12px; color:#999; margin-top:2px; }
.map-container { flex:1; border:1px solid #e8e8e8; border-radius:4px; }
.map-error { height:100%; display:flex; align-items:center; justify-content:center; padding:24px; color:#909399; text-align:center; background:#fafafa; }
.selected-info { display:flex; align-items:center; padding:10px 12px; background:#f0f9eb; border-radius:4px; font-size:14px; color:#67c23a; }
</style>
