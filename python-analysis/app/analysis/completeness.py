from app.analysis.common import rate
from app.models.report import Completeness, MissingData
from app.models.spring import TrainingRecord, TrainingSession


def _has_text(value: str | None) -> bool:
    return bool(value and value.strip())


def _invalid(record: TrainingRecord) -> bool:
    non_negative = [record.sets, record.repetitions, record.weight_kg, record.duration_minutes,
                    record.distance_meters, record.success_count, record.attempt_count]
    if any(value is not None and value < 0 for value in non_negative):
        return True
    return (record.success_count is not None and record.attempt_count is not None
            and record.success_count > record.attempt_count)


def analyze_completeness(sessions: list[TrainingSession], records: list[TrainingRecord]) -> Completeness:
    """分析に必要な基本データの入力完全性を評価する。"""
    session_ids_with_records = {item.training_session_id for item in records}
    with_records = sum(item.id in session_ids_with_records for item in sessions)
    with_feeling = sum(item.feeling is not None for item in sessions)
    with_note = sum(_has_text(item.note) for item in sessions)
    core = sum(item.training_date is not None and item.duration_minutes > 0
               and item.id in session_ids_with_records for item in sessions)
    missing = MissingData(sessions_without_records=len(sessions) - with_records,
        sessions_without_feeling=len(sessions) - with_feeling, sessions_without_note=len(sessions) - with_note,
        records_without_exercise=sum(item.exercise_id is None for item in records),
        records_with_invalid_values=sum(_invalid(item) for item in records))
    return Completeness(session_count=len(sessions), sessions_with_records=with_records,
        sessions_without_records=len(sessions) - with_records, record_count=len(records),
        sessions_with_feeling=with_feeling, sessions_with_note=with_note,
        records_with_note=sum(_has_text(item.note) for item in records), core_complete_session_count=core,
        core_complete_rate=rate(core, len(sessions)), missing_data=missing)
