import http from './http'
import type {
  TrainingSession,
  TrainingSessionCreateRequest,
} from '../types/trainingSession'

export async function getTrainingSessions(userId: number): Promise<TrainingSession[]> {
  const response = await http.get<TrainingSession[]>(`/training-sessions/users/${userId}`)

  return response.data
}

export async function createTrainingSession(
  request: TrainingSessionCreateRequest,
): Promise<TrainingSession> {
  const response = await http.post<TrainingSession>('/training-sessions', request)

  return response.data
}
