<script setup lang="ts">
import { computed } from 'vue'
import { t, formatLocalDate } from '../../i18n'
import { getPlantType, plantNames } from '../../utils/forest'
import type { TrainingSession } from '../../types/trainingSession'
import ForestPlant from './ForestPlant.vue'
const props = defineProps<{ sessions: readonly TrainingSession[]; highlightedId?: number; selectedId?: number }>()
const emit = defineEmits<{ select: [session: TrainingSession] }>()
// Fill from the back toward the viewer. Keep each plot to 36 sessions.
const tiles = computed(() => Array.from({ length: 36 }, (_, index) => {
  const row = Math.floor(index / 6)
  const col = index % 6
  return { index, row, col, x: 340 + (col - row) * 46, y: 148 + (col + row) * 23, session: props.sessions[index] }
}).sort((a, b) => a.row + a.col - b.row - b.col || a.col - b.col))
function points(x: number, y: number) { return `${x},${y - 23} ${x + 46},${y} ${x},${y + 23} ${x - 46},${y}` }
</script>

<template>
  <svg class="forest-scene" viewBox="0 0 680 470" role="group" :aria-label="t('あなたのトレーニングの森')">
    <ellipse cx="340" cy="397" rx="243" ry="30" fill="#688056" opacity=".10" />
    <g fill="#fffdf5" opacity=".65">
      <path d="M88 90c0-12 17-18 24-9 4-22 33-22 38-4 17-7 27 7 24 13z" />
      <path d="M494 107c0-10 13-14 18-7 3-17 25-17 29-3 13-6 21 5 18 10z" />
    </g>
    <path d="m64 263 276 138 276-138v18L340 419 64 281z" fill="#abbe82" />
    <path d="m340 401 276-138v18L340 419z" fill="#94aa70" />
    <path d="m340 125 276 138-276 138L64 263z" fill="#c5d69b" />
    <g v-for="tile in tiles" :key="`ground-${tile.index}`">
      <polygon :points="points(tile.x, tile.y)" :fill="(tile.row + tile.col) % 2 ? '#c0d393' : '#c8d99f'" stroke="#dfebbb" stroke-width=".7" />
      <path v-if="!tile.session" :d="`m${tile.x - 3} ${tile.y + 2} -2 -5 m4 5 2 -7 m0 7 4 -3`" stroke="#95b574" stroke-width="1.5" stroke-linecap="round" opacity=".6" />
    </g>
    <g v-for="tile in tiles" :key="tile.index">
      <g v-if="tile.session" class="planted-tree" :class="{ 'new-tree': tile.session.id === highlightedId }"
        role="button" tabindex="0" :aria-label="`${t(plantNames[getPlantType(tile.session)])} · ${formatLocalDate(tile.session.trainingDate)} · ${t('{minutes}分', { minutes: tile.session.durationMinutes })}`"
        :aria-pressed="selectedId === tile.session.id"
        @click="emit('select', tile.session)" @keydown.enter="emit('select', tile.session)" @keydown.space.prevent="emit('select', tile.session)">
        <title>{{ t(plantNames[getPlantType(tile.session)]) }} · {{ formatLocalDate(tile.session.trainingDate) }} · {{ t('{minutes}分', { minutes: tile.session.durationMinutes }) }}</title>
        <ellipse class="tree-focus" :cx="tile.x" :cy="tile.y + 2" rx="31" ry="14" :opacity="selectedId === tile.session.id ? 1 : 0" />
        <ForestPlant :type="getPlantType(tile.session)" :size="Math.min(76, 55 + tile.session.durationMinutes / 6)" :x="tile.x - Math.min(76, 55 + tile.session.durationMinutes / 6) / 2" :y="tile.y - Math.min(76, 55 + tile.session.durationMinutes / 6) * 1.09" />
      </g>
    </g>
    <g fill="#82a766"><ellipse cx="103" cy="326" rx="10" ry="4" /><ellipse cx="568" cy="226" rx="9" ry="4" /><ellipse cx="206" cy="396" rx="7" ry="3" /></g>
    <g fill="#fdf8d5"><circle cx="104" cy="322" r="3" /><circle cx="569" cy="222" r="3" /></g>
  </svg>
</template>

<style scoped>
.forest-scene { display: block; width: 100%; height: auto; max-height: 490px; }
.planted-tree { cursor: pointer; outline: none; }
.tree-focus { fill: var(--app-selected-color); stroke: var(--app-primary-color); stroke-width: 2; }
.planted-tree:hover .tree-focus, .planted-tree:focus-visible .tree-focus { opacity: 1; }
.new-tree { animation: plant-in .8s ease-out; }
@keyframes plant-in { from { opacity: 0; } to { opacity: 1; } }
@media (prefers-reduced-motion: reduce) { .new-tree { animation: none; } }
</style>
