import http from './http'
import type { TrainingRecordCreateRequest, TrainingRecordResponse, TrainingRecordUpdateRequest } from '../types/trainingRecord'

export async function getTrainingRecords(sessionId: number): Promise<TrainingRecordResponse[]> {
  const response = await http.get<TrainingRecordResponse[]>(`/training-sessions/${sessionId}/records`)
  return response.data
}

export async function getTrainingRecord(recordId: number): Promise<TrainingRecordResponse> {
  const response = await http.get<TrainingRecordResponse>(`/training-records/${recordId}`)
  return response.data
}

export async function createTrainingRecord(sessionId: number, request: TrainingRecordCreateRequest): Promise<TrainingRecordResponse> {
  const response = await http.post<TrainingRecordResponse>(`/training-sessions/${sessionId}/records`, request)
  return response.data
}

export async function updateTrainingRecord(recordId: number, request: TrainingRecordUpdateRequest): Promise<TrainingRecordResponse> {
  const response = await http.put<TrainingRecordResponse>(`/training-records/${recordId}`, request)
  return response.data
}

export async function deleteTrainingRecord(recordId: number): Promise<void> {
  await http.delete(`/training-records/${recordId}`)
}
