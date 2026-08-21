package com.shuttleup.backend.controller;

import com.shuttleup.backend.dto.response.ExerciseTypeResponse;
import com.shuttleup.backend.service.ExerciseMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exercise-types")
@CrossOrigin(origins = "http://localhost:5173")
public class ExerciseTypeController {
    private final ExerciseMasterService exerciseMasterService;

    public ExerciseTypeController(ExerciseMasterService exerciseMasterService) {
        this.exerciseMasterService = exerciseMasterService;
    }

    @GetMapping
    public ResponseEntity<List<ExerciseTypeResponse>> getTypes(
            @RequestParam Long categoryId) {
        return ResponseEntity.ok(exerciseMasterService.getTypes(categoryId));
    }
}
