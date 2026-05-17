/**
 * 高德地图 API 封装
 * 使用 JSONP 方式调用高德 Web服务 API（无需 SECRET）
 */

import { getConfigsByGroup } from '@/api/system/config'

const amapConfig = {
  webKey: window.__AMAP_WEB_KEY__ || import.meta.env.VITE_AMAP_WEB_KEY || '',
  jsKey: window.__AMAP_JS_KEY__ || import.meta.env.VITE_AMAP_JS_KEY || '',
  securityCode: window.__AMAP_SECURITY_CODE__ || import.meta.env.VITE_AMAP_SECURITY_CODE || ''
}

let _configLoader = null

async function ensureAMapConfig() {
  if (amapConfig.webKey && amapConfig.jsKey && amapConfig.securityCode) return amapConfig
  if (!_configLoader) {
    _configLoader = getConfigsByGroup('AMAP')
      .then(configs => {
        const map = Object.fromEntries((configs || []).map(item => [item.configKey, item.configValue || '']))
        amapConfig.webKey = amapConfig.webKey || map['amap.web_key'] || ''
        amapConfig.jsKey = amapConfig.jsKey || map['amap.js_key'] || ''
        amapConfig.securityCode = amapConfig.securityCode || map['amap.security_code'] || ''
        window.__AMAP_WEB_KEY__ = amapConfig.webKey
        window.__AMAP_JS_KEY__ = amapConfig.jsKey
        window.__AMAP_SECURITY_CODE__ = amapConfig.securityCode
        return amapConfig
      })
      .catch(() => amapConfig)
  }
  return _configLoader
}

/**
 * JSONP 请求（用于高德 Web API）
 */
function jsonp(url, params = {}) {
  return new Promise((resolve, reject) => {
    const key = amapConfig.webKey
    if (!key) return reject(new Error('未配置高德 Web Key'))
    const callbackName = '_amap_cb_' + Date.now()
    const script = document.createElement('script')
    const cleanParams = Object.fromEntries(
      Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '')
    )
    const query = new URLSearchParams({ ...cleanParams, key, output: 'jsonp', callback: callbackName })
    script.src = url + '?' + query.toString()
    script.onerror = () => reject(new Error('高德 API 请求失败'))
    window[callbackName] = (data) => {
      delete window[callbackName]
      document.body.removeChild(script)
      if (data.status === '1' || data.errcode === 0) resolve(data)
      else reject(new Error(data.info || '高德 API 错误'))
    }
    setTimeout(() => {
      if (window[callbackName]) {
        delete window[callbackName]
        document.body.removeChild(script)
        reject(new Error('高德 API 超时'))
      }
    }, 8000)
    document.body.appendChild(script)
  })
}

/**
 * 加载高德 JS SDK（异步，按需加载）
 */
let _amapLoader = null
function loadAMap() {
  if (_amapLoader) return _amapLoader
  _amapLoader = new Promise(async (resolve, reject) => {
    await ensureAMapConfig()
    if (window.AMap) { resolve(window.AMap); return }
    if (!amapConfig.jsKey) {
      reject(new Error('未配置高德 JS Key'))
      return
    }
    if (amapConfig.securityCode) {
      window._AMapSecurityConfig = { securityJsCode: amapConfig.securityCode }
    }
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${amapConfig.jsKey}&plugin=AMap.Geocoder,AMap.PlaceSearch,AMap.AutoComplete`
    script.onload = () => resolve(window.AMap)
    script.onerror = () => reject(new Error('高德 JS SDK 加载失败'))
    document.head.appendChild(script)
  })
  return _amapLoader
}

/**
 * 地址 → 坐标 + 结构化地址
 * @param {string} address
 * @returns {Promise<{ lng: number, lat: number, province: string, city: string, district: string, address: string }>}
 */
export async function geocodeAddress(address, city = '') {
  await ensureAMapConfig()
  const pois = await searchPlace(address, city).catch(() => [])
  const poi = pois.find(item => item.location) || null
  if (poi) {
    return {
      lng: poi.location.lng,
      lat: poi.location.lat,
      country: '中国',
      province: poi.pname || '',
      city: poi.cityname || poi.pname || '',
      district: poi.adname || '',
      adcode: poi.adcode || '',
      level: poi.type || 'POI',
      poiId: poi.id || '',
      poiName: poi.name || '',
      address: [poi.pname, poi.cityname, poi.adname, poi.address || poi.name].filter(Boolean).join('')
    }
  }
  const data = await jsonp('https://restapi.amap.com/v3/geocode/geo', { address, city })
  const geocodes = data.geocodes || []
  if (geocodes.length === 0) throw new Error('未找到地址：' + address)
  const g = geocodes[0]
  return {
    lng: parseFloat(g.location.split(',')[0]),
    lat: parseFloat(g.location.split(',')[1]),
    country: g.country || '中国',
    province: g.province,
    city: g.city || g.province,
    district: g.district || '',
    adcode: g.adcode || '',
    level: g.level || '',
    address: g.formatted_address || address
  }
}

/**
 * 坐标 → 地址（逆地理编码）
 * @param {number} lng
 * @param {number} lat
 * @returns {Promise<{ province: string, city: string, district: string, address: string }>}
 */
export async function regeocode(lng, lat) {
  await ensureAMapConfig()
  const data = await jsonp('https://restapi.amap.com/v3/geocode/regeo', {
    location: `${lng},${lat}`,
    extensions: 'all'
  })
  const r = data.regeocode || {}
  const a = r.addressComponent || {}
  return {
    country: a.country || '中国',
    province: a.province || '',
    city: a.city || a.province || '',
    district: a.district || '',
    adcode: a.adcode || '',
    address: r.formatted_address || ''
  }
}

/**
 * 关键词搜索地点
 * @param {string} keyword
 * @param {string} city 可选，限制城市
 * @returns {Promise<Array>}
 */
export async function searchPlace(keyword, city = '') {
  await ensureAMapConfig()
  const data = await jsonp('https://restapi.amap.com/v3/place/text', {
    keywords: keyword,
    city: city || undefined,
    citylimit: city ? true : undefined
  })
  return (data.pois || []).map(p => ({
    id: p.id || '',
    name: p.name,
    address: p.address || '',
    location: p.location ? { lng: parseFloat(p.location.split(',')[0]), lat: parseFloat(p.location.split(',')[1]) } : null,
    type: p.type || '',
    pname: p.pname || '',
    cityname: p.cityname || '',
    adname: p.adname || '',
    adcode: p.adcode || ''
  }))
}

/**
 * 获取 JS API 实例（地图展示用）
 * 需要先配置 VITE_AMAP_JS_KEY
 */
export function getAMapInstance() {
  if (window.AMap) return Promise.resolve(window.AMap)
  return loadAMap()
}

export { loadAMap }
