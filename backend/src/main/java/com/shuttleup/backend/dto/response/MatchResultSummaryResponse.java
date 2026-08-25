package com.shuttleup.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

/** 勝敗数の集計。 */
@Getter
@Builder
public class MatchResultSummaryResponse {
    private long wins;
    private long losses;
}
