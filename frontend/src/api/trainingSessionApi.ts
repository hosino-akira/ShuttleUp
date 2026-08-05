import http from './http'
import type {
  TrainingSession,
  TrainingSessionCreateRequest,
  TrainingSessionUpdateRequest,
} from '../types/trainingSession'

export async function getTrainingSessions(userId: number): Promise<TrainingSession[]> {
  const response = await http.get<TrainingSession[]>(`/training-sessions/users/${userId}`)

  return response.data
}

export async function updateTrainingSession(
  id: number,
  request: TrainingSessionUpdateRequest,
): Promise<TrainingSession> {
  const response = await http.put<TrainingSession>(`/training-sessions/${id}`, request)

  return response.data
}

/** 指定したトレーニング記録を削除する。 */
export async function deleteTrainingSession(id: number): Promise<void> {
  await http.delete(`/training-sessions/${id}`)
}

export async function createTrainingSession(
  request: TrainingSessionCreateRequest,
): Promise<TrainingSession> {
  const response = await http.post<TrainingSession>('/training-sessions', request)

  return response.data
}
