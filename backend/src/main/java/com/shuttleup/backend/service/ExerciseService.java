package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.*;
import com.shuttleup.backend.dto.response.ExerciseResponse;
import com.shuttleup.backend.entity.MasterKind;
import com.shuttleup.backend.exception.BadRequestException;
import com.shuttleup.backend.security.CurrentUser;
import com.shuttleup.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseService {
    private final ExerciseCatalogService catalogs;
    private final MasterDataService masters;
    private final UserRepository users;

    @Transactional(readOnly = true)
    public List<ExerciseResponse> getAvailableExercises(Long typeId, Long userId, boolean includeDisabled) {
        CurrentUser.requireOwner(userId);
        return catalogs.load().exerciseResponses(typeId, includeDisabled);
    }

    @Transactional(readOnly = true)
    public ExerciseResponse getExerciseById(Long id) {
        var catalog = catalogs.load();
        return catalog.exerciseResponse(catalog.exercise(id));
    }

    @Transactional
    public ExerciseResponse createExercise(ExerciseCreateRequest request) {
        CurrentUser.requireOwner(request.getUserId());
        users.findByIdForUpdate(CurrentUser.id()).orElseThrow();
        var catalog = catalogs.load();
        if (!catalog.typeAvailable(catalog.type(request.getExerciseTypeId())))
            throw new BadRequestException("無効な分類には種目を追加できません。");
        var created = masters.create(MasterKind.EXERCISE,
                new MasterCreateRequest(request.getName(), request.getExerciseTypeId(), null));
        return getExerciseById(created.id());
    }

    @Transactional
    public ExerciseResponse updateExercise(Long id, ExerciseUpdateRequest request) {
        users.findByIdForUpdate(CurrentUser.id()).orElseThrow();
        var catalog = catalogs.load();
        var exercise = catalog.exercise(id);
        if (!exercise.getExerciseType().getId().equals(request.getExerciseTypeId()))
            throw new BadRequestException("所属する分類は変更できません。");
        boolean enabled = request.getEnabled() == null
                ? catalog.exerciseResponse(exercise).getEnabled() : request.getEnabled();
        masters.update(MasterKind.EXERCISE, id, new MasterUpdateRequest(request.getName(), enabled));
        return getExerciseById(id);
    }
}
