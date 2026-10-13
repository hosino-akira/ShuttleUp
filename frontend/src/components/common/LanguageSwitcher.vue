<script setup lang="ts">
import { GlobalOutlined } from '@ant-design/icons-vue'
import { languages, locale, setLanguage, t } from '../../i18n'
</script>

<template>
  <div class="language-switcher">
    <GlobalOutlined aria-hidden="true" />
    <select :value="locale" :aria-label="t('言語を切り替える')" @change="setLanguage(($event.target as HTMLSelectElement).value)">
      <option v-for="language in languages" :key="language.value" :value="language.value">{{ language.label }}</option>
    </select>
  </div>
</template>

<style scoped>
.language-switcher { display: inline-flex; align-items: center; gap: 6px; color: var(--app-primary-color); }
select { max-width: 120px; padding: 4px 6px; border: 1px solid var(--app-border-color); border-radius: 6px; color: var(--app-heading-color); background: var(--app-surface-color); font: inherit; font-size: 13px; cursor: pointer; }
select:focus-visible { outline: 2px solid var(--app-primary-color); outline-offset: 2px; }
</style>
