export interface TrainingSession {
  id: number
  userId: number
  trainingDate: string
  durationMinutes: number
  feeling: number | null
  note: string | null
  createdAt: string
  updatedAt: string
}

export interface TrainingSessionCreateRequest {
  userId: number
  trainingDate: string
  durationMinutes: number
  feeling: number | null
  note: string | null
}

export interface TrainingSessionUpdateRequest {
  trainingDate: string
  durationMinutes: number
  feeling: number | null
  note: string | null
}
