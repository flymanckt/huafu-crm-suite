<template>
  <el-container class="main-layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <h2>华孚时尚CRM</h2>
      </div>
      <el-menu :default-active="activeMenu" router class="menu" background-color="#1d1e2c" text-color="#a0a3bd" active-text-color="#409eff">
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon>工作台</el-menu-item>
        <el-sub-menu index="customer">
          <template #title><el-icon><User /></el-icon>客户管理</template>
          <el-menu-item index="/customer">客户列表</el-menu-item>
          <el-menu-item index="/customer/public-pool">公海客户</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="opportunity">
          <template #title><el-icon><TrendCharts /></el-icon>商机管理</template>
          <el-menu-item index="/opportunity/lead">线索商情</el-menu-item>
          <el-menu-item index="/opportunity">商机列表</el-menu-item>
          <el-menu-item index="/opportunity/lost">丢单记录</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="exhibition">
          <template #title><el-icon><Flag /></el-icon>展会管理</template>
          <el-menu-item index="/business/exhibition-activity">展会活动</el-menu-item>
          <el-menu-item index="/business/exhibition-material">参展物资清单</el-menu-item>
          <el-menu-item index="/business/exhibition-sample">展会样品库</el-menu-item>
          <el-menu-item index="/business/exhibition-talk">展会洽谈记录</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="sample">
          <template #title><el-icon><Box /></el-icon>产品与样品</template>
          <el-menu-item index="/business/product-master">产品档案</el-menu-item>
          <el-menu-item index="/business/sample-inventory">样品库存</el-menu-item>
          <el-menu-item index="/business/sample-outbound">样品出库单</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="color-card">
          <template #title><el-icon><Collection /></el-icon>色卡派送</template>
          <el-menu-item index="/business/color-card-plan">色卡派送计划</el-menu-item>
          <el-menu-item index="/business/color-card-ledger">色卡派送总表</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="order">
          <template #title><el-icon><ShoppingCart /></el-icon>订单管理</template>
          <el-menu-item index="/business/sales-order">销售订单</el-menu-item>
          <el-menu-item index="/business/shipment">发货单</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="target">
          <template #title><el-icon><Aim /></el-icon>目标管理</template>
          <el-menu-item index="/target">目标与达成</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="quote">
          <template #title><el-icon><Tickets /></el-icon>报价管理</template>
          <el-menu-item index="/quote">报价单管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="admin">
          <template #title><el-icon><Setting /></el-icon>系统管理</template>
          <el-menu-item index="/admin/user">用户管理</el-menu-item>
          <el-menu-item index="/admin/role">角色管理</el-menu-item>
          <el-menu-item index="/admin/dept">部门管理</el-menu-item>
          <el-menu-item index="/system/dict">字典管理</el-menu-item>
          <el-menu-item index="/system/config">外围系统配置</el-menu-item>
          <el-menu-item index="/system/integration">集成平台</el-menu-item>
          <el-menu-item index="/wecom/message">企微消息日志</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="performance">
          <template #title><el-icon><Calendar /></el-icon>勤力度</template>
          <el-menu-item index="/performance/visit">拜访记录</el-menu-item>
          <el-menu-item index="/performance/daily-report">日报列表</el-menu-item>
          <el-menu-item index="/performance">勤力度评分</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="report">
          <template #title><el-icon><Document /></el-icon>报表与日报</template>
          <el-menu-item index="/business/custom-report">自定义报表</el-menu-item>
          <el-menu-item index="/business/work-daily-report">工作日报</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/ai"><el-icon><MagicStick /></el-icon>AI解析</el-menu-item>
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
import { computed, ref, watch, nextTick } from 'vue'
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
const userName = ref(localStorage.getItem('userName') || '管理员')

// 所有已打开的页面 path 列表，用于 keep-alive
const keepAliveComponents = computed(() => tabStore.tabs.map(t => t.path.replace(/\//g, '')))

const handleLogout = () => {
  localStorage.removeItem('token')
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
