import type { PlantType } from '../utils/forest'

export interface TrainingSessionResponse {
  id: number
  userId: number
  trainingDate: string
  durationMinutes: number
  feeling: number | null
  note: string | null
  plantType?: PlantType | null
  createdAt: string
  updatedAt: string
}

export interface TrainingSessionCreateRequest {
  userId: number
  trainingDate: string
  durationMinutes: number
  feeling: number | null
  note: string | null
  plantType?: PlantType
}

export type TrainingSession = TrainingSessionResponse

export interface TrainingSessionUpdateRequest {
  trainingDate: string
  durationMinutes: number
  feeling: number | null
  note: string | null
  plantType?: PlantType
}
