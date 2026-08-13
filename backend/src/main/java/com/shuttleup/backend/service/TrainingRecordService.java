package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.TrainingRecordCreateRequest;
import com.shuttleup.backend.dto.request.TrainingRecordUpdateRequest;
import com.shuttleup.backend.dto.response.TrainingRecordResponse;
import com.shuttleup.backend.entity.Exercise;
import com.shuttleup.backend.entity.ExerciseCategory;
import com.shuttleup.backend.entity.ExerciseType;
import com.shuttleup.backend.entity.TrainingRecord;
import com.shuttleup.backend.entity.TrainingSession;
import com.shuttleup.backend.repository.ExerciseRepository;
import com.shuttleup.backend.repository.TrainingRecordRepository;
import com.shuttleup.backend.repository.TrainingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrainingRecordService {

    private final TrainingRecordRepository trainingRecordRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final ExerciseRepository exerciseRepository;

    public TrainingRecordService(
            TrainingRecordRepository trainingRecordRepository,
            TrainingSessionRepository trainingSessionRepository,
            ExerciseRepository exerciseRepository) {
        this.trainingRecordRepository = trainingRecordRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.exerciseRepository = exerciseRepository;
    }

    /**
     * 指定したトレーニングセッションの記録をIDの昇順で取得する。
     */
    @Transactional(readOnly = true)
    public List<TrainingRecordResponse> getTrainingRecords(Long sessionId) {
        findTrainingSession(sessionId);
        return trainingRecordRepository.findByTrainingSessionIdOrderByIdAsc(sessionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * IDを指定してトレーニング記録を取得する。
     */
    @Transactional(readOnly = true)
    public TrainingRecordResponse getTrainingRecord(Long recordId) {
        return toResponse(findTrainingRecord(recordId));
    }

    /**
     * 指定したトレーニングセッションに記録を追加する。
     */
    @Transactional
    public TrainingRecordResponse createTrainingRecord(
            Long sessionId,
            TrainingRecordCreateRequest request) {
        TrainingSession trainingSession = findTrainingSession(sessionId);
        Exercise exercise = findExercise(request.getExerciseId());

        LocalDateTime now = LocalDateTime.now();
        TrainingRecord trainingRecord = new TrainingRecord();
        trainingRecord.setTrainingSession(trainingSession);
        trainingRecord.setExercise(exercise);
        applyValues(trainingRecord, request);
        trainingRecord.setCreatedAt(now);
        trainingRecord.setUpdatedAt(now);

        return toResponse(trainingRecordRepository.save(trainingRecord));
    }

    /**
     * トレーニング記録を更新する。所属セッションは変更しない。
     */
    @Transactional
    public TrainingRecordResponse updateTrainingRecord(
            Long recordId,
            TrainingRecordUpdateRequest request) {
        TrainingRecord trainingRecord = findTrainingRecord(recordId);
        trainingRecord.setExercise(findExercise(request.getExerciseId()));
        applyValues(trainingRecord, request);
        trainingRecord.setUpdatedAt(LocalDateTime.now());

        return toResponse(trainingRecordRepository.save(trainingRecord));
    }

    /**
     * IDを指定してトレーニング記録を削除する。
     */
    @Transactional
    public void deleteTrainingRecord(Long recordId) {
        trainingRecordRepository.delete(findTrainingRecord(recordId));
    }

    private TrainingSession findTrainingSession(Long sessionId) {
        return trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Training session not found"));
    }

    private TrainingRecord findTrainingRecord(Long recordId) {
        return trainingRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Training record not found"));
    }

    private Exercise findExercise(Long exerciseId) {
        return exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found"));
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

    private TrainingRecordResponse toResponse(TrainingRecord trainingRecord) {
        Exercise exercise = trainingRecord.getExercise();
        ExerciseType exerciseType = exercise.getExerciseType();
        ExerciseCategory category = exerciseType.getCategory();

        return TrainingRecordResponse.builder()
                .id(trainingRecord.getId())
                .trainingSessionId(trainingRecord.getTrainingSession().getId())
                .exerciseId(exercise.getId())
                .exerciseName(exercise.getName())
                .exerciseTypeId(exerciseType.getId())
                .exerciseTypeName(exerciseType.getName())
                .categoryId(category.getId())
                .categoryName(category.getName())
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
