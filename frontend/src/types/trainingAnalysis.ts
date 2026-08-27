export interface AnalysisPeriod {
  from: string;
  to: string;
  days: number;
}
export interface WeeklyTrend {
  weekStart: string;
  sessionCount: number;
  trainingDays: number;
  durationMinutes: number;
}
export interface TrainingFrequency {
  sessionCount: number;
  totalDurationMinutes: number;
  averageDurationMinutes: number;
  trainingDays: number;
  trainingDaysPerWeek: number;
  longestTrainingStreakDays: number;
  currentTrainingStreakDays: number;
  daysSinceLastTraining: number | null;
  weeklyTrend: WeeklyTrend[];
}
export interface WeightTrend {
  date: string;
  maxWeightKg: number;
}
export interface WeightProgress {
  exerciseId: number;
  exerciseName: string;
  recordCount: number;
  firstMaxWeightKg: number;
  latestMaxWeightKg: number;
  personalBestWeightKg: number;
  improvementKg: number;
  improvementRate: number | null;
  personalBestDate: string;
  trend: WeightTrend[];
}
export interface MatchSummary {
  matchCount: number;
  winCount: number;
  lossCount: number;
  winRate: number;
}
export interface OpponentSummary extends MatchSummary {
  opponentId: number;
  opponentName: string;
  latestMatchDate: string;
}
export interface MatchAnalysis {
  overall: MatchSummary;
  byOpponent: OpponentSummary[];
}
export interface TrainingBalance {
  physicalRecordCount: number;
  skillRecordCount: number;
  otherRecordCount: number;
  physicalRecordRate: number;
  skillRecordRate: number;
  otherRecordRate: number;
  physicalDurationMinutes: number;
  skillDurationMinutes: number;
  otherDurationMinutes: number;
  unallocatedDurationRecordCount: number;
  physicalDurationRate: number | null;
  skillDurationRate: number | null;
  otherDurationRate: number | null;
  unclassifiedCategories: string[];
}
export interface TrainingAnalysisResponse {
  userId: number;
  period: AnalysisPeriod;
  frequency: TrainingFrequency;
  weightProgress: WeightProgress[];
  matches: MatchAnalysis;
  trainingBalance: TrainingBalance;
  warnings: string[];
}
