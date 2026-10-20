package com.shuttleup.backend.repository;

import com.shuttleup.backend.entity.ExerciseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseTypeRepository
        extends JpaRepository<ExerciseType, Long> {

    @org.springframework.data.jpa.repository.Query("select c from ExerciseType c where c.user is null or c.user.id = :userId order by c.displayOrder, c.id")
    List<ExerciseType> findVisible(@org.springframework.data.repository.query.Param("userId") Long userId);

    List<ExerciseType> findByCategoryIdAndEnabledTrueOrderByDisplayOrderAscNameAsc(
            Long categoryId);
}
