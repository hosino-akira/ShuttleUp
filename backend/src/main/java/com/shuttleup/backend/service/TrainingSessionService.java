package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.training.TrainingSessionCreateRequest;
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
            TrainingSessionRepository trainingSessionRepository, UserRepository userRepository
    ) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.userRepository = userRepository;
    }

    /**
     * 指定したユーザーのトレーニング記録を、
     * トレーニング日の降順で取得する。
     *
     * @param userId ユーザーID
     * @return トレーニング記録一覧
     */
    @Transactional(readOnly = true)
    public List<TrainingSession> getTrainingSessions(Long userId) {
        return trainingSessionRepository
                .findByUserIdOrderByCreatedAtDesc((userId));
    }

    @Transactional
    public TrainingSession createTrainingSession(
            TrainingSessionCreateRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        TrainingSession trainingSession = new TrainingSession();

        trainingSession.setUser(user);
        trainingSession.setTrainingDate(request.getTrainingDate());
        trainingSession.setDurationMinutes(request.getDurationMinutes());
        trainingSession.setFeeling(request.getFeeling());
        trainingSession.setNote(request.getNote());

        LocalDateTime now = LocalDateTime.now();
        trainingSession.setCreatedAt(now);
        trainingSession.setUpdatedAt(now);

        return trainingSessionRepository.save(trainingSession);
    }
}