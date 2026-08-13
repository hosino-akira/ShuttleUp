package com.shuttleup.backend.repository;

import com.shuttleup.backend.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository
        extends JpaRepository<Exercise, Long> {
}