export interface MatchResponse {
  id: number
  trainingSessionId: number
  opponentId: number
  opponentName: string
  matchDate: string
  myScore: number | null
  opponentScore: number | null
  videoUrl: string | null
  note: string | null
  createdAt: string
  updatedAt: string
}

export interface MatchCreateRequest {
  opponentId: number
  matchDate: string
  myScore: number | null
  opponentScore: number | null
  videoUrl: string | null
  note: string | null
}

export type MatchUpdateRequest = MatchCreateRequest
