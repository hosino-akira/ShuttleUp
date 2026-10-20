<script setup lang="ts">
import { t } from '../../i18n';
import {
  computed,
  reactive,
  ref,
  watch,
} from "vue";
import { message } from "ant-design-vue";
import type { FormInstance } from "ant-design-vue";
import {
  getExerciseCategories,
  getExercises,
  getExerciseTypes,
} from "../../api/exerciseApi";
import {
  createTrainingRecord,
  deleteTrainingRecord,
  updateTrainingRecord,
} from "../../api/trainingRecordApi";
import type {
  ExerciseCategoryResponse,
  ExerciseResponse,
  ExerciseTypeResponse,
} from "../../types/exercise";
import type {
  TrainingRecordCreateRequest,
  TrainingRecordResponse,
} from "../../types/trainingRecord";
import BaseLargeModal from "../common/BaseLargeModal.vue";
import ConfirmModal from "../common/ConfirmModal.vue";
import BaseSelect from "../common/BaseSelect.vue";
import ExerciseCreateModal from "./ExerciseCreateModal.vue";
import { useUnsavedChanges } from "../../composables/useUnsavedChanges";
import { useLocalizedForm } from '../../composables/useLocalizedForm';
import { useExerciseFilters } from '../../composables/useExerciseFilters';

type Mode = "create" | "edit";
type ConfirmAction = "save" | "delete";

const props = defineProps<{
  open: boolean;
  mode: Mode;
  sessionId: number;
  record: TrainingRecordResponse | null;
  userId: number;
}>();

const emit = defineEmits<{
  "update:open": [value: boolean];
  saved: [];
}>();

const formRef = ref<FormInstance>();
const { onValidate: onFormValidate, reset: resetLocalizedValidation } = useLocalizedForm(formRef);
const categories = ref<ExerciseCategoryResponse[]>([]);
const exerciseTypes = ref<ExerciseTypeResponse[]>([]);
const exercises = ref<ExerciseResponse[]>([]);
const categoriesLoading = ref(false);
const typesLoading = ref(false);
const exercisesLoading = ref(false);
const exerciseCreateOpen = ref(false);
const submitting = ref(false);
const confirmOpen = ref(false);
const confirmAction = ref<ConfirmAction>("save");
type TrainingRecordForm = Omit<
  TrainingRecordCreateRequest,
  "exerciseId"
> & {
  exerciseId?: number;
};
let typeRequestId = 0;
let exerciseRequestId = 0;

const emptyForm = (): TrainingRecordForm => ({
  exerciseId: undefined,
  sets: null,
  repetitions: null,
  weightKg: null,
  durationMinutes: null,
  distanceMeters: null,
  successCount: null,
  attemptCount: null,
  note: null,
});
const form = reactive<TrainingRecordForm>(emptyForm());
const {
  categoryId,
  exerciseTypeId,
  filteredTypes,
  filteredExercises,
  handleCategoryChange,
  handleExerciseTypeChange,
} = useExerciseFilters(exerciseTypes, exercises, form);
const { reset: resetDirtyState, confirmLeave } = useUnsavedChanges(
  () => ({
    categoryId: categoryId.value ?? null,
    exerciseTypeId: exerciseTypeId.value ?? null,
    ...form,
    note: form.note ?? "",
  }),
);
const title = computed(() =>
  props.mode === "create"
    ? t('トレーニング種目の新規登録')
    : t('トレーニング種目の編集'),
);
const categoryOptions = computed(() =>
  categories.value.filter(item => item.available || item.id === props.record?.categoryId).map((item) => ({
    value: item.id,
    label: item.available ? item.name : `${item.name} (${t('無効')})`,
    disabled: !item.available,
  })),
);
const exerciseTypeOptions = computed(() =>
  filteredTypes.value.filter(item => item.available || item.id === props.record?.exerciseTypeId).map((item) => ({
    value: item.id,
    label: item.available ? item.name : `${item.name} (${t('無効')})`,
    disabled: !item.available,
  })),
);
const exerciseOptions = computed(() =>
  filteredExercises.value.filter(item => item.available || item.id === props.record?.exerciseId).map((exercise) => ({
    value: exercise.id,
    label: exercise.available ? exercise.name : `${exercise.name} (${t('無効')})`,
    disabled: !exercise.available,
  })),
);

