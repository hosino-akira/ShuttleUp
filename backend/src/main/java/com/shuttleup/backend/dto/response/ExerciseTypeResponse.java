package com.shuttleup.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExerciseTypeResponse {
    private Long id;
    private Long categoryId;
    private String name;
}
