export interface ExerciseResponse {
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

export type ExerciseUpdateRequest = Omit<ExerciseCreateRequest, 'userId'>

export interface ExerciseCategoryResponse {
  id: number
  name: string
}

export interface ExerciseTypeResponse {
  id: number
  categoryId: number
  name: string
}
