package com.shuttleup.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

/** Python分析サービスへ提供する業務データ。 */
@Getter
@Builder
public class AnalysisDataResponse {
    private Long userId;
    private List<TrainingSessionResponse> sessions;
    private List<AnalysisTrainingRecordResponse> records;
    private List<MatchResponse> matches;
}
