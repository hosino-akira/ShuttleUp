package com.shuttleup.backend.controller;

import com.shuttleup.backend.dto.request.ExerciseCreateRequest;
import com.shuttleup.backend.dto.request.ExerciseUpdateRequest;
import com.shuttleup.backend.dto.response.ExerciseResponse;
import com.shuttleup.backend.service.ExerciseService;
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
@RequestMapping("/api/exercises")
@CrossOrigin(origins = "http://localhost:5173")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    /**
     * すべての種目を取得する。
     */
    @GetMapping
    public ResponseEntity<List<ExerciseResponse>> getAllExercises() {
        return ResponseEntity.ok(exerciseService.getAllExercises());
    }

    /**
     * IDを指定して種目を取得する。
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponse> getExerciseById(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseService.getExerciseById(id));
    }

    /**
     * ユーザー用の種目を新規作成する。
     */
    @PostMapping
    public ResponseEntity<ExerciseResponse> createExercise(
            @RequestBody ExerciseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(exerciseService.createExercise(request));
    }

    /**
     * 種目を更新する。
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExerciseResponse> updateExercise(
            @PathVariable Long id,
            @RequestBody ExerciseUpdateRequest request) {
        return ResponseEntity.ok(exerciseService.updateExercise(id, request));
    }

    /**
     * IDを指定して種目を削除する。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExercise(@PathVariable Long id) {
        exerciseService.deleteExercise(id);
        return ResponseEntity.noContent().build();
    }
}
