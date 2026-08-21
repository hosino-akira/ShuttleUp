package com.shuttleup.backend.repository;

import com.shuttleup.backend.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExerciseRepository
        extends JpaRepository<Exercise, Long> {

    @Query("""
            select e from Exercise e
            where (:exerciseTypeId is null or e.exerciseType.id = :exerciseTypeId)
              and (e.systemPreset = true or e.user.id = :userId)
            order by e.name asc
            """)
    List<Exercise> findAvailableExercises(
            @Param("exerciseTypeId") Long exerciseTypeId,
            @Param("userId") Long userId);
}
