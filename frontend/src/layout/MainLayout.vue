<template>
  <el-container class="main-layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <h2>华孚时尚CRM</h2>
      </div>
      <el-menu :default-active="activeMenu" router class="menu" background-color="#1d1e2c" text-color="#a0a3bd" active-text-color="#409eff">
        <el-menu-item v-if="can('dashboard:view')" index="/dashboard"><el-icon><DataAnalysis /></el-icon>工作台</el-menu-item>
        <el-sub-menu v-if="canAny('customer:list', 'customer:public-pool')" index="customer">
          <template #title><el-icon><User /></el-icon>客户管理</template>
          <el-menu-item v-if="can('customer:list')" index="/customer">客户列表</el-menu-item>
          <el-menu-item v-if="can('customer:public-pool')" index="/customer/public-pool">公海客户</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="canAny('opportunity:lead', 'opportunity:list', 'opportunity:lost')" index="opportunity">
          <template #title><el-icon><TrendCharts /></el-icon>商机管理</template>
          <el-menu-item v-if="can('opportunity:lead')" index="/opportunity/lead">线索商情</el-menu-item>
          <el-menu-item v-if="can('opportunity:list')" index="/opportunity">商机列表</el-menu-item>
          <el-menu-item v-if="can('opportunity:lost')" index="/opportunity/lost">丢单记录</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="canAny('business:exhibition-activity', 'business:exhibition-material', 'business:exhibition-sample', 'business:exhibition-talk')" index="exhibition">
          <template #title><el-icon><Flag /></el-icon>展会管理</template>
          <el-menu-item v-if="can('business:exhibition-activity')" index="/business/exhibition-activity">展会活动</el-menu-item>
          <el-menu-item v-if="can('business:exhibition-material')" index="/business/exhibition-material">参展物资清单</el-menu-item>
          <el-menu-item v-if="can('business:exhibition-sample')" index="/business/exhibition-sample">展会样品库</el-menu-item>
          <el-menu-item v-if="can('business:exhibition-talk')" index="/business/exhibition-talk">展会洽谈记录</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="canAny('business:product-master', 'business:sample-inventory', 'business:sample-outbound')" index="sample">
          <template #title><el-icon><Box /></el-icon>产品与样品</template>
          <el-menu-item v-if="can('business:product-master')" index="/business/product-master">产品档案</el-menu-item>
          <el-menu-item v-if="can('business:sample-inventory')" index="/business/sample-inventory">样品库存</el-menu-item>
          <el-menu-item v-if="can('business:sample-outbound')" index="/business/sample-outbound">样品出库单</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="canAny('business:color-card-plan', 'business:color-card-ledger')" index="color-card">
          <template #title><el-icon><Collection /></el-icon>色卡派送</template>
          <el-menu-item v-if="can('business:color-card-plan')" index="/business/color-card-plan">色卡派送计划</el-menu-item>
          <el-menu-item v-if="can('business:color-card-ledger')" index="/business/color-card-ledger">色卡派送总表</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="canAny('business:sales-order', 'business:shipment')" index="order">
          <template #title><el-icon><ShoppingCart /></el-icon>订单管理</template>
          <el-menu-item v-if="can('business:sales-order')" index="/business/sales-order">销售订单</el-menu-item>
          <el-menu-item v-if="can('business:shipment')" index="/business/shipment">发货单</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="can('target:list')" index="target">
          <template #title><el-icon><Aim /></el-icon>目标管理</template>
          <el-menu-item index="/target">目标与达成</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="can('quote:list')" index="quote">
          <template #title><el-icon><Tickets /></el-icon>报价管理</template>
          <el-menu-item index="/quote">报价单管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="canAny('admin:user', 'admin:role', 'admin:dept', 'system:dict', 'system:config', 'system:integration', 'wecom:message')" index="admin">
          <template #title><el-icon><Setting /></el-icon>系统管理</template>
          <el-menu-item v-if="can('admin:user')" index="/admin/user">用户管理</el-menu-item>
          <el-menu-item v-if="can('admin:role')" index="/admin/role">角色管理</el-menu-item>
          <el-menu-item v-if="can('admin:dept')" index="/admin/dept">部门管理</el-menu-item>
          <el-menu-item v-if="can('system:dict')" index="/system/dict">字典管理</el-menu-item>
          <el-menu-item v-if="can('system:config')" index="/system/config">外围系统配置</el-menu-item>
          <el-menu-item v-if="can('system:integration')" index="/system/integration">集成平台</el-menu-item>
          <el-menu-item v-if="can('wecom:message')" index="/wecom/message">企微消息日志</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="canAny('performance:visit', 'performance:daily-report', 'performance:list')" index="performance">
          <template #title><el-icon><Calendar /></el-icon>勤力度</template>
          <el-menu-item v-if="can('performance:visit')" index="/performance/visit">拜访记录</el-menu-item>
          <el-menu-item v-if="can('performance:daily-report')" index="/performance/daily-report">日报列表</el-menu-item>
          <el-menu-item v-if="can('performance:list')" index="/performance">勤力度评分</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="canAny('business:custom-report', 'business:work-daily-report')" index="report">
          <template #title><el-icon><Document /></el-icon>报表与日报</template>
          <el-menu-item v-if="can('business:custom-report')" index="/business/custom-report">自定义报表</el-menu-item>
          <el-menu-item v-if="can('business:work-daily-report')" index="/business/work-daily-report">工作日报</el-menu-item>
        </el-sub-menu>
        <el-menu-item v-if="can('ai:view')" index="/ai"><el-icon><MagicStick /></el-icon>AI解析</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="page-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-name">{{ userName }} <el-icon><ArrowDown /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 顶部 Tab 栏 -->
      <TabBar />

      <el-main class="main-content">
        <router-view v-slot="{ Component, route }">
          <keep-alive :include="keepAliveComponents">
            <component :is="Component" :key="route.path" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Aim,
  ArrowDown,
  Box,
  Calendar,
  Collection,
  DataAnalysis,
  Document,
  Flag,
  MagicStick,
  Setting,
  ShoppingCart,
  Tickets,
  TrendCharts,
  User
} from '@element-plus/icons-vue'
import TabBar from '@/components/layout/TabBar.vue'
import { useTabStore } from '@/stores/tabStore'
import { businessModules } from '@/config/businessModules'

