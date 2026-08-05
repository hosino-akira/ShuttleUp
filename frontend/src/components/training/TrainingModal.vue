<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import type { TrainingSession, TrainingSessionUpdateRequest } from '../../types/trainingSession'

const props = defineProps<{
  open: boolean
  loading: boolean
  deleting: boolean
  session: TrainingSession | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  save: [request: TrainingSessionUpdateRequest]
  delete: []
}>()

const formRef = ref<FormInstance>()
const form = reactive({
  trainingDate: '' as string,
  durationMinutes: 60,
  feeling: null as number | null,
  note: '',
})

watch(
  () => props.session,
  (session) => {
    if (!session) return
    Object.assign(form, {
      trainingDate: session.trainingDate,
      durationMinutes: session.durationMinutes,
      feeling: session.feeling,
      note: session.note ?? '',
    })
    formRef.value?.clearValidate()
  },
  { immediate: true },
)

async function handleSave(): Promise<void> {
  try { await formRef.value?.validate() } catch { return }

  emit('save', {
    trainingDate: form.trainingDate,
    durationMinutes: form.durationMinutes,
    feeling: form.feeling,
    note: form.note.trim() || null,
  })
}
</script>

<template>
  <a-modal
    :open="open"
    title="トレーニング記録を編集"
    @update:open="emit('update:open', $event)"
  >
    <a-form ref="formRef" :model="form" layout="vertical">
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

      <a-form-item label="トレーニング時の感覚" name="feeling">
        <a-rate v-model:value="form.feeling" />
      </a-form-item>

      <a-form-item
        label="メモ"
        name="note"
        :rules="[{ max: 1000, message: 'メモは1000文字以内で入力してください。' }]"
      >
        <a-textarea v-model:value="form.note" :maxlength="1000" :rows="4" show-count />
      </a-form-item>
    </a-form>

    <template #footer>
      <div class="modal-footer">
        <a-popconfirm
          title="このトレーニング記録を削除しますか？"
          description="削除した記録は元に戻せません。"
          ok-text="削除"
          cancel-text="キャンセル"
          ok-type="danger"
          @confirm="emit('delete')"
        >
          <a-button danger :loading="deleting" :disabled="loading">削除</a-button>
        </a-popconfirm>

        <div class="footer-actions">
          <a-button :disabled="loading || deleting" @click="emit('update:open', false)">
            キャンセル
          </a-button>
          <a-button type="primary" :loading="loading" :disabled="deleting" @click="handleSave">
            保存
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<style scoped>
.full-width {
  width: 100%;
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.footer-actions {
  display: flex;
  gap: 8px;
}
</style>
