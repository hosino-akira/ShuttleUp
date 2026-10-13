<script setup lang="ts">
import { t } from '../../i18n';
import type { VxeTableEvents } from 'vxe-table'
import type { MatchResponse } from '../../types/match'

defineProps<{ matches: MatchResponse[]; loading: boolean }>()
const emit = defineEmits<{ edit: [match: MatchResponse]; delete: [match: MatchResponse] }>()

const editMatchOnDoubleClick: VxeTableEvents.CellDblclick<MatchResponse> = ({ row, $event }) => {
  if ($event.target instanceof Element && $event.target.closest('button, a')) return
  emit('edit', row)
}
</script>

<template>
  <div class="vxe-table-wrapper">
    <vxe-table border stripe show-overflow :loading="loading" :data="matches"
    :empty-text="t('試合記録はまだありません')" :cell-config="{ height: 36 }"
    :column-config="{ resizable: true }" :row-config="{ isCurrent: true }"
    @cell-dblclick="editMatchOnDoubleClick">
    <vxe-column field="matchDate" :title="t('試合日')" min-width="130" />
    <vxe-column field="opponentName" :title="t('対戦相手')" min-width="180" />
    <vxe-column :title="t('スコア')" min-width="130">
      <template #default="{ row }">{{ row.myScore ?? '-' }} - {{ row.opponentScore ?? '-' }}</template>
    </vxe-column>
    <vxe-column field="note" :title="t('メモ')" min-width="240" />
    <vxe-column :title="t('操作')" width="150" fixed="right">
      <template #default="{ row }"><a-button type="link" @click="emit('edit', row)">{{ t('編集') }}</a-button><a-popconfirm :title="t('この試合記録を削除しますか？')" @confirm="emit('delete', row)"><a-button type="link" danger>{{ t('削除') }}</a-button></a-popconfirm></template>
    </vxe-column>
    </vxe-table>
  </div>
</template>
