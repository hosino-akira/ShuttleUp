package com.shuttleup.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrainingRecordCreateRequest {

    @NotNull
    private Long exerciseId;
    private Integer sets;
    private Integer repetitions;
    private Double weightKg;
    private Integer durationMinutes;
    private Double distanceMeters;
    private Integer successCount;
    private Integer attemptCount;
    private String note;
}
