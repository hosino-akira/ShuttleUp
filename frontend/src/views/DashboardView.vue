<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import type { EChartsOption } from "echarts";
import BaseEChart from "../components/common/BaseEChart.vue";
import BaseSelect from "../components/common/BaseSelect.vue";
import { getDashboard } from "../api/dashboardApi";
import { getExercises } from "../api/exerciseApi";
import { getOpponents } from "../api/opponentApi";
import { CURRENT_USER_ID } from "../constants/user";
import type {
  DashboardQuery,
  DashboardResponse,
} from "../types/dashboard";
import type { ExerciseResponse } from "../types/exercise";
import type { OpponentResponse } from "../types/opponent";
import {
  createPeriodQuery,
  formatDuration,
  formatJapaneseDate,
  formatMonth,
  type DashboardPeriod,
} from "../utils/dashboard";

const dashboard = ref<DashboardResponse>();
const dashboardLoading = ref(false);
const dashboardError = ref(false);
const choicesLoading = ref(false);
const choicesError = ref(false);
const period = ref<DashboardPeriod>("all");
const customRange = ref<[string, string]>();
const selectedExerciseId = ref<number>();
const selectedOpponentId = ref<number>();
const exercises = ref<ExerciseResponse[]>([]);
const opponents = ref<OpponentResponse[]>([]);
let dashboardRequestId = 0;

const periodOptions = [
  { value: "all", label: "全期間" },
  { value: "month", label: "今月" },
  { value: "threeMonths", label: "過去3か月" },
  { value: "sixMonths", label: "過去6か月" },
  { value: "year", label: "過去1年" },
  { value: "custom", label: "カスタム期間" },
];
const exerciseOptions = computed(() =>
  [...exercises.value]
    .sort(
      (a, b) =>
        Number(isWeightExercise(b)) -
          Number(isWeightExercise(a)) ||
        a.name.localeCompare(b.name, "ja"),
    )
    .map((exercise) => ({
      value: exercise.id,
      label: exercise.name,
    })),
);
const opponentOptions = computed(() =>
  opponents.value.map((opponent) => ({
    value: opponent.id,
    label: opponent.name,
  })),
);
const selectedExerciseName = computed(
  () =>
    exercises.value.find(
      (item) => item.id === selectedExerciseId.value,
    )?.name ?? "",
);
const summary = computed(
  () =>
    dashboard.value?.summary ?? {
      trainingSessionCount: 0,
      totalDurationMinutes: 0,
      matchCount: 0,
      winCount: 0,
      lossCount: 0,
      winRate: 0,
    },
);

