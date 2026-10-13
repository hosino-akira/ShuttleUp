<script setup lang="ts">
import { t } from '../../i18n';
import { computed } from 'vue';

interface GoalItem {
  completed: boolean;
  label: string;
}

const goals = computed<readonly GoalItem[]>(() => [
  { label: t('今週3回練習する'), completed: true },
  {
    label: t('スマッシュ成功率を向上させる'),
    completed: false,
  },
  { label: t('フットワークを強化する'), completed: false },
]);
</script>

<template>
  <a-card :title="t('今週の目標')" :bordered="false">
    <a-space direction="vertical" :size="8">
      <a-checkbox
        v-for="goal in goals"
        :key="goal.label"
        :checked="goal.completed"
      >
        {{ goal.label }}
      </a-checkbox>
    </a-space>
  </a-card>
</template>
