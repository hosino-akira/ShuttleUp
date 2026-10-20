package com.shuttleup.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseUpdateRequest {

    private Boolean enabled;

    @NotNull
    private Long exerciseTypeId;

    @NotBlank
    @jakarta.validation.constraints.Size(max = 100)
    private String name;
}
