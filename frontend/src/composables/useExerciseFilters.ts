import { computed, ref, type Ref } from 'vue'
import type { ExerciseResponse, ExerciseTypeResponse } from '../types/exercise'

/** 大分類・中分類は任意の絞り込み条件。種目だけでも選択できる。 */
export function useExerciseFilters(
  types: Ref<ExerciseTypeResponse[]>,
  exercises: Ref<ExerciseResponse[]>,
  form: { exerciseId?: number },
) {
  const categoryId = ref<number>()
  const exerciseTypeId = ref<number>()
  const filteredTypes = computed(() => types.value.filter(type =>
    categoryId.value == null || type.categoryId === categoryId.value,
  ))
  const filteredExercises = computed(() => exercises.value.filter(exercise =>
    (categoryId.value == null || exercise.categoryId === categoryId.value)
    && (exerciseTypeId.value == null || exercise.exerciseTypeId === exerciseTypeId.value),
  ))

  function clearIncompatibleExercise() {
    if (form.exerciseId != null && !filteredExercises.value.some(exercise => exercise.id === form.exerciseId)) {
      form.exerciseId = undefined
    }
  }

  function handleCategoryChange(value: number | undefined) {
    categoryId.value = value
    if (exerciseTypeId.value != null && !filteredTypes.value.some(type => type.id === exerciseTypeId.value)) {
      exerciseTypeId.value = undefined
    }
    clearIncompatibleExercise()
  }

  function handleExerciseTypeChange(value: number | undefined) {
    exerciseTypeId.value = value
    clearIncompatibleExercise()
  }

  return { categoryId, exerciseTypeId, filteredTypes, filteredExercises, handleCategoryChange, handleExerciseTypeChange }
}
