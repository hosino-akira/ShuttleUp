<script setup lang="ts">
import { t } from '../../i18n';
import type { VxeTableEvents } from "vxe-table";
import type { TrainingRecordResponse } from "../../types/trainingRecord";

defineProps<{
  records: TrainingRecordResponse[];
  loading: boolean;
}>();

const emit = defineEmits<{
  edit: [record: TrainingRecordResponse];
  delete: [record: TrainingRecordResponse];
}>();

const editRecordOnDoubleClick: VxeTableEvents.CellDblclick<TrainingRecordResponse> =
  ({ row, $event }) => {
    if (
      $event.target instanceof Element &&
      $event.target.closest("button, a")
    ) return;
    emit("edit", row);
  };

function recordSummary(
  record: TrainingRecordResponse,
): string {
  const values: string[] = [];
  if (record.sets !== null)
    values.push(t('{0}セット', { 0: record.sets }));
  if (record.repetitions !== null)
    values.push(t('{0}回', { 0: record.repetitions }));
  if (record.weightKg !== null)
    values.push(`${record.weightKg} kg`);
  if (record.durationMinutes !== null)
    values.push(t('{0}分', { 0: record.durationMinutes }));
  if (record.distanceMeters !== null)
    values.push(`${record.distanceMeters} m`);
  if (
    record.successCount !== null ||
    record.attemptCount !== null
  ) {
    values.push(
      t('成功 {0} / 試行 {1}', { 0: record.successCount ?? "-", 1: record.attemptCount ?? "-" }),
    );
  }
  if (record.note) values.push(record.note);
  return values.join(t('・')) || t('記録値なし');
}
</script>

<template>
  <div class="vxe-table-wrapper">
    <vxe-table
    border
    stripe
    show-overflow
    :loading="loading"
    :data="records"
    :row-config="{ isCurrent: true }"
    :empty-text="t('トレーニング種目はまだありません')"
    :cell-config="{ height: 36 }"
    :column-config="{ resizable: true }"
    @cell-dblclick="editRecordOnDoubleClick"
  >
    <vxe-column
      field="categoryName"
      :title="t('大分類')"
      min-width="150"
    />
    <vxe-column
      field="exerciseTypeName"
      :title="t('中分類')"
      min-width="150"
    />
    <vxe-column
      field="exerciseName"
      :title="t('種目')"
      min-width="180"
    />
    <vxe-column :title="t('トレーニング内容')" min-width="320">
      <template #default="{ row }">{{
        recordSummary(row)
      }}</template>
    </vxe-column>
    <vxe-column :title="t('操作')" width="150" fixed="right">
      <template #default="{ row }">
        <a-button type="link" @click="emit('edit', row)"
          >{{ t('編集') }}</a-button
        >
        <a-popconfirm
          :title="t('この記録を削除しますか？')"
          @confirm="emit('delete', row)"
        >
          <a-button type="link" danger>{{ t('削除') }}</a-button>
        </a-popconfirm>
      </template>
    </vxe-column>
    </vxe-table>
  </div>
</template>
