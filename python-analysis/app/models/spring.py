from datetime import date
from pydantic import BaseModel, ConfigDict


class SpringModel(BaseModel):
    model_config = ConfigDict(alias_generator=lambda name: "".join(
        word.capitalize() if index else word for index, word in enumerate(name.split("_"))
    ), populate_by_name=True)


class TrainingSession(SpringModel):
    id: int
    user_id: int
    training_date: date
    duration_minutes: int
    feeling: int | None = None
    note: str | None = None


class TrainingRecord(SpringModel):
    id: int
    training_session_id: int
    exercise_id: int | None = None
    exercise_name: str | None = None
    category_name: str | None = None
    sets: int | None = None
    repetitions: int | None = None
    weight_kg: float | None = None
    duration_minutes: int | None = None
    distance_meters: float | None = None
    success_count: int | None = None
    attempt_count: int | None = None
    note: str | None = None


class Match(SpringModel):
    id: int
    training_session_id: int
    opponent_id: int
    opponent_name: str
    match_date: date
    my_score: int | None = None
    opponent_score: int | None = None


class AnalysisData(SpringModel):
    user_id: int
    sessions: list[TrainingSession]
    records: list[TrainingRecord]
    matches: list[Match]
