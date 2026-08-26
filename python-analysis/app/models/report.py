from datetime import date
from pydantic import BaseModel, ConfigDict


class ReportModel(BaseModel):
    model_config = ConfigDict(alias_generator=lambda name: "".join(
        word.capitalize() if index else word for index, word in enumerate(name.split("_"))
    ), populate_by_name=True)


class Period(ReportModel):
    from_: date
    to: date
    days: int
    model_config = ConfigDict(alias_generator=lambda name: "from" if name == "from_" else "".join(
        word.capitalize() if index else word for index, word in enumerate(name.split("_"))
    ), populate_by_name=True)


class WeeklyTrend(ReportModel):
    week_start: date
    session_count: int
    training_days: int
    duration_minutes: int


class Frequency(ReportModel):
    session_count: int
    total_duration_minutes: int
    average_duration_minutes: float
    training_days: int
    training_days_per_week: float
    longest_training_streak_days: int
    current_training_streak_days: int
    days_since_last_training: int | None
    weekly_trend: list[WeeklyTrend]


class WeightTrend(ReportModel):
    date: date
    max_weight_kg: float


class WeightProgress(ReportModel):
    exercise_id: int
    exercise_name: str
    record_count: int
    first_max_weight_kg: float
    latest_max_weight_kg: float
    personal_best_weight_kg: float
    improvement_kg: float
    improvement_rate: float | None
    personal_best_date: date
    trend: list[WeightTrend]


class MatchSummary(ReportModel):
    match_count: int
    win_count: int
    loss_count: int
    win_rate: float


class OpponentSummary(MatchSummary):
    opponent_id: int
    opponent_name: str
    latest_match_date: date


class MatchAnalysis(ReportModel):
    overall: MatchSummary
    by_opponent: list[OpponentSummary]


class MissingData(ReportModel):
    sessions_without_records: int
    sessions_without_feeling: int
    sessions_without_note: int
    records_without_exercise: int
    records_with_invalid_values: int


class Completeness(ReportModel):
    session_count: int
    sessions_with_records: int
    sessions_without_records: int
    record_count: int
    sessions_with_feeling: int
    sessions_with_note: int
    records_with_note: int
    core_complete_session_count: int
    core_complete_rate: float
    missing_data: MissingData


class TrainingBalance(ReportModel):
    physical_record_count: int
    skill_record_count: int
    other_record_count: int
    physical_record_rate: float
    skill_record_rate: float
    other_record_rate: float
    physical_duration_minutes: int
    skill_duration_minutes: int
    other_duration_minutes: int
    unallocated_duration_record_count: int
    physical_duration_rate: float | None
    skill_duration_rate: float | None
    other_duration_rate: float | None
    unclassified_categories: list[str]


class TrainingReport(ReportModel):
    user_id: int
    period: Period
    frequency: Frequency
    weight_progress: list[WeightProgress]
    matches: MatchAnalysis
    completeness: Completeness
    training_balance: TrainingBalance
    warnings: list[str]
