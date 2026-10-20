package com.shuttleup.backend.dto.request;

import com.shuttleup.backend.entity.TrainingKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MasterCreateRequest(@NotBlank @Size(max = 100) String name,
        Long parentId, TrainingKind statisticsType) { }
