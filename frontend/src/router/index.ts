import { createRouter, createWebHistory, type RouteLocationRaw } from 'vue-router'
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
      meta: { layout: 'auth', requiresAuth: false, guestOnly: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { layout: 'auth', requiresAuth: false, guestOnly: true },
    },
    {
      path: '/',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true },
    },
    {
      path: '/training-history',
      name: 'training-history',
      component: () => import('../views/TrainingHistoryView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/training-history/new',
      name: 'training-session-create',
      component: () => import('../views/TrainingSessionDetailView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/training-history/:sessionId',
      name: 'training-session-detail',
      component: () => import('../views/TrainingSessionDetailView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/plan',
      name: 'training-plan',
      component: () => import('../views/TrainingPlanView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/statistics',
      name: 'statistics',
      component: () => import('../views/StatisticsView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/master-data',
      name: 'master-data',
      component: () => import('../views/MasterDataView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: { name: 'dashboard' },
    },
  ],
})

export function resolveAuthRedirect(value: unknown): RouteLocationRaw {
  const fallback = { name: 'dashboard' }
  if (typeof value !== 'string' || !value.startsWith('/') || value.startsWith('//')
      || /[\\\u0000-\u001f\u007f]/.test(value)) return fallback
  const target = router.resolve(value)
  if (!target.matched.length || target.meta.guestOnly || target.matched.some(record => record.redirect)) return fallback
  return target.fullPath
}

router.beforeEach(async to => {
  // 新しいルートも明示的に公開するまでは保護する。
  const requiresAuth = to.meta.requiresAuth !== false
  if (!requiresAuth && !to.meta.guestOnly) return true
  const auth = useAuthStore()
  const authenticated = await auth.restoreSession()
  if (to.meta.guestOnly && authenticated) return resolveAuthRedirect(to.query.redirect)
  if (requiresAuth && !authenticated) {
    return {
      name: 'login',
      query: {
        redirect: to.fullPath,
        ...(auth.sessionVerificationFailed ? { reason: 'verification_failed' } : {}),
      },
      replace: true,
    }
  }
  return true
})

export default router
