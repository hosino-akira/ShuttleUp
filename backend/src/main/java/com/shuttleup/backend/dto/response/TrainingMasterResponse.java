package com.shuttleup.backend.dto.response;

import java.util.List;

public record TrainingMasterResponse(List<MasterItemResponse> categories,
        List<MasterItemResponse> types, List<MasterItemResponse> exercises) { }
