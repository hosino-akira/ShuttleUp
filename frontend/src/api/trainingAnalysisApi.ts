import analysisClient from './analysisClient'
import type { TrainingAnalysisResponse } from '../types/trainingAnalysis'

export interface TrainingAnalysisQuery { from: string; to: string }

/** Python 分析サービスから指定期間のトレーニングレポートを取得する。 */
export async function getTrainingAnalysis(userId: number, query: TrainingAnalysisQuery): Promise<TrainingAnalysisResponse> {
  const response = await analysisClient.get<TrainingAnalysisResponse>(`/api/analysis/users/${userId}/training-report`, { params: query })
  return response.data
}
