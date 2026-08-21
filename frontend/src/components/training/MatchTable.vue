<script setup lang="ts">
import type { MatchResponse } from '../../types/match'

defineProps<{ matches: MatchResponse[]; loading: boolean }>()
const emit = defineEmits<{ edit: [match: MatchResponse]; delete: [match: MatchResponse] }>()
</script>

<template>
  <div class="vxe-table-wrapper">
    <vxe-table border stripe show-overflow :loading="loading" :data="matches"
    empty-text="試合記録はまだありません" :cell-config="{ height: 36 }"
    :column-config="{ resizable: true }">
    <vxe-column field="matchDate" title="試合日" min-width="130" />
    <vxe-column field="opponentName" title="対戦相手" min-width="180" />
    <vxe-column title="スコア" min-width="130">
      <template #default="{ row }">{{ row.myScore ?? '-' }} - {{ row.opponentScore ?? '-' }}</template>
    </vxe-column>
    <vxe-column field="note" title="メモ" min-width="240" />
    <vxe-column title="操作" width="150" fixed="right">
      <template #default="{ row }"><a-button type="link" @click="emit('edit', row)">編集</a-button><a-popconfirm title="この試合記録を削除しますか？" @confirm="emit('delete', row)"><a-button type="link" danger>削除</a-button></a-popconfirm></template>
    </vxe-column>
    </vxe-table>
  </div>
</template>
