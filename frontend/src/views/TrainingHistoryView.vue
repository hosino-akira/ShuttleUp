<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, TableColumnsType } from 'ant-design-vue'
import {
  createTrainingSession,
  getTrainingSessions,
} from '../api/trainingSessionApi'
import type {
  TrainingSession,
  TrainingSessionCreateRequest,
} from '../types/trainingSession'

interface TrainingSessionForm {
  trainingDate: string | null
  durationMinutes: number
  feeling: number | null
  note: string
}

const userId = 1
const formRef = ref<FormInstance>()
const sessions = ref<TrainingSession[]>([])
const loading = ref(false)
const submitting = ref(false)

const form = reactive<TrainingSessionForm>({
  trainingDate: null,
  durationMinutes: 60,
  feeling: 3,
  note: '',
})

const columns: TableColumnsType<TrainingSession> = [
  { title: 'トレーニング日', dataIndex: 'trainingDate', key: 'trainingDate' },
  { title: '時間（分）', dataIndex: 'durationMinutes', key: 'durationMinutes' },
  { title: '感覚', dataIndex: 'feeling', key: 'feeling' },
  { title: 'メモ', dataIndex: 'note', key: 'note' },
]

async function loadTrainingSessions(): Promise<void> {
  loading.value = true

  try {
    sessions.value = await getTrainingSessions(userId)
  } catch (error: unknown) {
    console.error(error)
    message.error('トレーニング履歴の取得に失敗しました。')
  } finally {
    loading.value = false
  }
}

function resetForm(): void {
  form.trainingDate = null
  form.durationMinutes = 60
  form.feeling = 3
  form.note = ''
  formRef.value?.clearValidate()
}

function updateFeeling(value: number): void {
  form.feeling = value === 0 ? null : value
}

async function submitTrainingSession(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  if (form.trainingDate === null) {
    return
  }

  const request: TrainingSessionCreateRequest = {
    userId,
    trainingDate: form.trainingDate,
    durationMinutes: form.durationMinutes,
    feeling: form.feeling,
    note: form.note.trim() || null,
  }

  submitting.value = true

  try {
    await createTrainingSession(request)
    message.success('トレーニングを登録しました。')
    resetForm()
    await loadTrainingSessions()
  } catch (error: unknown) {
    console.error(error)
    message.error('トレーニングの登録に失敗しました。')
  } finally {
    submitting.value = false
  }
}

onMounted(loadTrainingSessions)
</script>

<template>
  <section class="training-history" aria-labelledby="training-history-title">
    <a-card :bordered="false" title="トレーニング登録">
      <a-typography-title id="training-history-title" :level="2">トレーニング履歴</a-typography-title>

      <a-form ref="formRef" :model="form" layout="vertical" @finish="submitTrainingSession">
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <a-form-item
              label="トレーニング日"
              name="trainingDate"
              :rules="[{ required: true, message: 'トレーニング日を選択してください。' }]"
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
                { required: true, message: 'トレーニング時間を入力してください。' },
                { type: 'number', min: 1, message: '1分以上で入力してください。' },
              ]"
            >
              <a-input-number v-model:value="form.durationMinutes" class="full-width" :min="1" />
            </a-form-item>
          </a-col>

          <a-col :span="24">
            <a-form-item
              label="トレーニング時の感覚"
              name="feeling"
              :rules="[{ type: 'number', min: 1, max: 5, message: '1から5で選択してください。' }]"
            >
              <a-rate v-model:value="form.feeling" @change="updateFeeling" />
            </a-form-item>
          </a-col>

          <a-col :span="24">
            <a-form-item
              label="メモ"
              name="note"
              :rules="[{ max: 1000, message: 'メモは1000文字以内で入力してください。' }]"
            >
              <a-textarea v-model:value="form.note" :maxlength="1000" :rows="4" show-count />
            </a-form-item>
          </a-col>
        </a-row>

        <a-button type="primary" html-type="submit" :loading="submitting">
          登録する
        </a-button>
      </a-form>
    </a-card>

    <a-card :bordered="false" title="登録済みトレーニング">
      <a-table
        :columns="columns"
        :data-source="sessions"
        :loading="loading"
        row-key="id"
        :scroll="{ x: 640 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'feeling'">
            <a-rate :value="record.feeling ?? 0" disabled />
          </template>
          <template v-else-if="column.key === 'note'">
            {{ record.note || '—' }}
          </template>
        </template>
      </a-table>
    </a-card>
  </section>
</template>

<style scoped>
.training-history {
  display: grid;
  gap: 24px;
}

.full-width {
  width: 100%;
}
</style>
