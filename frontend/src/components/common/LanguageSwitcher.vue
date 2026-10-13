<script setup lang="ts">
import { GlobalOutlined } from '@ant-design/icons-vue'
import { languages, locale, setLanguage, t } from '../../i18n'
import NativeSelect from './NativeSelect.vue'
</script>

<template>
  <div class="language-switcher">
    <GlobalOutlined aria-hidden="true" />
    <NativeSelect :model-value="locale" :label="t('言語を切り替える')" @update:model-value="setLanguage">
      <option v-for="language in languages" :key="language.value" :value="language.value">{{ language.label }}</option>
    </NativeSelect>
  </div>
</template>

<style scoped>
.language-switcher { display: inline-flex; align-items: center; gap: 6px; color: var(--app-primary-color); }
.language-switcher :deep(.native-select) { max-width: 124px; }
@media (max-width: 575px) { .language-switcher > .anticon { display: none; } }
</style>
