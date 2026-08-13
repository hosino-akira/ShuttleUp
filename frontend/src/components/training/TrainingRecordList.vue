<script setup lang="ts">
import type { TrainingRecordResponse } from '../../types/trainingRecord'

defineProps<{
  records: TrainingRecordResponse[]
  loading: boolean
  deletingId: number | null
}>()

const emit = defineEmits<{
  edit: [record: TrainingRecordResponse]
  delete: [recordId: number]
}>()

function recordSummary(record: TrainingRecordResponse): string {
  const values: string[] = []
  if (record.sets !== null) values.push(`${record.sets}セット`)
  if (record.repetitions !== null) values.push(`${record.repetitions}回`)
  if (record.weightKg !== null) values.push(`${record.weightKg} kg`)
  if (record.durationMinutes !== null) values.push(`${record.durationMinutes}分`)
  if (record.distanceMeters !== null) values.push(`${record.distanceMeters} m`)
  if (record.successCount !== null || record.attemptCount !== null) {
    values.push(`成功 ${record.successCount ?? '-'} / ${record.attemptCount ?? '-'}`)
  }
  return values.join('・') || '記録値なし'
}
</script>

<template>
  <a-spin :spinning="loading">
    <a-empty v-if="!loading && records.length === 0" description="トレーニング内容はまだありません" />
    <a-list v-else :data-source="records" item-layout="horizontal">
      <template #renderItem="{ item }: { item: TrainingRecordResponse }">
        <a-list-item>
          <template #actions>
            <a-button type="link" @click="emit('edit', item)">編集</a-button>
            <a-popconfirm
              title="この種目記録を削除しますか？"
              ok-text="削除"
              cancel-text="キャンセル"
              ok-type="danger"
              @confirm="emit('delete', item.id)"
            >
              <a-button type="link" danger :loading="deletingId === item.id">削除</a-button>
            </a-popconfirm>
          </template>
          <a-list-item-meta :description="`${item.categoryName} / ${item.exerciseTypeName}`">
            <template #title>{{ item.exerciseName }}</template>
          </a-list-item-meta>
          <div class="record-values">
            <div>{{ recordSummary(item) }}</div>
            <div v-if="item.note" class="record-note">{{ item.note }}</div>
          </div>
        </a-list-item>
      </template>
    </a-list>
  </a-spin>
</template>

<style scoped>
.record-values { min-width: 260px; text-align: right; }
.record-note { margin-top: 4px; color: rgba(0, 0, 0, 0.45); }
@media (max-width: 767px) { .record-values { min-width: 0; text-align: left; } }
</style>
