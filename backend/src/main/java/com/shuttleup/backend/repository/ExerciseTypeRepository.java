package com.shuttleup.backend.repository;

import com.shuttleup.backend.entity.ExerciseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseTypeRepository
        extends JpaRepository<ExerciseType, Long> {

    List<ExerciseType> findByCategoryIdAndEnabledTrueOrderByDisplayOrderAscNameAsc(
            Long categoryId);
}
