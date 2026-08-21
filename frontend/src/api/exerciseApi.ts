import http from './http'
import type { ExerciseCategoryResponse, ExerciseCreateRequest, ExerciseResponse, ExerciseTypeResponse, ExerciseUpdateRequest } from '../types/exercise'

export async function getExercises(exerciseTypeId?: number, userId = 1): Promise<ExerciseResponse[]> {
  const response = await http.get<ExerciseResponse[]>('/exercises', { params: { exerciseTypeId, userId } })
  return response.data
}

export async function getExerciseCategories(): Promise<ExerciseCategoryResponse[]> {
  const response = await http.get<ExerciseCategoryResponse[]>('/exercise-categories')
  return response.data
}

export async function getExerciseTypes(categoryId: number): Promise<ExerciseTypeResponse[]> {
  const response = await http.get<ExerciseTypeResponse[]>('/exercise-types', { params: { categoryId } })
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
