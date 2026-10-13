import type { TrainingSession } from '../types/trainingSession'

export const plantTypes = ['cedar', 'oak', 'sakura', 'maple', 'sunflower', 'lavender'] as const
export type PlantType = typeof plantTypes[number]
export const plantNames: Record<PlantType, string> = {
  cedar: 'スギ', oak: 'オーク', sakura: '桜', maple: 'カエデ', sunflower: 'ひまわり', lavender: 'ラベンダー',
}
export function isPlantType(value: unknown): value is PlantType {
  return typeof value === 'string' && plantTypes.includes(value as PlantType)
}
export function getPlantType(session: TrainingSession): PlantType {
  return isPlantType(session.plantType) ? session.plantType : 'cedar'
}
export function localDateKey(date = new Date()): string {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
/** A saved training session represents one plant. Future plans have not grown yet. */
export function forestSessions(sessions: readonly TrainingSession[], today: string): TrainingSession[] {
  return sessions.filter(session => session.trainingDate <= today)
    .sort((a, b) => a.trainingDate.localeCompare(b.trainingDate) || a.id - b.id)
}
export function trainingStreak(sessions: readonly TrainingSession[], today: string): number {
  const dates = new Set(sessions.map(session => session.trainingDate))
  const cursor = new Date(`${today}T12:00:00`)
  if (!dates.has(today)) cursor.setDate(cursor.getDate() - 1)
  let streak = 0
  while (dates.has(localDateKey(cursor))) {
    streak++
    cursor.setDate(cursor.getDate() - 1)
  }
  return streak
}
