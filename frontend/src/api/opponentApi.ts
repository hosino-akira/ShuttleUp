import http from './http'
import type { OpponentCreateRequest, OpponentResponse, OpponentUpdateRequest } from '../types/opponent'

export async function getOpponents(userId: number): Promise<OpponentResponse[]> {
  const response = await http.get<OpponentResponse[]>(`/users/${userId}/opponents`)
  return response.data
}

export async function createOpponent(userId: number, request: OpponentCreateRequest): Promise<OpponentResponse> {
  const response = await http.post<OpponentResponse>(`/users/${userId}/opponents`, request)
  return response.data
}

export async function updateOpponent(id: number, request: OpponentUpdateRequest): Promise<OpponentResponse> {
  const response = await http.put<OpponentResponse>(`/opponents/${id}`, request)
  return response.data
}

export async function deleteOpponent(id: number): Promise<void> {
  await http.delete(`/opponents/${id}`)
}
