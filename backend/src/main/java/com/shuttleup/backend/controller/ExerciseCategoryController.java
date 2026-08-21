package com.shuttleup.backend.controller;

import com.shuttleup.backend.dto.response.ExerciseCategoryResponse;
import com.shuttleup.backend.service.ExerciseMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exercise-categories")
@CrossOrigin(origins = "http://localhost:5173")
public class ExerciseCategoryController {
    private final ExerciseMasterService exerciseMasterService;

    public ExerciseCategoryController(ExerciseMasterService exerciseMasterService) {
        this.exerciseMasterService = exerciseMasterService;
    }

    @GetMapping
    public ResponseEntity<List<ExerciseCategoryResponse>> getCategories() {
        return ResponseEntity.ok(exerciseMasterService.getCategories());
    }
}
