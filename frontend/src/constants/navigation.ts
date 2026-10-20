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
    label: 'トレーニングの森',
    to: { name: 'training-plan' },
  },
  {
    key: 'statistics',
    label: '統計',
    to: { name: 'statistics' },
  },
  { key: 'master-data', label: 'マスタ管理', to: { name: 'master-data' } },
  { key: 'body-data', label: '身体データ', to: { name: 'body-data' } },
  {
    key: 'profile',
    label: 'プロフィール',
    to: { name: 'profile' },
  },
]
