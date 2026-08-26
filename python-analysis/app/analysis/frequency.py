from datetime import date, timedelta
import pandas as pd
from app.analysis.common import rate
from app.models.report import Frequency, WeeklyTrend
from app.models.spring import TrainingSession


def _streak(dates: list[date]) -> int:
    longest = current = 0
    previous: date | None = None
    for value in dates:
        current = current + 1 if previous and value == previous + timedelta(days=1) else 1
        longest = max(longest, current)
        previous = value
    return longest


def analyze_frequency(sessions: list[TrainingSession], from_date: date, to_date: date) -> Frequency:
    """頻度・時間・週次推移を分析する。"""
    if not sessions:
        return Frequency(session_count=0, total_duration_minutes=0, average_duration_minutes=0.0,
                         training_days=0, training_days_per_week=0.0,
                         longest_training_streak_days=0, current_training_streak_days=0,
                         days_since_last_training=None, weekly_trend=[])
    frame = pd.DataFrame([{"date": item.training_date, "duration": item.duration_minutes} for item in sessions])
    frame["date"] = pd.to_datetime(frame["date"])
    frame["week_start"] = frame["date"] - pd.to_timedelta(frame["date"].dt.weekday, unit="D")
    weekly = frame.groupby("week_start").agg(session_count=("date", "size"), training_days=("date", "nunique"),
                                               duration_minutes=("duration", "sum")).reset_index()
    unique_dates = sorted(value.date() for value in frame["date"].drop_duplicates())
    current = 0
    cursor = to_date
    date_set = set(unique_dates)
    while cursor in date_set:
        current += 1
        cursor -= timedelta(days=1)
    total = int(frame["duration"].sum())
    period_days = (to_date - from_date).days + 1
    return Frequency(
        session_count=len(sessions), total_duration_minutes=total,
        average_duration_minutes=round(total / len(sessions), 1), training_days=len(unique_dates),
        training_days_per_week=round(len(unique_dates) / period_days * 7, 1),
        longest_training_streak_days=_streak(unique_dates), current_training_streak_days=current,
        days_since_last_training=(to_date - unique_dates[-1]).days,
        weekly_trend=[WeeklyTrend(week_start=row.week_start.date(), session_count=int(row.session_count),
                                  training_days=int(row.training_days), duration_minutes=int(row.duration_minutes))
                      for row in weekly.itertuples(index=False)],
    )
