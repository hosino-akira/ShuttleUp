<script setup lang="ts">
import PageIntro from '../components/common/PageIntro.vue'
import { t } from '../i18n';
import {
  computed,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
  watch,
} from "vue";
import { message } from "ant-design-vue";
import { isAxiosError } from "axios";
import { requireUserId } from "../stores/auth";
import type { FormInstance } from "ant-design-vue";
import {
  onBeforeRouteLeave,
  useRoute,
  useRouter,
} from "vue-router";
import {
  createTrainingSession,
  getTrainingSession,
  getTrainingSessions,
  updateTrainingSession,
} from "../api/trainingSessionApi";
import {
  deleteTrainingRecord,
  getTrainingRecords,
} from "../api/trainingRecordApi";
import { deleteMatch, getMatches } from "../api/matchApi";
import { getOpponents } from "../api/opponentApi";
import type {
  TrainingSession,
  TrainingSessionUpdateRequest,
} from "../types/trainingSession";
import type { TrainingRecordResponse } from "../types/trainingRecord";
import type { MatchResponse } from "../types/match";
import type { OpponentResponse } from "../types/opponent";
import TrainingRecordTable from "../components/training/TrainingRecordTable.vue";
import TrainingRecordModal from "../components/training/TrainingRecordModal.vue";
import MatchTable from "../components/training/MatchTable.vue";
import MatchModal from "../components/training/MatchModal.vue";
import OpponentDrawer from "../components/training/OpponentDrawer.vue";
import { useUnsavedChanges } from "../composables/useUnsavedChanges";
import { getAccessToken, isAccessTokenValid } from "../utils/authSession";
import { useLocalizedForm } from '../composables/useLocalizedForm';
import PlantPicker from '../components/forest/PlantPicker.vue';
import { getPlantType, isPlantType, localDateKey, plantNames, type PlantType } from '../utils/forest';

