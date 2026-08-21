<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { message } from "ant-design-vue";
import type { FormInstance } from "ant-design-vue";
import { getExercises } from "../../api/exerciseApi";
import {
  createTrainingRecord,
  deleteTrainingRecord,
  updateTrainingRecord,
} from "../../api/trainingRecordApi";
import type { ExerciseResponse } from "../../types/exercise";
import type {
  TrainingRecordCreateRequest,
  TrainingRecordResponse,
} from "../../types/trainingRecord";
import BaseLargeModal from "../common/BaseLargeModal.vue";
import ConfirmModal from "../common/ConfirmModal.vue";

type Mode = "create" | "edit";
type ConfirmAction = "save" | "delete";

const props = defineProps<{
  open: boolean;
  mode: Mode;
  sessionId: number;
  record: TrainingRecordResponse | null;
}>();

const emit = defineEmits<{
  "update:open": [value: boolean];
  saved: [];
}>();

const formRef = ref<FormInstance>();
const exercises = ref<ExerciseResponse[]>([]);
const exercisesLoading = ref(false);
const submitting = ref(false);
const confirmOpen = ref(false);
const confirmAction = ref<ConfirmAction>("save");

const emptyForm = (): TrainingRecordCreateRequest => ({
  exerciseId: 0,
  sets: null,
  repetitions: null,
  weightKg: null,
  durationMinutes: null,
  distanceMeters: null,
  successCount: null,
  attemptCount: null,
  note: null,
});
const form =
  reactive<TrainingRecordCreateRequest>(emptyForm());
const title = computed(() =>
  props.mode === "create"
    ? "トレーニング種目の新規登録"
    : "トレーニング種目の編集",
);
const exerciseOptions = computed(() =>
  exercises.value.map((exercise) => ({
    value: exercise.id,
    label: `${exercise.name} — ${exercise.categoryName} / ${exercise.exerciseTypeName}`,
  })),
);

async function loadExercises(): Promise<void> {
  if (exercises.value.length > 0) return;
  exercisesLoading.value = true;
  try {
    exercises.value = await getExercises();
  } catch (error: unknown) {
    console.error(error);
    message.error("種目一覧の取得に失敗しました。");
  } finally {
    exercisesLoading.value = false;
  }
}

watch(
  () => props.open,
  (open) => {
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
    formRef.value?.clearValidate();
    void loadExercises();
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
      const request = {
        ...form,
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
    :z-index="1200"
    @update:open="emit('update:open', $event)"
  >
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item
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
        <a-select
          v-model:value="form.exerciseId"
          show-search
          option-filter-prop="label"
          :options="exerciseOptions"
          :loading="exercisesLoading"
          placeholder="種目名で検索"
        />
      </a-form-item>
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
</template>

<style scoped>
.full-width {
  width: 100%;
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
