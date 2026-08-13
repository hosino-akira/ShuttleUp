package com.shuttleup.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TrainingRecordResponse {

    private Long id;
    private Long trainingSessionId;
    private Long exerciseId;
    private String exerciseName;
    private Long exerciseTypeId;
    private String exerciseTypeName;
    private Long categoryId;
    private String categoryName;
    private Integer sets;
    private Integer repetitions;
    private Double weightKg;
    private Integer durationMinutes;
    private Double distanceMeters;
    private Integer successCount;
    private Integer attemptCount;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
