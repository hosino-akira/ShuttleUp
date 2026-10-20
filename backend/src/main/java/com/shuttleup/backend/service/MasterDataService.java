package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.*;
import com.shuttleup.backend.dto.response.*;
import com.shuttleup.backend.entity.*;
import com.shuttleup.backend.exception.BadRequestException;
import com.shuttleup.backend.repository.*;
import com.shuttleup.backend.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MasterDataService {
    private final ExerciseCatalogService catalogs;
    private final ExerciseCategoryRepository categories;
    private final ExerciseTypeRepository types;
    private final ExerciseRepository exercises;
    private final ExerciseMasterSettingRepository settings;
    private final UserRepository users;

    @Transactional(readOnly = true)
    public TrainingMasterResponse getTraining() {
        var catalog = catalogs.load();
        return new TrainingMasterResponse(catalog.items(MasterKind.CATEGORY),
                catalog.items(MasterKind.TYPE), catalog.items(MasterKind.EXERCISE));
    }

    /** ユーザー行をロックし、プリセットを含む本人の表示名の重複登録を直列化する。 */
    private User lockUser() {
        return users.findByIdForUpdate(CurrentUser.id()).orElseThrow(
                () -> new IllegalArgumentException("指定されたユーザーが見つかりません。"));
    }

    @Transactional
    public MasterItemResponse create(MasterKind kind, MasterCreateRequest request) {
        User user = lockUser();
        var catalog = catalogs.load();
        String name = request.name().strip();
        if (kind != MasterKind.CATEGORY && request.parentId() == null)
            throw new BadRequestException("上位分類を選択してください。");
        validateName(catalog, kind, null, request.parentId(), name);
        LocalDateTime now = LocalDateTime.now();
        Long id;
        switch (kind) {
            case CATEGORY -> {
                if (request.statisticsType() == null || request.parentId() != null)
                    throw new BadRequestException("統計区分を選択してください。");
                var category = ExerciseCategory.builder().name(name).user(user).enabled(true)
                        .statisticsType(request.statisticsType()).displayOrder(1000)
                        .createdAt(now).updatedAt(now).build();
                id = categories.save(category).getId();
            }
            case TYPE -> {
                var parent = catalog.category(request.parentId());
                var type = ExerciseType.builder().name(name).user(user).category(parent).enabled(true)
                        .displayOrder(1000).createdAt(now).updatedAt(now).build();
                id = types.save(type).getId();
            }
            case EXERCISE -> {
                var parent = catalog.type(request.parentId());
                var exercise = Exercise.builder().name(name).user(user).exerciseType(parent)
                        .systemPreset(false).enabled(true).createdAt(now).updatedAt(now).build();
                id = exercises.save(exercise).getId();
            }
            default -> throw new BadRequestException("マスタの種類が無効です。");
        }
        return findItem(catalogs.load(), kind, id);
    }

    @Transactional
    public MasterItemResponse update(MasterKind kind, Long id, MasterUpdateRequest request) {
        User user = lockUser();
        var catalog = catalogs.load();
        var item = findItem(catalog, kind, id);
        String name = request.name().strip();
        // 旧データに同名がある場合でも、名前を変えずに停用・復元できる。
        if (!name.equals(item.name())) validateName(catalog, kind, id, item.parentId(), name);
        if (item.systemPreset()) {
            var setting = catalog.setting(kind, id);
            if (setting == null) {
                setting = new ExerciseMasterSetting();
                setting.setUser(user);
                setting.setTargetKind(kind);
                setting.setTargetId(id);
            }
            setting.setName(name);
            setting.setEnabled(request.enabled());
            settings.save(setting);
        } else {
            LocalDateTime now = LocalDateTime.now();
            switch (kind) {
                case CATEGORY -> {
                    var category = catalog.category(id);
                    category.setName(name); category.setEnabled(request.enabled()); category.setUpdatedAt(now);
                }
                case TYPE -> {
                    var type = catalog.type(id);
                    type.setName(name); type.setEnabled(request.enabled()); type.setUpdatedAt(now);
                }
                case EXERCISE -> {
                    var exercise = catalog.exercise(id);
                    exercise.setName(name); exercise.setEnabled(request.enabled()); exercise.setUpdatedAt(now);
                }
            }
        }
        return findItem(catalogs.load(), kind, id);
    }

    private MasterItemResponse findItem(ExerciseCatalogService.Catalog catalog, MasterKind kind, Long id) {
        return catalog.items(kind).stream().filter(item -> item.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("指定されたマスタが見つかりません。"));
    }

    private void validateName(ExerciseCatalogService.Catalog catalog, MasterKind kind, Long id,
            Long parentId, String name) {
        if (name.isBlank() || name.length() > 100) throw new BadRequestException("名前は1〜100文字で入力してください。");
        if (catalog.items(kind).stream().anyMatch(item -> !Objects.equals(item.id(), id)
                && Objects.equals(item.parentId(), parentId) && item.name().equalsIgnoreCase(name))) {
            throw new IllegalStateException("同じ名前が既に登録されています。無効な項目は再度有効にするか、別の名前を入力してください。");
        }
    }
}
