export interface ExerciseResponse {
  enabled: boolean
  available: boolean
  id: number
  name: string
  exerciseTypeId: number
  exerciseTypeName: string
  categoryId: number
  categoryName: string
  systemPreset: boolean
  userId: number | null
  createdAt: string
  updatedAt: string
}

export interface ExerciseCreateRequest {
  exerciseTypeId: number
  name: string
  userId: number
}

export type ExerciseUpdateRequest = Omit<ExerciseCreateRequest, 'userId'> & { enabled?: boolean }

export interface ExerciseCategoryResponse {
  enabled: boolean
  available: boolean
  id: number
  name: string
}

export interface ExerciseTypeResponse {
  enabled: boolean
  available: boolean
  id: number
  categoryId: number
  name: string
}
