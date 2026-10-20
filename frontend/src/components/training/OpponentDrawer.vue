<script setup lang="ts">
import { t } from '../../i18n';
import { reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { createOpponent, updateOpponent } from '../../api/opponentApi'
import type { OpponentResponse } from '../../types/opponent'
import { apiError } from '../../utils/apiError'

const props = defineProps<{ open: boolean; userId: number; opponents: OpponentResponse[]; initialName?: string }>()
const emit = defineEmits<{ 'update:open': [value: boolean]; changed: [selectedId?: number] }>()
const editingId = ref<number | null>(null)
const submitting = ref(false)
const errorMessage = ref('')
const form = reactive({ name: '', memo: '' })
watch(() => props.open, open => { if (open) { resetForm(); form.name = props.initialName ?? '' } })
function resetForm() { editingId.value = null; form.name = ''; form.memo = ''; errorMessage.value = '' }
function edit(item: OpponentResponse) { editingId.value = item.id; form.name = item.name; form.memo = item.memo ?? '' }
async function save() {
  if (submitting.value) return
  const name = form.name.trim()
  if (!name) { message.warning(t('名前を入力してください。')); return }
  submitting.value = true
  try {
    const request = { name, memo: form.memo.trim() || null }
    const saved = editingId.value ? await updateOpponent(editingId.value, request) : await createOpponent(props.userId, request)
    message.success(editingId.value ? t('対戦相手を更新しました。') : t('対戦相手を登録しました。'))
    resetForm(); emit('changed', saved.enabled ? saved.id : undefined)
  } catch (error) { errorMessage.value = apiError(error, '対戦相手の保存に失敗しました。') }
  finally { submitting.value = false }
}
async function toggle(item: OpponentResponse) {
  if (submitting.value) return
  submitting.value = true
  errorMessage.value = ''
  try { await updateOpponent(item.id, { name: item.name, memo: item.memo, enabled: !item.enabled }); emit('changed') }
  catch (error) { errorMessage.value = apiError(error, '対戦相手の保存に失敗しました。') }
  finally { submitting.value = false }
}
</script>

<template>
  <a-drawer :open="open" :title="t('対戦相手管理')" width="min(720px, 92vw)" :z-index="1300" @close="emit('update:open', false)">
    <a-alert v-if="errorMessage" :message="t(errorMessage)" type="error" show-icon role="alert" />
    <a-table :data-source="opponents" :pagination="false" row-key="id" size="small">
      <a-table-column key="name" :title="t('名前')" data-index="name" />
      <a-table-column key="memo" :title="t('メモ')" data-index="memo" />
      <a-table-column key="actions" :title="t('操作')" width="150"><template #default="{ record }">
        <a-space><a-button size="small" :disabled="submitting" @click="edit(record)">{{ t('編集') }}</a-button>
        <a-switch :checked="record.enabled" :disabled="submitting" :aria-label="t('「{0}」の有効状態', { 0: record.name })" @change="toggle(record)" /></a-space>
      </template></a-table-column>
    </a-table>
    <a-divider>{{ editingId ? t('対戦相手を編集') : t('新規') }}</a-divider>
    <a-form layout="vertical"><a-form-item :label="t('名前')" required><a-input v-model:value="form.name" :maxlength="100" /></a-form-item>
      <a-form-item :label="t('メモ')"><a-textarea v-model:value="form.memo" :maxlength="1000" :rows="3" show-count /></a-form-item>
      <a-space><a-button type="primary" :loading="submitting" @click="save">{{ t('保存') }}</a-button><a-button v-if="editingId" @click="resetForm">{{ t('キャンセル') }}</a-button></a-space>
    </a-form>
  </a-drawer>
</template>
