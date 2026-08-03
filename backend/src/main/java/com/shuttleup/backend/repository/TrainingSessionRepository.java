package com.shuttleup.backend.repository;

import com.shuttleup.backend.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;

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
}



