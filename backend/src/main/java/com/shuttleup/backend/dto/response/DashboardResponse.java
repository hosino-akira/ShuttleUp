package com.shuttleup.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

/** Dashboard全体のレスポンス。 */
@Getter
@Builder
public class DashboardResponse {
    private DashboardSummaryResponse summary;
    private List<MonthlyTrainingResponse> monthlyTraining;
    private MatchResultSummaryResponse matchResults;
    private OpponentMatchSummaryResponse opponentMatchSummary;
    private List<ExerciseProgressResponse> exerciseProgress;
}
