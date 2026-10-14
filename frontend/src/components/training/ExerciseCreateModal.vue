<script setup lang="ts">
import { t } from '../../i18n';
import { computed, reactive, ref, watch } from "vue"
import { message } from "ant-design-vue"
import type { FormInstance } from "ant-design-vue"
import { useLocalizedForm } from '../../composables/useLocalizedForm'
import { createExercise, getExerciseCategories, getExerciseTypes } from "../../api/exerciseApi"
import type { ExerciseCategoryResponse, ExerciseResponse, ExerciseTypeResponse } from "../../types/exercise"
import BaseSelect from "../common/BaseSelect.vue"
import BaseSmallModal from "../common/BaseSmallModal.vue"

const props = defineProps<{
  open: boolean
  userId: number
  initialCategoryId?: number
  initialExerciseTypeId?: number
}>()
const emit = defineEmits<{
  "update:open": [value: boolean]
  created: [exercise: ExerciseResponse]
}>()

const formRef = ref<FormInstance>()
const { onValidate: onFormValidate, reset: resetLocalizedValidation } = useLocalizedForm(formRef)
const categories = ref<ExerciseCategoryResponse[]>([])
const types = ref<ExerciseTypeResponse[]>([])
const categoriesLoading = ref(false)
const typesLoading = ref(false)
const submitting = ref(false)
let typeRequestId = 0
const form = reactive({ categoryId: undefined as number | undefined, exerciseTypeId: undefined as number | undefined, name: "" })
const categoryOptions = computed(() => categories.value.map(item => ({ value: item.id, label: item.name })))
const filteredTypes = computed(() => types.value.filter(item => form.categoryId == null || item.categoryId === form.categoryId))
const typeOptions = computed(() => filteredTypes.value.map(item => ({ value: item.id, label: item.name })))

async function loadCategoriesAndTypes() {
  const requestId = ++typeRequestId
  categoriesLoading.value = true
  typesLoading.value = true
  try {
    const loaded = await getExerciseCategories()
    if (requestId !== typeRequestId) return
    categories.value = loaded
  } catch (error) {
    if (requestId === typeRequestId) {
      console.error(error); message.error(t('大分類の取得に失敗しました。'))
      typesLoading.value = false
    }
    return
  } finally { if (requestId === typeRequestId) categoriesLoading.value = false }
  try {
    const loaded = await Promise.all(categories.value.map(category => getExerciseTypes(category.id)))
    if (requestId !== typeRequestId) return
    types.value = loaded.flat()
    if (form.exerciseTypeId != null && !filteredTypes.value.some(item => item.id === form.exerciseTypeId)) {
      form.exerciseTypeId = undefined
    }
  } catch (error) {
    if (requestId !== typeRequestId) return
    console.error(error); message.error(t('中分類の取得に失敗しました。'))
  } finally { if (requestId === typeRequestId) typesLoading.value = false }
}

function handleCategoryChange(categoryId: number | undefined) {
  form.categoryId = categoryId
  if (form.exerciseTypeId != null && !filteredTypes.value.some(item => item.id === form.exerciseTypeId)) {
    form.exerciseTypeId = undefined
  }
}

function close() {
  if (!submitting.value) emit("update:open", false)
}

async function submit() {
  if (submitting.value || categoriesLoading.value || typesLoading.value) return
  try { await formRef.value?.validate() } catch { return }
  if (!form.exerciseTypeId || submitting.value) return
  submitting.value = true
  try {
    const created = await createExercise({ exerciseTypeId: form.exerciseTypeId, name: form.name.trim(), userId: props.userId })
    message.success(t('種目を登録しました。'))
    emit("created", created)
    emit("update:open", false)
  } catch (error) { console.error(error); message.error(t('種目の登録に失敗しました。')) }
  finally { submitting.value = false }
}

watch(() => props.open, async open => {
  resetLocalizedValidation()
  if (!open) {
    typeRequestId++
    categoriesLoading.value = false
    typesLoading.value = false
    types.value = []
    return
  }
  Object.assign(form, { categoryId: props.initialCategoryId, exerciseTypeId: props.initialExerciseTypeId, name: "" })
  types.value = []
  formRef.value?.clearValidate()
  await loadCategoriesAndTypes()
}, { immediate: true })
</script>

<template>
  <BaseSmallModal :open="open" :title="t('新規種目登録')" :loading="submitting" @update:open="close">
    <a-form ref="formRef" :model="form" layout="vertical" @validate="onFormValidate">
      <a-form-item :label="t('大分類')" name="categoryId">
        <BaseSelect v-model:value="form.categoryId" :options="categoryOptions" :loading="categoriesLoading" :placeholder="t('大分類を選択')" @change="handleCategoryChange" />
      </a-form-item>
      <a-form-item :label="t('中分類')" name="exerciseTypeId" :rules="[{ required: true, message: t('中分類を選択してください。') }]">
        <BaseSelect v-model:value="form.exerciseTypeId" :options="typeOptions" :loading="typesLoading" :placeholder="t('中分類を選択')" />
      </a-form-item>
      <a-form-item :label="t('種目名')" name="name" :rules="[{ required: true, whitespace: true, message: t('種目名を入力してください。') }, { max: 100, message: t('種目名は100文字以内で入力してください。') }]">
        <a-input v-model:value="form.name" :maxlength="100" :placeholder="t('種目名を入力')" @press-enter="submit" />
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="modal-footer"><a-button :disabled="submitting" @click="close">{{ t('キャンセル') }}</a-button><a-button type="primary" :disabled="categoriesLoading || typesLoading" :loading="submitting" @click="submit">{{ t('登録') }}</a-button></div>
    </template>
  </BaseSmallModal>
</template>

<style scoped>
.modal-footer { display: flex; justify-content: flex-end; gap: 8px; }
</style>
