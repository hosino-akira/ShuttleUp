package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.response.ExerciseCategoryResponse;
import com.shuttleup.backend.dto.response.ExerciseTypeResponse;
import com.shuttleup.backend.repository.ExerciseCategoryRepository;
import com.shuttleup.backend.repository.ExerciseTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExerciseMasterService {
    private final ExerciseCategoryRepository categoryRepository;
    private final ExerciseTypeRepository typeRepository;

    public ExerciseMasterService(
            ExerciseCategoryRepository categoryRepository,
            ExerciseTypeRepository typeRepository) {
        this.categoryRepository = categoryRepository;
        this.typeRepository = typeRepository;
    }

    /** 有効な大分類を表示順で取得する。 */
    @Transactional(readOnly = true)
    public List<ExerciseCategoryResponse> getCategories() {
        return categoryRepository.findByEnabledTrueOrderByDisplayOrderAscNameAsc()
                .stream()
                .map(category -> ExerciseCategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .toList();
    }

    /** 指定した大分類に属する有効な中分類を表示順で取得する。 */
    @Transactional(readOnly = true)
    public List<ExerciseTypeResponse> getTypes(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("指定された大分類が見つかりません。");
        }
        return typeRepository
                .findByCategoryIdAndEnabledTrueOrderByDisplayOrderAscNameAsc(categoryId)
                .stream()
                .map(type -> ExerciseTypeResponse.builder()
                        .id(type.getId())
                        .categoryId(type.getCategory().getId())
                        .name(type.getName())
                        .build())
                .toList();
    }
}
