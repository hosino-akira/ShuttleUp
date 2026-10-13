<script setup lang="ts">
import { DownOutlined } from '@ant-design/icons-vue'
defineProps<{ modelValue: string; label: string; disabled?: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
</script>

<template>
  <span class="native-select" :class="{ 'is-disabled': disabled }">
    <select :value="modelValue" :aria-label="label" :disabled="disabled"
      @change="emit('update:modelValue', ($event.target as HTMLSelectElement).value)"><slot /></select>
    <DownOutlined class="native-select-arrow" aria-hidden="true" />
  </span>
</template>

<style scoped>
.native-select { position: relative; display: inline-flex; min-width: 0; color: var(--app-text-color); }
select { appearance: none; -webkit-appearance: none; width: 100%; min-width: 0; height: 32px; padding: 0 36px 0 12px; border: 1px solid var(--app-border-color); border-radius: 8px; background: var(--app-elevated-color); color: inherit; font: inherit; font-size: 13px; cursor: pointer; text-overflow: ellipsis; transition: border-color .15s; }
select:hover { border-color: var(--app-primary-color); }
select:focus-visible { outline: 2px solid var(--app-primary-color); outline-offset: 2px; }
.native-select-arrow { position: absolute; inset-inline-end: 14px; top: 50%; transform: translateY(-50%); font-size: 10px; pointer-events: none; color: var(--app-muted-color); }
.is-disabled { opacity: .6; }
select:disabled { cursor: not-allowed; }
</style>
