package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.TrainingRecordCreateRequest;
import com.shuttleup.backend.dto.request.TrainingRecordUpdateRequest;
import com.shuttleup.backend.dto.response.TrainingRecordResponse;
import com.shuttleup.backend.entity.Exercise;
import com.shuttleup.backend.entity.ExerciseCategory;
import com.shuttleup.backend.entity.ExerciseType;
import com.shuttleup.backend.entity.TrainingRecord;
import com.shuttleup.backend.entity.TrainingSession;
import com.shuttleup.backend.repository.TrainingRecordRepository;
import com.shuttleup.backend.repository.TrainingSessionRepository;
import com.shuttleup.backend.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrainingRecordService {

    private final TrainingRecordRepository trainingRecordRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final ExerciseCatalogService catalogs;

    public TrainingRecordService(
            TrainingRecordRepository trainingRecordRepository,
            TrainingSessionRepository trainingSessionRepository,
            ExerciseCatalogService catalogs) {
        this.trainingRecordRepository = trainingRecordRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.catalogs = catalogs;
    }

    /**
     * 指定したトレーニングセッションの記録をIDの昇順で取得する。
     */
    @Transactional(readOnly = true)
    public List<TrainingRecordResponse> getTrainingRecords(Long sessionId) {
        findTrainingSession(sessionId);
        var catalog = catalogs.load();
        return trainingRecordRepository.findByTrainingSessionIdOrderByIdAsc(sessionId)
                .stream()
                .map(record -> toResponse(record, catalog))
                .toList();
    }

    /**
     * IDを指定してトレーニング記録を取得する。
     */
    @Transactional(readOnly = true)
    public TrainingRecordResponse getTrainingRecord(Long recordId) {
        return toResponse(findTrainingRecord(recordId), catalogs.load());
    }

    /**
     * 指定したトレーニングセッションに記録を追加する。
     */
    @Transactional
    public TrainingRecordResponse createTrainingRecord(
            Long sessionId,
            TrainingRecordCreateRequest request) {
        TrainingSession trainingSession = findTrainingSession(sessionId);
        var catalog = catalogs.load();
        Exercise exercise = catalog.exercise(request.getExerciseId());
        catalog.requireAvailable(exercise);

        LocalDateTime now = LocalDateTime.now();
        TrainingRecord trainingRecord = new TrainingRecord();
        trainingRecord.setTrainingSession(trainingSession);
        trainingRecord.setExercise(exercise);
        applyValues(trainingRecord, request);
        trainingRecord.setCreatedAt(now);
        trainingRecord.setUpdatedAt(now);

        return toResponse(trainingRecordRepository.save(trainingRecord), catalogs.load());
    }

    /**
     * トレーニング記録を更新する。所属セッションは変更しない。
     */
    @Transactional
    public TrainingRecordResponse updateTrainingRecord(
            Long recordId,
            TrainingRecordUpdateRequest request) {
        TrainingRecord trainingRecord = findTrainingRecord(recordId);
        var catalog = catalogs.load();
        var exercise = catalog.exercise(request.getExerciseId());
        if (!trainingRecord.getExercise().getId().equals(exercise.getId())) catalog.requireAvailable(exercise);
        trainingRecord.setExercise(exercise);
        applyValues(trainingRecord, request);
        trainingRecord.setUpdatedAt(LocalDateTime.now());

        return toResponse(trainingRecordRepository.save(trainingRecord), catalogs.load());
    }

    /**
     * IDを指定してトレーニング記録を削除する。
     */
    @Transactional
    public void deleteTrainingRecord(Long recordId) {
        trainingRecordRepository.delete(findTrainingRecord(recordId));
    }

    private TrainingSession findTrainingSession(Long sessionId) {
        TrainingSession session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("指定されたトレーニングが見つかりません。"));
        CurrentUser.requireOwner(session.getUser().getId());
        return session;
    }

    private TrainingRecord findTrainingRecord(Long recordId) {
        TrainingRecord record = trainingRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("指定されたトレーニング種目が見つかりません。"));
        CurrentUser.requireOwner(record.getTrainingSession().getUser().getId());
        return record;
    }

    private void applyValues(
            TrainingRecord trainingRecord,
            TrainingRecordCreateRequest request) {
        trainingRecord.setSets(request.getSets());
        trainingRecord.setRepetitions(request.getRepetitions());
        trainingRecord.setWeightKg(request.getWeightKg());
        trainingRecord.setDurationMinutes(request.getDurationMinutes());
        trainingRecord.setDistanceMeters(request.getDistanceMeters());
        trainingRecord.setSuccessCount(request.getSuccessCount());
        trainingRecord.setAttemptCount(request.getAttemptCount());
        trainingRecord.setNote(request.getNote());
    }

    private void applyValues(
            TrainingRecord trainingRecord,
            TrainingRecordUpdateRequest request) {
        trainingRecord.setSets(request.getSets());
        trainingRecord.setRepetitions(request.getRepetitions());
        trainingRecord.setWeightKg(request.getWeightKg());
        trainingRecord.setDurationMinutes(request.getDurationMinutes());
        trainingRecord.setDistanceMeters(request.getDistanceMeters());
        trainingRecord.setSuccessCount(request.getSuccessCount());
        trainingRecord.setAttemptCount(request.getAttemptCount());
        trainingRecord.setNote(request.getNote());
    }

    private TrainingRecordResponse toResponse(TrainingRecord trainingRecord, ExerciseCatalogService.Catalog catalog) {
        Exercise exercise = trainingRecord.getExercise();
        ExerciseType exerciseType = exercise.getExerciseType();
        ExerciseCategory category = exerciseType.getCategory();

        return TrainingRecordResponse.builder()
                .id(trainingRecord.getId())
                .trainingSessionId(trainingRecord.getTrainingSession().getId())
                .exerciseId(exercise.getId())
                .exerciseName(catalog.exerciseName(exercise))
                .exerciseTypeId(exerciseType.getId())
                .exerciseTypeName(catalog.typeName(exerciseType))
                .categoryId(category.getId())
                .categoryName(catalog.categoryName(category))
                .sets(trainingRecord.getSets())
                .repetitions(trainingRecord.getRepetitions())
                .weightKg(trainingRecord.getWeightKg())
                .durationMinutes(trainingRecord.getDurationMinutes())
                .distanceMeters(trainingRecord.getDistanceMeters())
                .successCount(trainingRecord.getSuccessCount())
                .attemptCount(trainingRecord.getAttemptCount())
                .note(trainingRecord.getNote())
                .createdAt(trainingRecord.getCreatedAt())
                .updatedAt(trainingRecord.getUpdatedAt())
                .build();
    }
}