async function loadCategories(): Promise<void> {
  const requestId = ++typeRequestId;
  categoriesLoading.value = true;
  typesLoading.value = true;
  try {
    const loaded = await getExerciseCategories(props.mode === "edit");
    if (requestId !== typeRequestId) return;
    categories.value = loaded;
  } catch (error) {
    if (requestId === typeRequestId) {
      console.error(error);
      message.error(t('大分類の取得に失敗しました。'));
      typesLoading.value = false;
    }
    return;
  } finally {
    if (requestId === typeRequestId) categoriesLoading.value = false;
  }
  try {
    // 全大分類の中分類を取得し、選択された条件でローカルに絞り込む。
    const loaded = await Promise.all(categories.value.map(category => getExerciseTypes(category.id, props.mode === "edit")));
    if (requestId !== typeRequestId) return;
    exerciseTypes.value = loaded.flat();
  } catch (error) {
    if (requestId === typeRequestId) {
      console.error(error);
      message.error(t('中分類の取得に失敗しました。'));
    }
  } finally {
    if (requestId === typeRequestId)
      typesLoading.value = false;
  }
}

async function loadExercises(): Promise<void> {
  const requestId = ++exerciseRequestId;
  exercisesLoading.value = true;
  try {
    const loaded = await getExercises(
      undefined,
      props.userId,
      props.mode === "edit",
    );
    if (requestId !== exerciseRequestId) return;
    exercises.value = loaded;
  } catch (error: unknown) {
    if (requestId === exerciseRequestId) {
      console.error(error);
      message.error(t('種目一覧の取得に失敗しました。'));
    }
  } finally {
    if (requestId === exerciseRequestId)
      exercisesLoading.value = false;
  }
}

function handleExerciseCreated(
  exercise: ExerciseResponse,
): void {
  exerciseRequestId++;
  exercisesLoading.value = false;
  exercises.value = [...exercises.value.filter(item => item.id !== exercise.id), exercise];
  categoryId.value = exercise.categoryId;
  exerciseTypeId.value = exercise.exerciseTypeId;
  form.exerciseId = exercise.id;
}

watch(
  () => props.open,
  async (open) => {
    if (!open) {
      typeRequestId++;
      exerciseRequestId++;
      return;
    }
    Object.assign(
      form,
      props.record
        ? {
            exerciseId: props.record.exerciseId,
            sets: props.record.sets,
            repetitions: props.record.repetitions,
            weightKg: props.record.weightKg,
            durationMinutes: props.record.durationMinutes,
            distanceMeters: props.record.distanceMeters,
            successCount: props.record.successCount,
            attemptCount: props.record.attemptCount,
            note: props.record.note,
          }
        : emptyForm(),
    );
    categoryId.value = props.record?.categoryId;
    exerciseTypeId.value = props.record?.exerciseTypeId;
    exerciseTypes.value = [];
    exercises.value = [];
    formRef.value?.clearValidate();
    resetLocalizedValidation();
    resetDirtyState();
    await Promise.all([loadCategories(), loadExercises()]);
  },
  { immediate: true },
);

async function requestClose(): Promise<void> {
  if (!(await confirmLeave())) return;
  emit("update:open", false);
}

async function requestSave(): Promise<void> {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }
  confirmAction.value = "save";
  confirmOpen.value = true;
}

function requestDelete(): void {
  confirmAction.value = "delete";
  confirmOpen.value = true;
}

