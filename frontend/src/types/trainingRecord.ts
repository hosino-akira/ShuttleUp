export interface TrainingRecordResponse {
  id: number
  trainingSessionId: number
  exerciseId: number
  exerciseName: string
  exerciseTypeId: number
  exerciseTypeName: string
  categoryId: number
  categoryName: string
  sets: number | null
  repetitions: number | null
  weightKg: number | null
  durationMinutes: number | null
  distanceMeters: number | null
  successCount: number | null
  attemptCount: number | null
  note: string | null
  createdAt: string
  updatedAt: string
}

export interface TrainingRecordCreateRequest {
  exerciseId: number
  sets: number | null
  repetitions: number | null
  weightKg: number | null
  durationMinutes: number | null
  distanceMeters: number | null
  successCount: number | null
  attemptCount: number | null
  note: string | null
}

export type TrainingRecordUpdateRequest = TrainingRecordCreateRequest
