package com.shuttleup.backend.repository;

import com.shuttleup.backend.entity.TrainingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingRecordRepository
        extends JpaRepository<TrainingRecord, Long> {

    List<TrainingRecord> findByTrainingSessionIdOrderByIdAsc(Long trainingSessionId);
}