async function executeAction(): Promise<void> {
  submitting.value = true;
  try {
    if (confirmAction.value === "delete") {
      if (!props.record) return;
      await deleteTrainingRecord(props.record.id);
      message.success(t('トレーニング種目を削除しました。'));
    } else {
      if (form.exerciseId == null || form.exerciseId <= 0) {
        message.error(t('種目を選択してください。'));
        return;
      }

      const request: TrainingRecordCreateRequest = {
        ...form,
        exerciseId: form.exerciseId,
        note: form.note?.trim() || null,
      };
      if (props.mode === "edit" && props.record)
        await updateTrainingRecord(
          props.record.id,
          request,
        );
      else
        await createTrainingRecord(
          props.sessionId,
          request,
        );
      message.success(
        props.mode === "edit"
          ? t('トレーニング種目を更新しました。')
          : t('トレーニング種目を登録しました。'),
      );
    }
    confirmOpen.value = false;
    resetDirtyState();
    emit("update:open", false);
    emit("saved");
  } catch (error: unknown) {
    console.error(error);
    message.error(
      confirmAction.value === "delete"
        ? t('トレーニング種目の削除に失敗しました。')
        : t('トレーニング種目の保存に失敗しました。'),
    );
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <BaseLargeModal
    :open="open"
    :title="title"
    @update:open="(value) => value || requestClose()"
  >
    <a-form ref="formRef" :model="form" layout="vertical" @validate="onFormValidate">
      <a-row :gutter="12">
        <a-col :xs="24" :md="8"
          ><a-form-item :label="t('大分類')">
            <BaseSelect
              v-model:value="categoryId"
              :options="categoryOptions"
              :loading="categoriesLoading"
              :debug-name="t('大分類')"
              :placeholder="t('大分類を選択')"
              @change="handleCategoryChange"
            /> </a-form-item
        ></a-col>
        <a-col :xs="24" :md="8"
          ><a-form-item :label="t('中分類')">
            <BaseSelect
              v-model:value="exerciseTypeId"
              :options="exerciseTypeOptions"
              :loading="typesLoading"
              :placeholder="t('中分類を選択')"
              @change="handleExerciseTypeChange"
            /> </a-form-item
        ></a-col>
        <a-col :xs="24" :md="8"
          ><a-form-item
            :label="t('種目')"
            name="exerciseId"
            :rules="[
              {
                required: true,
                type: 'number',
                min: 1,
                message: t('種目を選択してください。'),
              },
            ]"
          >
            <div class="exercise-select-row">
              <BaseSelect
                v-model:value="form.exerciseId"
                :options="exerciseOptions"
                :loading="exercisesLoading"
                :placeholder="t('種目を選択')"
              /><a-button
                type="primary"
                :disabled="categoriesLoading || typesLoading || exercisesLoading"
                @click="exerciseCreateOpen = true"
                >{{ t('新規') }}</a-button
              >
            </div>
          </a-form-item></a-col
        >
      </a-row>
      <a-row :gutter="12">
        <a-col :xs="24" :md="8"
          ><a-form-item :label="t('セット数')"
            ><a-input-number
              v-model:value="form.sets"
              class="full-width"
              :min="0" /></a-form-item
        ></a-col>
        <a-col :xs="24" :md="8"
          ><a-form-item :label="t('回数')"
            ><a-input-number
              v-model:value="form.repetitions"
              class="full-width"
              :min="0" /></a-form-item
        ></a-col>
        <a-col :xs="24" :md="8"
          ><a-form-item :label="t('重量（kg）')"
            ><a-input-number
              v-model:value="form.weightKg"
              class="full-width"
              :min="0"
              :step="0.5" /></a-form-item
        ></a-col>
      </a-row>
      <a-row :gutter="12">
        <a-col :xs="24" :md="12"
          ><a-form-item :label="t('実施時間（分）')"
            ><a-input-number
              v-model:value="form.durationMinutes"
              class="full-width"
              :min="0" /></a-form-item
        ></a-col>
        <a-col :xs="24" :md="12"
          ><a-form-item :label="t('距離（m）')"
            ><a-input-number
              v-model:value="form.distanceMeters"
              class="full-width"
              :min="0" /></a-form-item
        ></a-col>
      </a-row>
      <a-row :gutter="12">
        <a-col :xs="24" :md="12"
          ><a-form-item :label="t('成功回数')"
            ><a-input-number
              v-model:value="form.successCount"
              class="full-width"
              :min="0" /></a-form-item
        ></a-col>
        <a-col :xs="24" :md="12"
          ><a-form-item :label="t('試行回数')"
            ><a-input-number
              v-model:value="form.attemptCount"
              class="full-width"
              :min="0" /></a-form-item
        ></a-col>
      </a-row>
      <a-form-item :label="t('メモ')"
        ><a-textarea
          v-model:value="form.note"
          :maxlength="1000"
          :rows="2"
          show-count
      /></a-form-item>
    </a-form>

    <template #footer>
      <div class="modal-footer">
        <a-button
          v-if="mode === 'edit'"
          type="primary"
          danger
          :disabled="submitting"
          @click="requestDelete"
          >{{ t('削除') }}</a-button
        >
        <span v-else />
        <div class="footer-actions">
          <a-button
            :disabled="submitting"
            @click="requestClose"
            >{{ t('キャンセル') }}</a-button
          >
          <a-button
            type="primary"
            :loading="submitting && confirmOpen"
            @click="requestSave"
            >{{ t('保存') }}</a-button
          >
        </div>
      </div>
    </template>
  </BaseLargeModal>

  <ConfirmModal
    v-model:open="confirmOpen"
    :message="
      confirmAction === 'delete'
        ? t('削除してもよろしいですか？')
        : t('保存してもよろしいですか？')
    "
    :description="
      confirmAction === 'delete'
        ? t('削除したデータは元に戻せません。')
        : ''
    "
    :danger="confirmAction === 'delete'"
    :loading="submitting"
    :z-index="1300"
    @confirm="executeAction"
  />
  <ExerciseCreateModal
    v-model:open="exerciseCreateOpen"
    :user-id="userId"
    :initial-category-id="categoryId"
    :initial-exercise-type-id="exerciseTypeId"
    @created="handleExerciseCreated"
  />
</template>

<style scoped>
.full-width {
  width: 100%;
}
.exercise-select-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.exercise-select-row :deep(.ant-select) {
  flex: 1;
  min-width: 0;
}
.form-notice {
  margin-bottom: 16px;
}
.modal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.footer-actions {
  display: flex;
  gap: 8px;
}
</style>