type Mode = "create" | "edit";
const route = useRoute();
const router = useRouter();
const userId = requireUserId();
const sessionId = ref<number | null>(
  route.name === "training-session-create"
    ? null
    : Number(route.params.sessionId),
);
const isCreate = computed(() => sessionId.value === null);
const loading = ref(true);
const saving = ref(false);
const session = ref<TrainingSession | null>(null);
const records = ref<TrainingRecordResponse[]>([]);
const matches = ref<MatchResponse[]>([]);
const opponents = ref<OpponentResponse[]>([]);
const formRef = ref<FormInstance>();
const { onValidate: onFormValidate } = useLocalizedForm(formRef);
function getTodayDate(): string {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const day = String(today.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}
const form = reactive<TrainingSessionUpdateRequest & { plantType: PlantType }>({
  trainingDate: isCreate.value ? getTodayDate() : "",
  durationMinutes: 60,
  feeling: 3,
  note: null,
  plantType: isPlantType(route.query.plant) ? route.query.plant : 'cedar',
});
const saveError = ref('');
const conflictingTrainingDate = ref<string | null>(null);
const duplicateDate = computed(() => conflictingTrainingDate.value !== null
  && conflictingTrainingDate.value === form.trainingDate);
watch(() => form.trainingDate, (date) => {
  if (conflictingTrainingDate.value !== null && date !== conflictingTrainingDate.value) {
    conflictingTrainingDate.value = null;
    saveError.value = '';
  }
});
function showDuplicateDateError(date: string) {
  conflictingTrainingDate.value = date;
  saveError.value = 'この日付のトレーニングは既に登録されています。1日に1件のみ登録できます。';
  message.error({ content: t('日付が重複しています'), duration: 6 });
  formRef.value?.scrollToField('trainingDate');
}
const recordModalOpen = ref(false);
const recordMode = ref<Mode>("create");
const editingRecord = ref<TrainingRecordResponse | null>(
  null,
);
const matchModalOpen = ref(false);
const matchMode = ref<Mode>("create");
const editingMatch = ref<MatchResponse | null>(null);
const opponentDrawerOpen = ref(false);
const selectedOpponentId = ref<number | undefined>();
const opponentInitialName = ref("");
function snapshot() {
  return { ...form, note: form.note ?? "" };
}
const {
  isDirty: dirty,
  reset: resetDirtyState,
  confirmLeave,
} = useUnsavedChanges(snapshot);
function applySession(value: TrainingSession) {
  Object.assign(form, {
    trainingDate: value.trainingDate,
    durationMinutes: value.durationMinutes,
    feeling: value.feeling,
    note: value.note ?? "",
    plantType: getPlantType(value),
  });
  resetDirtyState();
}
async function initialize() {
  if (isCreate.value) {
    resetDirtyState();
    loading.value = false;
    return;
  }
  if (
    !Number.isInteger(sessionId.value) ||
    sessionId.value! <= 0
  ) {
    message.error(t('トレーニングIDが正しくありません。'));
    await router.replace({ name: "training-history" });
    return;
  }
  loading.value = true;
  try {
    const loadedSession = await getTrainingSession(
      sessionId.value!,
    );
    const [loadedRecords, loadedMatches, loadedOpponents] =
      await Promise.all([
        getTrainingRecords(sessionId.value!),
        getMatches(sessionId.value!),
        getOpponents(loadedSession.userId),
      ]);
    session.value = loadedSession;
    records.value = loadedRecords;
    matches.value = loadedMatches;
    opponents.value = loadedOpponents;
    applySession(loadedSession);
  } catch (error) {
    console.error(error);
    message.error(t('トレーニング詳細の取得に失敗しました。'));
    await router.replace({ name: "training-history" });
  } finally {
    loading.value = false;
  }
}
async function saveSession() {
  if (saving.value || loading.value) return;
  saving.value = true;
  saveError.value = '';
  conflictingTrainingDate.value = null;
  try {
    await formRef.value?.validate();
  } catch {
    saving.value = false;
    return;
  }
  try {
    const request = {
      ...form,
      note: form.note?.trim() || null,
    };
    const existingSessions = await getTrainingSessions(userId);
    if (existingSessions.some(existing => existing.trainingDate === request.trainingDate
      && existing.id !== sessionId.value)) {
      showDuplicateDateError(request.trainingDate);
      return;
    }
    const creating = isCreate.value;
    const saved = creating
      ? await createTrainingSession({ userId, ...request })
      : await updateTrainingSession(
          sessionId.value!,
          request,
        );
    session.value = saved;
    sessionId.value = saved.id;
    applySession(saved);
    if (creating) {
      opponents.value = await getOpponents(saved.userId);
      await router.replace({
        name: "training-session-detail",
        params: { sessionId: saved.id },
      });
    }
    message.success(creating && saved.trainingDate <= localDateKey()
      ? t('トレーニングを保存し、{plant}を森に植えました。', { plant: t(plantNames[getPlantType(saved)]) })
      : t('トレーニングを保存しました。'));
  } catch (error) {
    console.error(error);
    const duplicateDateMessage = 'この日付のトレーニングは既に登録されています。1日に1件のみ登録できます。';
    if (isAxiosError(error) && error.response?.status === 409
      && (error.response.data?.code === 'TRAINING_DATE_ALREADY_EXISTS'
        || error.response.data?.message === duplicateDateMessage)) {
      showDuplicateDateError(form.trainingDate);
    } else {
      saveError.value = isAxiosError(error) && !error.response
        ? '通信に失敗しました。時間をおいて再度お試しください。'
        : 'トレーニングの保存に失敗しました。';
      message.error({ content: t(saveError.value), duration: 6 });
    }
  } finally {
    saving.value = false;
  }
}
function openRecordCreate() {
  recordMode.value = "create";
  editingRecord.value = null;
  recordModalOpen.value = true;
}
function openRecordEdit(record: TrainingRecordResponse) {
  recordMode.value = "edit";
  editingRecord.value = record;
  recordModalOpen.value = true;
}
async function refreshRecords() {
  if (sessionId.value === null) return;
  records.value = await getTrainingRecords(sessionId.value);
}
async function removeRecord(
  record: TrainingRecordResponse,
) {
  try {
    await deleteTrainingRecord(record.id);
    message.success(t('トレーニング記録を削除しました。'));
    await refreshRecords();
  } catch (error) {
    console.error(error);
    message.error(t('トレーニング記録の削除に失敗しました。'));
  }
}
function openMatchCreate() {
  matchMode.value = "create";
  editingMatch.value = null;
  selectedOpponentId.value = undefined;
  matchModalOpen.value = true;
}
function openMatchEdit(match: MatchResponse) {
  matchMode.value = "edit";
  editingMatch.value = match;
  matchModalOpen.value = true;
}
async function refreshMatches() {
  if (sessionId.value === null) return;
  matches.value = await getMatches(sessionId.value);
}
async function removeMatch(match: MatchResponse) {
  try {
    await deleteMatch(match.id);
    message.success(t('試合記録を削除しました。'));
    await refreshMatches();
  } catch (error) {
    console.error(error);
    message.error(t('試合記録の削除に失敗しました。'));
  }
}
async function refreshOpponents(selectedId?: number) {
  if (!session.value) return;
  opponents.value = await getOpponents(
    session.value.userId,
  );
  if (selectedId !== undefined)
    selectedOpponentId.value = selectedId;
  await refreshMatches();
}
function createOpponentFromMatch(name: string) {
  opponentInitialName.value = name;
  opponentDrawerOpen.value = true;
}
async function goBack() {
  if (dirty.value && !(await confirmLeave())) return;
  resetDirtyState();
  await router.push({ name: "training-history" });
}
function beforeUnload(event: BeforeUnloadEvent) {
  if (!dirty.value) return;
  event.preventDefault();
}
onBeforeRouteLeave(
  async () => !isAccessTokenValid(getAccessToken()) || !dirty.value || (await confirmLeave()),
);
onMounted(() => {
  window.addEventListener("beforeunload", beforeUnload);
  void initialize();
});
onBeforeUnmount(() =>
  window.removeEventListener("beforeunload", beforeUnload),
);
</script>

<template>
  <a-spin :spinning="loading">
    <section class="detail-page">
      <PageIntro :title="isCreate ? t('トレーニング新規登録') : t('トレーニング詳細・編集')" />
      <a-card
        class="form-card"
        :title="t('基本情報')"
        :bordered="false"
        ><template #extra>
          <a-space wrap>
            <RouterLink class="app-button" :to="{ name: 'training-plan', query: sessionId ? { planted: sessionId } : {} }">{{ t('森を見る') }}</RouterLink>
            <a-button @click="goBack"
              >{{ t('一覧へ戻る') }}</a-button
            ><a-button
              type="primary"
              :loading="saving"
              @click="saveSession"
              >{{ isCreate && form.trainingDate <= localDateKey() ? t('保存して植える') : t('保存') }}</a-button
            ></a-space
          ></template
        >
        <a-alert
          v-if="saveError"
          class="session-save-error"
          type="error"
          show-icon
          role="alert"
          :message="duplicateDate ? t('日付が重複しています') : t('トレーニングの保存に失敗しました。')"
          :description="t(saveError)"
        />
        <a-form
          ref="formRef"
          @validate="onFormValidate"
          :model="form"
          :disabled="saving"
          layout="vertical"
        >
          <a-row :gutter="12">
            <a-col :xs="24" :md="8"
              ><a-form-item
                :label="t('トレーニング日')"
                name="trainingDate"
                :validate-status="duplicateDate ? 'error' : undefined"
                :help="duplicateDate ? t('この日付のトレーニングは既に登録されています。1日に1件のみ登録できます。') : undefined"
                :rules="[
                  {
                    required: true,
                    message: t('日付を選択してください。'),
                  },
                ]"
                ><a-date-picker
                  v-model:value="form.trainingDate"
                  value-format="YYYY-MM-DD"
                  class="full-width" /></a-form-item
            ></a-col>
            <a-col :xs="24" :md="8"
              ><a-form-item
                :label="t('トレーニング時間（分）')"
                name="durationMinutes"
                :rules="[
                  {
                    required: true,
                    type: 'number',
                    min: 1,
                    message: t('1分以上で入力してください。'),
                  },
                ]"
                ><a-input-number
                  v-model:value="form.durationMinutes"
                  :min="1"
                  class="full-width" /></a-form-item
            ></a-col>
            <a-col :xs="24" :md="8"
              ><a-form-item :label="t('コンディション')"
                ><a-rate
                  v-model:value="
                    form.feeling
                  " /></a-form-item
            ></a-col>
            <a-col :span="24">
              <a-form-item :label="t('植える植物を選ぶ')" name="plantType">
                <div class="training-plant-picker"><PlantPicker v-model="form.plantType" :disabled="saving" /></div>
                <p class="plant-help">{{ t('1回のトレーニングにつき1株。編集しても植物は増えません。未来の日付の記録は、その日から森に表示されます。') }}</p>
              </a-form-item>
            </a-col>
            <a-col :span="24"
              ><a-form-item
                :label="t('メモ')"
                name="note"
                :rules="[
                  {
                    max: 1000,
                    message:
                      t('メモは1000文字以内で入力してください。'),
                  },
                ]"
                ><a-textarea
                  v-model:value="form.note"
                  :maxlength="1000"
                  :rows="2"
                  show-count /></a-form-item
            ></a-col>
          </a-row>
        </a-form>
      </a-card>

      <a-alert
        v-if="isCreate"
        class="save-required-notice"
        type="info"
        show-icon
        :message="t('先に基本情報を「保存」すると、トレーニング記録・試合記録を追加できます。')"
      />

      <a-card class="record-card" :bordered="false"
        ><template #title
          >{{ t('トレーニング記録') }}
          <a-tag>{{ records.length }}{{ t('件') }}</a-tag></template
        ><template #extra
          ><a-button
            type="primary"
            :disabled="sessionId === null"
            @click="openRecordCreate"
            >{{ t('新規') }}</a-button
          ></template
        >
        <TrainingRecordTable
          :records="records"
          :loading="false"
          @edit="openRecordEdit"
          @delete="removeRecord"
        />
      </a-card>
      <a-card class="match-card" :bordered="false"
        ><template #title
          >{{ t('試合記録') }}
          <a-tag>{{ matches.length }}{{ t('件') }}</a-tag></template
        ><template #extra
          ><a-space
            ><a-button
              type="primary"
              @click="opponentDrawerOpen = true"
              >{{ t('対戦相手を管理') }}</a-button
            ><a-button
              type="primary"
              :disabled="sessionId === null"
              @click="openMatchCreate"
              >{{ t('新規') }}</a-button
            ></a-space
          ></template
        >
        <MatchTable
          :matches="matches"
          :loading="false"
          @edit="openMatchEdit"
          @delete="removeMatch"
        />
      </a-card>
    </section>
  </a-spin>

  <TrainingRecordModal
    v-if="sessionId !== null"
    v-model:open="recordModalOpen"
    :mode="recordMode"
    :session-id="sessionId"
    :user-id="session?.userId ?? userId"
    :record="editingRecord"
    @saved="refreshRecords"
  />
  <MatchModal
    v-if="sessionId !== null"
    v-model:open="matchModalOpen"
    :mode="matchMode"
    :session-id="sessionId"
    :match="editingMatch"
    :opponents="opponents"
    :selected-opponent-id="selectedOpponentId"
    @saved="refreshMatches"
    @manage-opponents="
      opponentInitialName = '';
      opponentDrawerOpen = true;
    "
    @create-opponent="createOpponentFromMatch"
  />
  <OpponentDrawer
    v-if="session"
    v-model:open="opponentDrawerOpen"
    :user-id="session.userId"
    :opponents="opponents"
    :initial-name="opponentInitialName"
    @changed="refreshOpponents"
  />
