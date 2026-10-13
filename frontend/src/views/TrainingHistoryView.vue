<script setup lang="ts">
import { t } from '../i18n';
import { computed, onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { requireUserId } from "../stores/auth";
import { useRouter } from "vue-router";
import type {
  VxeTableEvents,
  VxeTableInstance,
  VxeTablePropTypes,
} from "vxe-table";
import {
  deleteTrainingSession,
  getTrainingSessions,
} from "../api/trainingSessionApi";
import ConfirmModal from "../components/common/ConfirmModal.vue";
import type { TrainingSession } from "../types/trainingSession";

const userId = requireUserId();
const router = useRouter();
const tableRef = ref<VxeTableInstance<TrainingSession>>();
const sessions = ref<TrainingSession[]>([]);
const selectedSession = ref<TrainingSession | null>(null);
const deleteConfirmOpen = ref(false);
const deleteSubmitting = ref(false);
const sortState = reactive<{
  field: string;
  order: "asc" | "desc" | null;
}>({ field: "", order: null });
const pagination = reactive({
  currentPage: 1,
  pageSize: 50,
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
    message.error(t('トレーニング履歴の取得に失敗しました。'));
  }
}

function openCreateModal(): void {
  void router.push({ name: "training-session-create" });
}

function openEditModal(): void {
  if (!selectedSession.value) return;
  void router.push({
    name: "training-session-detail",
    params: { sessionId: selectedSession.value.id },
  });
}

const editSessionOnDoubleClick: VxeTableEvents.CellDblclick<TrainingSession> =
  ({ row }) => {
    selectedSession.value = row;
    openEditModal();
  };

async function deleteSelectedSession(): Promise<void> {
  if (!selectedSession.value) return;
  deleteSubmitting.value = true;
  try {
    await deleteTrainingSession(selectedSession.value.id);
    message.success(t('トレーニングを削除しました。'));
    selectedSession.value = null;
    deleteConfirmOpen.value = false;
    await loadTrainingSessions();
  } catch (error: unknown) {
    console.error(error);
    message.error(t('トレーニングの削除に失敗しました。'));
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
    <div class="operation-bar">
      <h2 id="training-history-title">
        {{ t('トレーニング履歴の操作') }}
      </h2>
      <div class="operation-actions">
        <a-button type="primary" @click="openCreateModal"
          >{{ t('新規登録') }}</a-button
        >
        <a-button
          type="primary"
          :disabled="selectedSession === null"
          @click="openEditModal"
          >{{ t('編集') }}</a-button
        >
        <a-button
          type="primary"
          danger
          :disabled="selectedSession === null"
          @click="deleteConfirmOpen = true"
          >{{ t('削除') }}</a-button
        >
      </div>
    </div>

    <a-card
      class="table-card"
      :bordered="false"
      :title="t('登録済みトレーニング')"
    >
      <div class="table-scroll-area vxe-table-wrapper">
        <vxe-table
          ref="tableRef"
          height="100%"
          border
          stripe
          show-overflow
          :row-config="{ isCurrent: true }"
          :cell-config="{ height: 32 }"
          :column-config="columnConfig"
          :data="pagedSessions"
          :sort-config="{ remote: true }"
          :empty-text="t('データがありません')"
          @current-change="selectSession"
          @cell-dblclick="editSessionOnDoubleClick"
          @sort-change="handleSortChange"
        >
          <vxe-column
            field="trainingDate"
            :title="t('トレーニング日')"
            width="180"
            sortable
          />
          <vxe-column
            field="durationMinutes"
            :title="t('時間（分）')"
            width="160"
            sortable
          />
          <vxe-column
            field="feeling"
            :title="t('感覚')"
            width="210"
            sortable
          >
            <template #default="{ row }"
              ><a-rate :value="row.feeling ?? 0" disabled
            /></template>
          </vxe-column>
          <vxe-column
            field="note"
            :title="t('メモ')"
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
        :page-sizes="[50, 100, 200]"
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

    <ConfirmModal
      v-model:open="deleteConfirmOpen"
      :message="t('削除してもよろしいですか？')"
      :description="t('削除したデータは元に戻せません。')"
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
  flex: 1;
  gap: var(--app-section-gap);
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}
.operation-bar {
  display: flex;
  flex: 0 0 auto;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 8px 12px;
  padding: 8px 12px;
  border-radius: 8px;
  background: var(--app-surface-color);
}
.operation-bar h2 {
  margin: 0;
  color: var(--app-heading-color);
  font-size: 14px;
  font-weight: 600;
}
.operation-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
.table-card {
  display: flex;
  flex: 1;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  flex-direction: column;
}
.table-card :deep(.ant-card-head) {
  flex: 0 0 auto;
  min-height: 38px;
}
.table-card :deep(.ant-card-body) {
  display: flex;
  flex: 1;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
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
