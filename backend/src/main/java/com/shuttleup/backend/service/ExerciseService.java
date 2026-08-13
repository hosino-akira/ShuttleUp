package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.ExerciseCreateRequest;
import com.shuttleup.backend.dto.request.ExerciseUpdateRequest;
import com.shuttleup.backend.dto.response.ExerciseResponse;
import com.shuttleup.backend.entity.Exercise;
import com.shuttleup.backend.entity.ExerciseCategory;
import com.shuttleup.backend.entity.ExerciseType;
import com.shuttleup.backend.repository.ExerciseRepository;
import com.shuttleup.backend.repository.ExerciseTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;

    public ExerciseService(
            ExerciseRepository exerciseRepository,
            ExerciseTypeRepository exerciseTypeRepository) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseTypeRepository = exerciseTypeRepository;
    }

    /**
     * すべての種目を取得する。
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getAllExercises() {
        return exerciseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * IDを指定して種目を取得する。
     */
    @Transactional(readOnly = true)
    public ExerciseResponse getExerciseById(Long id) {
        return toResponse(findExercise(id));
    }

    /**
     * ユーザー用の種目を新規作成する。
     */
    @Transactional
    public ExerciseResponse createExercise(ExerciseCreateRequest request) {
        ExerciseType exerciseType = findExerciseType(request.getExerciseTypeId());

        LocalDateTime now = LocalDateTime.now();
        Exercise exercise = new Exercise();
        exercise.setExerciseType(exerciseType);
        exercise.setName(request.getName());
        exercise.setSystemPreset(false);
        exercise.setCreatedAt(now);
        exercise.setUpdatedAt(now);

        return toResponse(exerciseRepository.save(exercise));
    }

    /**
     * 種目を更新する。システムプリセット区分は変更しない。
     */
    @Transactional
    public ExerciseResponse updateExercise(Long id, ExerciseUpdateRequest request) {
        Exercise exercise = findExercise(id);
        exercise.setExerciseType(findExerciseType(request.getExerciseTypeId()));
        exercise.setName(request.getName());
        exercise.setUpdatedAt(LocalDateTime.now());

        return toResponse(exerciseRepository.save(exercise));
    }

    /**
     * IDを指定して種目を削除する。
     */
    @Transactional
    public void deleteExercise(Long id) {
        exerciseRepository.delete(findExercise(id));
    }

    private Exercise findExercise(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found"));
    }

    private ExerciseType findExerciseType(Long id) {
        return exerciseTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exercise type not found"));
    }

    private ExerciseResponse toResponse(Exercise exercise) {
        ExerciseType exerciseType = exercise.getExerciseType();
        ExerciseCategory category = exerciseType.getCategory();

        return ExerciseResponse.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .exerciseTypeId(exerciseType.getId())
                .exerciseTypeName(exerciseType.getName())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .systemPreset(exercise.getSystemPreset())
                .createdAt(exercise.getCreatedAt())
                .updatedAt(exercise.getUpdatedAt())
                .build();
    }
}
