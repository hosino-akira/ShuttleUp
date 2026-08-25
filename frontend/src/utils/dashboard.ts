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

/** 分数を日本語の読みやすい時間表記へ変換する。 */
export function formatDuration(minutes: number): string {
  if (minutes < 60) return `${minutes}分`
  const hours = Math.floor(minutes / 60)
  const remainder = minutes % 60
  return remainder === 0 ? `${hours}時間` : `${hours}時間${remainder}分`
}

export function formatMonth(month: string): string {
  const [year, monthNumber] = month.split('-')
  return `${year}年${Number(monthNumber)}月`
}

export function formatJapaneseDate(date: string): string {
  const [year, month, day] = date.split('-')
  return `${year}年${Number(month)}月${Number(day)}日`
}
