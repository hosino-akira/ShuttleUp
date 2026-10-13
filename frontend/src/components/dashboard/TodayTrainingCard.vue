<script setup lang="ts">
import { t } from '../../i18n';
import { computed } from 'vue';

interface TrainingPlanItem {
  duration: string;
  title: string;
}

const trainingPlan = computed<readonly TrainingPlanItem[]>(() => [
  { duration: t('10分'), title: t('ウォームアップ・ストレッチ') },
  { duration: t('25分'), title: t('フットワークドリル') },
  { duration: t('20分'), title: t('スマッシュ練習') },
]);
</script>

<template>
  <a-card :title="t('今日のトレーニング')" :bordered="false">
    <a-list
      :data-source="trainingPlan"
      item-layout="horizontal"
    >
      <template
        #renderItem="{ item }: { item: TrainingPlanItem }"
      >
        <a-list-item>
          <a-list-item-meta :title="item.title">
            <template #description>{{
              item.duration
            }}</template>
          </a-list-item-meta>
        </a-list-item>
      </template>
    </a-list>
    <a-button type="primary" block
      >{{ t('トレーニング開始') }}</a-button
    >
  </a-card>
</template>
