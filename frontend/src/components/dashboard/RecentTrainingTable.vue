<script setup lang="ts">
import { t } from '../../i18n';
import { computed } from 'vue';

import type { TableColumnsType } from "ant-design-vue";

interface RecentTraining {
  date: string;
  duration: string;
  key: string;
  training: string;
}

const columns = computed<TableColumnsType<RecentTraining>>(() => [
  { title: t('日付'), dataIndex: "date", key: "date" },
  {
    title: t('トレーニング'),
    dataIndex: "training",
    key: "training",
  },
  { title: t('トレーニング時間'), dataIndex: "duration", key: "duration" },
]);

const trainingRecords = computed<readonly RecentTraining[]>(() => [
  {
    key: "1",
    date: "2026/07/27",
    training: t('フットワーク基礎'),
    duration: t('45分'),
  },
  {
    key: "2",
    date: "2026/07/25",
    training: t('スマッシュ練習'),
    duration: t('60分'),
  },
  {
    key: "3",
    date: "2026/07/23",
    training: t('クリア・ドロップ'),
    duration: t('40分'),
  },
]);
</script>

<template>
  <a-card :title="t('最近のトレーニング')" :bordered="false">
    <a-table
      :columns="columns"
      :data-source="trainingRecords"
      :pagination="false"
      size="small"
      :scroll="{ x: 560 }"
    />
  </a-card>
</template>
