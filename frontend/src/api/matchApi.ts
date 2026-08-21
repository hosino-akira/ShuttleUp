import http from './http'
import type { MatchCreateRequest, MatchResponse, MatchUpdateRequest } from '../types/match'

export async function getMatches(sessionId: number): Promise<MatchResponse[]> {
  const response = await http.get<MatchResponse[]>(`/training-sessions/${sessionId}/matches`)
  return response.data
}

export async function createMatch(sessionId: number, request: MatchCreateRequest): Promise<MatchResponse> {
  const response = await http.post<MatchResponse>(`/training-sessions/${sessionId}/matches`, request)
  return response.data
}

export async function updateMatch(id: number, request: MatchUpdateRequest): Promise<MatchResponse> {
  const response = await http.put<MatchResponse>(`/matches/${id}`, request)
  return response.data
}

export async function deleteMatch(id: number): Promise<void> {
  await http.delete(`/matches/${id}`)
}
