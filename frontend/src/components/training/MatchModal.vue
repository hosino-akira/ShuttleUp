<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { createMatch, deleteMatch, updateMatch } from '../../api/matchApi'
import type { MatchCreateRequest, MatchResponse } from '../../types/match'
import type { OpponentResponse } from '../../types/opponent'
import BaseLargeModal from '../common/BaseLargeModal.vue'
import ConfirmModal from '../common/ConfirmModal.vue'

type Mode = 'create' | 'edit'
type Action = 'save' | 'delete'
const props = defineProps<{ open: boolean; mode: Mode; sessionId: number; match: MatchResponse | null; opponents: OpponentResponse[]; selectedOpponentId?: number }>()
const emit = defineEmits<{ 'update:open': [value: boolean]; saved: []; manageOpponents: []; createOpponent: [name: string] }>()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const confirmOpen = ref(false)
const action = ref<Action>('save')
const opponentSearch = ref('')
const emptyForm = (): MatchCreateRequest => ({ opponentId: 0, matchDate: '', myScore: null, opponentScore: null, videoUrl: null, note: null })
const form = reactive<MatchCreateRequest>(emptyForm())
const options = computed(() => {
  const items = props.opponents.map(item => ({ value: item.id, label: item.name }))
  const name = opponentSearch.value.trim()
  if (name && !props.opponents.some(item => item.name === name)) items.push({ value: -1, label: `＋「${name}」を新規登録` })
  return items
})

watch(() => props.open, open => {
  if (!open) return
  Object.assign(form, props.match ? {
    opponentId: props.match.opponentId, matchDate: props.match.matchDate,
    myScore: props.match.myScore, opponentScore: props.match.opponentScore,
    videoUrl: props.match.videoUrl, note: props.match.note,
  } : emptyForm())
  formRef.value?.clearValidate()
})
watch(() => props.selectedOpponentId, id => {
  if (props.open && props.mode === 'create' && id !== undefined) form.opponentId = id
})
function handleOpponentChange(value: number) {
  if (value !== -1) return
  const name = opponentSearch.value.trim()
  form.opponentId = 0
  if (name) emit('createOpponent', name)
}

async function requestSave() {
  try { await formRef.value?.validate() } catch { return }
  action.value = 'save'; confirmOpen.value = true
}
function requestDelete() { action.value = 'delete'; confirmOpen.value = true }
async function executeAction() {
  submitting.value = true
  try {
    if (action.value === 'delete') {
      if (!props.match) return
      await deleteMatch(props.match.id)
      message.success('試合記録を削除しました。')
    } else {
      const request = { ...form, videoUrl: form.videoUrl?.trim() || null, note: form.note?.trim() || null }
      if (props.mode === 'edit' && props.match) await updateMatch(props.match.id, request)
      else await createMatch(props.sessionId, request)
      message.success(props.mode === 'edit' ? '試合記録を更新しました。' : '試合記録を登録しました。')
    }
    confirmOpen.value = false; emit('update:open', false); emit('saved')
  } catch (error) { console.error(error); message.error(action.value === 'delete' ? '試合記録の削除に失敗しました。' : '試合記録の保存に失敗しました。') }
  finally { submitting.value = false }
}
</script>

<template>
  <BaseLargeModal :open="open" :title="mode === 'create' ? '試合記録の新規登録' : '試合記録の編集'"
    :z-index="1200" @update:open="emit('update:open', $event)">
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-row :gutter="16">
        <a-col :xs="24" :md="12"><a-form-item label="対戦相手" name="opponentId"
          :rules="[{ required: true, type: 'number', min: 1, message: '対戦相手を選択してください。' }]">
          <a-select v-model:value="form.opponentId" show-search option-filter-prop="label" :options="options" placeholder="名前で検索" @search="opponentSearch = $event" @change="handleOpponentChange" />
          <a-button type="link" class="manage-button" @click="emit('manageOpponents')">対戦相手を管理</a-button>
        </a-form-item></a-col>
        <a-col :xs="24" :md="12"><a-form-item label="試合日" name="matchDate"
          :rules="[{ required: true, message: '試合日を選択してください。' }]">
          <a-date-picker v-model:value="form.matchDate" value-format="YYYY-MM-DD" class="full-width" />
        </a-form-item></a-col>
        <a-col :xs="12" :md="6"><a-form-item label="自分のスコア"><a-input-number v-model:value="form.myScore" :min="0" class="full-width" /></a-form-item></a-col>
        <a-col :xs="12" :md="6"><a-form-item label="相手のスコア"><a-input-number v-model:value="form.opponentScore" :min="0" class="full-width" /></a-form-item></a-col>
        <a-col :xs="24" :md="12"><a-form-item label="動画URL"><a-input v-model:value="form.videoUrl" :maxlength="2048" /></a-form-item></a-col>
      </a-row>
      <a-form-item label="メモ"><a-textarea v-model:value="form.note" :maxlength="1000" :rows="3" show-count /></a-form-item>
    </a-form>
    <template #footer><div class="modal-footer">
      <a-button v-if="mode === 'edit'" danger :disabled="submitting" @click="requestDelete">削除</a-button><span v-else />
      <a-space><a-button :disabled="submitting" @click="emit('update:open', false)">キャンセル</a-button><a-button type="primary" :loading="submitting" @click="requestSave">保存</a-button></a-space>
    </div></template>
  </BaseLargeModal>
  <ConfirmModal v-model:open="confirmOpen" :message="action === 'delete' ? '削除してもよろしいですか？' : '保存してもよろしいですか？'"
    :description="action === 'delete' ? '削除したデータは元に戻せません。' : ''" :danger="action === 'delete'" :loading="submitting" :z-index="1400" @confirm="executeAction" />
</template>

<style scoped>.full-width{width:100%}.manage-button{padding-left:0}.modal-footer{display:flex;align-items:center;justify-content:space-between}</style>