</template>

<style scoped>
.detail-page {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  gap: var(--app-section-gap);
}
.form-card,
.record-card {
  flex: 0 0 auto;
  min-width: 0;
  overflow: hidden;
}
.match-card {
  display: flex;
  flex: 1;

  flex-direction: column;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}
.record-card :deep(.ant-card-body),
.match-card :deep(.ant-card-body) {
  flex: 1;
  min-width: 0;
  min-height: 0;
  overflow-x: hidden;
}
.match-card :deep(.ant-card-body) {
  overflow-y: auto;
}
.full-width {
  width: 100%;
}
.session-save-error {
  margin-bottom: 16px;
  border-width: 2px;
}
.training-plant-picker { max-width: 650px; }
.training-plant-picker :deep(.plant-picker) { grid-template-columns: repeat(6, minmax(0, 1fr)); }
.plant-help { margin: 8px 0 0; font-size: 12px; color: var(--app-muted-color); }
@media (max-width: 576px) {
  .training-plant-picker :deep(.plant-picker) { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  :deep(.ant-card-head) {
    padding-block: 4px;
  }
  :deep(.ant-card-head-wrapper) {
    flex-wrap: wrap;
    gap: 4px 8px;
  }
  :deep(.ant-card-head-title) {
    flex: 1 0 auto;
    white-space: normal;
  }

}
</style>
