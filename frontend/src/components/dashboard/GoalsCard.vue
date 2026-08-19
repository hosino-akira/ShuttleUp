<script setup lang="ts">
interface GoalItem {
  completed: boolean;
  label: string;
}

const goals: readonly GoalItem[] = [
  { label: "今週3回練習する", completed: true },
  {
    label: "スマッシュ成功率を向上させる",
    completed: false,
  },
  { label: "フットワークを強化する", completed: false },
];
</script>

<template>
  <a-card title="今週の目標" :bordered="false">
    <a-space direction="vertical" :size="16">
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
