package com.shuttleup.backend.config;

import com.shuttleup.backend.entity.Exercise;
import com.shuttleup.backend.entity.ExerciseCategory;
import com.shuttleup.backend.entity.ExerciseType;
import com.shuttleup.backend.repository.ExerciseCategoryRepository;
import com.shuttleup.backend.repository.ExerciseRepository;
import com.shuttleup.backend.repository.ExerciseTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer {

    private final ExerciseCategoryRepository categoryRepository;
    private final ExerciseTypeRepository typeRepository;
    private final ExerciseRepository exerciseRepository;

    @Bean
    @Transactional
    CommandLineRunner initializeExerciseMasterData() {
        return args -> {
            if (categoryRepository.existsByName("フィジカル")) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();

            ExerciseCategory physicalCategory =
                    categoryRepository.save(
                            ExerciseCategory.builder()
                                    .name("フィジカル")
                                    .displayOrder(1)
                                    .enabled(true)
                                    .createdAt(now)
                                    .updatedAt(now)
                                    .build()
                    );

            ExerciseCategory badmintonCategory =
                    categoryRepository.save(
                            ExerciseCategory.builder()
                                    .name("バドミントン")
                                    .displayOrder(2)
                                    .enabled(true)
                                    .createdAt(now)
                                    .updatedAt(now)
                                    .build()
                    );

            ExerciseType strengthType =
                    typeRepository.save(
                            ExerciseType.builder()
                                    .category(physicalCategory)
                                    .name("筋力")
                                    .displayOrder(1)
                                    .enabled(true)
                                    .createdAt(now)
                                    .updatedAt(now)
                                    .build()
                    );

            ExerciseType powerType =
                    typeRepository.save(
                            ExerciseType.builder()
                                    .category(physicalCategory)
                                    .name("パワー")
                                    .displayOrder(2)
                                    .enabled(true)
                                    .createdAt(now)
                                    .updatedAt(now)
                                    .build()
                    );

            ExerciseType strokeType =
                    typeRepository.save(
                            ExerciseType.builder()
                                    .category(badmintonCategory)
                                    .name("ストローク")
                                    .displayOrder(1)
                                    .enabled(true)
                                    .createdAt(now)
                                    .updatedAt(now)
                                    .build()
                    );

            ExerciseType footworkType =
                    typeRepository.save(
                            ExerciseType.builder()
                                    .category(badmintonCategory)
                                    .name("フットワーク")
                                    .displayOrder(2)
                                    .enabled(true)
                                    .createdAt(now)
                                    .updatedAt(now)
                                    .build()
                    );

            exerciseRepository.saveAll(
                    List.of(
                            createExercise("スクワット", strengthType, now),
                            createExercise("ベンチプレス", strengthType, now),
                            createExercise("デッドリフト", strengthType, now),

                            createExercise("クリーン", powerType, now),
                            createExercise("ボックスジャンプ", powerType, now),

                            createExercise("スマッシュ", strokeType, now),
                            createExercise("クリア", strokeType, now),
                            createExercise("ドロップ", strokeType, now),

                            createExercise("フットワーク練習", footworkType, now)
                    )
            );
        };
    }

    private Exercise createExercise(
            String name,
            ExerciseType exerciseType,
            LocalDateTime now
    ) {
        return Exercise.builder()
                .exerciseType(exerciseType)
                .name(name)
                .systemPreset(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}