package com.shuttleup.backend.config;

import com.shuttleup.backend.entity.Exercise;
import com.shuttleup.backend.entity.ExerciseCategory;
import com.shuttleup.backend.entity.ExerciseType;
import com.shuttleup.backend.repository.ExerciseCategoryRepository;
import com.shuttleup.backend.repository.ExerciseRepository;
import com.shuttleup.backend.repository.ExerciseTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@org.springframework.core.annotation.Order(0)
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer implements CommandLineRunner {
    private final ExerciseCategoryRepository categoryRepository;
    private final ExerciseTypeRepository typeRepository;
    private final ExerciseRepository exerciseRepository;

    private record TypeKey(Long categoryId, String name) {}
    private record ExerciseKey(Long typeId, String name) {}

    /** 起動ごとに不足分のみ補完する。既存ID・表示設定・ユーザー種目は保持する。 */
    @Override
    @Transactional
    public void run(String... args) {
        Map<String, ExerciseCategory> categories = new HashMap<>();
        categoryRepository.findAll().stream().filter(category -> category.getUser() == null).forEach(category -> categories.putIfAbsent(category.getName(), category));
        Map<TypeKey, ExerciseType> types = new HashMap<>();
        typeRepository.findAll().stream().filter(type -> type.getUser() == null).forEach(type -> types.putIfAbsent(
                new TypeKey(type.getCategory().getId(), type.getName()), type));
        Set<ExerciseKey> exercises = new HashSet<>();
        exerciseRepository.findBySystemPresetTrue().forEach(exercise -> exercises.add(
                new ExerciseKey(exercise.getExerciseType().getId(), exercise.getName())));

        LocalDateTime now = LocalDateTime.now();
        int addedCategories = 0;
        int addedTypes = 0;
        int addedExercises = 0;
        int categoryOrder = 0;
        for (var categoryPreset : ExercisePresetCatalog.categories()) {
            categoryOrder++;
            ExerciseCategory category = categories.get(categoryPreset.name());
            if (category == null) {
                category = categoryRepository.save(ExerciseCategory.builder()
                        .name(categoryPreset.name()).displayOrder(categoryOrder).enabled(true)
                        .createdAt(now).updatedAt(now).build());
                categories.put(category.getName(), category);
                addedCategories++;
            }
            int typeOrder = 0;
            for (var typePreset : categoryPreset.types()) {
                typeOrder++;
                TypeKey typeKey = new TypeKey(category.getId(), typePreset.name());
                ExerciseType type = types.get(typeKey);
                if (type == null) {
                    type = typeRepository.save(ExerciseType.builder()
                            .category(category).name(typePreset.name()).displayOrder(typeOrder).enabled(true)
                            .createdAt(now).updatedAt(now).build());
                    types.put(typeKey, type);
                    addedTypes++;
                }
                for (String name : typePreset.exercises()) {
                    if (exercises.add(new ExerciseKey(type.getId(), name))) {
                        exerciseRepository.save(Exercise.builder()
                                .exerciseType(type).name(name).systemPreset(true)
                                .createdAt(now).updatedAt(now).build());
                        addedExercises++;
                    }
                }
            }
        }
        log.info("Exercise presets added: {} categories, {} types, {} exercises",
                addedCategories, addedTypes, addedExercises);
    }
}
