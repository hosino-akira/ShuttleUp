package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.TrainingSessionCreateRequest;
import com.shuttleup.backend.dto.request.TrainingSessionUpdateRequest;
import com.shuttleup.backend.dto.response.TrainingSessionResponse;
import com.shuttleup.backend.entity.TrainingSession;
import com.shuttleup.backend.entity.User;
import com.shuttleup.backend.repository.TrainingSessionRepository;
import com.shuttleup.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrainingSessionService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final UserRepository userRepository;

    public TrainingSessionService(
            TrainingSessionRepository trainingSessionRepository,
            UserRepository userRepository) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.userRepository = userRepository;
    }

    /**
     * 指定したユーザーのトレーニング記録を作成日時の降順で取得する。
     */
    @Transactional(readOnly = true)
    public List<TrainingSessionResponse> getTrainingSessions(Long userId) {
        return trainingSessionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * IDを指定してトレーニング記録を取得する。
     */
    @Transactional(readOnly = true)
    public TrainingSessionResponse getTrainingSessionById(Long id) {
        return toResponse(findTrainingSession(id));
    }

    /**
     * トレーニング記録を新規作成する。
     */
    @Transactional
    public TrainingSessionResponse createTrainingSession(
            TrainingSessionCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        TrainingSession trainingSession = new TrainingSession();
        trainingSession.setUser(user);
        trainingSession.setTrainingDate(request.getTrainingDate());
        trainingSession.setDurationMinutes(request.getDurationMinutes());
        trainingSession.setFeeling(request.getFeeling());
        trainingSession.setNote(request.getNote());
        trainingSession.setCreatedAt(now);
        trainingSession.setUpdatedAt(now);

        return toResponse(trainingSessionRepository.save(trainingSession));
    }

    /**
     * トレーニング記録を更新する。
     */
    @Transactional
    public TrainingSessionResponse updateTrainingSession(
            Long id, TrainingSessionUpdateRequest request) {
        TrainingSession trainingSession = findTrainingSession(id);
        trainingSession.setTrainingDate(request.getTrainingDate());
        trainingSession.setDurationMinutes(request.getDurationMinutes());
        trainingSession.setFeeling(request.getFeeling());
        trainingSession.setNote(request.getNote());
        trainingSession.setUpdatedAt(LocalDateTime.now());

        return toResponse(trainingSessionRepository.save(trainingSession));
    }

    /**
     * IDを指定してトレーニング記録を削除する。
     */
    @Transactional
    public void deleteTrainingSession(Long id) {
        trainingSessionRepository.delete(findTrainingSession(id));
    }

    private TrainingSession findTrainingSession(Long id) {
        return trainingSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training session not found"));
    }

    private TrainingSessionResponse toResponse(TrainingSession trainingSession) {
        return TrainingSessionResponse.builder()
                .id(trainingSession.getId())
                .userId(trainingSession.getUser().getId())
                .trainingDate(trainingSession.getTrainingDate())
                .durationMinutes(trainingSession.getDurationMinutes())
                .feeling(trainingSession.getFeeling())
                .note(trainingSession.getNote())
                .createdAt(trainingSession.getCreatedAt())
                .updatedAt(trainingSession.getUpdatedAt())
                .build();
    }
}
