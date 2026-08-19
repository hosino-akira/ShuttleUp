<script setup lang="ts">
interface TrainingPlanItem {
  duration: string;
  title: string;
}

const trainingPlan: readonly TrainingPlanItem[] = [
  { duration: "10分", title: "ウォームアップ・ストレッチ" },
  { duration: "25分", title: "フットワークドリル" },
  { duration: "20分", title: "スマッシュ練習" },
];
</script>

<template>
  <a-card title="今日のトレーニング" :bordered="false">
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
      >トレーニング開始</a-button
    >
  </a-card>
</template>
