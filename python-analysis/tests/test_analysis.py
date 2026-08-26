from datetime import date
from app.analysis.balance import analyze_balance
from app.analysis.classification import TrainingKind, classify_category
from app.analysis.completeness import analyze_completeness
from app.analysis.frequency import analyze_frequency
from app.analysis.matches import analyze_matches
from app.analysis.weight import analyze_weight
from app.models.spring import Match, TrainingRecord, TrainingSession


def session(id_: int, day: date, minutes: int = 60, feeling: int | None = None,
            note: str | None = None) -> TrainingSession:
    return TrainingSession(id=id_, user_id=1, training_date=day, duration_minutes=minutes,
                           feeling=feeling, note=note)


def record(id_: int, session_id: int, exercise_id: int = 1, weight: float | None = None,
           category: str = "フィジカル", duration: int | None = None) -> TrainingRecord:
    return TrainingRecord(id=id_, training_session_id=session_id, exercise_id=exercise_id,
                          exercise_name=f"種目{exercise_id}", category_name=category,
                          weight_kg=weight, duration_minutes=duration)


def test_同日を一日と数え週開始と連続日数を計算する() -> None:
    sessions = [session(1, date(2026, 8, 3)), session(2, date(2026, 8, 3)),
                session(3, date(2026, 8, 4))]
    result = analyze_frequency(sessions, date(2026, 8, 1), date(2026, 8, 4))
    assert result.training_days == 2
    assert result.longest_training_streak_days == 2
    assert result.current_training_streak_days == 2
    assert result.weekly_trend[0].week_start == date(2026, 8, 3)


def test_期間終了日に練習がなければ現在連続日数はゼロ() -> None:
    result = analyze_frequency([session(1, date(2026, 8, 2))], date(2026, 8, 1), date(2026, 8, 3))
    assert result.current_training_streak_days == 0
    assert result.days_since_last_training == 1


def test_空の頻度分析と閏年が動作する() -> None:
    result = analyze_frequency([], date(2028, 2, 29), date(2028, 2, 29))
    assert result.session_count == 0
    assert result.training_days_per_week == 0.0


def test_同日最大重量と改善率を計算しnull重量を除外する() -> None:
    sessions = [session(1, date(2026, 8, 1)), session(2, date(2026, 8, 2))]
    records = [record(1, 1, weight=50), record(2, 1, weight=55),
               record(3, 2, weight=60), record(4, 2, weight=None)]
    result = analyze_weight(records, sessions)[0]
    assert [item.max_weight_kg for item in result.trend] == [55.0, 60.0]
    assert result.personal_best_weight_kg == 60.0
    assert result.improvement_rate == 9.1


def test_初回重量ゼロの改善率はnull() -> None:
    result = analyze_weight([record(1, 1, weight=0), record(2, 2, weight=10)],
                            [session(1, date(2026, 8, 1)), session(2, date(2026, 8, 2))])[0]
    assert result.improvement_rate is None


def test_勝敗と対戦相手別成績を計算し引き分けを警告する() -> None:
    matches = [Match(id=1, training_session_id=1, opponent_id=1, opponent_name="田中",
                     match_date=date(2026, 8, 1), my_score=21, opponent_score=10),
               Match(id=2, training_session_id=1, opponent_id=1, opponent_name="田中",
                     match_date=date(2026, 8, 2), my_score=10, opponent_score=21),
               Match(id=3, training_session_id=1, opponent_id=2, opponent_name="佐藤",
                     match_date=date(2026, 8, 3), my_score=20, opponent_score=20)]
    warnings: list[str] = []
    result = analyze_matches(matches, warnings)
    assert result.overall.win_rate == 50.0
    assert result.by_opponent[0].match_count == 2
    assert len(warnings) == 1


def test_Recordなしと任意項目欠損を完全性へ反映する() -> None:
    sessions = [session(1, date(2026, 8, 1), feeling=4, note="記録"), session(2, date(2026, 8, 2))]
    result = analyze_completeness(sessions, [record(1, 1)])
    assert result.sessions_without_records == 1
    assert result.missing_data.sessions_without_feeling == 1
    assert result.missing_data.sessions_without_note == 1
    assert result.core_complete_rate == 50.0
    assert result.missing_data.records_with_invalid_values == 0


def test_カテゴリ分類と件数時間比率を計算する() -> None:
    assert classify_category(" フィジカル ")[0] == TrainingKind.PHYSICAL
    assert classify_category("バドミントン技術")[0] == TrainingKind.SKILL
    assert classify_category("その他")[0] == TrainingKind.OTHER
    assert classify_category("筋力 技術") == (TrainingKind.OTHER, True)
    warnings: list[str] = []
    result = analyze_balance([record(1, 1, category="筋力", duration=30),
                              record(2, 1, category="技術", duration=20),
                              record(3, 1, category="その他")], warnings)
    assert result.physical_record_rate == 33.3
    assert result.physical_duration_rate == 60.0
    assert result.unclassified_categories == ["その他"]


def test_時間データなしの時間比率はnull() -> None:
    result = analyze_balance([record(1, 1, duration=None)], [])
    assert result.physical_duration_rate is None
