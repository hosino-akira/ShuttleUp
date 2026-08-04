<script setup lang="ts">
import { useGlobalLoading } from '../../composables/useGlobalLoading'

const { isLoading } = useGlobalLoading()
</script>

<template>
  <div
    v-if="isLoading"
    class="global-loading"
    role="status"
    aria-live="polite"
    aria-label="読み込み中"
  >
    <a-spin size="large" tip="読み込み中..." />
  </div>
</template>

<style scoped>
.global-loading {
  position: fixed;
  inset: 0;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgb(255 255 255 / 65%);
  cursor: wait;
  pointer-events: auto;
}
</style>
