package com.shuttleup.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExerciseCategoryResponse {
    private Long id;
    private String name;
}
