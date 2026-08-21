<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import type {
  VxeTableInstance,
  VxeTablePropTypes,
} from "vxe-table";
import {
  deleteTrainingSession,
  getTrainingSessions,
} from "../api/trainingSessionApi";
import ConfirmModal from "../components/common/ConfirmModal.vue";
import TrainingSessionModal from "../components/training/TrainingSessionModal.vue";
import type { TrainingSession } from "../types/trainingSession";

type Mode = "create" | "edit";

const userId = 1;
const tableRef = ref<VxeTableInstance<TrainingSession>>();
const sessions = ref<TrainingSession[]>([]);
const selectedSession = ref<TrainingSession | null>(null);
const sessionModalOpen = ref(false);
const sessionModalMode = ref<Mode>("create");
const deleteConfirmOpen = ref(false);
const deleteSubmitting = ref(false);
const sortState = reactive<{
  field: string;
  order: "asc" | "desc" | null;
}>({ field: "", order: null });
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});
const columnConfig =
  reactive<VxeTablePropTypes.ColumnConfig>({
    resizable: true,
  });

const sortedSessions = computed(() => {
  if (!sortState.field || !sortState.order)
    return sessions.value;
  return [...sessions.value].sort((a, b) => {
    const left =
      a[sortState.field as keyof TrainingSession];
    const right =
      b[sortState.field as keyof TrainingSession];
    const result =
      typeof left === "number" && typeof right === "number"
        ? left - right
        : String(left ?? "").localeCompare(
            String(right ?? ""),
          );
    return sortState.order === "asc" ? result : -result;
  });
});

const pagedSessions = computed(() => {
  const start =
    (pagination.currentPage - 1) * pagination.pageSize;
  return sortedSessions.value.slice(
    start,
    start + pagination.pageSize,
  );
});

function handleSortChange({
  field,
  order,
}: {
  field: string;
  order: "asc" | "desc" | "" | null;
}): void {
  sortState.field = field;
  sortState.order = order || null;
  pagination.currentPage = 1;
}

function handlePageChange({
  currentPage,
  pageSize,
}: {
  currentPage: number;
  pageSize: number;
}): void {
  pagination.currentPage =
    pageSize === pagination.pageSize ? currentPage : 1;
  pagination.pageSize = pageSize;
  selectedSession.value = null;
  tableRef.value?.clearCurrentRow();
}

function selectSession({
  row,
}: {
  row: TrainingSession;
}): void {
  selectedSession.value = row;
}

async function loadTrainingSessions(
  preferredId?: number,
): Promise<void> {
  try {
    sessions.value = await getTrainingSessions(userId);
    pagination.total = sessions.value.length;
    const lastPage = Math.max(
      1,
      Math.ceil(pagination.total / pagination.pageSize),
    );
    pagination.currentPage = Math.min(
      pagination.currentPage,
      lastPage,
    );
    const selectedId =
      preferredId ?? selectedSession.value?.id;
    const refreshedSelection =
      selectedId === undefined
        ? null
        : (sessions.value.find(
            (session) => session.id === selectedId,
          ) ?? null);
    selectedSession.value = refreshedSelection;
    if (refreshedSelection)
      tableRef.value?.setCurrentRow(refreshedSelection);
    else tableRef.value?.clearCurrentRow();
  } catch (error: unknown) {
    console.error(error);
    message.error("トレーニング履歴の取得に失敗しました。");
  }
}

function openCreateModal(): void {
  sessionModalMode.value = "create";
  sessionModalOpen.value = true;
}

function openEditModal(): void {
  if (!selectedSession.value) return;
  sessionModalMode.value = "edit";
  sessionModalOpen.value = true;
}

async function handleSessionSaved(
  session: TrainingSession,
): Promise<void> {
  await loadTrainingSessions(session.id);
}

async function handleSessionDeleted(): Promise<void> {
  selectedSession.value = null;
  await loadTrainingSessions();
}

