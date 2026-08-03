package com.shuttleup.backend.controller;

import com.shuttleup.backend.dto.training.TrainingSessionCreateRequest;
import com.shuttleup.backend.entity.TrainingSession;
import com.shuttleup.backend.service.TrainingSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training-sessions")
@CrossOrigin(origins = "http://localhost:5173")
public class TrainingSessionController {

    private final TrainingSessionService trainingSessionService;

    public TrainingSessionController(
            TrainingSessionService trainingSessionService) {
        this.trainingSessionService = trainingSessionService;
    }

    /**
     * ユーザーIDを指定してトレーニングセッション一覧を取得する。
     *
     * @param userId ユーザーID
     * @return トレーニングセッション一覧
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<TrainingSession>> getTrainingSessions(
            @PathVariable Long userId) {

        List<TrainingSession> sessions =
                trainingSessionService.getTrainingSessions(userId);

        return ResponseEntity.ok(sessions);
    }
    /**
     * トレーニング記録を追加
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingSession createTrainingSession(
            @RequestBody TrainingSessionCreateRequest request) {

        return trainingSessionService.createTrainingSession(request);
    }
}