package com.shuttleup.backend.repository;

import com.shuttleup.backend.entity.ExerciseMasterSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExerciseMasterSettingRepository extends JpaRepository<ExerciseMasterSetting, Long> {
    List<ExerciseMasterSetting> findByUserId(Long userId);
}