const route = useRoute()
const router = useRouter()
const tabStore = useTabStore()

const activeMenu = computed(() => route.path)
const resolveRouteTitle = (targetRoute = route) => {
  if (targetRoute.name === 'BusinessModule') {
    return businessModules[targetRoute.params.moduleKey]?.title || '业务台账'
  }
  return targetRoute.meta.title || '工作台'
}
const currentTitle = computed(() => resolveRouteTitle(route))
const currentUser = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}')
  } catch {
    return {}
  }
})
const userName = computed(() =>
  currentUser.value.realName || currentUser.value.username || localStorage.getItem('userName') || '未登录'
)
const permissions = computed(() => currentUser.value.permissions || [])
const isSuperAdmin = computed(() => {
  const roleKeys = currentUser.value.roleKeys || []
  return currentUser.value.username === 'admin' || roleKeys.includes('ROLE_ADMIN') || permissions.value.includes('*')
})
const can = permission => isSuperAdmin.value || permissions.value.includes(permission)
const canAny = (...items) => items.some(item => can(item))

// 所有已打开的页面 path 列表，用于 keep-alive
const keepAliveComponents = computed(() => tabStore.tabs.map(t => t.path.replace(/\//g, '')))

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('userId')
  localStorage.removeItem('userName')
  router.push('/login')
}

// 监听路由变化，自动打开 tab
watch(
  () => route.path,
  (path) => {
    if (path === '/login') return
    const title = resolveRouteTitle(route) || path
    tabStore.openTab(path, title)
    // 重置所有 el-scrollbar 的滚动位置，防止跨页面残留
    nextTick(() => {
      document.querySelectorAll('.el-scrollbar__wrap').forEach(el => {
        el.scrollTop = 0
        el.scrollLeft = 0
      })
    })
  },
  { immediate: true }
)
</script>

<style scoped>
.main-layout { height: 100vh; }
.aside { background: #1d1e2c; overflow-y: auto; }
.logo { padding: 20px 16px; text-align: center; }
.logo h2 { color: #fff; font-size: 18px; margin: 0; }
.menu { border-right: none; }
.header { display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1px solid #ebeef5; }
.page-title { font-size: 16px; font-weight: 600; color: #303133; }
.user-name { cursor: pointer; color: #606266; display: flex; align-items: center; gap: 4px; }
.main-content { background: #f5f7fa; padding: 0; }
</style>
