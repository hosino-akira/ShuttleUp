package com.shuttleup.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseUpdateRequest {

    private Long exerciseTypeId;
    private String name;
}
