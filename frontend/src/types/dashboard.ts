export interface DashboardSummary {
  trainingSessionCount: number
  totalDurationMinutes: number
  matchCount: number
  winCount: number
  lossCount: number
  winRate: number
}

export interface MonthlyTraining {
  month: string
  sessionCount: number
  durationMinutes: number
}

export interface MatchResults {
  wins: number
  losses: number
}

export interface OpponentMatchSummary {
  opponentId: number
  opponentName: string
  matchCount: number
  winCount: number
  lossCount: number
  winRate: number
}

export interface ExerciseProgress {
  date: string
  maxWeightKg: number
}

export interface DashboardResponse {
  summary: DashboardSummary
  monthlyTraining: MonthlyTraining[]
  matchResults: MatchResults
  opponentMatchSummary: OpponentMatchSummary | null
  exerciseProgress: ExerciseProgress[]
}

export interface DashboardQuery {
  from?: string
  to?: string
  exerciseId?: number
  opponentId?: number
}
