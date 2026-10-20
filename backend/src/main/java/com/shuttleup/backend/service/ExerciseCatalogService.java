package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.response.*;
import com.shuttleup.backend.entity.*;
import com.shuttleup.backend.exception.BadRequestException;
import com.shuttleup.backend.repository.*;
import com.shuttleup.backend.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** リクエスト内で使う本人用カタログ。一覧と履歴で同じ名前・利用可否を解決する。 */
@Service
@RequiredArgsConstructor
public class ExerciseCatalogService {
    private final ExerciseCategoryRepository categoryRepository;
    private final ExerciseTypeRepository typeRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseMasterSettingRepository settingRepository;

    @Transactional(readOnly = true)
    public Catalog load() {
        Long userId = CurrentUser.id();
        return new Catalog(categoryRepository.findVisible(userId), typeRepository.findVisible(userId),
                exerciseRepository.findAvailableExercises(null, userId), settingRepository.findByUserId(userId));
    }

    public static final class Catalog {
        private record Key(MasterKind kind, Long id) { }
        private final Map<Long, ExerciseCategory> categories;
        private final Map<Long, ExerciseType> types;
        private final Map<Long, Exercise> exercises;
        private final Map<Key, ExerciseMasterSetting> settings;

        public Catalog(List<ExerciseCategory> categories, List<ExerciseType> types,
                List<Exercise> exercises, List<ExerciseMasterSetting> settings) {
            this.categories = index(categories, ExerciseCategory::getId);
            this.types = index(types, ExerciseType::getId);
            this.exercises = index(exercises, Exercise::getId);
            this.settings = settings.stream().collect(Collectors.toMap(
                    setting -> new Key(setting.getTargetKind(), setting.getTargetId()), Function.identity()));
        }

        private static <T> Map<Long, T> index(List<T> items, Function<T, Long> id) {
            return items.stream().collect(Collectors.toMap(id, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        }

        public ExerciseCategory category(Long id) {
            return required(categories.get(id));
        }

        public ExerciseType type(Long id) {
            ExerciseType type = required(types.get(id));
            category(type.getCategory().getId());
            return type;
        }

        public Exercise exercise(Long id) {
            Exercise exercise = required(exercises.get(id));
            type(exercise.getExerciseType().getId());
            return exercise;
        }

        private static <T> T required(T item) {
            if (item == null) throw new IllegalArgumentException("指定されたマスタが見つかりません。");
            return item;
        }

        public ExerciseMasterSetting setting(MasterKind kind, Long id) {
            return settings.get(new Key(kind, id));
        }

        private String name(MasterKind kind, Long id, String fallback) {
            var setting = setting(kind, id);
            return setting == null ? fallback : setting.getName();
        }

        private boolean enabled(MasterKind kind, Long id, Boolean fallback) {
            var setting = setting(kind, id);
            return Boolean.TRUE.equals(setting == null ? fallback : setting.getEnabled());
        }

        public String categoryName(ExerciseCategory category) {
            return name(MasterKind.CATEGORY, category.getId(), category.getName());
        }

        public String typeName(ExerciseType type) {
            return name(MasterKind.TYPE, type.getId(), type.getName());
        }

        public String exerciseName(Exercise exercise) {
            return name(MasterKind.EXERCISE, exercise.getId(), exercise.getName());
        }

        public boolean categoryAvailable(ExerciseCategory category) {
            return enabled(MasterKind.CATEGORY, category.getId(), category.getEnabled());
        }

        public boolean typeAvailable(ExerciseType type) {
            return enabled(MasterKind.TYPE, type.getId(), type.getEnabled())
                    && categoryAvailable(category(type.getCategory().getId()));
        }

        public boolean exerciseAvailable(Exercise exercise) {
            return enabled(MasterKind.EXERCISE, exercise.getId(), exercise.getEnabled())
                    && typeAvailable(type(exercise.getExerciseType().getId()));
        }

        public void requireAvailable(Exercise exercise) {
            if (!exerciseAvailable(exercise)) throw new BadRequestException("無効な種目は選択できません。");
        }

        public List<MasterItemResponse> items(MasterKind kind) {
            return switch (kind) {
                case CATEGORY -> categories.values().stream().map(c -> new MasterItemResponse(c.getId(), null,
                        categoryName(c), categoryAvailable(c), categoryAvailable(c), c.getUser() == null,
                        c.getStatisticsType())).toList();
                case TYPE -> types.values().stream().filter(t -> categories.containsKey(t.getCategory().getId()))
                        .map(t -> new MasterItemResponse(t.getId(), t.getCategory().getId(), typeName(t),
                                enabled(kind, t.getId(), t.getEnabled()), typeAvailable(t), t.getUser() == null, null)).toList();
                case EXERCISE -> exercises.values().stream().filter(e -> types.containsKey(e.getExerciseType().getId())
                                && categories.containsKey(e.getExerciseType().getCategory().getId()))
                        .map(e -> new MasterItemResponse(e.getId(), e.getExerciseType().getId(), exerciseName(e),
                                enabled(kind, e.getId(), e.getEnabled()), exerciseAvailable(e),
                                Boolean.TRUE.equals(e.getSystemPreset()), null)).toList();
            };
        }

        public ExerciseResponse exerciseResponse(Exercise exercise) {
            ExerciseType type = exercise.getExerciseType();
            ExerciseCategory category = type.getCategory();
            return ExerciseResponse.builder().id(exercise.getId()).name(exerciseName(exercise))
                    .exerciseTypeId(type.getId()).exerciseTypeName(typeName(type))
                    .categoryId(category.getId()).categoryName(categoryName(category))
                    .systemPreset(exercise.getSystemPreset())
                    .userId(exercise.getUser() == null ? null : exercise.getUser().getId())
                    .enabled(enabled(MasterKind.EXERCISE, exercise.getId(), exercise.getEnabled()))
                    .available(exerciseAvailable(exercise))
                    .createdAt(exercise.getCreatedAt()).updatedAt(exercise.getUpdatedAt()).build();
        }

        public List<ExerciseResponse> exerciseResponses(Long typeId, boolean includeDisabled) {
            return items(MasterKind.EXERCISE).stream()
                    .filter(item -> (typeId == null || typeId.equals(item.parentId()))
                            && (includeDisabled || item.available()))
                    .map(item -> exerciseResponse(exercise(item.id())))
                    .sorted(Comparator.comparing(ExerciseResponse::getName)).toList();
        }
    }
}
