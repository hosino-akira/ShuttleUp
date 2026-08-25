package com.shuttleup.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

/** 月別トレーニング集計。 */
@Getter
@Builder
public class MonthlyTrainingResponse {
    private String month;
    private long sessionCount;
    private long durationMinutes;
}
