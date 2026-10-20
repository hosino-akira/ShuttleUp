package com.shuttleup.backend.controller;

import com.shuttleup.backend.dto.request.*;
import com.shuttleup.backend.dto.response.*;
import com.shuttleup.backend.entity.MasterKind;
import com.shuttleup.backend.service.MasterDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/master-data")
@RequiredArgsConstructor
public class MasterDataController {
    private final MasterDataService service;

    @GetMapping("/training")
    public TrainingMasterResponse training() {
        return service.getTraining();
    }

    @PostMapping("/{kind}")
    @ResponseStatus(HttpStatus.CREATED)
    public MasterItemResponse create(@PathVariable MasterKind kind, @Valid @RequestBody MasterCreateRequest request) {
        return service.create(kind, request);
    }

    @PutMapping("/{kind}/{id}")
    public MasterItemResponse update(@PathVariable MasterKind kind, @PathVariable Long id,
            @Valid @RequestBody MasterUpdateRequest request) {
        return service.update(kind, id, request);
    }
}
