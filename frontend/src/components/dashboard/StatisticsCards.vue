<script setup lang="ts">
import { computed } from "vue";
import type { TrainingSession } from "../../types/trainingSession";

const props = withDefaults(
  defineProps<{
    sessions: readonly TrainingSession[];
    loading?: boolean;
  }>(),
  {
    loading: false,
  },
);

interface StatisticItem {
  precision: number;
  suffix: string;
  title: string;
  value: number;
}

const statistics = computed<readonly StatisticItem[]>(
  () => {
    const totalMinutes = props.sessions.reduce(
      (total, session) => total + session.durationMinutes,
      0,
    );
    const feelings = props.sessions.flatMap((session) =>
      session.feeling === null ? [] : [session.feeling],
    );
    const averageFeeling =
      feelings.length === 0
        ? 0
        : feelings.reduce(
            (total, feeling) => total + feeling,
            0,
          ) / feelings.length;

    return [
      {
        title: "トレーニング回数",
        value: props.sessions.length,
        suffix: "回",
        precision: 0,
      },
      {
        title: "累計時間",
        value: totalMinutes / 60,
        suffix: "時間",
        precision: 1,
      },
      {
        title: "平均コンディション",
        value: averageFeeling,
        suffix: "/ 5",
        precision: 1,
      },
    ];
  },
);
</script>

<template>
  <a-row :gutter="[16, 16]">
    <a-col
      v-for="statistic in statistics"
      :key="statistic.title"
      :xs="24"
      :sm="12"
      :xl="8"
    >
      <a-card
        class="statistic-card"
        :bordered="false"
        :loading="loading"
      >
        <a-statistic
          :title="statistic.title"
          :value="statistic.value"
          :precision="statistic.precision"
          :suffix="statistic.suffix"
        />
      </a-card>
    </a-col>
  </a-row>
</template>

<style scoped>
.statistic-card {
  height: 100%;
}
</style>
