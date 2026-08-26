import pandas as pd
from app.analysis.common import rate
from app.models.report import MatchAnalysis, MatchSummary, OpponentSummary
from app.models.spring import Match


def analyze_matches(matches: list[Match], warnings: list[str]) -> MatchAnalysis:
    """全体および対戦相手別の勝敗を分析する。"""
    rows: list[dict[str, object]] = []
    for match in matches:
        if match.my_score is None or match.opponent_score is None or match.my_score == match.opponent_score:
            warnings.append(f"試合ID {match.id} は勝敗を判定できないため集計から除外しました。")
            continue
        rows.append({"opponent_id": match.opponent_id, "opponent_name": match.opponent_name,
                     "date": match.match_date, "win": int(match.my_score > match.opponent_score)})
    if not rows:
        return MatchAnalysis(overall=MatchSummary(match_count=0, win_count=0, loss_count=0, win_rate=0.0), by_opponent=[])
    frame = pd.DataFrame(rows)
    wins = int(frame["win"].sum())
    summaries: list[OpponentSummary] = []
    for (opponent_id, opponent_name), group in frame.groupby(["opponent_id", "opponent_name"]):
        count, opponent_wins = len(group), int(group["win"].sum())
        summaries.append(OpponentSummary(opponent_id=int(opponent_id), opponent_name=str(opponent_name),
            match_count=count, win_count=opponent_wins, loss_count=count - opponent_wins,
            win_rate=rate(opponent_wins, count), latest_match_date=max(group["date"])))
    summaries.sort(key=lambda item: (-item.match_count, -item.latest_match_date.toordinal(), item.opponent_name))
    return MatchAnalysis(overall=MatchSummary(match_count=len(frame), win_count=wins,
        loss_count=len(frame) - wins, win_rate=rate(wins, len(frame))), by_opponent=summaries)
