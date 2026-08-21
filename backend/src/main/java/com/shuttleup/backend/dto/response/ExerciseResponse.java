package com.shuttleup.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ExerciseResponse {

    private Long id;
    private String name;
    private Long exerciseTypeId;
    private String exerciseTypeName;
    private Long categoryId;
    private String categoryName;
    private Boolean systemPreset;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
