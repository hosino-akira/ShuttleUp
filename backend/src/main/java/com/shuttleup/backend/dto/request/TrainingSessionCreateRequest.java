package com.shuttleup.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TrainingSessionCreateRequest {

    private Long userId;
    private LocalDate trainingDate;
    private Integer durationMinutes;
    private Integer feeling;
    private String note;
}
