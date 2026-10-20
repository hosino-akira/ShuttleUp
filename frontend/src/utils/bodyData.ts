/** Height is entered in centimetres; BMI is derived and never persisted. */
export function formatBmi(heightCm: number | null, bodyWeightKg: number | null): string {
  if (heightCm == null || bodyWeightKg == null
      || !Number.isFinite(heightCm) || !Number.isFinite(bodyWeightKg)
      || heightCm < 0.01 || heightCm > 999.99 || bodyWeightKg < 0.01 || bodyWeightKg > 999.99) return '—'
  return (bodyWeightKg / (heightCm / 100) ** 2).toFixed(1)
}
