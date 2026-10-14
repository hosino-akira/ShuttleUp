<script setup lang="ts">
import BadmintonIcon from './BadmintonIcon.vue'
</script>

<template>
  <span class="shuttle-brand">
    <span class="shuttle-brand-mark"><BadmintonIcon :size="25" /></span>
    <span class="shuttle-brand-name">Shuttle<span>Up</span></span>
  </span>
</template>

<style scoped>
.shuttle-brand { display: inline-flex; align-items: center; gap: 10px; white-space: nowrap; }
.shuttle-brand-mark { display: grid; place-items: center; width: 36px; height: 36px; border-radius: 11px; background: var(--app-primary-color); color: #fff; box-shadow: 0 3px 8px color-mix(in srgb, var(--app-primary-color) 14%, transparent); }
.shuttle-brand-name { color: var(--app-heading-color); font-size: 21px; font-weight: 750; letter-spacing: -.7px; }
.shuttle-brand-name > span { color: var(--app-primary-color); }
</style>
