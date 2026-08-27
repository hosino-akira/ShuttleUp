from datetime import date
from app.analysis.balance import analyze_balance
from app.analysis.frequency import analyze_frequency
from app.analysis.matches import analyze_matches
from app.analysis.weight import analyze_weight
from app.models.report import Period, TrainingReport
from app.models.spring import AnalysisData


def create_training_report(data: AnalysisData, from_date: date, to_date: date) -> TrainingReport:
    """取得済み業務データから分析レポートを作成する。"""
    warnings: list[str] = []
    return TrainingReport(user_id=data.user_id,
        period=Period(from_=from_date, to=to_date, days=(to_date - from_date).days + 1),
        frequency=analyze_frequency(data.sessions, from_date, to_date),
        weight_progress=analyze_weight(data.records, data.sessions),
        matches=analyze_matches(data.matches, warnings),
        training_balance=analyze_balance(data.records, warnings), warnings=warnings)
