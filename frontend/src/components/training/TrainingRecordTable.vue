<script setup lang="ts">
import type { TrainingRecordResponse } from "../../types/trainingRecord";

defineProps<{
  records: TrainingRecordResponse[];
  loading: boolean;
}>();

const emit = defineEmits<{
  edit: [record: TrainingRecordResponse];
  delete: [record: TrainingRecordResponse];
}>();

function recordSummary(
  record: TrainingRecordResponse,
): string {
  const values: string[] = [];
  if (record.sets !== null)
    values.push(`${record.sets}セット`);
  if (record.repetitions !== null)
    values.push(`${record.repetitions}回`);
  if (record.weightKg !== null)
    values.push(`${record.weightKg} kg`);
  if (record.durationMinutes !== null)
    values.push(`${record.durationMinutes}分`);
  if (record.distanceMeters !== null)
    values.push(`${record.distanceMeters} m`);
  if (
    record.successCount !== null ||
    record.attemptCount !== null
  ) {
    values.push(
      `成功 ${record.successCount ?? "-"} / 試行 ${record.attemptCount ?? "-"}`,
    );
  }
  if (record.note) values.push(record.note);
  return values.join("・") || "記録値なし";
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
    :empty-text="'トレーニング種目はまだありません'"
    :cell-config="{ height: 36 }"
    :column-config="{ resizable: true }"
  >
    <vxe-column
      field="categoryName"
      title="大分類"
      min-width="150"
    />
    <vxe-column
      field="exerciseTypeName"
      title="中分類"
      min-width="150"
    />
    <vxe-column
      field="exerciseName"
      title="種目"
      min-width="180"
    />
    <vxe-column title="トレーニング内容" min-width="320">
      <template #default="{ row }">{{
        recordSummary(row)
      }}</template>
    </vxe-column>
    <vxe-column title="操作" width="150" fixed="right">
      <template #default="{ row }">
        <a-button type="link" @click="emit('edit', row)"
          >編集</a-button
        >
        <a-popconfirm
          title="この記録を削除しますか？"
          @confirm="emit('delete', row)"
        >
          <a-button type="link" danger>削除</a-button>
        </a-popconfirm>
      </template>
    </vxe-column>
    </vxe-table>
  </div>
</template>
