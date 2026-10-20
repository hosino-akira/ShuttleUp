package com.shuttleup.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseCreateRequest {

    @NotNull
    private Long exerciseTypeId;

    @NotNull
    private Long userId;

    @NotBlank
    @jakarta.validation.constraints.Size(max = 100)
    private String name;
}
