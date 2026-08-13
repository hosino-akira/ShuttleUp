import http from './http'
import type { ExerciseCreateRequest, ExerciseResponse, ExerciseUpdateRequest } from '../types/exercise'

export async function getExercises(): Promise<ExerciseResponse[]> {
  const response = await http.get<ExerciseResponse[]>('/exercises')
  return response.data
}

export async function getExercise(id: number): Promise<ExerciseResponse> {
  const response = await http.get<ExerciseResponse>(`/exercises/${id}`)
  return response.data
}

export async function createExercise(request: ExerciseCreateRequest): Promise<ExerciseResponse> {
  const response = await http.post<ExerciseResponse>('/exercises', request)
  return response.data
}

export async function updateExercise(id: number, request: ExerciseUpdateRequest): Promise<ExerciseResponse> {
  const response = await http.put<ExerciseResponse>(`/exercises/${id}`, request)
  return response.data
}

export async function deleteExercise(id: number): Promise<void> {
  await http.delete(`/exercises/${id}`)
}
