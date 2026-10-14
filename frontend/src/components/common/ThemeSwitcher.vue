<script setup lang="ts">
import {
  currentTheme,
  setTheme,
  themeOptions,
} from "../../theme";
import { t } from "../../i18n";
import NativeSelect from "./NativeSelect.vue";
</script>

<template>
  <div class="theme-switcher">
    <!-- <span class="theme-swatch" aria-hidden="true" /> -->
    <NativeSelect
      :model-value="currentTheme"
      :label="t('テーマを切り替える')"
      @update:model-value="setTheme"
    >
      <option
        v-for="option in themeOptions"
        :key="option.value"
        :value="option.value"
      >
        {{ t(option.label) }}
      </option>
    </NativeSelect>
  </div>
</template>

<style scoped>
.theme-switcher {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}
.theme-swatch {
  width: 12px;
  height: 12px;
  flex-shrink: 0;
  border-radius: 50%;
  background: var(--app-primary-color);
  box-shadow: 0 0 0 3px var(--app-soft-color);
}
.theme-switcher :deep(.native-select) {
  max-width: 162px;
}
@media (max-width: 575px) {
  .theme-swatch {
    display: none;
  }
  .theme-switcher :deep(.native-select) {
    max-width: 128px;
  }
}
</style>
