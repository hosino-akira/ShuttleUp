<script setup lang="ts">
import {
  computed,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
} from "vue";
import { Modal, message } from "ant-design-vue";
import type { FormInstance } from "ant-design-vue";
import {
  onBeforeRouteLeave,
  useRoute,
  useRouter,
} from "vue-router";
import {
  createTrainingSession,
  getTrainingSession,
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

type Mode = "create" | "edit";
const route = useRoute();
const router = useRouter();
const userId = 1;
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
const form = reactive<TrainingSessionUpdateRequest>({
  trainingDate: "",
  durationMinutes: 60,
  feeling: 3,
  note: null,
});
const savedSnapshot = ref("");
const dirtyTrackingReady = ref(false);
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
const dirty = computed(
  () =>
    dirtyTrackingReady.value &&
    savedSnapshot.value !== snapshot(),
);

function snapshot() {
  return JSON.stringify({ ...form, note: form.note ?? "" });
}
function applySession(value: TrainingSession) {
  Object.assign(form, {
    trainingDate: value.trainingDate,
    durationMinutes: value.durationMinutes,
    feeling: value.feeling,
    note: value.note ?? "",
  });
  savedSnapshot.value = snapshot();
}
async function initialize() {
  if (isCreate.value) {
    savedSnapshot.value = snapshot();
    dirtyTrackingReady.value = true;
    loading.value = false;
    return;
  }
  if (
    !Number.isInteger(sessionId.value) ||
    sessionId.value! <= 0
  ) {
    message.error("トレーニングIDが正しくありません。");
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
    dirtyTrackingReady.value = true;
  } catch (error) {
    console.error(error);
    message.error("トレーニング詳細の取得に失敗しました。");
    await router.replace({ name: "training-history" });
  } finally {
    loading.value = false;
  }
}
async function saveSession() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }
  saving.value = true;
  try {
    const request = {
      ...form,
      note: form.note?.trim() || null,
    };
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
    message.success("トレーニングを保存しました。");
  } catch (error) {
    console.error(error);
    message.error("トレーニングの保存に失敗しました。");
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
    message.success("トレーニング記録を削除しました。");
    await refreshRecords();
  } catch (error) {
    console.error(error);
    message.error("トレーニング記録の削除に失敗しました。");
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
    message.success("試合記録を削除しました。");
    await refreshMatches();
  } catch (error) {
    console.error(error);
    message.error("試合記録の削除に失敗しました。");
  }
}
async function refreshOpponents(selectedId?: number) {
  if (!session.value) return;
  opponents.value = await getOpponents(
    session.value.userId,
  );
  if (selectedId !== undefined)
    selectedOpponentId.value = selectedId;
}
function createOpponentFromMatch(name: string) {
  opponentInitialName.value = name;
  opponentDrawerOpen.value = true;
}
function confirmLeave(): Promise<boolean> {
  return new Promise((resolve) =>
    Modal.confirm({
      title: "未保存の変更があります。移動しますか？",
      content:
        "トレーニング基本情報への変更は破棄されます。",
      okText: "移動する",
      cancelText: "キャンセル",
      onOk: () => resolve(true),
      onCancel: () => resolve(false),
    }),
  );
}
async function goBack() {
  if (dirty.value && !(await confirmLeave())) return;
  savedSnapshot.value = snapshot();
  await router.push({ name: "training-history" });
}
function beforeUnload(event: BeforeUnloadEvent) {
  if (!dirty.value) return;
  event.preventDefault();
}
onBeforeRouteLeave(
  async () => !dirty.value || (await confirmLeave()),
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
      <header class="page-header">
        <h1>
          {{
            isCreate
              ? "トレーニング新規登録"
              : "トレーニング詳細・編集"
          }}
        </h1>
      </header>
      <a-card
        class="form-card"
        title="基本情報"
        :bordered="false"
        ><template #extra>
          <a-space wrap>
            <a-button type="primary" @click="goBack"
              >一覧へ戻る</a-button
            ><a-button
              type="primary"
              :loading="saving"
              @click="saveSession"
              >保存</a-button
            ></a-space
          ></template
        >
        <a-form
          ref="formRef"
          :model="form"
          layout="vertical"
        >
          <a-row :gutter="16">
            <a-col :xs="24" :md="8"
              ><a-form-item
                label="トレーニング日"
                name="trainingDate"
                :rules="[
                  {
                    required: true,
                    message: '日付を選択してください。',
                  },
                ]"
                ><a-date-picker
                  v-model:value="form.trainingDate"
                  value-format="YYYY-MM-DD"
                  class="full-width" /></a-form-item
            ></a-col>
            <a-col :xs="24" :md="8"
              ><a-form-item
                label="トレーニング時間（分）"
                name="durationMinutes"
                :rules="[
                  {
                    required: true,
                    type: 'number',
                    min: 1,
                    message: '1分以上で入力してください。',
                  },
                ]"
                ><a-input-number
                  v-model:value="form.durationMinutes"
                  :min="1"
                  class="full-width" /></a-form-item
            ></a-col>
            <a-col :xs="24" :md="8"
              ><a-form-item label="コンディション"
                ><a-rate
                  v-model:value="
                    form.feeling
                  " /></a-form-item
            ></a-col>
            <a-col :span="24"
              ><a-form-item
                label="メモ"
                name="note"
                :rules="[
                  {
                    max: 1000,
                    message:
                      'メモは1000文字以内で入力してください。',
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

      <a-card class="record-card" :bordered="false"
        ><template #title
          >トレーニング記録
          <a-tag>{{ records.length }}件</a-tag></template
        ><template #extra
          ><a-button
            type="primary"
            :disabled="sessionId === null"
            @click="openRecordCreate"
            >＋ 新規登録</a-button
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
          >試合記録
          <a-tag>{{ matches.length }}件</a-tag></template
        ><template #extra
          ><a-space
            ><a-button
              type="primary"
              @click="opponentDrawerOpen = true"
              >対戦相手を管理</a-button
            ><a-button
              type="primary"
              :disabled="sessionId === null"
              @click="openMatchCreate"
              >＋ 新規登録</a-button
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
  min-height: 0;
  gap: 12px;
}
.form-card,
.record-card {
  flex: 0 0 auto;
}
.match-card {
  display: flex;
  flex: 1;

  flex-direction: column;
  min-height: 0;
}
.match-card :deep(.ant-card-body) {
  flex: 1;
  min-height: 0;
  overflow: auto;
}
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
}
.page-header h1 {
  margin: 0;
  font-size: 24px;
}
.full-width {
  width: 100%;
}
@media (max-width: 576px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
  }
  .page-header h1 {
    font-size: 20px;
  }
}
</style>
