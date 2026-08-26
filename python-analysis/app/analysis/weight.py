import pandas as pd
from app.models.report import WeightProgress, WeightTrend
from app.models.spring import TrainingRecord, TrainingSession


def analyze_weight(records: list[TrainingRecord], sessions: list[TrainingSession]) -> list[WeightProgress]:
    """種目別の日次最大重量と改善量を分析する。"""
    dates = {item.id: item.training_date for item in sessions}
    rows = [{"exercise_id": item.exercise_id, "exercise_name": item.exercise_name,
             "date": dates.get(item.training_session_id), "weight": item.weight_kg}
            for item in records if item.exercise_id is not None and item.weight_kg is not None
            and dates.get(item.training_session_id) is not None]
    if not rows:
        return []
    frame = pd.DataFrame(rows)
    daily = frame.groupby(["exercise_id", "exercise_name", "date"], dropna=False)["weight"].max().reset_index()
    results: list[WeightProgress] = []
    for (exercise_id, exercise_name), group in daily.groupby(["exercise_id", "exercise_name"], sort=False):
        group = group.sort_values("date")
        first, latest = float(group.iloc[0]["weight"]), float(group.iloc[-1]["weight"])
        best_index = group["weight"].idxmax()
        best = float(group.loc[best_index, "weight"])
        improvement = round(latest - first, 3)
        results.append(WeightProgress(exercise_id=int(exercise_id), exercise_name=str(exercise_name),
            record_count=int(len(frame[frame["exercise_id"] == exercise_id])), first_max_weight_kg=first,
            latest_max_weight_kg=latest, personal_best_weight_kg=best, improvement_kg=improvement,
            improvement_rate=None if first == 0 else round(improvement / first * 100, 1),
            personal_best_date=group.loc[best_index, "date"],
            trend=[WeightTrend(date=row.date, max_weight_kg=float(row.weight)) for row in group.itertuples(index=False)]))
    return results
