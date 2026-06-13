import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'Odometer' },
      },
      {
        path: 'cases',
        name: 'Cases',
        component: () => import('@/views/case/index.vue'),
        meta: { title: '测试用例', icon: 'Document' },
      },
      {
        path: 'cases/create',
        name: 'CaseCreate',
        component: () => import('@/views/case/create.vue'),
        meta: { title: '新建用例', icon: 'Document', hidden: true },
      },
      {
        path: 'cases/:id',
        name: 'CaseDetail',
        component: () => import('@/views/case/detail.vue'),
        meta: { title: '用例详情', icon: 'Document', hidden: true },
      },
      {
        path: 'cases/:id/data-table',
        name: 'CaseDataTable',
        component: () => import('@/views/case/TestDataTable.vue'),
        meta: { title: '测试数据管理', icon: 'Document', hidden: true },
      },
      {
        path: 'suites',
        name: 'Suites',
        component: () => import('@/views/suite/index.vue'),
        meta: { title: '用例套件', icon: 'FolderOpened' },
      },
      {
        path: 'suites/:id',
        name: 'SuiteDetail',
        component: () => import('@/views/suite/detail.vue'),
        meta: { title: '套件详情', icon: 'FolderOpened', hidden: true },
      },
      {
        path: 'executions',
        name: 'Executions',
        component: () => import('@/views/execution/index.vue'),
        meta: { title: '执行记录', icon: 'VideoPlay' },
      },
      {
        path: 'executions/:id',
        name: 'ExecutionDetail',
        component: () => import('@/views/execution/detail.vue'),
        meta: { title: '执行详情', icon: 'VideoPlay', hidden: true },
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('@/views/knowledge/index.vue'),
        meta: { title: '知识库', icon: 'Collection' },
      },
      {
        path: 'defects',
        name: 'Defects',
        component: () => import('@/views/defect/index.vue'),
        meta: { title: '缺陷管理', icon: 'WarningFilled' },
      },
      {
        path: 'risk-analysis',
        name: 'RiskAnalysis',
        component: () => import('@/views/defect/RiskAnalysis.vue'),
        meta: { title: '风险分析', icon: 'DataAnalysis', hidden: true },
      },
      {
        path: 'analysis',
        name: 'Analysis',
        component: () => import('@/views/analysis/index.vue'),
        meta: { title: '需求分析', icon: 'TrendCharts' },
      },
      {
        path: 'skills',
        name: 'Skills',
        component: () => import('@/views/skill/index.vue'),
        meta: { title: 'AI 技能', icon: 'MagicStick' },
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/llm/index.vue'),
        meta: { title: 'AI 对话', icon: 'ChatDotRound' },
      },
      {
        path: 'record',
        name: 'Record',
        component: () => import('@/views/record/index.vue'),
        meta: { title: '录制回放', icon: 'Monitor' },
      },
      {
        path: 'record/:sessionId',
        name: 'RecordDetail',
        component: () => import('@/views/record/detail.vue'),
        meta: { title: '录制详情', icon: 'Monitor', hidden: true },
      },
      {
        path: 'systems',
        name: 'Systems',
        component: () => import('@/views/system/SystemList.vue'),
        meta: { title: '系统管理', icon: 'Platform' },
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/system/UserList.vue'),
        meta: { title: '用户管理', icon: 'User' },
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/views/system/RoleList.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' },
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/system/Settings.vue'),
        meta: { title: '系统设置', icon: 'Setting' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Navigation guard
router.beforeEach((to, _from, next) => {
  const token = getToken()

  if (to.path === '/login') {
    if (token) {
      next('/')
    } else {
      next()
    }
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