const monthlyOption = computed<EChartsOption>(() => {
  const rows = dashboard.value?.monthlyTraining ?? [];
  const manyMonths = rows.length > 18;
  const zoomStart = Math.max(
    0,
    100 - 1800 / Math.max(rows.length, 1),
  );
  return {
    animationDuration: 250,
    color: ["#1677ff", "#52c41a"],
    tooltip: { trigger: "axis" },
    legend: {
      data: ["トレーニング回数", "トレーニング時間"],
      top: 0,
    },
    grid: {
      left: 52,
      right: 62,
      top: 50,
      bottom: manyMonths ? 70 : 42,
    },
    xAxis: {
      type: "category",
      data: rows.map((row) => formatMonth(row.month)),
      axisLabel: {
        hideOverlap: true,
        rotate: manyMonths ? 35 : 0,
      },
    },
    yAxis: [
      {
        type: "value",
        name: "回数",
        splitNumber: 5,
        minInterval: 1,
      },
      {
        type: "value",
        name: "時間（分）",
        splitNumber: 5,
        splitLine: {
          show: false,
        },
      },
    ],
    dataZoom: manyMonths
      ? [
          { type: "inside", start: zoomStart, end: 100 },
          {
            type: "slider",
            height: 18,
            bottom: 4,
            start: zoomStart,
            end: 100,
          },
        ]
      : [],
    series: [
      {
        name: "トレーニング回数",
        type: "bar",
        data: rows.map((row) => row.sessionCount),
        tooltip: {
          valueFormatter: (value) => `${value}回`,
        },
      },
      {
        name: "トレーニング時間",
        type: "line",
        yAxisIndex: 1,
        smooth: true,
        symbolSize: 7,
        data: rows.map((row) => row.durationMinutes),
        tooltip: {
          valueFormatter: (value) =>
            formatDuration(Number(value)),
        },
      },
    ],
  };
});
const matchOption = computed<EChartsOption>(() => ({
  animationDuration: 250,
  color: ["#1677ff", "#d46b6b"],
  tooltip: {
    trigger: "item",
    formatter: "{b}：{c}回（{d}%）",
  },
  legend: { bottom: 0 },
  series: [
    {
      name: "試合結果",
      type: "pie",
      radius: ["48%", "70%"],
      center: ["50%", "45%"],
      label: { formatter: "{b}\n{c}回" },
      data: [
        {
          value: dashboard.value?.matchResults.wins ?? 0,
          name: "勝利",
        },
        {
          value: dashboard.value?.matchResults.losses ?? 0,
          name: "敗北",
        },
      ],
    },
  ],
}));
const exerciseOption = computed<EChartsOption>(() => {
  const rows = dashboard.value?.exerciseProgress ?? [];
  return {
    animationDuration: 250,
    color: ["#722ed1"],
    tooltip: {
      trigger: "axis",
      valueFormatter: (value) => `${value} kg`,
    },
    grid: { left: 58, right: 24, top: 30, bottom: 48 },
    xAxis: {
      type: "category",
      boundaryGap: rows.length === 1,
      data: rows.map((row) => formatJapaneseDate(row.date)),
      axisLabel: { hideOverlap: true },
    },
    yAxis: {
      type: "value",
      name: "重量（kg）",
      scale: true,
    },
    series: [
      {
        name: selectedExerciseName.value,
        type: "line",
        symbol: "circle",
        symbolSize: 8,
        data: rows.map((row) => row.maxWeightKg),
      },
    ],
  };
});

function isWeightExercise(
  exercise: ExerciseResponse,
): boolean {
  return /筋力|ウェイト|重量|フィジカル/.test(
    `${exercise.categoryName} ${exercise.exerciseTypeName}`,
  );
}
function createQuery(): DashboardQuery {
  return {
    ...createPeriodQuery(period.value, customRange.value),
    exerciseId: selectedExerciseId.value,
    opponentId: selectedOpponentId.value,
  };
}
async function loadDashboard(): Promise<void> {
  if (period.value === "custom" && !customRange.value) {
    dashboardRequestId++;
    dashboard.value = undefined;
    dashboardError.value = false;
    dashboardLoading.value = false;
    return;
  }
  const requestId = ++dashboardRequestId;
  dashboardLoading.value = true;
  dashboardError.value = false;
  dashboard.value = undefined;
  try {
    const loaded = await getDashboard(
      CURRENT_USER_ID,
      createQuery(),
    );
    if (requestId === dashboardRequestId)
      dashboard.value = loaded;
  } catch (error: unknown) {
    if (requestId === dashboardRequestId) {
      console.error(error);
      dashboardError.value = true;
    }
  } finally {
    if (requestId === dashboardRequestId)
      dashboardLoading.value = false;
  }
}
async function loadChoices(): Promise<void> {
  choicesLoading.value = true;
  choicesError.value = false;
  try {
    const [loadedExercises, loadedOpponents] =
      await Promise.all([
        getExercises(undefined, CURRENT_USER_ID),
        getOpponents(CURRENT_USER_ID),
      ]);
    exercises.value = loadedExercises;
    opponents.value = loadedOpponents;
  } catch (error: unknown) {
    console.error(error);
    choicesError.value = true;
  } finally {
    choicesLoading.value = false;
  }
}
function handlePeriodChange(value: string): void {
  period.value = value as DashboardPeriod;
  if (period.value !== "custom")
    customRange.value = undefined;
}

