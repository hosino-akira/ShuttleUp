<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import TrainingModal from '../components/training/TrainingModal.vue'
import {
  createTrainingSession,
  deleteTrainingSession,
  getTrainingSessions,
  updateTrainingSession,
} from '../api/trainingSessionApi'
import type {
  TrainingSession,
  TrainingSessionCreateRequest,
  TrainingSessionUpdateRequest,
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
const submitting = ref(false)
const editSubmitting = ref(false)
const deleteSubmitting = ref(false)
const editVisible = ref(false)
const editingSession = ref<TrainingSession | null>(null)
const sortState = reactive<{ field: string; order: 'asc' | 'desc' | null }>({
  field: '',
  order: null,
})
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const emptyForm = (): TrainingSessionForm => ({
  trainingDate: null,
  durationMinutes: 60,
  feeling: 3,
  note: '',
})
const form = reactive<TrainingSessionForm>(emptyForm())

const sortedSessions = computed(() => {
  if (!sortState.field || !sortState.order) return sessions.value
  return [...sessions.value].sort((a, b) => {
    const left = a[sortState.field as keyof TrainingSession]
    const right = b[sortState.field as keyof TrainingSession]
    const result = typeof left === 'number' && typeof right === 'number'
      ? left - right
      : String(left ?? '').localeCompare(String(right ?? ''))
    return sortState.order === 'asc' ? result : -result
  })
})

const pagedSessions = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  return sortedSessions.value.slice(start, start + pagination.pageSize)
})

function handleSortChange({ field, order }: { field: string; order: 'asc' | 'desc' | '' | null }): void {
  sortState.field = field
  sortState.order = order || null
  pagination.currentPage = 1
}

function handlePageChange({ currentPage, pageSize }: { currentPage: number; pageSize: number }): void {
  pagination.currentPage = pageSize === pagination.pageSize ? currentPage : 1
  pagination.pageSize = pageSize
}

async function loadTrainingSessions(): Promise<void> {
  try {
    sessions.value = await getTrainingSessions(userId)
    pagination.total = sessions.value.length
    const lastPage = Math.max(1, Math.ceil(pagination.total / pagination.pageSize))
    pagination.currentPage = Math.min(pagination.currentPage, lastPage)
  } catch (error: unknown) {
    console.error(error)
    message.error('トレーニング履歴の取得に失敗しました。')
  }
}

function resetForm(): void {
  Object.assign(form, emptyForm())
  formRef.value?.clearValidate()
}

async function submitTrainingSession(): Promise<void> {
  try { await formRef.value?.validate() } catch { return }
  if (form.trainingDate === null) return

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

function openEditModal(record: TrainingSession): void {
  editingSession.value = record
  editVisible.value = true
}

async function submitEdit(request: TrainingSessionUpdateRequest): Promise<void> {
  if (editingSession.value === null) return
  editSubmitting.value = true
  try {
    await updateTrainingSession(editingSession.value.id, request)
    message.success('トレーニングを更新しました。')
    editVisible.value = false
    await loadTrainingSessions()
  } catch (error: unknown) {
    console.error(error)
    message.error('トレーニングの更新に失敗しました。')
  } finally {
    editSubmitting.value = false
  }
}

// 削除後に一覧を再取得し、ページ番号も有効な範囲へ調整する。
async function submitDelete(): Promise<void> {
  if (editingSession.value === null) return

  deleteSubmitting.value = true
  try {
    await deleteTrainingSession(editingSession.value.id)
    message.success('トレーニング記録を削除しました。')
    editVisible.value = false
    editingSession.value = null
    await loadTrainingSessions()
  } catch (error: unknown) {
    console.error(error)
    message.error('トレーニング記録の削除に失敗しました。')
  } finally {
    deleteSubmitting.value = false
  }
}

onMounted(loadTrainingSessions)
</script>

<template>
  <section class="training-history" aria-labelledby="training-history-title">
    <a-card class="registration-card" :bordered="false">
      <template #title>
        <span id="training-history-title">トレーニング登録</span>
      </template>
      <a-form
        ref="formRef"
        class="registration-form"
        :model="form"
        layout="vertical"
        @finish="submitTrainingSession"
      >
        <a-row :gutter="12" align="bottom">
          <a-col :xs="24" :md="5">
            <a-form-item label="トレーニング日" name="trainingDate" :rules="[{ required: true, message: 'トレーニング日を選択してください。' }]">
              <a-date-picker v-model:value="form.trainingDate" class="full-width" value-format="YYYY-MM-DD" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="5">
            <a-form-item label="トレーニング時間（分）" name="durationMinutes" :rules="[{ required: true, message: '時間を入力してください。' }, { type: 'number', min: 1, message: '1分以上で入力してください。' }]">
              <a-input-number v-model:value="form.durationMinutes" class="full-width" :min="1" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="4">
            <a-form-item label="トレーニング時の感覚" name="feeling">
              <a-rate v-model:value="form.feeling" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="7">
            <a-form-item label="メモ" name="note" :rules="[{ max: 1000, message: 'メモは1000文字以内で入力してください。' }]">
              <a-input v-model:value="form.note" :maxlength="1000" placeholder="メモを入力" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="3">
            <a-form-item class="submit-form-item">
              <a-button block type="primary" html-type="submit" :loading="submitting">
                登録する
              </a-button>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <a-card class="table-card" :bordered="false" title="登録済みトレーニング">
      <div class="table-scroll-area">
        <vxe-table
          height="100%"
          border
          stripe
          show-overflow
          :data="pagedSessions"
          :sort-config="{ remote: true }"
          :empty-text="'データがありません'"
          @sort-change="handleSortChange"
        >
          <vxe-column field="trainingDate" title="トレーニング日" width="180" sortable />
          <vxe-column field="durationMinutes" title="時間（分）" width="160" sortable />
          <vxe-column field="feeling" title="感覚" width="210" sortable>
            <template #default="{ row }"><a-rate :value="row.feeling ?? 0" disabled /></template>
          </vxe-column>
          <vxe-column field="note" title="メモ" min-width="240">
            <template #default="{ row }">{{ row.note || '—' }}</template>
          </vxe-column>
          <vxe-column title="操作" width="110" fixed="right">
            <template #default="{ row }"><a-button type="link" @click="openEditModal(row)">編集</a-button></template>
          </vxe-column>
        </vxe-table>
      </div>
      <vxe-pager
        class="table-pager"
        :current-page="pagination.currentPage"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[5, 10, 20, 50]"
        :layouts="['Total', 'Sizes', 'PrevPage', 'Number', 'NextPage', 'Jump']"
        @page-change="handlePageChange"
      />
    </a-card>

    <TrainingModal
      v-model:open="editVisible"
      :loading="editSubmitting"
      :deleting="deleteSubmitting"
      :session="editingSession"
      @save="submitEdit"
      @delete="submitDelete"
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

.full-width {
  width: 100%;
}

.registration-card {
  flex: 0 0 auto;
}

.registration-card :deep(.ant-card-head) {
  min-height: 42px;
  padding: 0 16px;
}

.registration-card :deep(.ant-card-body) {
  padding: 10px 16px 6px;
}

.registration-form :deep(.ant-form-item) {
  margin-bottom: 8px;
}

.registration-form :deep(.ant-form-item-label) {
  padding-bottom: 2px;
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

@media (max-width: 767px) {
  .registration-card {
    max-height: 42%;
    overflow: auto;
  }
}
</style>
