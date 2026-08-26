import unicodedata
from enum import StrEnum

PHYSICAL_CATEGORY_KEYWORDS = ("フィジカル", "筋力", "体力", "ウエイト", "ウェイト", "ストレングス", "コンディショニング", "有酸素")
SKILL_CATEGORY_KEYWORDS = ("バドミントン", "技術", "スキル", "ノック", "フットワーク", "ショット", "ラケット")


class TrainingKind(StrEnum):
    PHYSICAL = "PHYSICAL"
    SKILL = "SKILL"
    OTHER = "OTHER"


def normalize_category(value: str | None) -> str:
    return unicodedata.normalize("NFKC", value or "").strip().casefold()


def classify_category(value: str | None) -> tuple[TrainingKind, bool]:
    """カテゴリ名を分類し、複数分類への一致有無も返す。"""
    normalized = normalize_category(value)
    physical = any(normalize_category(word) in normalized for word in PHYSICAL_CATEGORY_KEYWORDS)
    skill = any(normalize_category(word) in normalized for word in SKILL_CATEGORY_KEYWORDS)
    if physical and skill:
        return TrainingKind.OTHER, True
    if physical:
        return TrainingKind.PHYSICAL, False
    if skill:
        return TrainingKind.SKILL, False
    return TrainingKind.OTHER, False
