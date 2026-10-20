<script setup lang="ts">
import PageIntro from '../components/common/PageIntro.vue'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTrainingSessions } from '../api/trainingSessionApi'
import { requireUserId } from '../stores/auth'
import { t, formatLocalDate } from '../i18n'
import type { TrainingSession } from '../types/trainingSession'
import { forestSessions, getPlantType, localDateKey, plantNames, trainingStreak, type PlantType } from '../utils/forest'
import ForestPlant from '../components/forest/ForestPlant.vue'
import ForestScene from '../components/forest/ForestScene.vue'
import PlantPicker from '../components/forest/PlantPicker.vue'
import NativeSelect from '../components/common/NativeSelect.vue'

const router = useRouter()
const route = useRoute()
const userId = requireUserId()
const sessions = ref<TrainingSession[]>([])
const loading = ref(true)
const failed = ref(false)
const today = ref(localDateKey())
const period = ref('all')
const page = ref(1)
const chosenPlant = ref<PlantType>('cedar')
const selectedSession = ref<TrainingSession | null>(null)
const highlightedId = Number(route.query.planted) || undefined
const grown = computed(() => forestSessions(sessions.value, today.value))
const months = computed(() => [...new Set(grown.value.map(session => session.trainingDate.slice(0, 7)))].sort().reverse())
const filtered = computed(() => grown.value.filter(session => period.value === 'all' || session.trainingDate.startsWith(period.value)))
const plotCount = computed(() => Math.max(1, Math.ceil(filtered.value.length / 36)))
const plot = computed(() => filtered.value.slice((page.value - 1) * 36, page.value * 36))
const recent = computed(() => [...filtered.value].reverse().slice(0, 3))
const minutes = computed(() => filtered.value.reduce((sum, session) => sum + session.durationMinutes, 0))
const streak = computed(() => trainingStreak(grown.value, today.value))
const species = computed(() => new Set(filtered.value.map(getPlantType)).size)
const milestone = computed(() => (Math.floor(grown.value.length / 10) + 1) * 10)
const milestoneProgress = computed(() => grown.value.length % 10 * 10)
watch(period, () => { page.value = plotCount.value })

async function loadForest() {
  loading.value = true
  failed.value = false
  today.value = localDateKey()
  try {
    sessions.value = await getTrainingSessions(userId)
    const index = filtered.value.findIndex(session => session.id === highlightedId)
    page.value = index >= 0 ? Math.floor(index / 36) + 1 : plotCount.value
  } catch {
    failed.value = true
  } finally {
    loading.value = false
  }
}
function startTraining() {
  void router.push({ name: 'training-session-create', query: { plant: chosenPlant.value } })
}
function openTraining(session: TrainingSession) {
  void router.push({ name: 'training-session-detail', params: { sessionId: session.id } })
}
onMounted(loadForest)
</script>

