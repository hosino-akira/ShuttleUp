package com.shuttleup.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseUpdateRequest {

    @NotNull
    private Long exerciseTypeId;

    @NotBlank
    private String name;
}
