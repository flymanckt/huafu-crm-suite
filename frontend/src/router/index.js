import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/DashboardView.vue'),
        meta: { title: '工作台' }
      },
      // 客户管理
      {
        path: 'customer',
        name: 'CustomerList',
        component: () => import('../views/customer/CustomerList.vue'),
        meta: { title: '客户列表' }
      },
      {
        path: 'customer/public-pool',
        name: 'PublicPool',
        component: () => import('../views/customer/PublicPool.vue'),
        meta: { title: '公海客户' }
      },
      {
        path: 'customer/create',
        name: 'CustomerCreate',
        component: () => import('../views/customer/CustomerForm.vue'),
        meta: { title: '新建客户' }
      },
      {
        path: 'customer/detail/:id',
        name: 'CustomerDetail',
        component: () => import('../views/customer/CustomerDetail.vue'),
        meta: { title: '客户详情' }
      },
      {
        path: 'customer/edit/:id',
        name: 'CustomerEdit',
        component: () => import('../views/customer/CustomerForm.vue'),
        meta: { title: '编辑客户' }
      },
      // 商机管理
      {
        path: 'opportunity/lead',
        name: 'LeadList',
        component: () => import('../views/opportunity/LeadList.vue'),
        meta: { title: '线索商情' }
      },
      {
        path: 'opportunity',
        name: 'OpportunityList',
        component: () => import('../views/opportunity/OpportunityList.vue'),
        meta: { title: '商机列表' }
      },
      {
        path: 'opportunity/:id',
        name: 'OpportunityDetail',
        component: () => import('../views/opportunity/OpportunityDetail.vue'),
        meta: { title: '商机详情' }
      },
      {
        path: 'opportunity/lost',
        name: 'LostOrderList',
        component: () => import('../views/opportunity/LostOrderList.vue'),
        meta: { title: '丢单记录' }
      },
      // 需求表补齐模块：展会、产品样品、色卡派送、订单报表等配置化台账
      {
        path: 'business/:moduleKey',
        name: 'BusinessModule',
        component: () => import('../views/business/GenericModulePage.vue'),
        meta: { title: '业务台账' }
      },
      // 目标管理
      {
        path: 'target',
        name: 'TargetList',
        component: () => import('../views/target/TargetList.vue'),
        meta: { title: '目标管理' }
      },
      // 报价管理
      {
        path: 'quote',
        name: 'QuoteList',
        component: () => import('../views/quote/QuoteList.vue'),
        meta: { title: '报价单管理' }
      },
      // 系统管理（后台）
      {
        path: 'admin/user',
        name: 'AdminUser',
        component: () => import('../views/admin/UserList.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'admin/role',
        name: 'AdminRole',
        component: () => import('../views/admin/RoleList.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'admin/dept',
        name: 'AdminDept',
        component: () => import('../views/admin/DeptTree.vue'),
        meta: { title: '部门管理' }
      },
      // 数据字典
      {
        path: 'system/dict',
        name: 'DictManagement',
        component: () => import('../views/system/DictManagement.vue'),
        meta: { title: '数据字典' }
      },
      // 系统配置
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: () => import('../views/system/SystemConfig.vue'),
        meta: { title: '系统配置' }
      },
      {
        path: 'system/integration',
        name: 'IntegrationPlatform',
        component: () => import('../views/system/IntegrationPlatform.vue'),
        meta: { title: '集成平台' }
      },
      // 勤力度
      {
        path: 'performance/visit',
        name: 'VisitRecordList',
        component: () => import('../views/performance/VisitRecordList.vue'),
        meta: { title: '拜访记录' }
      },
      {
        path: 'performance/daily-report',
        name: 'DailyReportList',
        component: () => import('../views/performance/DailyReportList.vue'),
        meta: { title: '日报列表' }
      },
      {
        path: 'performance',
        name: 'PerformanceList',
        component: () => import('../views/performance/PerformanceList.vue'),
        meta: { title: '勤力度评分' }
      },
      // 企微集成
      {
        path: 'wecom/message',
        name: 'MessageLogList',
        component: () => import('../views/wecom/MessageLogList.vue'),
        meta: { title: '消息日志' }
      },
      // AI解析
      {
        path: 'ai',
        name: 'AiAnalysis',
        component: () => import('../views/ai/AiView.vue'),
        meta: { title: 'AI解析' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 导航守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