watch(
  [
    period,
    customRange,
    selectedExerciseId,
    selectedOpponentId,
  ],
  loadDashboard,
  { flush: "post" },
);
onMounted(() => {
  void loadChoices();
  void loadDashboard();
});
</script>

<template>
  <section
    class="dashboard"
    aria-labelledby="dashboard-title"
  >
    <a-card :bordered="false">
      <a-typography-title id="dashboard-title" :level="2"
        >ダッシュボード</a-typography-title
      >
      <a-typography-paragraph
        class="dashboard-description"
        type="secondary"
        >トレーニングと試合の実績を確認できます。</a-typography-paragraph
      >
    </a-card>

    <a-card title="検索条件" :bordered="false">
      <a-alert
        v-if="choicesError"
        class="choice-alert"
        type="warning"
        show-icon
        message="種目または対戦相手の選択肢を取得できませんでした。"
      >
        <template #action
          ><a-button
            size="small"
            :loading="choicesLoading"
            @click="loadChoices"
            >再読み込み</a-button
          ></template
        >
      </a-alert>
      <div class="filter-grid">
        <a-form-item label="集計期間" class="filter-item"
          ><a-select
            :value="period"
            :options="periodOptions"
            @change="handlePeriodChange"
        /></a-form-item>
        <a-form-item
          v-if="period === 'custom'"
          label="カスタム期間"
          class="filter-item"
        >
          <a-range-picker
            v-model:value="customRange"
            value-format="YYYY-MM-DD"
            format="YYYY年MM月DD日"
          />
        </a-form-item>
        <a-form-item
          label="重量推移を表示する種目"
          class="filter-item"
        >
          <BaseSelect
            v-model:value="selectedExerciseId"
            :options="exerciseOptions"
            :loading="choicesLoading"
            placeholder="種目を選択"
          />
        </a-form-item>
        <a-form-item
          label="対戦成績を表示する相手"
          class="filter-item"
        >
          <BaseSelect
            v-model:value="selectedOpponentId"
            :options="opponentOptions"
            :loading="choicesLoading"
            placeholder="対戦相手を選択"
          />
        </a-form-item>
      </div>
    </a-card>

    <a-alert
      v-if="dashboardError"
      message="ダッシュボードデータの取得に失敗しました。"
      type="error"
      show-icon
    >
      <template #action
        ><a-button
          type="primary"
          danger
          size="small"
          :loading="dashboardLoading"
          @click="loadDashboard"
          >再読み込み</a-button
        ></template
      >
    </a-alert>

    <a-spin
      :spinning="dashboardLoading"
      tip="ダッシュボードを読み込み中..."
    >
      <div
        class="dashboard-content"
        :class="{ 'is-loading': dashboardLoading }"
      >
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :xl="6"
            ><a-card :bordered="false" class="summary-card"
              ><a-statistic
                title="トレーニング回数"
                :value="summary.trainingSessionCount"
                suffix="回" /></a-card
          ></a-col>
          <a-col :xs="24" :sm="12" :xl="6"
            ><a-card :bordered="false" class="summary-card"
              ><a-statistic
                title="トレーニング時間"
                :value="
                  formatDuration(
                    summary.totalDurationMinutes,
                  )
                " /></a-card
          ></a-col>
          <a-col :xs="24" :sm="12" :xl="6"
            ><a-card :bordered="false" class="summary-card"
              ><a-statistic
                title="試合数"
                :value="summary.matchCount"
                suffix="試合" /></a-card
          ></a-col>
          <a-col :xs="24" :sm="12" :xl="6"
            ><a-card :bordered="false" class="summary-card"
              ><a-statistic
                title="勝率"
                :value="summary.winRate"
                :precision="1"
                suffix="%" /></a-card
          ></a-col>
        </a-row>

        <a-card
          title="月別トレーニング推移"
          :bordered="false"
        >
          <BaseEChart
            :option="monthlyOption"
            :loading="dashboardLoading"
            :empty="!dashboard?.monthlyTraining.length"
            empty-description="トレーニングデータがありません。"
            height="360px"
          />
        </a-card>

        <div class="chart-grid">
          <a-card title="試合結果" :bordered="false">
            <BaseEChart
              :option="matchOption"
              :loading="dashboardLoading"
              :empty="summary.matchCount === 0"
              empty-description="試合データがありません。"
              height="300px"
            />
            <div
              v-if="summary.matchCount > 0"
              class="match-summary"
            >
              <span
                >総試合数：{{
                  summary.matchCount
                }}試合</span
              ><span>勝利：{{ summary.winCount }}回</span>
              <span>敗北：{{ summary.lossCount }}回</span
              ><span
                >勝率：{{
                  summary.winRate.toFixed(1)
                }}%</span
              >
            </div>
          </a-card>
          <a-card title="種目別重量推移" :bordered="false">
            <template #extra
              ><span v-if="selectedExerciseName">{{
                selectedExerciseName
              }}</span></template
            >
            <BaseEChart
              :option="exerciseOption"
              :loading="dashboardLoading"
              :empty="
                !selectedExerciseId ||
                !dashboard?.exerciseProgress.length
              "
              :empty-description="
                selectedExerciseId
                  ? '選択した種目の重量データがありません。'
                  : '種目を選択すると重量推移を確認できます。'
              "
              height="340px"
            />
          </a-card>
        </div>

        <a-card
          v-if="
            selectedOpponentId &&
            dashboard?.opponentMatchSummary
          "
          :bordered="false"
          :title="`${dashboard.opponentMatchSummary.opponentName}さんとの対戦成績`"
        >
          <a-empty
            v-if="
              dashboard.opponentMatchSummary.matchCount ===
              0
            "
            description="選択した対戦相手との試合データがありません。"
          />
          <a-row
            v-else
            :gutter="[16, 16]"
            class="opponent-summary"
          >
            <a-col :xs="12" :md="6"
              >対戦回数：{{
                dashboard.opponentMatchSummary.matchCount
              }}回</a-col
            >
            <a-col :xs="12" :md="6"
              >勝利：{{
                dashboard.opponentMatchSummary.winCount
              }}回</a-col
            >
            <a-col :xs="12" :md="6"
              >敗北：{{
                dashboard.opponentMatchSummary.lossCount
              }}回</a-col
            >
            <a-col :xs="12" :md="6"
              >勝率：{{
                dashboard.opponentMatchSummary.winRate.toFixed(
                  1,
                )
              }}%</a-col
            >
          </a-row>
        </a-card>
      </div>
    </a-spin>
  </section>
</template>

<style scoped>
.dashboard,
.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}
.dashboard-description,
.filter-item {
  margin-bottom: 0;
}
.dashboard-content.is-loading {
  min-height: 360px;
}
.choice-alert {
  margin-bottom: 16px;
}
.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(180px, 1fr));
  gap: 16px;
  align-items: end;
}
.filter-item :deep(.ant-picker),
.filter-item :deep(.ant-select) {
  width: 100%;
}
.summary-card {
  height: 100%;
}
.chart-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.5fr);
  gap: 16px;
}
.match-summary {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px 18px;
  color: rgba(0, 0, 0, 0.65);
}
.opponent-summary {
  font-size: 16px;
}
@media (max-width: 991px) {
  .filter-grid {
    grid-template-columns: repeat(2, minmax(180px, 1fr));
  }
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 575px) {
  .filter-grid {
    grid-template-columns: 1fr;
  }
  .chart-grid {
    gap: 12px;
  }
  .dashboard,
  .dashboard-content {
    gap: 12px;
  }
  :deep(.ant-card-body) {
    padding: 16px;
  }
}
</style>
