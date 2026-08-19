<script setup lang="ts">
import type { TableColumnsType } from "ant-design-vue";

interface RecentTraining {
  date: string;
  duration: string;
  key: string;
  training: string;
}

const columns: TableColumnsType<RecentTraining> = [
  { title: "日付", dataIndex: "date", key: "date" },
  {
    title: "トレーニング",
    dataIndex: "training",
    key: "training",
  },
  { title: "時間", dataIndex: "duration", key: "duration" },
];

const trainingRecords: readonly RecentTraining[] = [
  {
    key: "1",
    date: "2026/07/27",
    training: "フットワーク基礎",
    duration: "45分",
  },
  {
    key: "2",
    date: "2026/07/25",
    training: "スマッシュ練習",
    duration: "60分",
  },
  {
    key: "3",
    date: "2026/07/23",
    training: "クリア・ドロップ",
    duration: "40分",
  },
];
</script>

<template>
  <a-card title="最近のトレーニング" :bordered="false">
    <a-table
      :columns="columns"
      :data-source="trainingRecords"
      :pagination="false"
      :scroll="{ x: 560 }"
    />
  </a-card>
</template>
