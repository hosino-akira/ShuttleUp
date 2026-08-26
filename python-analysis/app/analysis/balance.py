import pandas as pd
from app.analysis.classification import TrainingKind, classify_category
from app.analysis.common import rate
from app.models.report import TrainingBalance
from app.models.spring import TrainingRecord


def analyze_balance(records: list[TrainingRecord], warnings: list[str]) -> TrainingBalance:
    """カテゴリ別の件数比率と入力済み時間比率を分析する。"""
    rows: list[dict[str, object]] = []
    unclassified: set[str] = set()
    for record in records:
        kind, ambiguous = classify_category(record.category_name)
        category = (record.category_name or "").strip()
        if kind == TrainingKind.OTHER and category:
            unclassified.add(category)
        if ambiguous:
            warnings.append(f"カテゴリ「{category}」は複数分類に一致したためOTHERに分類しました。")
        rows.append({"kind": kind.value, "duration": record.duration_minutes})
    frame = pd.DataFrame(rows, columns=["kind", "duration"])
    counts = frame["kind"].value_counts().to_dict() if not frame.empty else {}
    valid_duration = frame[frame["duration"].notna() & (frame["duration"] > 0)].copy() if not frame.empty else frame
    durations = valid_duration.groupby("kind")["duration"].sum().to_dict() if not valid_duration.empty else {}
    total_duration = int(valid_duration["duration"].sum()) if not valid_duration.empty else 0
    count_total = len(records)
    get_count = lambda kind: int(counts.get(kind.value, 0))
    get_duration = lambda kind: int(durations.get(kind.value, 0))
    return TrainingBalance(
        physical_record_count=get_count(TrainingKind.PHYSICAL), skill_record_count=get_count(TrainingKind.SKILL),
        other_record_count=get_count(TrainingKind.OTHER), physical_record_rate=rate(get_count(TrainingKind.PHYSICAL), count_total),
        skill_record_rate=rate(get_count(TrainingKind.SKILL), count_total), other_record_rate=rate(get_count(TrainingKind.OTHER), count_total),
        physical_duration_minutes=get_duration(TrainingKind.PHYSICAL), skill_duration_minutes=get_duration(TrainingKind.SKILL),
        other_duration_minutes=get_duration(TrainingKind.OTHER),
        unallocated_duration_record_count=sum(item.duration_minutes is None or item.duration_minutes <= 0 for item in records),
        physical_duration_rate=None if total_duration == 0 else rate(get_duration(TrainingKind.PHYSICAL), total_duration),
        skill_duration_rate=None if total_duration == 0 else rate(get_duration(TrainingKind.SKILL), total_duration),
        other_duration_rate=None if total_duration == 0 else rate(get_duration(TrainingKind.OTHER), total_duration),
        unclassified_categories=sorted(unclassified))
