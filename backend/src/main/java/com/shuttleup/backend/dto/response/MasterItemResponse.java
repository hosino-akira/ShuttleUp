package com.shuttleup.backend.dto.response;

import com.shuttleup.backend.entity.TrainingKind;

/** enabledは自身のSwitch、availableは上位分類も考慮した実際の選択可否。 */
public record MasterItemResponse(Long id, Long parentId, String name, boolean enabled,
        boolean available, boolean systemPreset, TrainingKind statisticsType) { }
