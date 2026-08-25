package com.shuttleup.backend.repository;

import com.shuttleup.backend.entity.TrainingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TrainingRecordRepository
        extends JpaRepository<TrainingRecord, Long> {

    List<TrainingRecord> findByTrainingSessionIdOrderByIdAsc(Long trainingSessionId);

    /** 指定種目の日別最大重量を取得する。 */
    @Query("""
            select tr.trainingSession.trainingDate as trainingDate,
                   max(tr.weightKg) as maxWeightKg
            from TrainingRecord tr
            where tr.trainingSession.user.id = :userId
              and tr.exercise.id = :exerciseId
              and tr.weightKg is not null
              and (:from is null or tr.trainingSession.trainingDate >= :from)
              and (:to is null or tr.trainingSession.trainingDate <= :to)
            group by tr.trainingSession.trainingDate
            order by tr.trainingSession.trainingDate asc
            """)
    List<ExerciseProgressRow> findExerciseProgress(
            @Param("userId") Long userId,
            @Param("exerciseId") Long exerciseId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    interface ExerciseProgressRow {
        LocalDate getTrainingDate();
        Double getMaxWeightKg();
    }
}
