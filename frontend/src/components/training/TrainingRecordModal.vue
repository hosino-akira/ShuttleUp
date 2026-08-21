<script setup lang="ts">
import {
  computed,
  onMounted,
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
const categories = ref<ExerciseCategoryResponse[]>([]);
const exerciseTypes = ref<ExerciseTypeResponse[]>([]);
const exercises = ref<ExerciseResponse[]>([]);
const categoryId = ref<number>();
const exerciseTypeId = ref<number>();
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
const title = computed(() =>
  props.mode === "create"
    ? "トレーニング種目の新規登録"
    : "トレーニング種目の編集",
);
const categoryOptions = computed(() =>
  categories.value.map((item) => ({
    value: item.id,
    label: item.name,
  })),
);
const exerciseTypeOptions = computed(() =>
  exerciseTypes.value.map((item) => ({
    value: item.id,
    label: item.name,
  })),
);
const exerciseOptions = computed(() =>
  exercises.value.map((exercise) => ({
    value: exercise.id,
    label: exercise.name,
  })),
);

async function loadCategories(): Promise<void> {
  categoriesLoading.value = true;
  try {
    categories.value = await getExerciseCategories();
  } catch (error) {
    console.error(error);
    message.error("大分類の取得に失敗しました。");
  } finally {
    categoriesLoading.value = false;
  }
}

async function loadExerciseTypes(
  selectedCategoryId: number,
  selectedTypeId?: number,
): Promise<void> {
  const requestId = ++typeRequestId;
  typesLoading.value = true;
  try {
    const loaded = await getExerciseTypes(
      selectedCategoryId,
    );
    if (requestId !== typeRequestId) return;
    exerciseTypes.value = loaded;
    if (
      selectedTypeId &&
      loaded.some((item) => item.id === selectedTypeId)
    )
      exerciseTypeId.value = selectedTypeId;
  } catch (error) {
    if (requestId === typeRequestId) {
      console.error(error);
      message.error("中分類の取得に失敗しました。");
    }
  } finally {
    if (requestId === typeRequestId)
      typesLoading.value = false;
  }
}

async function loadExercises(
  selectedTypeId: number,
  selectedExerciseId?: number,
): Promise<void> {
  const requestId = ++exerciseRequestId;
  exercisesLoading.value = true;
  try {
    const loaded = await getExercises(
      selectedTypeId,
      props.userId,
    );
    if (requestId !== exerciseRequestId) return;
    exercises.value = loaded;
    if (
      selectedExerciseId &&
      loaded.some((item) => item.id === selectedExerciseId)
    )
      form.exerciseId = selectedExerciseId;
  } catch (error: unknown) {
    if (requestId === exerciseRequestId) {
      console.error(error);
      message.error("種目一覧の取得に失敗しました。");
    }
  } finally {
    if (requestId === exerciseRequestId)
      exercisesLoading.value = false;
  }
}

async function handleCategoryChange(
  value: number | undefined,
): Promise<void> {
  typeRequestId++;
  exerciseRequestId++;
  exerciseTypeId.value = undefined;
  form.exerciseId = 0;
  exerciseTypes.value = [];
  exercises.value = [];
  if (value) await loadExerciseTypes(value);
}

async function handleExerciseTypeChange(
  value: number | undefined,
): Promise<void> {
  exerciseRequestId++;
  form.exerciseId = 0;
  exercises.value = [];
  if (value) await loadExercises(value);
}

async function handleExerciseCreated(
  exercise: ExerciseResponse,
): Promise<void> {
  categoryId.value = exercise.categoryId;
  exerciseTypeId.value = undefined;
  exerciseTypes.value = [];
  exercises.value = [];
  await loadExerciseTypes(
    exercise.categoryId,
    exercise.exerciseTypeId,
  );
  await loadExercises(exercise.exerciseTypeId, exercise.id);
}

onMounted(async () => {
  await loadCategories();
});

watch(
  () => props.open,
  async (open) => {
    if (!open) return;
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
    exerciseTypeId.value = undefined;
    exerciseTypes.value = [];
    exercises.value = [];
    formRef.value?.clearValidate();
    if (categories.value.length === 0)
      await loadCategories();
    if (props.record) {
      await loadExerciseTypes(
        props.record.categoryId,
        props.record.exerciseTypeId,
      );
      await loadExercises(
        props.record.exerciseTypeId,
        props.record.exerciseId,
      );
    }
  },
);

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
      message.success("トレーニング種目を削除しました。");
    } else {
      if (form.exerciseId == null || form.exerciseId <= 0) {
        message.error("種目を選択してください。");
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
          ? "トレーニング種目を更新しました。"
          : "トレーニング種目を登録しました。",
      );
    }
    confirmOpen.value = false;
    emit("update:open", false);
    emit("saved");
  } catch (error: unknown) {
    console.error(error);
    message.error(
      confirmAction.value === "delete"
        ? "トレーニング種目の削除に失敗しました。"
        : "トレーニング種目の保存に失敗しました。",
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
    @update:open="emit('update:open', $event)"
  >
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-row :gutter="12">
        <a-col :xs="24" :md="8"
          ><a-form-item label="大分類">
            <BaseSelect
              v-model:value="categoryId"
              :options="categoryOptions"
              :loading="categoriesLoading"
              debug-name="大分類"
              placeholder="大分類を選択"
              @change="handleCategoryChange"
            /> </a-form-item
        ></a-col>
        <a-col :xs="24" :md="8"
          ><a-form-item label="中分類">
            <BaseSelect
              v-model:value="exerciseTypeId"
              :options="exerciseTypeOptions"
              :loading="typesLoading"
              :disabled="!categoryId"
              placeholder="中分類を選択"
              @change="handleExerciseTypeChange"
            /> </a-form-item
        ></a-col>
        <a-col :xs="24" :md="8"
          ><a-form-item
            label="種目"
            name="exerciseId"
            :rules="[
              {
                required: true,
                type: 'number',
                min: 1,
                message: '種目を選択してください。',
              },
            ]"
          >
            <div class="exercise-select-row">
              <BaseSelect
                v-model:value="form.exerciseId"
                :options="exerciseOptions"
                :loading="exercisesLoading"
                :disabled="!exerciseTypeId"
                placeholder="種目を選択"
              /><a-button
                type="primary"
                :disabled="categoriesLoading"
                @click="exerciseCreateOpen = true"
                >新規</a-button
              >
            </div>
          </a-form-item></a-col
        >
      </a-row>
      <a-divider orientation="left">筋力・回数</a-divider>
      <a-row :gutter="12">
        <a-col :xs="24" :md="8"
          ><a-form-item label="セット数"
            ><a-input-number
              v-model:value="form.sets"
              class="full-width"
              :min="0" /></a-form-item
        ></a-col>
        <a-col :xs="24" :md="8"
          ><a-form-item label="回数"
            ><a-input-number
              v-model:value="form.repetitions"
              class="full-width"
              :min="0" /></a-form-item
        ></a-col>
        <a-col :xs="24" :md="8"
          ><a-form-item label="重量（kg）"
            ><a-input-number
              v-model:value="form.weightKg"
              class="full-width"
              :min="0"
              :step="0.5" /></a-form-item
        ></a-col>
      </a-row>
      <a-divider orientation="left">時間・距離</a-divider>
      <a-row :gutter="12">
        <a-col :xs="24" :md="12"
          ><a-form-item label="実施時間（分）"
            ><a-input-number
              v-model:value="form.durationMinutes"
              class="full-width"
              :min="0" /></a-form-item
        ></a-col>
        <a-col :xs="24" :md="12"
          ><a-form-item label="距離（m）"
            ><a-input-number
              v-model:value="form.distanceMeters"
              class="full-width"
              :min="0" /></a-form-item
        ></a-col>
      </a-row>
      <a-divider orientation="left">成功率</a-divider>
      <a-row :gutter="12">
        <a-col :xs="24" :md="12"
          ><a-form-item label="成功回数"
            ><a-input-number
              v-model:value="form.successCount"
              class="full-width"
              :min="0" /></a-form-item
        ></a-col>
        <a-col :xs="24" :md="12"
          ><a-form-item label="試行回数"
            ><a-input-number
              v-model:value="form.attemptCount"
              class="full-width"
              :min="0" /></a-form-item
        ></a-col>
      </a-row>
      <a-form-item label="メモ"
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
          >削除</a-button
        >
        <span v-else />
        <div class="footer-actions">
          <a-button
            :disabled="submitting"
            @click="emit('update:open', false)"
            >キャンセル</a-button
          >
          <a-button
            type="primary"
            :loading="submitting && confirmOpen"
            @click="requestSave"
            >保存</a-button
          >
        </div>
      </div>
    </template>
  </BaseLargeModal>

  <ConfirmModal
    v-model:open="confirmOpen"
    :message="
      confirmAction === 'delete'
        ? '削除してもよろしいですか？'
        : '保存してもよろしいですか？'
    "
    :description="
      confirmAction === 'delete'
        ? '削除したデータは元に戻せません。'
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
