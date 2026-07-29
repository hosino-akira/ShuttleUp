import type { RouteLocationRaw } from 'vue-router'

export interface NavigationMenuItem {
  key: string
  label: string
  to: RouteLocationRaw
}

export const NAVIGATION_MENU: readonly NavigationMenuItem[] = [
  {
    key: 'dashboard',
    label: 'ダッシュボード',
    to: { name: 'dashboard' },
  },
  {
    key: 'training-history',
    label: 'トレーニング履歴',
    to: { name: 'training-history' },
  },
  {
    key: 'training-plan',
    label: 'トレーニングプラン',
    to: { name: 'training-plan' },
  },
  {
    key: 'statistics',
    label: '統計',
    to: { name: 'statistics' },
  },
  {
    key: 'profile',
    label: 'プロフィール',
    to: { name: 'profile' },
  },
]
