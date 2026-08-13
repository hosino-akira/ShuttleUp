export interface ExerciseResponse {
  id: number
  name: string
  exerciseTypeId: number
  exerciseTypeName: string
  categoryId: number
  categoryName: string
  systemPreset: boolean
  createdAt: string
  updatedAt: string
}

export interface ExerciseCreateRequest {
  exerciseTypeId: number
  name: string
}

export type ExerciseUpdateRequest = ExerciseCreateRequest
