package com.shuttleup.backend.config;

import com.shuttleup.backend.entity.Exercise;
import com.shuttleup.backend.entity.ExerciseCategory;
import com.shuttleup.backend.entity.ExerciseType;
import com.shuttleup.backend.entity.User;
import com.shuttleup.backend.repository.ExerciseCategoryRepository;
import com.shuttleup.backend.repository.ExerciseRepository;
import com.shuttleup.backend.repository.ExerciseTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevDataInitializerTest {
    @Mock ExerciseCategoryRepository categoryRepository;
    @Mock ExerciseTypeRepository typeRepository;
    @Mock ExerciseRepository exerciseRepository;

    private final List<ExerciseCategory> categories = new ArrayList<>();
    private final List<ExerciseType> types = new ArrayList<>();
    private final List<Exercise> exercises = new ArrayList<>();
    private DevDataInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new DevDataInitializer(categoryRepository, typeRepository, exerciseRepository);
        when(categoryRepository.findAll()).thenAnswer(invocation -> new ArrayList<>(categories));
        when(typeRepository.findAll()).thenAnswer(invocation -> new ArrayList<>(types));
        when(exerciseRepository.findBySystemPresetTrue()).thenAnswer(invocation -> exercises.stream()
                .filter(exercise -> Boolean.TRUE.equals(exercise.getSystemPreset())).toList());
        // A second run must not call these save stubs.
        lenient().when(categoryRepository.save(any(ExerciseCategory.class))).thenAnswer(invocation -> {
            ExerciseCategory category = invocation.getArgument(0);
            category.setId((long) categories.size() + 1);
            categories.add(category);
            return category;
        });
        lenient().when(typeRepository.save(any(ExerciseType.class))).thenAnswer(invocation -> {
            ExerciseType type = invocation.getArgument(0);
            type.setId((long) types.size() + 1);
            types.add(type);
            return type;
        });
        lenient().when(exerciseRepository.save(any(Exercise.class))).thenAnswer(invocation -> {
            Exercise exercise = invocation.getArgument(0);
            exercise.setId((long) exercises.size() + 1);
            exercises.add(exercise);
            return exercise;
        });
    }

    @Test
    void emptyDatabaseGetsCompleteSelectableHierarchy() {
        initializer.run();

        assertThat(categories).hasSize(6).extracting(ExerciseCategory::getName).doesNotHaveDuplicates();
        assertThat(types).hasSize(36);
        assertThat(exercises).hasSize(216);
        for (int i = 0; i < categories.size(); i++) {
            ExerciseCategory category = categories.get(i);
            assertThat(category.getEnabled()).isTrue();
            assertThat(category.getDisplayOrder()).isEqualTo(i + 1);
            assertThat(category.getCreatedAt()).isNotNull();
            List<ExerciseType> children = types.stream().filter(type -> type.getCategory() == category).toList();
            assertThat(children).hasSize(6).extracting(ExerciseType::getName).doesNotHaveDuplicates();
            for (int j = 0; j < children.size(); j++) {
                ExerciseType type = children.get(j);
                assertThat(type.getEnabled()).isTrue();
                assertThat(type.getDisplayOrder()).isEqualTo(j + 1);
                List<Exercise> leaves = exercises.stream().filter(exercise -> exercise.getExerciseType() == type).toList();
                assertThat(leaves).hasSize(6).extracting(Exercise::getName).doesNotHaveDuplicates();
                leaves.forEach(exercise -> {
                    assertThat(exercise.getSystemPreset()).isTrue();
                    assertThat(exercise.getUser()).isNull();
                    assertThat(exercise.getCreatedAt()).isNotNull();
                    assertThat(exercise.getUpdatedAt()).isEqualTo(exercise.getCreatedAt());
                    assertThat(exercise.getName()).isNotBlank().hasSizeLessThanOrEqualTo(100);
                });
            }
        }
    }

    @Test
    void restartDoesNotInsertOrUpdateExistingData() {
        initializer.run();
        List<Long> originalIds = exercises.stream().map(Exercise::getId).toList();
        clearInvocations(categoryRepository, typeRepository, exerciseRepository);

        initializer.run();

        assertThat(exercises).extracting(Exercise::getId).containsExactlyElementsOf(originalIds);
        verify(categoryRepository, never()).save(any());
        verify(typeRepository, never()).save(any());
        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void originalNineExercisesAreKeptWhileMissingPresetsAreAdded() {
        LocalDateTime originalTime = LocalDateTime.of(2025, 1, 1, 0, 0);
        // Reproduce the old initializer's two categories, four types and nine exercises.
        for (var categoryPreset : ExercisePresetCatalog.categories().subList(0, 2)) {
            ExerciseCategory category = categoryRepository.save(ExerciseCategory.builder()
                    .name(categoryPreset.name()).displayOrder(categories.size() + 1).enabled(true)
                    .createdAt(originalTime).updatedAt(originalTime).build());
            for (var typePreset : categoryPreset.types().subList(0, 2)) {
                ExerciseType type = typeRepository.save(ExerciseType.builder()
                        .category(category).name(typePreset.name()).displayOrder(types.size() % 2 + 1).enabled(true)
                        .createdAt(originalTime).updatedAt(originalTime).build());
                int oldCount = switch (type.getName()) {
                    case "筋力", "ストローク" -> 3;
                    case "パワー" -> 2;
                    default -> 1;
                };
                typePreset.exercises().subList(0, oldCount).forEach(name -> exerciseRepository.save(Exercise.builder()
                        .exerciseType(type).name(name).systemPreset(true)
                        .createdAt(originalTime).updatedAt(originalTime).build()));
            }
        }
        List<Exercise> oldExercises = List.copyOf(exercises);
        categories.getFirst().setDisplayOrder(99);
        types.getFirst().setEnabled(false);

        initializer.run();

        assertThat(categories).hasSize(6);
        assertThat(types).hasSize(36);
        assertThat(exercises).hasSize(216).containsAll(oldExercises);
        oldExercises.forEach(exercise -> {
            assertThat(exercise.getCreatedAt()).isEqualTo(originalTime);
            assertThat(exercise.getUpdatedAt()).isEqualTo(originalTime);
            assertThat(exercises.get(exercise.getId().intValue() - 1)).isSameAs(exercise);
        });
        assertThat(categories.getFirst().getDisplayOrder()).isEqualTo(99);
        assertThat(types.getFirst().getEnabled()).isFalse();
    }

    @Test
    void partiallySeededDatabaseRepairsMissingLeafAndKeepsUserExercise() {
        initializer.run();
        Exercise missingPreset = exercises.removeFirst();
        Exercise customExercise = Exercise.builder().id(1000L).name(missingPreset.getName())
                .exerciseType(missingPreset.getExerciseType()).systemPreset(false)
                .user(User.builder().id(7L).build()).build();
        exercises.add(customExercise);

        initializer.run();

        assertThat(categories).hasSize(6);
        assertThat(types).hasSize(36);
        assertThat(exercises).hasSize(217).contains(customExercise);
        assertThat(exercises.stream().filter(exercise -> Boolean.TRUE.equals(exercise.getSystemPreset())))
                .hasSize(216);
        assertThat(customExercise.getUser().getId()).isEqualTo(7L);
        assertThat(customExercise.getSystemPreset()).isFalse();
    }

    @Test
    void identicalNamesUnderOtherParentsDoNotSuppressMissingData() {
        ExerciseCategory otherCategory = categoryRepository.save(ExerciseCategory.builder()
                .name("独自カテゴリー").enabled(true).build());
        ExerciseType otherType = typeRepository.save(ExerciseType.builder()
                .category(otherCategory).name("筋力").enabled(true).build());
        exerciseRepository.save(Exercise.builder().exerciseType(otherType)
                .name("スクワット").systemPreset(true).build());

        initializer.run();

        assertThat(categories).hasSize(7);
        assertThat(types).hasSize(37);
        assertThat(exercises).hasSize(217);
        assertThat(types.stream().filter(type -> type.getName().equals("筋力"))).hasSize(2);
        assertThat(exercises.stream().filter(exercise -> exercise.getName().equals("スクワット"))).hasSize(2);
    }
}
