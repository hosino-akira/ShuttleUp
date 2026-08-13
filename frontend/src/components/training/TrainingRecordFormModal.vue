<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import type { ExerciseResponse } from '../../types/exercise'
import type { TrainingRecordCreateRequest, TrainingRecordResponse } from '../../types/trainingRecord'

const props = defineProps<{
  open: boolean
  loading: boolean
  exercises: ExerciseResponse[]
  exercisesLoading: boolean
  record: TrainingRecordResponse | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  save: [request: TrainingRecordCreateRequest]
}>()

const formRef = ref<FormInstance>()
const emptyForm = (): TrainingRecordCreateRequest => ({
  exerciseId: 0, sets: null, repetitions: null, weightKg: null,
  durationMinutes: null, distanceMeters: null, successCount: null,
  attemptCount: null, note: null,
})
const form = reactive<TrainingRecordCreateRequest>(emptyForm())
const exerciseOptions = computed(() => props.exercises.map((exercise) => ({
  value: exercise.id,
  label: `${exercise.name} — ${exercise.categoryName} / ${exercise.exerciseTypeName}`,
})))

watch(() => [props.open, props.record] as const, ([open, record]) => {
  if (!open) return
  Object.assign(form, record ? {
    exerciseId: record.exerciseId,
    sets: record.sets,
    repetitions: record.repetitions,
    weightKg: record.weightKg,
    durationMinutes: record.durationMinutes,
    distanceMeters: record.distanceMeters,
    successCount: record.successCount,
    attemptCount: record.attemptCount,
    note: record.note,
  } : emptyForm())
  formRef.value?.clearValidate()
}, { immediate: true })

async function submit(): Promise<void> {
  try { await formRef.value?.validate() } catch { return }
  emit('save', { ...form, note: form.note?.trim() || null })
}
</script>

<template>
  <a-modal
    :open="open"
    :title="record ? '種目記録を編集' : '種目を追加'"
    :confirm-loading="loading"
    ok-text="保存"
    cancel-text="キャンセル"
    @ok="submit"
    @update:open="emit('update:open', $event)"
  >
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item label="種目" name="exerciseId" :rules="[{ required: true, type: 'number', min: 1, message: '種目を選択してください。' }]">
        <a-select v-model:value="form.exerciseId" show-search option-filter-prop="label" :options="exerciseOptions" :loading="exercisesLoading" placeholder="種目名で検索" />
      </a-form-item>
      <a-divider orientation="left">筋力・回数</a-divider>
      <a-row :gutter="12">
        <a-col :span="8"><a-form-item label="セット数"><a-input-number v-model:value="form.sets" class="full-width" :min="0" /></a-form-item></a-col>
        <a-col :span="8"><a-form-item label="回数"><a-input-number v-model:value="form.repetitions" class="full-width" :min="0" /></a-form-item></a-col>
        <a-col :span="8"><a-form-item label="重量 (kg)"><a-input-number v-model:value="form.weightKg" class="full-width" :min="0" :step="0.5" /></a-form-item></a-col>
      </a-row>
      <a-divider orientation="left">時間・距離</a-divider>
      <a-row :gutter="12">
        <a-col :span="12"><a-form-item label="実施時間 (分)"><a-input-number v-model:value="form.durationMinutes" class="full-width" :min="0" /></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="距離 (m)"><a-input-number v-model:value="form.distanceMeters" class="full-width" :min="0" /></a-form-item></a-col>
      </a-row>
      <a-divider orientation="left">成功率</a-divider>
      <a-row :gutter="12">
        <a-col :span="12"><a-form-item label="成功回数"><a-input-number v-model:value="form.successCount" class="full-width" :min="0" /></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="試行回数"><a-input-number v-model:value="form.attemptCount" class="full-width" :min="0" /></a-form-item></a-col>
      </a-row>
      <a-form-item label="メモ"><a-textarea v-model:value="form.note" :maxlength="1000" :rows="3" show-count /></a-form-item>
    </a-form>
  </a-modal>
</template>

<style scoped>.full-width { width: 100%; }</style>
