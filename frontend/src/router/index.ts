import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      // ログイン画面はサイドバーのない専用レイアウトで表示する。
      meta: { layout: 'auth' },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { layout: 'auth' },
    },
    {
      path: '/',
      name: 'dashboard',
      component: DashboardView,
    },
    {
      path: '/training-history',
      name: 'training-history',
      component: () => import('../views/TrainingHistoryView.vue'),
    },
    {
      path: '/training-history/new',
      name: 'training-session-create',
      component: () => import('../views/TrainingSessionDetailView.vue'),
    },
    {
      path: '/training-history/:sessionId',
      name: 'training-session-detail',
      component: () => import('../views/TrainingSessionDetailView.vue'),
    },
    {
      path: '/plan',
      name: 'training-plan',
      component: () => import('../views/TrainingPlanView.vue'),
    },
    {
      path: '/statistics',
      name: 'statistics',
      component: () => import('../views/StatisticsView.vue'),
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
    },
  ],
})

router.beforeEach(async to => {
  if (to.meta.layout === 'auth') return true
  if (await useAuthStore().restoreSession()) return true
  return { name: 'login' }
})

export default router
