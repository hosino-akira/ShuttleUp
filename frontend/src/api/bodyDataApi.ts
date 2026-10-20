import http from './http'
import type { BodyData } from '../types/bodyData'

export async function getMyBodyData(): Promise<BodyData> {
  return (await http.get<BodyData>('/auth/me/body-data')).data
}

export async function updateMyBodyData(request: BodyData): Promise<BodyData> {
  return (await http.put<BodyData>('/auth/me/body-data', request)).data
}
