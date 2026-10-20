<script setup lang="ts" generic="T extends { id: number; name: string; enabled: boolean; available: boolean; memo?: string | null }">
import { computed, nextTick, ref, watch } from 'vue'
import type { VxeTableEvents, VxeTableInstance } from 'vxe-table'
import { InfoCircleOutlined } from '@ant-design/icons-vue'
import { t } from '../../i18n'
import { COMPACT_TABLE_CELL_CONFIG } from '../../constants/table'

const props = withDefaults(defineProps<{
  title: string
  items: T[]
  selectedId?: number
  ready?: boolean
  busy: boolean
  showMemo?: boolean
}>(), { ready: true, showMemo: false })
const emit = defineEmits<{
  select: [id: number]
  create: []
  edit: [item: T]
  toggle: [item: T]
}>()
const search = ref('')
const tableRef = ref<VxeTableInstance<T>>()
const filtered = computed(() => props.items.filter(item => item.name.toLocaleLowerCase().includes(search.value.trim().toLocaleLowerCase())))

function isSwitchEvent(event: Event) {
  return event.target instanceof Element && Boolean(event.target.closest('[data-master-toggle]'))
}

const selectRow: VxeTableEvents.CellClick<T> = ({ row, $event }) => {
  if (!props.busy && !isSwitchEvent($event)) emit('select', row.id)
}
const editRow: VxeTableEvents.CellDblclick<T> = ({ row, $event }) => {
  if (props.busy || isSwitchEvent($event)) return
  emit('select', row.id)
  emit('edit', row)
}

// 選択は親画面で管理し、Switchの操作では現在行を変更しない。
watch([() => props.selectedId, filtered, tableRef], async () => {
  await nextTick()
  const row = filtered.value.find(item => item.id === props.selectedId)
  if (row) await tableRef.value?.setCurrentRow(row)
  else await tableRef.value?.clearCurrentRow()
}, { immediate: true, flush: 'post' })
</script>

<template>
  <section class="master-data-table" :aria-label="title">
    <a-card class="table-card" :title="title" :bordered="false">
      <template #extra><a-button type="primary" size="small" :disabled="!ready || busy" @click="emit('create')">{{ t('新規') }}</a-button></template>
    <a-input v-model:value="search" :placeholder="t('名称で検索')" :aria-label="t('名称で検索')" allow-clear />
    <div class="vxe-table-wrapper">
      <vxe-table
        ref="tableRef"
        border
        stripe
        show-overflow
        :data="filtered"
        :loading="busy"
        :row-config="{ keyField: 'id', isCurrent: true, isHover: true }"
        :current-row-config="{ trigger: 'manual', strict: true }"
        :virtual-y-config="{ enabled: false }"
        :column-config="{ resizable: true }"
        :cell-config="COMPACT_TABLE_CELL_CONFIG"
        :empty-text="ready ? t('項目がありません。') : t('上位分類を選択してください。')"
        @cell-click="selectRow"
        @cell-dblclick="editRow"
      >
        <vxe-column field="name" :title="t('名称')" min-width="130">
          <template #default="{ row }">
            <span :class="{ inactive: !row.available }">{{ row.name }}</span>
            <a-tooltip v-if="row.enabled && !row.available" :title="t('上位分類が無効')"><InfoCircleOutlined class="parent-disabled" :aria-label="t('上位分類が無効')" /></a-tooltip>
          </template>
        </vxe-column>
        <vxe-column v-if="showMemo" field="memo" :title="t('メモ')" min-width="220" />
        <vxe-column field="enabled" :title="t('有効／無効')" width="110" align="center">
          <template #default="{ row }">
            <span data-master-toggle @click.stop @dblclick.stop>
              <a-switch
                :checked="row.enabled"
                :disabled="busy"
                :checked-children="t('有効')"
                :un-checked-children="t('無効')"
                :aria-label="t('「{0}」の有効状態', { 0: row.name })"
                @change="emit('toggle', row)"
              />
            </span>
          </template>
        </vxe-column>
      </vxe-table>
    </div>
    </a-card>
  </section>
</template>

<style scoped>
.master-data-table { min-width: 0; }
.vxe-table-wrapper { margin-top: 8px; }
.inactive, .parent-disabled { color: var(--app-muted-color); }
.parent-disabled { margin-inline-start: 6px; font-size: 12px; }
:deep(.vxe-body--row) { cursor: pointer; }
</style>
