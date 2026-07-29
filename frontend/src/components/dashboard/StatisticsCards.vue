<script setup lang="ts">
interface StatisticItem {
  suffix: string
  title: string
  value: number
}

const statistics: readonly StatisticItem[] = [
  { title: '連続トレーニング', value: 12, suffix: '日' },
  { title: '今週', value: 3, suffix: 'セッション' },
  { title: '今月', value: 14, suffix: 'セッション' },
  { title: '合計時間', value: 26.5, suffix: '時間' },
]
</script>

<template>
  <a-row :gutter="[16, 16]">
    <a-col v-for="statistic in statistics" :key="statistic.title" :xs="24" :sm="12" :xl="6">
      <a-card :bordered="false">
        <a-statistic :title="statistic.title" :value="statistic.value" :suffix="statistic.suffix" />
      </a-card>
    </a-col>
  </a-row>
</template>
