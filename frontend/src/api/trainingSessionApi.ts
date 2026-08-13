import http from './http'
import type { TrainingSessionCreateRequest, TrainingSessionResponse, TrainingSessionUpdateRequest } from '../types/trainingSession'

export async function getTrainingSessions(userId: number): Promise<TrainingSessionResponse[]> {
  const response = await http.get<TrainingSessionResponse[]>(`/training-sessions/users/${userId}`)
  return response.data
}

export async function getTrainingSession(id: number): Promise<TrainingSessionResponse> {
  const response = await http.get<TrainingSessionResponse>(`/training-sessions/${id}`)
  return response.data
}

export async function createTrainingSession(request: TrainingSessionCreateRequest): Promise<TrainingSessionResponse> {
  const response = await http.post<TrainingSessionResponse>('/training-sessions', request)
  return response.data
}

export async function updateTrainingSession(id: number, request: TrainingSessionUpdateRequest): Promise<TrainingSessionResponse> {
  const response = await http.put<TrainingSessionResponse>(`/training-sessions/${id}`, request)
  return response.data
}

export async function deleteTrainingSession(id: number): Promise<void> {
  await http.delete(`/training-sessions/${id}`)
}
