import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
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

export default router
