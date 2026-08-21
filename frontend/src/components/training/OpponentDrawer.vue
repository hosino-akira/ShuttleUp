<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { createOpponent, deleteOpponent, updateOpponent } from '../../api/opponentApi'
import type { OpponentResponse } from '../../types/opponent'

const props = defineProps<{ open: boolean; userId: number; opponents: OpponentResponse[]; initialName?: string }>()
const emit = defineEmits<{ 'update:open': [value: boolean]; changed: [selectedId?: number] }>()
const editingId = ref<number | null>(null)
const submitting = ref(false)
const form = reactive({ name: '', memo: '' })
watch(() => props.open, open => { if (open) { resetForm(); form.name = props.initialName ?? '' } })
function resetForm() { editingId.value = null; form.name = ''; form.memo = '' }
function edit(item: OpponentResponse) { editingId.value = item.id; form.name = item.name; form.memo = item.memo ?? '' }
async function save() {
  const name = form.name.trim()
  if (!name) { message.warning('名前を入力してください。'); return }
  submitting.value = true
  try {
    const request = { name, memo: form.memo.trim() || null }
    const saved = editingId.value ? await updateOpponent(editingId.value, request) : await createOpponent(props.userId, request)
    message.success(editingId.value ? '対戦相手を更新しました。' : '対戦相手を登録しました。')
    resetForm(); emit('changed', saved.id)
  } catch (error) { console.error(error); message.error('対戦相手の保存に失敗しました。') }
  finally { submitting.value = false }
}
async function remove(item: OpponentResponse) {
  try { await deleteOpponent(item.id); message.success('対戦相手を削除しました。'); if (editingId.value === item.id) resetForm(); emit('changed') }
  catch (error) { console.error(error); message.error('この対戦相手は試合記録で使用されているため削除できません。') }
}
</script>

<template>
  <a-drawer :open="open" title="対戦相手管理" width="min(720px, 92vw)" :z-index="1300" @close="emit('update:open', false)">
    <a-table :data-source="opponents" :pagination="false" row-key="id" size="small">
      <a-table-column key="name" title="名前" data-index="name" />
      <a-table-column key="memo" title="メモ" data-index="memo" />
      <a-table-column key="actions" title="操作" width="150"><template #default="{ record }">
        <a-button type="link" @click="edit(record)">編集</a-button>
        <a-popconfirm title="この対戦相手を削除しますか？" @confirm="remove(record)"><a-button type="link" danger>削除</a-button></a-popconfirm>
      </template></a-table-column>
    </a-table>
    <a-divider>{{ editingId ? '対戦相手を編集' : '新規登録' }}</a-divider>
    <a-form layout="vertical"><a-form-item label="名前" required><a-input v-model:value="form.name" :maxlength="100" /></a-form-item>
      <a-form-item label="メモ"><a-textarea v-model:value="form.memo" :maxlength="1000" :rows="3" show-count /></a-form-item>
      <a-space><a-button type="primary" :loading="submitting" @click="save">保存</a-button><a-button v-if="editingId" @click="resetForm">キャンセル</a-button></a-space>
    </a-form>
  </a-drawer>
</template>
