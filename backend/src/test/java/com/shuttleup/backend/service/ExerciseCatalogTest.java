package com.shuttleup.backend.service;

import com.shuttleup.backend.entity.*;
import com.shuttleup.backend.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ExerciseCatalogTest {
    private final ExerciseCategory category = ExerciseCategory.builder().id(1L).name("フィジカル")
            .statisticsType(TrainingKind.PHYSICAL).enabled(true).build();
    private final ExerciseType type = ExerciseType.builder().id(2L).category(category).name("筋力").enabled(true).build();
    private final Exercise exercise = Exercise.builder().id(3L).exerciseType(type).name("スクワット").systemPreset(true).build();

    private ExerciseCatalogService.Catalog catalog(List<ExerciseMasterSetting> settings) {
        return new ExerciseCatalogService.Catalog(List.of(category), List.of(type), List.of(exercise), settings);
    }

    @Test void parentSwitchPreservesIndividualChildSwitches() {
        var catalog = catalog(List.of());
        exercise.setEnabled(false);
        category.setEnabled(false);
        assertFalse(catalog.typeAvailable(type));
        assertFalse(catalog.exerciseAvailable(exercise));
        assertTrue(type.getEnabled());
        assertFalse(exercise.getEnabled());
        category.setEnabled(true);
        assertTrue(catalog.typeAvailable(type));
        assertFalse(catalog.exerciseAvailable(exercise));
    }

    @Test void personalNamesAndSwitchesDoNotMutateSharedData() {
        var setting = new ExerciseMasterSetting();
        setting.setTargetKind(MasterKind.CATEGORY); setting.setTargetId(1L);
        setting.setName("My renamed category"); setting.setEnabled(false);
        var mine = catalog(List.of(setting));
        var other = catalog(List.of());
        assertEquals("My renamed category", mine.exerciseResponse(exercise).getCategoryName());
        assertEquals("フィジカル", other.exerciseResponse(exercise).getCategoryName());
        assertFalse(mine.exerciseAvailable(exercise));
        assertTrue(other.exerciseAvailable(exercise));
        assertEquals(TrainingKind.PHYSICAL, category.getStatisticsType());
    }

    @Test void disabledItemsRemainInHistoricalListsButNotNewChoices() {
        exercise.setEnabled(false);
        var catalog = catalog(List.of());
        assertTrue(catalog.exerciseResponses(null, false).isEmpty());
        assertEquals(3L, catalog.exerciseResponses(null, true).getFirst().getId());
        assertThrows(BadRequestException.class, () -> catalog.requireAvailable(exercise));
    }

    @Test void inaccessibleParentsCannotBeReachedViaAnExerciseId() {
        var catalog = new ExerciseCatalogService.Catalog(List.of(), List.of(type), List.of(exercise), List.of());
        assertThrows(IllegalArgumentException.class, () -> catalog.exercise(3L));
        assertTrue(catalog.items(MasterKind.EXERCISE).isEmpty());
    }

    @Test void legacyStatisticsMigrationPreservesOldClassification() {
        assertEquals(TrainingKind.PHYSICAL, TrainingKind.fromLegacyName("フィジカル"));
        assertEquals(TrainingKind.SKILL, TrainingKind.fromLegacyName("バドミントン技術"));
        assertEquals(TrainingKind.OTHER, TrainingKind.fromLegacyName("筋力・技術"));
        assertEquals(TrainingKind.OTHER, TrainingKind.fromLegacyName("自由な名前"));
    }
}
