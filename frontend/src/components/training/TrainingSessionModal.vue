<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { message } from "ant-design-vue";
import type { FormInstance } from "ant-design-vue";
import {
  createTrainingSession,
  updateTrainingSession,
} from "../../api/trainingSessionApi";
import { getTrainingRecords } from "../../api/trainingRecordApi";
import type { TrainingRecordResponse } from "../../types/trainingRecord";
import type {
  TrainingSession,
  TrainingSessionCreateRequest,
  TrainingSessionUpdateRequest,
} from "../../types/trainingSession";
import BaseLargeModal from "../common/BaseLargeModal.vue";
import ConfirmModal from "../common/ConfirmModal.vue";
import TrainingRecordModal from "./TrainingRecordModal.vue";
import TrainingRecordTable from "./TrainingRecordTable.vue";

type Mode = "create" | "edit";

const props = defineProps<{
  open: boolean;
  mode: Mode;
  session: TrainingSession | null;
  userId: number;
}>();

const emit = defineEmits<{
  "update:open": [value: boolean];
  saved: [session: TrainingSession];
}>();

const formRef = ref<FormInstance>();
const currentMode = ref<Mode>("create");
const currentSession = ref<TrainingSession | null>(null);
const records = ref<TrainingRecordResponse[]>([]);
const recordsLoading = ref(false);
const submitting = ref(false);
const confirmOpen = ref(false);
const recordModalOpen = ref(false);
const recordMode = ref<Mode>("create");
const editingRecord = ref<TrainingRecordResponse | null>(
  null,
);

const emptyForm = () => ({
  trainingDate: "",
  durationMinutes: 60,
  feeling: 3 as number | null,
  note: "",
});
const form = reactive(emptyForm());
const title = computed(() =>
  currentMode.value === "create"
    ? "トレーニング新規登録"
    : "トレーニング編集",
);
const sessionId = computed(
  () => currentSession.value?.id ?? null,
);

function applySession(
  session: TrainingSession | null,
): void {
  Object.assign(
    form,
    session
      ? {
          trainingDate: session.trainingDate,
          durationMinutes: session.durationMinutes,
          feeling: session.feeling,
          note: session.note ?? "",
        }
      : emptyForm(),
  );
  formRef.value?.clearValidate();
}

async function loadRecords(): Promise<void> {
  if (sessionId.value === null) {
    records.value = [];
    return;
  }
  recordsLoading.value = true;
  try {
    records.value = await getTrainingRecords(
      sessionId.value,
    );
  } catch (error: unknown) {
    console.error(error);
    message.error("トレーニング種目の取得に失敗しました。");
  } finally {
    recordsLoading.value = false;
  }
}

watch(
  () => props.open,
  (open) => {
    if (!open) return;
    currentMode.value = props.mode;
    currentSession.value =
      props.mode === "edit" ? props.session : null;
    records.value = [];
    recordModalOpen.value = false;
    editingRecord.value = null;
    applySession(currentSession.value);
    if (currentSession.value) void loadRecords();
  },
);

async function requestSave(): Promise<void> {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }
  confirmOpen.value = true;
}

async function saveSession(): Promise<void> {
  submitting.value = true;
  try {
    const request: TrainingSessionUpdateRequest = {
      trainingDate: form.trainingDate,
      durationMinutes: form.durationMinutes,
      feeling: form.feeling,
      note: form.note.trim() || null,
    };
    let savedSession: TrainingSession;
    if (currentSession.value) {
      savedSession = await updateTrainingSession(
        currentSession.value.id,
        request,
      );
      message.success("トレーニングを更新しました。");
    } else {
      const createRequest: TrainingSessionCreateRequest = {
        userId: props.userId,
        ...request,
      };
      savedSession =
        await createTrainingSession(createRequest);
      message.success(
        "トレーニングを登録しました。続けて種目を追加できます。",
      );
    }
    currentSession.value = savedSession;
    currentMode.value = "edit";
    applySession(savedSession);
    confirmOpen.value = false;
    emit("saved", savedSession);
    await loadRecords();
  } catch (error: unknown) {
    console.error(error);
    message.error("トレーニングの保存に失敗しました。");
  } finally {
    submitting.value = false;
  }
}

function openRecordCreate(): void {
  if (sessionId.value === null) return;
  recordMode.value = "create";
  editingRecord.value = null;
  recordModalOpen.value = true;
}

function openRecordEdit(
  record: TrainingRecordResponse,
): void {
  recordMode.value = "edit";
  editingRecord.value = record;
  recordModalOpen.value = true;
}

async function handleRecordSaved(): Promise<void> {
  await loadRecords();
}
</script>

<template>
  <BaseLargeModal
    :open="open"
    :title="title"
    @update:open="emit('update:open', $event)"
  >
    <a-typography-title :level="4"
      >基本情報</a-typography-title
    >
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-row :gutter="16">
        <a-col :xs="24" :md="12">
          <a-form-item
            label="トレーニング日"
            name="trainingDate"
            :rules="[
              {
                required: true,
                message:
                  'トレーニング日を選択してください。',
              },
            ]"
          >
            <a-date-picker
              v-model:value="form.trainingDate"
              class="full-width"
              value-format="YYYY-MM-DD"
            />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-form-item
            label="トレーニング時間（分）"
            name="durationMinutes"
            :rules="[
              {
                required: true,
                message:
                  'トレーニング時間を入力してください。',
              },
              {
                type: 'number',
                min: 1,
                message: '1分以上で入力してください。',
              },
            ]"
          >
            <a-input-number
              v-model:value="form.durationMinutes"
              class="full-width"
              :min="1"
            />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-form-item
            label="トレーニング時の感覚"
            name="feeling"
            ><a-rate v-model:value="form.feeling"
          /></a-form-item>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-form-item
            label="メモ"
            name="note"
            :rules="[
              {
                max: 1000,
                message:
                  'メモは1000文字以内で入力してください。',
              },
            ]"
          >
            <a-textarea
              v-model:value="form.note"
              :maxlength="1000"
              :rows="3"
              show-count
            />
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>

    <a-divider />
    <div class="records-header">
      <a-typography-title :level="4"
        >トレーニング種目</a-typography-title
      >
      <a-button
        type="primary"
        :disabled="sessionId === null"
        @click="openRecordCreate"
        >種目を追加</a-button
      >
    </div>
    <TrainingRecordTable
      :records="records"
      :loading="recordsLoading"
      @edit="openRecordEdit"
    />

    <template #footer>
      <div class="footer-actions">
        <a-button
          :disabled="submitting"
          @click="emit('update:open', false)"
          >キャンセル</a-button
        >
        <a-button
          type="primary"
          :loading="submitting"
          @click="requestSave"
          >保存</a-button
        >
      </div>
    </template>
  </BaseLargeModal>

  <TrainingRecordModal
    v-if="sessionId !== null"
    v-model:open="recordModalOpen"
    :mode="recordMode"
    :session-id="sessionId"
    :record="editingRecord"
    @saved="handleRecordSaved"
  />

  <ConfirmModal
    v-model:open="confirmOpen"
    message="保存してもよろしいですか？"
    :loading="submitting"
    @confirm="saveSession"
  />
</template>

<style scoped>
.full-width {
  width: 100%;
}
.records-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}
.records-header :deep(.ant-typography) {
  margin: 0;
}
.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
