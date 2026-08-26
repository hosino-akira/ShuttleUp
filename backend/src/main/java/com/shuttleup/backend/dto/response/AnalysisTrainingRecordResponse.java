package com.shuttleup.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

/** 分析に必要なトレーニング明細。 */
@Getter
@Builder
public class AnalysisTrainingRecordResponse {
    private Long id;
    private Long trainingSessionId;
    private Long exerciseId;
    private String exerciseName;
    private String categoryName;
    private Integer sets;
    private Integer repetitions;
    private Double weightKg;
    private Integer durationMinutes;
    private Double distanceMeters;
    private Integer successCount;
    private Integer attemptCount;
    private String note;
}
