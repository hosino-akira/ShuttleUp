<script setup lang="ts">
import type { PlantType } from '../../utils/forest'
withDefaults(defineProps<{ type: PlantType; size?: number }>(), { size: 100 })
</script>

<template>
  <svg viewBox="0 0 100 120" :width="size" :height="size * 1.2" fill="none" aria-hidden="true" class="forest-plant">
    <ellipse cx="50" cy="109" rx="26" ry="7" fill="#496f40" opacity=".16" />
    <template v-if="type === 'cedar'">
      <path d="M46 70h8v37l-4 3-4-3z" fill="#936b48" />
      <path d="M50 22 15 91q35 14 70 0z" fill="#397350" />
      <path d="M50 12 21 72q29 14 58 0z" fill="#4c8d5b" />
      <path d="M50 5 29 49q21 9 42 0z" fill="#73a56a" />
      <path d="m50 5 0 44q-12 0-21-0z" fill="#87b578" opacity=".65" />
    </template>
    <template v-else-if="type === 'oak'">
      <path d="M45 55h10v51l-5 4-5-4z" fill="#8e6746" />
      <path d="m50 85-15-15m15 10 16-17" stroke="#8e6746" stroke-width="5" stroke-linecap="round" />
      <path d="M21 73C6 69 9 47 22 43 15 23 37 15 48 24c10-18 32-10 32 7 22 3 24 28 10 36 1 22-30 24-38 14-10 11-33 10-31-8" fill="#558e54" />
      <ellipse cx="34" cy="43" rx="20" ry="21" fill="#83ac64" />
      <ellipse cx="59" cy="35" rx="19" ry="18" fill="#83ac64" />
      <ellipse cx="72" cy="56" rx="20" ry="21" fill="#6d9d58" />
      <ellipse cx="39" cy="63" rx="19" ry="18" fill="#6d9d58" />
      <path d="M22 41q3-11 13-12" stroke="#a3c17b" stroke-width="4" stroke-linecap="round" />
    </template>
    <template v-else-if="type === 'sakura'">
      <path d="M45 53h10v53l-5 4-5-4z" fill="#97644f" />
      <path d="m50 87-17-18m17 11 18-19" stroke="#97644f" stroke-width="5" stroke-linecap="round" />
      <path d="M19 71C4 65 9 46 21 40 15 22 36 17 46 24c12-18 35-13 35 6 22 5 25 26 11 36 4 19-24 26-40 15-15 13-36 8-33-10" fill="#d891a2" />
      <circle cx="33" cy="42" r="21" fill="#f1b9c2" /><circle cx="59" cy="34" r="20" fill="#f1b9c2" />
      <circle cx="73" cy="54" r="20" fill="#eaa6b6" /><circle cx="39" cy="64" r="20" fill="#eaa6b6" />
      <g fill="#ffe0df"><circle cx="28" cy="35" r="3" /><circle cx="58" cy="25" r="3" /><circle cx="70" cy="51" r="3" /><circle cx="40" cy="59" r="3" /><circle cx="59" cy="63" r="2" /></g>
      <path d="m82 90-3 5m-53 1-3 4" stroke="#f1b9c2" stroke-width="3" stroke-linecap="round" />
    </template>
    <template v-else-if="type === 'maple'">
      <path d="M46 54h8v52l-4 4-4-4z" fill="#906349" />
      <path d="m50 81-17-19m17 13 15-19" stroke="#906349" stroke-width="4" stroke-linecap="round" />
      <path d="m50 7 12 17 16-4-2 17 18 12-14 14 5 18-25 3-10 10-10-10-26-3 6-18L7 49l17-12-1-17 15 4z" fill="#cf7847" />
      <path d="m50 7 12 17 16-4-2 17 18 12-23 7-21 24-20-21-23-10 17-12-1-17 15 4z" fill="#e9a45b" />
      <path d="M50 19v54M30 41l20 22 21-22" stroke="#f4be72" stroke-width="3" stroke-linecap="round" />
    </template>
    <template v-else-if="type === 'sunflower'">
      <path d="M50 50v56" stroke="#5f8c4a" stroke-width="5" stroke-linecap="round" />
      <path d="M48 92C24 91 23 78 24 76c19-1 25 10 24 16m4-15c22-1 23-16 22-18-17 2-23 12-22 18" fill="#7aa357" />
      <g fill="#f1c252"><ellipse cx="50" cy="25" rx="8" ry="17" /><ellipse cx="50" cy="63" rx="8" ry="17" /><ellipse cx="31" cy="44" rx="17" ry="8" /><ellipse cx="69" cy="44" rx="17" ry="8" /><ellipse cx="36" cy="30" rx="8" ry="15" transform="rotate(-45 36 30)" /><ellipse cx="64" cy="30" rx="8" ry="15" transform="rotate(45 64 30)" /><ellipse cx="36" cy="58" rx="8" ry="15" transform="rotate(45 36 58)" /><ellipse cx="64" cy="58" rx="8" ry="15" transform="rotate(-45 64 58)" /></g>
      <circle cx="50" cy="44" r="15" fill="#9b7145" /><circle cx="47" cy="40" r="10" fill="#b58a51" />
      <g fill="#8c623d"><circle cx="46" cy="39" r="1.5" /><circle cx="54" cy="42" r="1.5" /><circle cx="48" cy="48" r="1.5" /></g>
    </template>
    <template v-else>
      <path d="m50 107-3-73m1 71L28 53m23 50 20-59m-23 61-12-47m16 45 9-61" stroke="#789164" stroke-width="3" stroke-linecap="round" />
      <path d="M48 105C29 97 24 87 23 80c13 3 23 10 25 25m4-1c17-10 21-20 23-28-16 5-23 15-23 28" fill="#8ea575" />
      <g fill="#a392bf"><ellipse cx="46" cy="36" rx="6" ry="18" transform="rotate(-3 46 36)" /><ellipse cx="27" cy="51" rx="6" ry="17" transform="rotate(-23 27 51)" /><ellipse cx="70" cy="43" rx="6" ry="18" transform="rotate(19 70 43)" /></g>
      <g fill="#bfafd1"><ellipse cx="36" cy="59" rx="6" ry="16" transform="rotate(-12 36 59)" /><ellipse cx="61" cy="57" rx="6" ry="16" transform="rotate(11 61 57)" /><ellipse cx="45" cy="30" rx="4" ry="10" /></g>
    </template>
  </svg>
</template>

<style scoped>
.forest-plant { display: block; overflow: visible; flex-shrink: 0; }
</style>
