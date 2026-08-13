<script setup lang="ts">
import { onMounted, ref } from 'vue'
import StatisticsCards from '../components/dashboard/StatisticsCards.vue'
import { getTrainingSessions } from '../api/trainingSessionApi'
import type { TrainingSession } from '../types/trainingSession'

const userId = 1
const sessions = ref<TrainingSession[]>([])
const loading = ref(false)
const loadError = ref(false)

async function loadTrainingSessions(): Promise<void> {
  loading.value = true
  loadError.value = false

  try {
    sessions.value = await getTrainingSessions(userId)
  } catch (error: unknown) {
    console.error(error)
    loadError.value = true
  } finally {
    loading.value = false
  }
}

onMounted(loadTrainingSessions)
</script>

<template>
  <section class="dashboard" aria-labelledby="dashboard-title">
    <a-card :bordered="false">
      <a-typography-title id="dashboard-title" :level="2">ダッシュボード</a-typography-title>
      <a-typography-paragraph class="dashboard-description" type="secondary">
        トレーニングの概要を確認できます。
      </a-typography-paragraph>
    </a-card>

    <a-alert
      v-if="loadError"
      message="トレーニングデータを取得できませんでした。"
      type="error"
      show-icon
    >
      <template #action>
        <a-button size="small" danger :loading="loading" @click="loadTrainingSessions">
          再読み込み
        </a-button>
      </template>
    </a-alert>

    <StatisticsCards :sessions="sessions" :loading="loading" />
  </section>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dashboard-description {
  margin-bottom: 0;
}
</style>