<template>
  <section class="forest-page" aria-labelledby="forest-title">
    <PageIntro title-id="forest-title" :title="t('トレーニングの森')" :description="t('トレーニングをひとつ終えるたび、あなたの森に新しい命が育ちます。')">
      <template #actions>
        <RouterLink class="app-button" :to="{ name: 'training-history' }">{{ t('トレーニング履歴') }}</RouterLink>
      </template>
    </PageIntro>

    <div v-if="failed" class="forest-message" role="alert">
      <p>{{ t('森の読み込みに失敗しました。もう一度お試しください。') }}</p>
      <button class="app-button app-button-primary" type="button" @click="loadForest">{{ t('再読み込み') }}</button>
    </div>
    <div v-else-if="loading" class="forest-message" role="status"><a-spin /><p>{{ t('森を読み込んでいます…') }}</p></div>
    <template v-else>
      <div class="forest-stats">
        <div><span class="stat-icon" aria-hidden="true">♧</span><div><p>{{ t('育てた植物') }}</p><strong>{{ filtered.length }} <small>{{ t('株') }}</small></strong></div></div>
        <div><span class="stat-icon" aria-hidden="true">◷</span><div><p>{{ t('積み重ねた時間') }}</p><strong>{{ Math.floor(minutes / 60) }}<small>{{ t('時間') }}</small> {{ minutes % 60 }}<small>{{ t('分') }}</small></strong></div></div>
        <div><span class="stat-icon" aria-hidden="true">☀</span><div><p>{{ t('現在の連続トレーニング') }}</p><strong>{{ streak }} <small>{{ t('日') }}</small></strong></div></div>
        <div><span class="stat-icon" aria-hidden="true">❀</span><div><p>{{ t('森の植物の種類') }}</p><strong>{{ species }} <small>{{ t('種類') }}</small></strong></div></div>
      </div>

      <div class="forest-layout">
        <section class="garden-card" aria-labelledby="garden-title">
          <header class="garden-header">
            <div class="garden-heading"><h2 id="garden-title">{{ t('あなたの森') }}</h2><p>{{ t('一株ずつ、努力の足あと。') }}</p></div>
            <NativeSelect v-model="period" :label="t('表示する期間')" class="period-select">
              <option value="all">{{ t('すべての期間') }}</option>
              <option v-for="month in months" :key="month" :value="month">{{ formatLocalDate(month, { year: 'numeric', month: 'long' }) }}</option>
            </NativeSelect>
          </header>
          <div class="garden-scene">
            <span class="garden-badge"><span aria-hidden="true">●</span> {{ t('{count}株の思い出', { count: filtered.length }) }}</span>
            <ForestScene :sessions="plot" :highlighted-id="highlightedId" :selected-id="selectedSession?.id" @select="selectedSession = $event" />
            <div v-if="!filtered.length" class="empty-forest"><strong>{{ t('ここから、あなたの森がはじまります。') }}</strong><p>{{ t('好きな植物を選んで、最初のトレーニングを記録しましょう。') }}</p></div>
          </div>
          <footer class="garden-footer">
            <p>{{ filtered.length ? t('植物をクリックすると、トレーニングの思い出を見られます。') : t('1回のトレーニングが、1株の植物に。') }}</p>
            <div v-if="plotCount > 1" class="plot-pagination">
              <button class="app-button app-button-icon" type="button" :disabled="page <= 1" :aria-label="t('前の区画')" @click="page--">←</button>
              <span>{{ t('区画 {page} / {total}', { page, total: plotCount }) }}</span>
              <button class="app-button app-button-icon" type="button" :disabled="page >= plotCount" :aria-label="t('次の区画')" @click="page++">→</button>
            </div>
          </footer>
        </section>

        <aside class="seed-card" aria-labelledby="seed-title">
          <p class="eyebrow">{{ t('次の一株') }}</p>
          <h2 id="seed-title">{{ t('今日は、何を育てよう？') }}</h2>
          <div class="seed-preview"><div class="preview-ring"><ForestPlant :type="chosenPlant" :size="108" /></div><span class="preview-spark" aria-hidden="true">✧</span></div>
          <h3>{{ t(plantNames[chosenPlant]) }}</h3>
          <p class="seed-description">{{ t('あなたの頑張りが、この植物の根になります。') }}</p>
          <PlantPicker v-model="chosenPlant" />
          <button class="app-button app-button-primary start-button" type="button" @click="startTraining"><span aria-hidden="true">＋</span> {{ t('トレーニングを記録する') }}</button>
          <p class="seed-hint">{{ t('記録を保存すると、選んだ植物が森に加わります。') }}</p>
        </aside>
      </div>

      <div class="forest-bottom">
        <section class="recent-section" aria-labelledby="recent-title">
          <h2 id="recent-title">{{ t('最近育てた植物') }}</h2>
          <div v-if="recent.length" class="recent-plants">
            <button v-for="session in recent" :key="session.id" type="button" @click="selectedSession = session">
              <span class="recent-illustration"><ForestPlant :type="getPlantType(session)" :size="39" /></span>
              <span><strong>{{ t(plantNames[getPlantType(session)]) }}</strong><small>{{ formatLocalDate(session.trainingDate) }} · {{ t('{minutes}分', { minutes: session.durationMinutes }) }}</small></span><span class="recent-arrow" aria-hidden="true">↗</span>
            </button>
          </div>
          <p v-else class="no-recent">{{ t('最初の一株を楽しみにしています。') }}</p>
        </section>
        <aside class="milestone-card">
          <span class="milestone-symbol" aria-hidden="true">✿</span>
          <div><h3>{{ t('次の節目は、{count}株の森。', { count: milestone }) }}</h3><p>{{ t('あと{count}回。一歩ずつ、緑を増やしていきましょう。', { count: milestone - grown.length }) }}</p><div class="milestone-track" role="progressbar" :aria-label="t('次の節目までの進捗')" :aria-valuenow="grown.length % 10" :aria-valuemin="0" :aria-valuemax="10"><span :style="{ width: `${milestoneProgress}%` }" /></div></div>
        </aside>
      </div>
    </template>

    <a-modal :open="selectedSession !== null" :title="t('植物の思い出')" :footer="null" @cancel="selectedSession = null">
      <div v-if="selectedSession" class="plant-memory">
        <div class="memory-plant"><ForestPlant :type="getPlantType(selectedSession)" :size="110" /></div>
        <h2>{{ t(plantNames[getPlantType(selectedSession)]) }}</h2>
        <p>{{ formatLocalDate(selectedSession.trainingDate) }} · {{ t('{minutes}分', { minutes: selectedSession.durationMinutes }) }}</p>
        <p v-if="selectedSession.note" class="memory-note">{{ selectedSession.note }}</p>
        <p v-else class="memory-note">{{ t('この一株は、あなたが積み重ねた努力の証です。') }}</p>
        <button class="app-button app-button-primary" type="button" @click="openTraining(selectedSession)">{{ t('トレーニング詳細を見る') }} ↗</button>
      </div>
    </a-modal>
  </section>