async function deleteSelectedSession(): Promise<void> {
  if (!selectedSession.value) return;
  deleteSubmitting.value = true;
  try {
    await deleteTrainingSession(selectedSession.value.id);
    message.success("トレーニングを削除しました。");
    selectedSession.value = null;
    deleteConfirmOpen.value = false;
    await loadTrainingSessions();
  } catch (error: unknown) {
    console.error(error);
    message.error("トレーニングの削除に失敗しました。");
  } finally {
    deleteSubmitting.value = false;
  }
}

onMounted(loadTrainingSessions);
</script>

<template>
  <section
    class="training-history"
    aria-labelledby="training-history-title"
  >
    <a-card
      class="operation-card"
      :bordered="false"
      title="トレーニング履歴の操作"
    >
      <a-space wrap>
        <a-button type="primary" @click="openCreateModal"
          >新規登録</a-button
        >
        <a-button
          type="primary"
          :disabled="selectedSession === null"
          @click="openEditModal"
          >編集</a-button
        >
        <a-button
          type="primary"
          danger
          :disabled="selectedSession === null"
          @click="deleteConfirmOpen = true"
          >削除</a-button
        >
      </a-space>
    </a-card>

    <a-card
      class="table-card"
      :bordered="false"
      title="登録済みトレーニング"
    >
      <div class="table-scroll-area">
        <vxe-table
          ref="tableRef"
          height="100%"
          border
          stripe
          show-overflow
          highlight-current-row
          :cell-config="{ height: 32 }"
          :column-config="columnConfig"
          :data="pagedSessions"
          :sort-config="{ remote: true }"
          :empty-text="'データがありません'"
          @current-change="selectSession"
          @sort-change="handleSortChange"
        >
          <vxe-column
            field="trainingDate"
            title="トレーニング日"
            width="180"
            sortable
          />
          <vxe-column
            field="durationMinutes"
            title="時間（分）"
            width="160"
            sortable
          />
          <vxe-column
            field="feeling"
            title="感覚"
            width="210"
            sortable
          >
            <template #default="{ row }"
              ><a-rate :value="row.feeling ?? 0" disabled
            /></template>
          </vxe-column>
          <vxe-column
            field="note"
            title="メモ"
            min-width="240"
          >
            <template #default="{ row }">{{
              row.note || "—"
            }}</template>
          </vxe-column>
        </vxe-table>
      </div>
      <vxe-pager
        class="table-pager"
        :current-page="pagination.currentPage"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[5, 10, 20, 50]"
        :layouts="[
          'Total',
          'Sizes',
          'PrevPage',
          'Number',
          'NextPage',
          'Jump',
        ]"
        @page-change="handlePageChange"
      />
    </a-card>

    <TrainingSessionModal
      v-model:open="sessionModalOpen"
      :mode="sessionModalMode"
      :session="
        sessionModalMode === 'edit' ? selectedSession : null
      "
      :user-id="userId"
      @saved="handleSessionSaved"
      @deleted="handleSessionDeleted"
    />
    <ConfirmModal
      v-model:open="deleteConfirmOpen"
      message="削除してもよろしいですか？"
      description="削除したデータは元に戻せません。"
      danger
      :loading="deleteSubmitting"
      @confirm="deleteSelectedSession"
    />
  </section>
</template>

<style scoped>
.training-history {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: calc(100vh - 64px - clamp(40px, 6vw, 80px));
  min-height: 0;
  overflow: hidden;
}
.operation-card {
  flex: 0 0 auto;
}
.operation-card :deep(.ant-card-head) {
  min-height: 42px;
  padding: 0 16px;
}
.operation-card :deep(.ant-card-body) {
  padding: 12px 16px;
}
.table-card {
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: column;
}
.table-card :deep(.ant-card-head) {
  flex: 0 0 auto;
  min-height: 44px;
}
.table-card :deep(.ant-card-body) {
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: column;
  padding: 12px 16px;
}
.table-scroll-area {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
.table-pager {
  flex: 0 0 auto;
  margin-top: 8px;
}
</style>
