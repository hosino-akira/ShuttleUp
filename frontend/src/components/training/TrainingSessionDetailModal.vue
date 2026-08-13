<script setup lang="ts">
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { getExercises } from '../../api/exerciseApi'
import { createTrainingRecord, deleteTrainingRecord, getTrainingRecords, updateTrainingRecord } from '../../api/trainingRecordApi'
import type { ExerciseResponse } from '../../types/exercise'
import type { TrainingRecordCreateRequest, TrainingRecordResponse } from '../../types/trainingRecord'
import type { TrainingSessionResponse } from '../../types/trainingSession'
import TrainingRecordFormModal from './TrainingRecordFormModal.vue'
import TrainingRecordList from './TrainingRecordList.vue'

const props = defineProps<{ open: boolean; session: TrainingSessionResponse | null }>()
const emit = defineEmits<{ 'update:open': [value: boolean] }>()

const records = ref<TrainingRecordResponse[]>([])
const exercises = ref<ExerciseResponse[]>([])
const loading = ref(false)
const exercisesLoading = ref(false)
const saving = ref(false)
const deletingId = ref<number | null>(null)
const formOpen = ref(false)
const editingRecord = ref<TrainingRecordResponse | null>(null)

async function loadRecords(): Promise<void> {
  if (!props.session) return
  loading.value = true
  try { records.value = await getTrainingRecords(props.session.id) }
  catch (error) { console.error(error); message.error('トレーニング内容の取得に失敗しました。') }
  finally { loading.value = false }
}

async function loadExercises(): Promise<void> {
  if (exercises.value.length > 0) return
  exercisesLoading.value = true
  try { exercises.value = await getExercises() }
  catch (error) { console.error(error); message.error('種目一覧の取得に失敗しました。') }
  finally { exercisesLoading.value = false }
}

function openCreate(): void { editingRecord.value = null; formOpen.value = true; void loadExercises() }
function openEdit(record: TrainingRecordResponse): void { editingRecord.value = record; formOpen.value = true; void loadExercises() }

async function saveRecord(request: TrainingRecordCreateRequest): Promise<void> {
  if (!props.session) return
  saving.value = true
  try {
    if (editingRecord.value) await updateTrainingRecord(editingRecord.value.id, request)
    else await createTrainingRecord(props.session.id, request)
    message.success(editingRecord.value ? '種目記録を更新しました。' : '種目記録を追加しました。')
    formOpen.value = false
    await loadRecords()
  } catch (error) { console.error(error); message.error('種目記録の保存に失敗しました。') }
  finally { saving.value = false }
}

async function removeRecord(recordId: number): Promise<void> {
  deletingId.value = recordId
  try { await deleteTrainingRecord(recordId); message.success('種目記録を削除しました。'); await loadRecords() }
  catch (error) { console.error(error); message.error('種目記録の削除に失敗しました。') }
  finally { deletingId.value = null }
}

watch(() => [props.open, props.session?.id] as const, ([open]) => {
  if (open) void loadRecords()
  else { records.value = []; formOpen.value = false; editingRecord.value = null }
})
</script>

<template>
  <a-modal :open="open" title="トレーニング詳細" width="820px" :footer="null" @update:open="emit('update:open', $event)">
    <template v-if="session">
      <a-descriptions bordered size="small" :column="2">
        <a-descriptions-item label="日付">{{ session.trainingDate }}</a-descriptions-item>
        <a-descriptions-item label="時間">{{ session.durationMinutes }}分</a-descriptions-item>
        <a-descriptions-item label="調子"><a-rate :value="session.feeling ?? 0" disabled /></a-descriptions-item>
        <a-descriptions-item label="メモ">{{ session.note || '—' }}</a-descriptions-item>
      </a-descriptions>
      <div class="records-header">
        <a-typography-title :level="4">トレーニング内容</a-typography-title>
        <a-button type="primary" @click="openCreate">＋ 種目を追加</a-button>
      </div>
      <TrainingRecordList :records="records" :loading="loading" :deleting-id="deletingId" @edit="openEdit" @delete="removeRecord" />
    </template>
    <TrainingRecordFormModal v-model:open="formOpen" :loading="saving" :exercises="exercises" :exercises-loading="exercisesLoading" :record="editingRecord" @save="saveRecord" />
  </a-modal>
</template>

<style scoped>
.records-header { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.records-header :deep(.ant-typography) { margin: 0; }
</style>
