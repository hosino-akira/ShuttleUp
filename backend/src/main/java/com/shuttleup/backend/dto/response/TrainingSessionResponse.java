package com.shuttleup.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TrainingSessionResponse {

    private Long id;
    private Long userId;
    private LocalDate trainingDate;
    private Integer durationMinutes;
    private Integer feeling;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
