<script setup lang="ts">
import { t } from '../../i18n';
import { selectPopupContainer } from '../../utils/selectPopupContainer';
export interface SelectOption {
  value: number;
  label: string;
  disabled?: boolean;
}

withDefaults(
  defineProps<{
    value?: number;
    options?: SelectOption[];
    placeholder?: string;
    loading?: boolean;
    disabled?: boolean;
    allowClear?: boolean;
  }>(),
  {
    value: undefined,
    options: () => [],
    placeholder: "選択してください",
    loading: false,
    disabled: false,
    allowClear: true,
  },
);

const emit = defineEmits<{
  "update:value": [value: number | undefined];
  change: [value: number | undefined];
}>();

function handleChange(value: number | undefined): void {
  emit("update:value", value);
  emit("change", value);
}

/** 入力文字列に応じて選択肢を部分一致で絞り込む。 */
function filterOption(
  input: string,
  option?: SelectOption,
): boolean {
  if (!option) return false;

  return option.label
    .toLocaleLowerCase()
    .includes(input.toLocaleLowerCase());
}
</script>

<template>
  <a-select
    class="base-select"
    :value="value"
    :options="options"
    :placeholder="placeholder === '選択してください' ? t('選択してください') : placeholder"
    :loading="loading"
    :disabled="disabled"
    :allow-clear="allowClear"
    :get-popup-container="selectPopupContainer"
    :dropdown-style="{ pointerEvents: 'auto' }"
    show-search
    option-filter-prop="label"
    :filter-option="filterOption"
    @change="handleChange"
  />
</template>

<style scoped>
.base-select {
  width: 100%;
}
</style>
