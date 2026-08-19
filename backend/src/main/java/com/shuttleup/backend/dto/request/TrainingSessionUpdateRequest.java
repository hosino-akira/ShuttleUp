package com.shuttleup.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TrainingSessionUpdateRequest {

    @NotNull
    private LocalDate trainingDate;

    @NotNull
    @Positive
    private Integer durationMinutes;

    @Min(1)
    @Max(5)
    private Integer feeling;
    private String note;
}
