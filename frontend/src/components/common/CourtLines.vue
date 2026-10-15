<script setup lang="ts">
withDefaults(defineProps<{ orientation?: 'portrait' | 'landscape' }>(), {
  orientation: 'portrait',
})
</script>

<template>
  <svg :viewBox="orientation === 'landscape' ? '0 0 1340 610' : '0 0 610 1340'" preserveAspectRatio="xMidYMid meet" fill="none" stroke="currentColor" stroke-width="1.2" aria-hidden="true" focusable="false" class="court-lines">
    <!-- Centimetres: 13.40 m × 6.10 m, including the 4 cm boundary lines. -->
    <g :transform="orientation === 'landscape' ? 'translate(1340 0) rotate(90)' : undefined">
      <rect x="2" y="2" width="606" height="1336" />
      <!-- Singles sidelines: 5.18 m wide, 0.46 m inside each doubles sideline. -->
      <path d="M48 2V1338M562 2V1338" />
      <!-- Short service lines: net-facing edge 1.98 m from the net. -->
      <path d="M2 470H608M2 870H608" />
      <!-- Doubles long service lines: 0.76 m inside each back boundary. -->
      <path d="M2 78H608M2 1262H608" />
      <!-- Centre lines stop at the short service lines; there is no centre line at the net. -->
      <path d="M305 2V470M305 870V1338" />
      <path d="M2 670H608" stroke-width="1.8" stroke-dasharray="6 5" />
    </g>
  </svg>
</template>

<style scoped>
.court-lines { display: block; height: auto; pointer-events: none; }
.court-lines :is(rect, path) { vector-effect: non-scaling-stroke; }
</style>
