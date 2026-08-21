<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue"
import { message } from "ant-design-vue"
import type { FormInstance } from "ant-design-vue"
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
const categories = ref<ExerciseCategoryResponse[]>([])
const types = ref<ExerciseTypeResponse[]>([])
const categoriesLoading = ref(false)
const typesLoading = ref(false)
const submitting = ref(false)
let typeRequestId = 0
const form = reactive({ categoryId: undefined as number | undefined, exerciseTypeId: undefined as number | undefined, name: "" })
const categoryOptions = computed(() => categories.value.map(item => ({ value: item.id, label: item.name })))
const typeOptions = computed(() => types.value.map(item => ({ value: item.id, label: item.name })))

async function loadCategories() {
  categoriesLoading.value = true
  try { categories.value = await getExerciseCategories() }
  catch (error) { console.error(error); message.error("大分類の取得に失敗しました。") }
  finally { categoriesLoading.value = false }
}

async function loadTypes(categoryId: number, selectedTypeId?: number) {
  const requestId = ++typeRequestId
  typesLoading.value = true
  try {
    const loaded = await getExerciseTypes(categoryId)
    if (requestId !== typeRequestId) return
    types.value = loaded
    if (selectedTypeId && loaded.some(item => item.id === selectedTypeId)) form.exerciseTypeId = selectedTypeId
  } catch (error) {
    if (requestId !== typeRequestId) return
    console.error(error); message.error("中分類の取得に失敗しました。")
  } finally { if (requestId === typeRequestId) typesLoading.value = false }
}

async function handleCategoryChange(categoryId: number | undefined) {
  form.exerciseTypeId = undefined
  types.value = []
  if (categoryId) await loadTypes(categoryId)
}

function close() {
  if (!submitting.value) emit("update:open", false)
}

async function submit() {
  try { await formRef.value?.validate() } catch { return }
  if (!form.exerciseTypeId || submitting.value) return
  submitting.value = true
  try {
    const created = await createExercise({ exerciseTypeId: form.exerciseTypeId, name: form.name.trim(), userId: props.userId })
    message.success("種目を登録しました。")
    emit("created", created)
    emit("update:open", false)
  } catch (error) { console.error(error); message.error("種目の登録に失敗しました。") }
  finally { submitting.value = false }
}

watch(() => props.open, async open => {
  if (!open) { typeRequestId++; formRef.value?.resetFields(); types.value = []; return }
  Object.assign(form, { categoryId: props.initialCategoryId, exerciseTypeId: undefined, name: "" })
  formRef.value?.clearValidate()
  if (categories.value.length === 0) await loadCategories()
  if (form.categoryId) await loadTypes(form.categoryId, props.initialExerciseTypeId)
})
</script>

<template>
  <BaseSmallModal :open="open" title="新規種目登録" :loading="submitting" @update:open="close">
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item label="大分類" name="categoryId" :rules="[{ required: true, message: '大分類を選択してください。' }]">
        <BaseSelect v-model:value="form.categoryId" :options="categoryOptions" :loading="categoriesLoading" placeholder="大分類を選択" @change="handleCategoryChange" />
      </a-form-item>
      <a-form-item label="中分類" name="exerciseTypeId" :rules="[{ required: true, message: '中分類を選択してください。' }]">
        <BaseSelect v-model:value="form.exerciseTypeId" :options="typeOptions" :loading="typesLoading" :disabled="!form.categoryId" placeholder="中分類を選択" />
      </a-form-item>
      <a-form-item label="種目名" name="name" :rules="[{ required: true, whitespace: true, message: '種目名を入力してください。' }, { max: 100, message: '種目名は100文字以内で入力してください。' }]">
        <a-input v-model:value="form.name" :maxlength="100" placeholder="種目名を入力" @press-enter="submit" />
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="modal-footer"><a-button :disabled="submitting" @click="close">キャンセル</a-button><a-button type="primary" :loading="submitting" @click="submit">登録</a-button></div>
    </template>
  </BaseSmallModal>
</template>

<style scoped>
.modal-footer { display: flex; justify-content: flex-end; gap: 8px; }
</style>
