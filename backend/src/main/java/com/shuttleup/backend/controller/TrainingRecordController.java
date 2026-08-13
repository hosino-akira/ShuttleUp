package com.shuttleup.backend.controller;

import com.shuttleup.backend.dto.request.TrainingRecordCreateRequest;
import com.shuttleup.backend.dto.request.TrainingRecordUpdateRequest;
import com.shuttleup.backend.dto.response.TrainingRecordResponse;
import com.shuttleup.backend.service.TrainingRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class TrainingRecordController {

    private final TrainingRecordService trainingRecordService;

    public TrainingRecordController(TrainingRecordService trainingRecordService) {
        this.trainingRecordService = trainingRecordService;
    }

    /**
     * 指定したトレーニングセッションの記録一覧を取得する。
     */
    @GetMapping("/training-sessions/{sessionId}/records")
    public ResponseEntity<List<TrainingRecordResponse>> getTrainingRecords(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(trainingRecordService.getTrainingRecords(sessionId));
    }

    /**
     * IDを指定してトレーニング記録を取得する。
     */
    @GetMapping("/training-records/{recordId}")
    public ResponseEntity<TrainingRecordResponse> getTrainingRecord(
            @PathVariable Long recordId) {
        return ResponseEntity.ok(trainingRecordService.getTrainingRecord(recordId));
    }

    /**
     * 指定したトレーニングセッションに記録を追加する。
     */
    @PostMapping("/training-sessions/{sessionId}/records")
    public ResponseEntity<TrainingRecordResponse> createTrainingRecord(
            @PathVariable Long sessionId,
            @Valid @RequestBody TrainingRecordCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainingRecordService.createTrainingRecord(sessionId, request));
    }

    /**
     * トレーニング記録を更新する。
     */
    @PutMapping("/training-records/{recordId}")
    public ResponseEntity<TrainingRecordResponse> updateTrainingRecord(
            @PathVariable Long recordId,
            @Valid @RequestBody TrainingRecordUpdateRequest request) {
        return ResponseEntity.ok(
                trainingRecordService.updateTrainingRecord(recordId, request));
    }

    /**
     * IDを指定してトレーニング記録を削除する。
     */
    @DeleteMapping("/training-records/{recordId}")
    public ResponseEntity<Void> deleteTrainingRecord(@PathVariable Long recordId) {
        trainingRecordService.deleteTrainingRecord(recordId);
        return ResponseEntity.noContent().build();
    }
}
