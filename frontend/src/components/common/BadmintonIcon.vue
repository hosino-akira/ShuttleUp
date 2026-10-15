<script setup lang="ts">
import CourtLines from './CourtLines.vue'
withDefaults(defineProps<{ kind?: 'shuttle' | 'racket' | 'court'; size?: number }>(), {
  kind: 'shuttle', size: 24,
})
</script>

<template>
  <svg v-if="kind !== 'court'" :width="size" :height="size" viewBox="0 0 32 32" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false" class="badminton-icon">
    <g v-if="kind === 'shuttle'" transform="rotate(28 16 16)">
      <path d="M11.5 22h9v3a4.5 4.5 0 0 1-9 0z" fill="currentColor" fill-opacity=".16" />
      <path d="M11.5 22 5 7l5-3 6 2 6-2 5 3-6.5 15z" />
      <path d="m10 4 4 18M16 6v16M22 4l-4 18M8 14h16M10 18h12" />
    </g>
    <g v-else-if="kind === 'racket'" transform="rotate(32 16 16)">
      <ellipse cx="16" cy="11" rx="8" ry="10" fill="currentColor" fill-opacity=".06" />
      <path d="M16 21v4m-2 0h4v6h-4zM12 3v16M16 1v20M20 3v16M9 7h14M8 11h16M9 15h14" />
    </g>
  </svg>
  <CourtLines v-else orientation="landscape" :style="{ width: `${size}px`, height: `${size}px` }" class="badminton-icon badminton-court-icon" />
</template>

<style scoped>
.badminton-icon { display: inline-block; flex: 0 0 auto; vertical-align: middle; }
.badminton-court-icon :deep(rect), .badminton-court-icon :deep(path) { stroke-width: .7px; }
</style>
