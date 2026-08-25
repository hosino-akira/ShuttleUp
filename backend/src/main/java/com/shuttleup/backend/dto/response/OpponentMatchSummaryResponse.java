package com.shuttleup.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

/** 特定対戦相手との対戦集計。 */
@Getter
@Builder
public class OpponentMatchSummaryResponse {
    private Long opponentId;
    private String opponentName;
    private long matchCount;
    private long winCount;
    private long lossCount;
    private double winRate;
}
