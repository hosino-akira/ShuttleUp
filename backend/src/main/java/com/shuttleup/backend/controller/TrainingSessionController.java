package com.shuttleup.backend.controller;

import com.shuttleup.backend.dto.request.TrainingSessionCreateRequest;
import com.shuttleup.backend.dto.request.TrainingSessionUpdateRequest;
import com.shuttleup.backend.dto.response.TrainingSessionResponse;
import com.shuttleup.backend.service.TrainingSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training-sessions")
@CrossOrigin(origins = "http://localhost:5173")
public class TrainingSessionController {

    private final TrainingSessionService trainingSessionService;

    public TrainingSessionController(TrainingSessionService trainingSessionService) {
        this.trainingSessionService = trainingSessionService;
    }

    /**
     * 指定したユーザーのトレーニング記録一覧を取得する。
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<TrainingSessionResponse>> getTrainingSessions(
            @PathVariable Long userId) {
        return ResponseEntity.ok(trainingSessionService.getTrainingSessions(userId));
    }

    /**
     * IDを指定してトレーニング記録を取得する。
     */
    @GetMapping("/{id}")
    public ResponseEntity<TrainingSessionResponse> getTrainingSessionById(
            @PathVariable Long id) {
        return ResponseEntity.ok(trainingSessionService.getTrainingSessionById(id));
    }

    /**
     * トレーニング記録を新規作成する。
     */
    @PostMapping
    public ResponseEntity<TrainingSessionResponse> createTrainingSession(
            @Valid @RequestBody TrainingSessionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainingSessionService.createTrainingSession(request));
    }

    /**
     * トレーニング記録を更新する。
     */
    @PutMapping("/{id}")
    public ResponseEntity<TrainingSessionResponse> updateTrainingSession(
            @PathVariable Long id,
            @Valid @RequestBody TrainingSessionUpdateRequest request) {
        return ResponseEntity.ok(trainingSessionService.updateTrainingSession(id, request));
    }

    /**
     * IDを指定してトレーニング記録を削除する。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainingSession(@PathVariable Long id) {
        trainingSessionService.deleteTrainingSession(id);
        return ResponseEntity.noContent().build();
    }
}
