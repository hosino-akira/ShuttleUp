package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.*;
import com.shuttleup.backend.entity.*;
import com.shuttleup.backend.exception.BadRequestException;
import com.shuttleup.backend.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MasterDataServiceTest {
    private final ExerciseCatalogService catalogs = mock(ExerciseCatalogService.class);
    private final ExerciseCategoryRepository categories = mock(ExerciseCategoryRepository.class);
    private final ExerciseTypeRepository types = mock(ExerciseTypeRepository.class);
    private final ExerciseRepository exercises = mock(ExerciseRepository.class);
    private final ExerciseMasterSettingRepository settings = mock(ExerciseMasterSettingRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final User user = new User();
    private final List<ExerciseMasterSetting> overrides = new ArrayList<>();
    private final ExerciseCategory category = ExerciseCategory.builder().id(1L).name("Preset")
            .statisticsType(TrainingKind.PHYSICAL).enabled(true).build();
    private final ExerciseType type = ExerciseType.builder().id(2L).category(category).name("Type").enabled(true).build();
    private final List<Exercise> items = new ArrayList<>();
    private final MasterDataService service = new MasterDataService(catalogs, categories, types, exercises, settings, users);

    @BeforeEach void setup() {
        user.setId(10L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("10", "", List.of()));
        when(users.findByIdForUpdate(10L)).thenReturn(Optional.of(user));
        when(catalogs.load()).thenAnswer(invocation -> new ExerciseCatalogService.Catalog(
                List.of(category), List.of(type), items, overrides));
        when(settings.save(any())).thenAnswer(invocation -> {
            ExerciseMasterSetting setting = invocation.getArgument(0);
            if (!overrides.contains(setting)) overrides.add(setting);
            return setting;
        });
        when(exercises.save(any())).thenAnswer(invocation -> {
            Exercise exercise = invocation.getArgument(0); exercise.setId(100L); items.add(exercise); return exercise;
        });
    }

    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    @Test void sharedPresetUpdateCreatesAnOwnedOverride() {
        var updated = service.update(MasterKind.CATEGORY, 1L, new MasterUpdateRequest("Personal", false));
        assertEquals("Personal", updated.name());
        assertFalse(updated.enabled());
        assertEquals("Preset", category.getName());
        assertTrue(category.getEnabled());
        assertEquals(user, overrides.getFirst().getUser());
        assertEquals(TrainingKind.PHYSICAL, updated.statisticsType());
        verify(users).findByIdForUpdate(10L);
        verifyNoInteractions(categories);
    }

    @Test void ownExerciseUpdatesInPlaceAndKeepsItsParent() {
        var exercise = Exercise.builder().id(3L).name("Own").user(user).exerciseType(type).build();
        items.add(exercise);
        service.update(MasterKind.EXERCISE, 3L, new MasterUpdateRequest("New name", false));
        assertEquals("New name", exercise.getName());
        assertFalse(exercise.getEnabled());
        assertSame(type, exercise.getExerciseType());
        verifyNoInteractions(settings);
    }

    @Test void disabledDuplicateCannotBeCreated() {
        items.add(Exercise.builder().id(3L).name("Existing").user(user).exerciseType(type).enabled(false).build());
        assertThrows(IllegalStateException.class, () -> service.create(MasterKind.EXERCISE,
                new MasterCreateRequest(" existing ", 2L, null)));
        verifyNoInteractions(exercises);
    }

    @Test void aliasNamesParticipateInDuplicateChecks() {
        items.add(Exercise.builder().id(3L).name("Preset exercise").systemPreset(true).exerciseType(type).build());
        service.update(MasterKind.EXERCISE, 3L, new MasterUpdateRequest("Alias", true));
        assertThrows(IllegalStateException.class, () -> service.create(MasterKind.EXERCISE,
                new MasterCreateRequest("Alias", 2L, null)));
    }

    @Test void newExercisesAreOwnedAndReuseTheExistingParentId() {
        var created = service.create(MasterKind.EXERCISE, new MasterCreateRequest(" New ", 2L, null));
        assertEquals("New", created.name());
        assertFalse(created.systemPreset());
        assertEquals(user, items.getFirst().getUser());
        assertSame(type, items.getFirst().getExerciseType());
    }

    @Test void inaccessibleIdsCannotBeModified() {
        assertThrows(IllegalArgumentException.class, () -> service.update(MasterKind.EXERCISE, 999L,
                new MasterUpdateRequest("Not mine", true)));
        verifyNoInteractions(settings, exercises);
    }

    @Test void newCategoryRequiresAnExplicitStatisticsType() {
        assertThrows(BadRequestException.class, () -> service.create(MasterKind.CATEGORY,
                new MasterCreateRequest("New category", null, null)));
        verifyNoInteractions(categories);
    }

    @Test void legacyDuplicateNamesDoNotPreventDisablingAnItem() {
        items.add(Exercise.builder().id(3L).name("Duplicate").user(user).exerciseType(type).build());
        items.add(Exercise.builder().id(4L).name("Duplicate").systemPreset(true).exerciseType(type).build());
        assertFalse(service.update(MasterKind.EXERCISE, 3L,
                new MasterUpdateRequest("Duplicate", false)).enabled());
    }
}
