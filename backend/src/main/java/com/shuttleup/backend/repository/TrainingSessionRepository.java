package com.shuttleup.backend.repository;

import com.shuttleup.backend.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TrainingSessionRepository
        extends JpaRepository<TrainingSession, Long> {

    /**
     * ユーザーIDに紐づくトレーニング記録を、
     * トレーニング日の降順で取得する。
     */
    List<TrainingSession> findByUserIdOrderByTrainingDateDesc(Long userId);

    /**
     * 指定したユーザーと日付のトレーニング記録を取得する。
     */
    List<TrainingSession> findByUserIdAndTrainingDate(
            Long userId,
            LocalDate trainingDate
    );

    /**
     * ユーザーIDに紐づくトレーニング記録を、
     * 作成日の降順で取得する。
     */
    List<TrainingSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Dashboard用に日付と時間だけを取得する。 */
    @Query("""
            select ts.trainingDate as trainingDate, ts.durationMinutes as durationMinutes
            from TrainingSession ts
            where ts.user.id = :userId
              and (:from is null or ts.trainingDate >= :from)
              and (:to is null or ts.trainingDate <= :to)
            order by ts.trainingDate asc
            """)
    List<TrainingSummaryRow> findDashboardRows(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    interface TrainingSummaryRow {
        LocalDate getTrainingDate();
        Integer getDurationMinutes();
    }
}



