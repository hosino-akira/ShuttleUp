import { formatLocalDate, t } from '../i18n';
import type { DashboardQuery } from '../types/dashboard'

export type DashboardPeriod = 'all' | 'month' | 'threeMonths' | 'sixMonths' | 'year' | 'custom'

function formatDate(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/** 選択期間からAPIの日付条件を作成する。 */
export function createPeriodQuery(
  period: DashboardPeriod,
  customRange?: readonly [string, string],
  today = new Date(),
): Pick<DashboardQuery, 'from' | 'to'> {
  if (period === 'all') return {}
  if (period === 'custom') {
    return customRange ? { from: customRange[0], to: customRange[1] } : {}
  }

  const to = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  let from: Date
  if (period === 'month') {
    from = new Date(today.getFullYear(), today.getMonth(), 1)
  } else {
    const months = period === 'threeMonths' ? 3 : period === 'sixMonths' ? 6 : 12
    from = new Date(today.getFullYear(), today.getMonth() - months + 1, 1)
  }
  return { from: formatDate(from), to: formatDate(to) }
}

/** Format a duration in the selected interface language. */
export function formatDuration(minutes: number): string {
  if (minutes < 60) return t('{0}分', { 0: minutes })
  const hours = Math.floor(minutes / 60)
  const remainder = minutes % 60
  return remainder === 0 ? t('{0}時間', { 0: hours }) : t('{0}時間{1}分', { 0: hours, 1: remainder })
}

export function formatMonth(month: string): string {
  return formatLocalDate(month, { year: 'numeric', month: 'short' })
}

export function formatJapaneseDate(date: string): string {
  return formatLocalDate(date)
}
