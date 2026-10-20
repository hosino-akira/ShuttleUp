package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.response.*;
import com.shuttleup.backend.entity.MasterKind;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseMasterService {
    private final ExerciseCatalogService catalogs;

    @Transactional(readOnly = true)
    public List<ExerciseCategoryResponse> getCategories(boolean includeDisabled) {
        return catalogs.load().items(MasterKind.CATEGORY).stream()
                .filter(item -> includeDisabled || item.available())
                .map(item -> ExerciseCategoryResponse.builder().id(item.id()).name(item.name())
                        .enabled(item.enabled()).available(item.available()).build()).toList();
    }

    @Transactional(readOnly = true)
    public List<ExerciseTypeResponse> getTypes(Long categoryId, boolean includeDisabled) {
        var catalog = catalogs.load();
        catalog.category(categoryId);
        return catalog.items(MasterKind.TYPE).stream()
                .filter(item -> categoryId.equals(item.parentId()) && (includeDisabled || item.available()))
                .map(item -> ExerciseTypeResponse.builder().id(item.id()).name(item.name())
                        .categoryId(item.parentId()).enabled(item.enabled()).available(item.available()).build()).toList();
    }
}
