<script setup lang="ts">
import PageIntro from '../components/common/PageIntro.vue'
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { t } from '../i18n'
import { requireUserId } from '../stores/auth'
import { createMaster, getTrainingMaster, updateMaster } from '../api/masterDataApi'
import { createOpponent, getOpponents, updateOpponent } from '../api/opponentApi'
import type { MasterItem, MasterKind, TrainingKind, TrainingMaster } from '../types/masterData'
import type { OpponentResponse } from '../types/opponent'
import { apiError } from '../utils/apiError'
import { selectPopupContainer } from '../utils/selectPopupContainer'
import { useLocalizedForm } from '../composables/useLocalizedForm'
import BaseSmallModal from '../components/common/BaseSmallModal.vue'
import MasterDataTable from '../components/training/MasterDataTable.vue'

const tab = ref('training')
const master = ref<TrainingMaster>({ categories: [], types: [], exercises: [] })
const opponents = ref<OpponentResponse[]>([])
const categoryId = ref<number>()
const typeId = ref<number>()
const exerciseId = ref<number>()
const opponentId = ref<number>()
const loading = ref(false)
const saving = ref(false)
const loadError = ref(false)
const error = ref('')
const modalOpen = ref(false)
const editingKind = ref<MasterKind | 'OPPONENT'>('CATEGORY')
const editingId = ref<number>()
const editingParentId = ref<number>()
const editingEnabled = ref(true)
const form = reactive({ name: '', memo: '', statisticsType: 'OTHER' as TrainingKind })
const formRef = ref<FormInstance>()
const { onValidate, reset: resetValidation } = useLocalizedForm(formRef)
let requestId = 0
const busy = computed(() => loading.value || saving.value)
const types = computed(() => master.value.types.filter(item => item.parentId === categoryId.value))
const exercises = computed(() => master.value.exercises.filter(item => item.parentId === typeId.value))
const kindNames = computed(() => ({ CATEGORY: t('大分類'), TYPE: t('中分類'), EXERCISE: t('小分類'), OPPONENT: t('対戦相手') }))
const title = computed(() => `${kindNames.value[editingKind.value]} · ${editingId.value == null ? t('新規') : t('編集')}`)
const statisticsOptions = computed(() => [
  { value: 'PHYSICAL', label: t('フィジカル') }, { value: 'SKILL', label: t('技術練習') }, { value: 'OTHER', label: t('その他') },
])
const parentName = computed(() => {
  if (editingKind.value === 'TYPE') return master.value.categories.find(item => item.id === editingParentId.value)?.name
  if (editingKind.value === 'EXERCISE') return master.value.types.find(item => item.id === editingParentId.value)?.name
  return undefined
})

async function load() {
  const current = ++requestId
  loading.value = true
  loadError.value = false
  try {
    const [training, people] = await Promise.all([getTrainingMaster(), getOpponents(requireUserId())])
    if (current !== requestId) return
    master.value = training
    opponents.value = people
    if (!training.categories.some(item => item.id === categoryId.value)) categoryId.value = training.categories[0]?.id
    if (!types.value.some(item => item.id === typeId.value)) typeId.value = types.value[0]?.id
    if (!exercises.value.some(item => item.id === exerciseId.value)) exerciseId.value = exercises.value[0]?.id
    if (!people.some(item => item.id === opponentId.value)) opponentId.value = undefined
  } catch {
    if (current === requestId) loadError.value = true
  } finally {
    if (current === requestId) loading.value = false
  }
}

function selectCategory(id: number) {
  if (categoryId.value === id) return
  categoryId.value = id
  typeId.value = types.value[0]?.id
  exerciseId.value = exercises.value[0]?.id
}

function selectType(id: number) {
  if (typeId.value === id) return
  typeId.value = id
  exerciseId.value = exercises.value[0]?.id
}

function openEditor(kind: MasterKind | 'OPPONENT', item?: MasterItem | OpponentResponse) {
  if (busy.value) return
  editingKind.value = kind
  editingId.value = item?.id
  editingEnabled.value = item?.enabled ?? true
  editingParentId.value = item && 'parentId' in item ? item.parentId ?? undefined
    : kind === 'TYPE' ? categoryId.value : kind === 'EXERCISE' ? typeId.value : undefined
  Object.assign(form, { name: item?.name ?? '', memo: item && 'memo' in item ? item.memo ?? '' : '',
    statisticsType: item && 'statisticsType' in item ? item.statisticsType ?? 'OTHER' : 'OTHER' })
  error.value = ''
  formRef.value?.clearValidate()
  resetValidation()
  modalOpen.value = true
}

async function save() {
  if (busy.value) return
  try { await formRef.value?.validate() } catch { return }
  if (busy.value) return
  saving.value = true
  error.value = ''
  try {
    const name = form.name.trim()
    if (editingKind.value === 'OPPONENT') {
      const request = { name, memo: form.memo.trim() || null }
      if (editingId.value == null) await createOpponent(requireUserId(), request)
      else await updateOpponent(editingId.value, { ...request, enabled: editingEnabled.value })
    } else if (editingId.value == null) {
      const created = await createMaster(editingKind.value, { name, parentId: editingParentId.value,
        statisticsType: editingKind.value === 'CATEGORY' ? form.statisticsType : undefined })
      if (editingKind.value === 'CATEGORY') categoryId.value = created.id
      if (editingKind.value === 'TYPE') typeId.value = created.id
      if (editingKind.value === 'EXERCISE') exerciseId.value = created.id
    } else {
      await updateMaster(editingKind.value, editingId.value, name, editingEnabled.value)
    }
    modalOpen.value = false
    message.success(t('保存しました。'))
    await load()
  } catch (cause) {
    error.value = apiError(cause, '保存に失敗しました。')
  } finally { saving.value = false }
}

