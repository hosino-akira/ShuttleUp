import http from './http'
import type { DashboardQuery, DashboardResponse } from '../types/dashboard'

/** 指定ユーザーのDashboard集計を取得する。 */
export async function getDashboard(
  userId: number,
  query: DashboardQuery = {},
): Promise<DashboardResponse> {
  const params = Object.fromEntries(
    Object.entries(query).filter(([, value]) => value !== undefined),
  )
  const response = await http.get<DashboardResponse>(`/dashboard/users/${userId}`, { params })
  return response.data
}
