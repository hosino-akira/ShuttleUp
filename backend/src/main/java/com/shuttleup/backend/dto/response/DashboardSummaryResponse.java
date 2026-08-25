package com.shuttleup.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

/** Dashboardの全体集計。 */
@Getter
@Builder
public class DashboardSummaryResponse {
    private long trainingSessionCount;
    private long totalDurationMinutes;
    private long matchCount;
    private long winCount;
    private long lossCount;
    private double winRate;
}
