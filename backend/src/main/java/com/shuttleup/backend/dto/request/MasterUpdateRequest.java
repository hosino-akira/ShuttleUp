package com.shuttleup.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 親分類・統計区分は更新対象に含めない。 */
public record MasterUpdateRequest(@NotBlank @Size(max = 100) String name,
        @NotNull Boolean enabled) { }
