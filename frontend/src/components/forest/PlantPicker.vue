<script setup lang="ts">
import { t } from '../../i18n'
import { plantNames, plantTypes, type PlantType } from '../../utils/forest'
import ForestPlant from './ForestPlant.vue'
defineProps<{ modelValue: PlantType; disabled?: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [value: PlantType] }>()
</script>

<template>
  <div class="plant-picker" role="group" :aria-label="t('植える植物を選ぶ')">
    <button v-for="plant in plantTypes" :key="plant" type="button" class="plant-option"
      :class="{ selected: modelValue === plant }" :aria-pressed="modelValue === plant" :disabled="disabled"
      @click="emit('update:modelValue', plant)">
      <ForestPlant :type="plant" :size="44" />
      <span>{{ t(plantNames[plant]) }}</span>
      <span v-if="modelValue === plant" class="check" aria-hidden="true">✓</span>
    </button>
  </div>
</template>

<style scoped>
.plant-picker { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px; }
.plant-option { position: relative; display: flex; align-items: center; flex-direction: column; gap: 7px; padding: 12px 4px 10px; background: var(--app-elevated-color); border: 1px solid var(--app-border-color); border-radius: 14px; color: var(--app-text-color); cursor: pointer; font: inherit; font-size: 12px; transition: background .2s, border-color .2s; }
.plant-option:hover { background: var(--app-soft-color); }
.plant-option.selected { border-color: var(--app-primary-color); background: var(--app-selected-color); box-shadow: inset 0 0 0 1px var(--app-primary-color); }
.plant-option:focus-visible { outline: 3px solid var(--app-primary-color); outline-offset: 3px; }
.plant-option:disabled { cursor: default; opacity: .65; }
.check { position: absolute; top: 6px; right: 8px; color: var(--app-primary-color); font-size: 12px; }
</style>