async function toggle(kind: MasterKind, item: MasterItem) {
  if (busy.value) return
  saving.value = true
  error.value = ''
  try {
    await updateMaster(kind, item.id, item.name, !item.enabled)
    await load()
  } catch (cause) { error.value = apiError(cause, '保存に失敗しました。') }
  finally { saving.value = false }
}

async function toggleOpponent(item: OpponentResponse) {
  if (busy.value) return
  saving.value = true
  error.value = ''
  try {
    await updateOpponent(item.id, { name: item.name, memo: item.memo, enabled: !item.enabled })
    await load()
  } catch (cause) { error.value = apiError(cause, '保存に失敗しました。') }
  finally { saving.value = false }
}

onMounted(load)
onBeforeUnmount(() => { requestId++ })
</script>

<template>
  <div class="master-page">
    <PageIntro :title="t('マスタ管理')">
      <template #description>
        <span>{{ t('自分だけに反映。クリックで選択、ダブルクリックで編集。') }}</span>
        <span>{{ t('有効＝新規選択可／無効＝非表示（履歴保持）') }}</span>
      </template>
      <template #actions>
        <a-button :loading="loading" :disabled="saving" @click="load">{{ t('再読み込み') }}</a-button>
      </template>
    </PageIntro>
    <a-alert v-if="error && !modalOpen" type="error" :message="t(error)" show-icon role="alert" />
    <a-alert v-if="loadError" type="error" :message="t('マスタの取得に失敗しました。再読み込みしてください。')" show-icon role="alert" />
    <a-card class="content-card" :bordered="false">
    <a-spin :spinning="loading">
      <a-tabs v-model:active-key="tab">
        <a-tab-pane key="training" :tab="t('トレーニング分類')">
          <div class="master-grid">
            <MasterDataTable :title="t('大分類')" :items="master.categories" :selected-id="categoryId" :ready="!loadError" :busy="busy || loadError" @select="selectCategory" @create="openEditor('CATEGORY')" @edit="openEditor('CATEGORY', $event)" @toggle="toggle('CATEGORY', $event)" />
            <MasterDataTable :title="t('中分類')" :items="types" :selected-id="typeId" :ready="categoryId != null && !loadError" :busy="busy || loadError" @select="selectType" @create="openEditor('TYPE')" @edit="openEditor('TYPE', $event)" @toggle="toggle('TYPE', $event)" />
            <MasterDataTable :title="t('小分類')" :items="exercises" :selected-id="exerciseId" :ready="typeId != null && !loadError" :busy="busy || loadError" @select="exerciseId = $event" @create="openEditor('EXERCISE')" @edit="openEditor('EXERCISE', $event)" @toggle="toggle('EXERCISE', $event)" />
          </div>
        </a-tab-pane>
        <a-tab-pane key="opponents" :tab="t('対戦相手')">
          <MasterDataTable :title="t('対戦相手')" :items="opponents" :selected-id="opponentId" :ready="!loadError" :busy="busy || loadError" show-memo @select="opponentId = $event" @create="openEditor('OPPONENT')" @edit="openEditor('OPPONENT', $event)" @toggle="toggleOpponent" />
        </a-tab-pane>
      </a-tabs>
    </a-spin>
    </a-card>

    <BaseSmallModal :open="modalOpen" :title="title" :loading="saving" @update:open="modalOpen = $event">
      <a-alert v-if="error" type="error" :message="t(error)" show-icon role="alert" class="editor-error" />
      <p v-if="parentName" class="hint">{{ t('所属分類') }}: {{ parentName }}</p>
      <a-form ref="formRef" :model="form" layout="vertical" @validate="onValidate">
        <a-form-item :label="t('名称')" name="name" :rules="[{ required: true, whitespace: true, message: t('名前を入力してください。') }, { max: 100, message: t('名前は1〜100文字で入力してください。') }]"><a-input v-model:value="form.name" :maxlength="100" :disabled="saving" @press-enter="save" /></a-form-item>
        <a-form-item v-if="editingKind === 'CATEGORY'" name="statisticsType" :label="t('統計区分')"><a-select v-model:value="form.statisticsType" :options="statisticsOptions" :get-popup-container="selectPopupContainer" :dropdown-style="{ pointerEvents: 'auto' }" :disabled="editingId != null || saving" /><p class="hint">{{ t('統計区分は作成後に変更できません。') }}</p></a-form-item>
        <a-form-item v-if="editingKind === 'OPPONENT'" name="memo" :label="t('メモ')"><a-textarea v-model:value="form.memo" :maxlength="1000" :rows="3" :disabled="saving" show-count /></a-form-item>
      </a-form>
      <template #footer><div class="editor-footer"><a-button :disabled="saving" @click="modalOpen = false">{{ t('キャンセル') }}</a-button><a-button type="primary" :loading="saving" @click="save">{{ t('保存') }}</a-button></div></template>
    </BaseSmallModal>
  </div>
</template>

<style scoped>
.master-page { display: flex; flex-direction: column; gap: var(--app-section-gap); }
.master-page :deep(.page-intro-description) { display: flex; flex-wrap: wrap; gap: 2px 16px; }
.hint { color: var(--app-muted-color); font-size: 12px; margin: 4px 0 8px; }
.master-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: var(--app-section-gap); align-items: start; }
.editor-footer { display: flex; justify-content: flex-end; gap: 8px; }
.editor-error { margin-bottom: 16px; }
@media (max-width: 800px) { .master-grid { grid-template-columns: 1fr; } }
</style>
