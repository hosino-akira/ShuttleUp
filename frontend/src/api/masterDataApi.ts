import http from './http'
import type { MasterCreateRequest, MasterItem, MasterKind, TrainingMaster } from '../types/masterData'

export async function getTrainingMaster(): Promise<TrainingMaster> {
  return (await http.get<TrainingMaster>('/master-data/training')).data
}

export async function createMaster(kind: MasterKind, request: MasterCreateRequest): Promise<MasterItem> {
  return (await http.post<MasterItem>(`/master-data/${kind}`, request)).data
}

export async function updateMaster(kind: MasterKind, id: number, name: string, enabled: boolean): Promise<MasterItem> {
  return (await http.put<MasterItem>(`/master-data/${kind}/${id}`, { name, enabled })).data
}