</template>

<style scoped>
.forest-page { --forest-ink: var(--app-heading-color); --forest-muted: var(--app-muted-color); display: flex; flex-direction: column; gap: var(--app-section-gap); min-width: 0; width: 100%; color: var(--forest-ink); }
.eyebrow { margin: 0 0 8px; font-size: 11px; font-weight: 600; color: var(--app-muted-color); letter-spacing: .12em; }
.forest-stats { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: var(--app-section-gap); }
.forest-stats > div { display: flex; align-items: center; gap: 14px; background: var(--app-surface-color); padding: 12px; border-radius: var(--app-card-radius); }
.stat-icon { display: grid; place-items: center; width: 42px; height: 42px; flex-shrink: 0; border-radius: 13px; background: var(--app-soft-color); color: var(--app-primary-color); font-size: 25px; }
.forest-stats p { margin: 0 0 4px; font-size: 11px; color: var(--forest-muted); }
.forest-stats strong { font-size: 25px; font-weight: 600; line-height: 1.3; white-space: nowrap; }
.forest-stats small { font-size: 12px; color: var(--forest-muted); font-weight: 400; }
.forest-layout { display: grid; grid-template-columns: minmax(0, 1fr) 300px; gap: var(--app-section-gap); }
.garden-card { display: flex; flex-direction: column; min-width: 0; background: var(--app-surface-color); border-radius: var(--app-card-radius); overflow: hidden; }
.garden-header { display: flex; justify-content: space-between; align-items: center; padding: 12px; border-bottom: 1px solid var(--app-border-color); gap: 12px; }
h2 { margin: 0; font-size: 14px; font-weight: 600; }
.garden-heading { display: flex; flex-wrap: wrap; align-items: baseline; gap: 4px 16px; min-width: 0; }
.garden-header p { margin: 0; color: var(--app-muted-color); font-size: 12px; }
.period-select { max-width: 50%; }
.garden-scene { position: relative; display: flex; align-items: center; flex: 1; min-height: 350px; background: radial-gradient(ellipse at 50% 65%, var(--app-soft-color), transparent 75%); }
.garden-badge { position: absolute; top: 18px; left: 24px; color: var(--app-muted-color); font-size: 11px; }
.garden-badge span { color: var(--app-accent-color); font-size: 8px; margin-right: 5px; }
.empty-forest { position: absolute; bottom: 12px; inset-inline: 20px; text-align: center; background: var(--app-soft-color); padding: 10px; border-radius: 12px; }
.empty-forest strong { font-size: 13px; font-weight: 500; }
.empty-forest p { margin: 5px 0 0; color: var(--app-muted-color); font-size: 11px; }
.garden-footer { padding: 12px; display: flex; justify-content: space-between; align-items: center; gap: 12px; border-top: 1px solid var(--app-border-color); background: var(--app-surface-color); }
.garden-footer p { margin: 0; font-size: 11px; line-height: 1.7; color: var(--app-muted-color); }
.plot-pagination { display: flex; align-items: center; gap: 9px; font-size: 11px; flex-shrink: 0; }
.plot-pagination button:disabled { opacity: .4; cursor: default; }
.seed-card { text-align: center; padding: 12px; background: var(--app-surface-color); border-radius: var(--app-card-radius); }
.seed-card h2 { font-size: 14px; }
.seed-preview { position: relative; margin: 16px auto 9px; width: 164px; }
.preview-ring { display: grid; place-items: center; width: 164px; height: 164px; border: 1px solid var(--app-border-color); outline: 7px solid var(--app-page-color); border-radius: 50%; background: radial-gradient(circle, var(--app-soft-color) 40%, var(--app-surface-color) 70%); }
.preview-spark { position: absolute; right: 3px; top: 20px; color: var(--app-accent-color); font-size: 30px; }
.seed-card h3 { margin: 0; font-size: 16px; font-weight: 600; }
.seed-description { margin: 5px 0 16px; color: var(--app-muted-color); font-size: 11px; line-height: 1.6; }
button:focus-visible, a:focus-visible { outline: 2px solid var(--app-primary-color); outline-offset: 3px; }
.start-button { width: 100%; margin-top: 18px; }
.seed-hint { margin: 9px 0 0; color: var(--app-muted-color); font-size: 10px; line-height: 1.8; }
.forest-bottom { display: grid; grid-template-columns: minmax(0, 1fr) 300px; gap: var(--app-section-gap); }
.recent-section { min-width: 0; padding: 12px; background: var(--app-surface-color); border-radius: var(--app-card-radius); }
.recent-section h2 { margin: 0 0 12px; font-size: 14px; }
.recent-plants { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px; }
.recent-plants button { display: flex; align-items: center; gap: 9px; min-width: 0; text-align: start; background: var(--app-surface-color); border: 1px solid var(--app-border-color); border-radius: 13px; padding: 10px; color: var(--forest-ink); font: inherit; cursor: pointer; }
.recent-illustration { flex-shrink: 0; background: var(--app-soft-color); border-radius: 10px; padding: 4px; }
.recent-plants strong { display: block; font-size: 12px; font-weight: 500; }
.recent-plants small { display: block; margin-top: 4px; color: var(--app-muted-color); font-size: 10px; line-height: 1.5; }
.recent-arrow { margin-left: auto; color: var(--app-neutral-color); }
.no-recent { color: var(--forest-muted); font-size: 12px; }
.milestone-card { display: flex; align-items: center; gap: 13px; padding: 12px; border-radius: var(--app-card-radius); background: var(--app-surface-color); }
.milestone-symbol { font-size: 32px; color: var(--app-accent-color); }
.milestone-card > div { flex: 1; }
.milestone-card h3 { margin: 0; font-size: 12px; font-weight: 600; }
.milestone-card p { margin: 6px 0 12px; font-size: 10px; color: var(--app-muted-color); line-height: 1.8; }
.milestone-track { height: 5px; border-radius: 5px; background: var(--app-selected-color); overflow: hidden; }
.milestone-track span { display: block; height: 100%; border-radius: 5px; background: var(--app-primary-color); }
.forest-message { text-align: center; padding: 100px 20px; color: var(--app-muted-color); }
.plant-memory { text-align: center; color: var(--app-heading-color); padding: 12px 0; }
.memory-plant { display: flex; justify-content: center; padding: 20px; background: var(--app-soft-color); width: 180px; margin: 0 auto 15px; border-radius: 50%; }
.memory-note { white-space: pre-wrap; overflow-wrap: anywhere; color: var(--app-muted-color); background: var(--app-surface-color); padding: 14px; border-radius: 12px; margin: 18px 0; }
@media (min-width: 1500px) { .forest-layout, .forest-bottom { grid-template-columns: minmax(0, 1fr) 340px; } }
@media (max-width: 1150px) { .forest-stats > div { gap: 9px; } .stat-icon { width: 34px; height: 34px; font-size: 22px; } .forest-stats strong { font-size: 22px; } .forest-layout, .forest-bottom { grid-template-columns: minmax(0, 1fr) 280px; gap: var(--app-section-gap); } .recent-plants { grid-template-columns: 1fr; } .recent-plants button { padding: 8px 12px; } }
@media (max-width: 760px) { .forest-stats { grid-template-columns: repeat(2, minmax(0, 1fr)); } .forest-stats p { font-size: 10px; } .forest-stats strong { font-size: 21px; } .forest-layout, .forest-bottom { grid-template-columns: 1fr; } .garden-scene { min-height: 290px; } .garden-badge { top: 12px; left: 16px; } .garden-footer { flex-wrap: wrap; padding: 12px; } .seed-card :deep(.plant-picker) { grid-template-columns: repeat(6, minmax(0, 1fr)); } .seed-preview { margin-top: 22px; } .seed-card :deep(.plant-option) { font-size: 10px; } }
@media (max-width: 440px) { .seed-card :deep(.plant-picker) { grid-template-columns: repeat(3, minmax(0, 1fr)); } .garden-scene { min-height: 260px; } .empty-forest { bottom: 0; inset-inline: 8px; } }
</style>
