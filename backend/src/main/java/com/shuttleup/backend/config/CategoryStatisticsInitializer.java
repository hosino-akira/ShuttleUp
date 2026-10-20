package com.shuttleup.backend.config;

import com.shuttleup.backend.entity.TrainingKind;
import com.shuttleup.backend.repository.ExerciseCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 既存ID・名称・所属を保持して、未設定の統計区分のみ一度補完する。 */
@Component
@Order(1)
@RequiredArgsConstructor
public class CategoryStatisticsInitializer implements CommandLineRunner {
    private final ExerciseCategoryRepository categories;

    @Override
    @Transactional
    public void run(String... args) {
        categories.findAll().stream().filter(category -> category.getStatisticsType() == null)
                .forEach(category -> category.setStatisticsType(TrainingKind.fromLegacyName(category.getName())));
    }
}
