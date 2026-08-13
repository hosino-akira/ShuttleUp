package com.shuttleup.backend.repository;

import com.shuttleup.backend.entity.ExerciseType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseTypeRepository
        extends JpaRepository<ExerciseType, Long> {
}