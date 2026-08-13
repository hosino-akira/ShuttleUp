package com.shuttleup.backend.repository;

import com.shuttleup.backend.entity.ExerciseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseCategoryRepository
        extends JpaRepository<ExerciseCategory, Long> {

    boolean existsByName(String name);
}