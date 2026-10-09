<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import axios from "axios";
import type { EChartsOption } from "echarts";
import {
  CalendarOutlined,
  FieldTimeOutlined,
  FireOutlined,
  RiseOutlined,
} from "@ant-design/icons-vue";
import BaseEChart from "../components/common/BaseEChart.vue";
import { getTrainingAnalysis } from "../api/trainingAnalysisApi";
import { requireUserId } from "../stores/auth";
import type { TrainingAnalysisResponse } from "../types/trainingAnalysis";

interface DateLike {
  isAfter: (date: string, unit: "day") => boolean;
}
const today = formatDate(new Date());
const initialFrom = new Date();
initialFrom.setMonth(initialFrom.getMonth() - 3);
const selectedRange = ref<[string, string]>([
  formatDate(initialFrom),
  today,
]);
const report = ref<TrainingAnalysisResponse>();
const loading = ref(false);
const errorMessage = ref("");
let requestId = 0;

const sortedWeights = computed(() =>
  [...(report.value?.weightProgress ?? [])].sort(
    (a, b) => b.improvementKg - a.improvementKg,
  ),
);
const warnings = computed(() => [
  ...new Set(report.value?.warnings ?? []),
]);
const isEntirelyEmpty = computed(
  () =>
    report.value?.frequency.sessionCount === 0 &&
    report.value.weightProgress.length === 0 &&
    report.value.matches.overall.matchCount === 0,
);
const weeklyOption = computed<EChartsOption>(() => {
  const rows = report.value?.frequency.weeklyTrend ?? [];
  return {
    animationDuration: 250,
    color: ["#1677ff"],
    tooltip: {
      trigger: "axis",
      valueFormatter: (value) => `${value}日`,
    },
    grid: { left: 48, right: 20, top: 28, bottom: 48 },
    xAxis: {
      type: "category",
      data: rows.map((row) =>
        formatShortDate(row.weekStart),
      ),
      axisLabel: { hideOverlap: true },
    },
    yAxis: { type: "value", name: "日数", minInterval: 1 },
    series: [
      {
        name: "トレーニング日数",
        type: "bar",
        data: rows.map((row) => row.trainingDays),
      },
    ],
  };
});
const weightOption = computed<EChartsOption>(() => ({
  animationDuration: 250,
  color: ["#722ed1"],
  tooltip: {
    trigger: "axis",
    formatter: (params) => {
      const item = Array.isArray(params)
        ? params[0]
        : params;
      const row = sortedWeights.value[item?.dataIndex ?? 0];
      return row
        ? `${row.exerciseName}<br/>増加重量：${formatKg(row.improvementKg)} kg<br/>増加率：${formatRate(row.improvementRate)}<br/>自己ベスト：${formatKg(row.personalBestWeightKg)} kg`
        : "";
    },
  },
  grid: { left: 110, right: 28, top: 20, bottom: 40 },
  xAxis: { type: "value", name: "増加重量（kg）" },
  yAxis: {
    type: "category",
    inverse: true,
    data: sortedWeights.value.map(
      (row) => row.exerciseName,
    ),
    axisLabel: { width: 96, overflow: "truncate" },
  },
  series: [
    {
      name: "増加重量",
      type: "bar",
      data: sortedWeights.value.map(
        (row) => row.improvementKg,
      ),
    },
  ],
}));
const balanceOption = computed<EChartsOption>(() => {
  const balance = report.value?.trainingBalance;
  return {
    animationDuration: 250,
    color: ["#1677ff", "#52c41a", "#bfbfbf"],
    tooltip: { trigger: "item", formatter: "{b}：{c}%" },
    legend: { bottom: 0 },
    series: [
      {
        type: "pie",
        radius: ["48%", "70%"],
        center: ["50%", "43%"],
        label: { formatter: "{b}\n{d}%" },
        data: [
          {
            name: "フィジカル",
            value: balance?.physicalRecordRate ?? 0,
          },
          {
            name: "技術練習",
            value: balance?.skillRecordRate ?? 0,
          },
          {
            name: "その他",
            value: balance?.otherRecordRate ?? 0,
          },
        ],
      },
    ],
  };
});

function formatDate(date: Date): string {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}
function formatShortDate(value: string): string {
  return `${Number(value.slice(5, 7))}/${Number(value.slice(8, 10))}週`;
}
function formatJapaneseDate(value: string): string {
  const [y, m, d] = value.split("-").map(Number);
  return `${y}年${m}月${d}日`;
}
function formatKg(value: number): string {
  return Number.isInteger(value)
    ? String(value)
    : value.toFixed(1);
}
function formatRate(value: number | null): string {
  return value === null ? "－" : `${value.toFixed(1)}%`;
}
function disableFutureDate(value: DateLike): boolean {
  return value.isAfter(today, "day");
}
function getErrorMessage(error: unknown): string {
  if (!axios.isAxiosError(error))
    return "トレーニング分析データの取得に失敗しました。";
  switch (error.response?.status) {
    case 404:
      return "対象ユーザーまたは分析可能なデータが見つかりませんでした。";
    case 400:
    case 422:
      return "入力した分析期間を確認してください。";
    case 503:
      return "分析サービスまたはデータサービスに接続できませんでした。";
    case 504:
      return "分析処理またはデータ取得がタイムアウトしました。";
    default:
      return "トレーニング分析データの取得に失敗しました。";
  }
}
async function loadAnalysis(): Promise<void> {
  if (
    !selectedRange.value?.[0] ||
    !selectedRange.value?.[1] ||
    selectedRange.value[0] > selectedRange.value[1]
  ) {
    errorMessage.value =
      "開始日と終了日を正しく入力してください。";
    return;
  }
  const currentRequest = ++requestId;
  const [from, to] = selectedRange.value;
  loading.value = true;
  errorMessage.value = "";
  try {
    const loaded = await getTrainingAnalysis(
      requireUserId(),
      { from, to },
    );
    if (currentRequest === requestId) report.value = loaded;
  } catch (error: unknown) {
    if (currentRequest === requestId) {
      report.value = undefined;
      errorMessage.value = getErrorMessage(error);
    }
  } finally {
    if (currentRequest === requestId) loading.value = false;
  }
}
onMounted(() => void loadAnalysis());
</script>

<template>
  <section
    class="statistics"
    aria-labelledby="statistics-title"
  >
    <a-card :bordered="false">
      <a-typography-title id="statistics-title" :level="2"
        >統計</a-typography-title
      >
      <a-typography-paragraph
        class="description"
        type="secondary"
        >トレーニングの継続性や成長傾向を詳しく確認できます。</a-typography-paragraph
      >
    </a-card>
    <a-card title="分析期間" :bordered="false">
      <div class="period-controls">
        <a-form-item
          label="開始日・終了日"
          class="period-field"
          required
        >
          <a-range-picker
            v-model:value="selectedRange"
            :disabled-date="disableFutureDate"
            value-format="YYYY-MM-DD"
            format="YYYY年MM月DD日"
            :allow-clear="false"
            :placeholder="['開始日', '終了日']"
          />
        </a-form-item>
        <a-button
          type="primary"
          :loading="loading"
          @click="loadAnalysis"
          >分析する</a-button
        >
      </div>
    </a-card>
    <a-alert
      v-if="errorMessage"
      :message="errorMessage"
      type="error"
      show-icon
    >
      <template #action
        ><a-button
          danger
          size="small"
          :loading="loading"
          @click="loadAnalysis"
          >再試行</a-button
        ></template
      >
    </a-alert>

    <a-spin
      :spinning="loading"
      tip="トレーニングデータを分析しています。"
    >
      <div
        v-if="report && !loading"
        class="analysis-content"
      >
        <a-empty
          v-if="isEntirelyEmpty"
          description="選択した期間には分析可能なトレーニングデータがありません。"
        />
        <template v-else>
          <a-card
            title="トレーニング継続性"
            :bordered="false"
            ><a-row :gutter="[16, 16]">
              <a-col :xs="24" :sm="12" :xl="6"
                ><div class="metric">
                  <CalendarOutlined /><span
                    >週平均トレーニング日数</span
                  ><strong
                    >{{
                      report.frequency.trainingDaysPerWeek.toFixed(
                        1,
                      )
                    }}日</strong
                  >
                </div></a-col
              >
              <a-col :xs="24" :sm="12" :xl="6"
                ><div class="metric">
                  <FireOutlined /><span>最長連続日数</span
                  ><strong
                    >{{
                      report.frequency
                        .longestTrainingStreakDays
                    }}日</strong
                  >
                </div></a-col
              >
              <a-col :xs="24" :sm="12" :xl="6"
                ><div class="metric">
                  <RiseOutlined /><span>現在の連続日数</span
                  ><strong
                    >{{
                      report.frequency
                        .currentTrainingStreakDays
                    }}日</strong
                  >
                </div></a-col
              >
              <a-col :xs="24" :sm="12" :xl="6"
                ><div class="metric">
                  <FieldTimeOutlined /><span
                    >最終トレーニングからの日数</span
                  ><strong>{{
                    report.frequency
                      .daysSinceLastTraining === null
                      ? "－"
                      : `${report.frequency.daysSinceLastTraining}日`
                  }}</strong>
                </div></a-col
              >
            </a-row></a-card
          >
          <a-card
            title="週別トレーニング傾向"
            :bordered="false"
            ><BaseEChart
              :option="weeklyOption"
              :empty="
                report.frequency.weeklyTrend.length === 0
              "
              empty-description="週別傾向を表示するデータがありません。"
              height="320px"
          /></a-card>

          <div class="two-column">
            <a-card title="重量成長分析" :bordered="false">
              <BaseEChart
                :option="weightOption"
                :empty="sortedWeights.length === 0"
                empty-description="重量の分析に必要なデータがありません。"
                :height="`${Math.max(280, Math.min(sortedWeights.length, 10) * 42 + 80)}px`"
              />
              <div
                v-if="sortedWeights.length"
                class="scroll-list"
              >
                <div
                  v-for="item in sortedWeights"
                  :key="item.exerciseId"
                  class="best-item"
                >
                  <strong>{{ item.exerciseName }}</strong
                  ><span
                    >自己ベスト：{{
                      formatKg(item.personalBestWeightKg)
                    }}
                    kg</span
                  ><span
                    >達成日：{{
                      formatJapaneseDate(
                        item.personalBestDate,
                      )
                    }}</span
                  >
                </div>
              </div>
            </a-card>
            <a-card
              title="トレーニングバランス"
              :bordered="false"
            >
              <BaseEChart
                :option="balanceOption"
                :empty="
                  report.trainingBalance
                    .physicalRecordCount +
                    report.trainingBalance
                      .skillRecordCount +
                    report.trainingBalance
                      .otherRecordCount ===
                  0
                "
                empty-description="練習比率を表示するデータがありません。"
                height="330px"
              />
              <a-typography-paragraph
                class="note"
                type="secondary"
                >種目カテゴリー名を基に分類しています。</a-typography-paragraph
              >
              <a-alert
                v-if="
                  report.trainingBalance
                    .unclassifiedCategories.length
                "
                type="warning"
                show-icon
                :message="`分類を確認する必要があるカテゴリー：${report.trainingBalance.unclassifiedCategories.join('、')}`"
              />
            </a-card>
          </div>

          <div>
            <a-card
              title="対戦相手別分析"
              :bordered="false"
            >
              <a-empty
                v-if="
                  report.matches.byOpponent.length === 0
                "
                description="対戦分析に必要な試合データがありません。"
              />
              <div v-else>
                <div
                  v-for="opponent in report.matches
                    .byOpponent"
                  :key="opponent.opponentId"
                  class="opponent-item"
                >
                  <div class="opponent-heading">
                    <strong>{{
                      opponent.opponentName
                    }}</strong>
                  </div>
                  <a-progress
                    :percent="opponent.winRate"
                    :format="
                      (percent?: number) =>
                        `${(percent ?? 0).toFixed(1)}%`
                    "
                  /><span class="secondary"
                    >対戦回数：{{
                      opponent.matchCount
                    }}回</span
                  >
                </div>
              </div>
            </a-card>
          </div>
          <a-alert
            v-if="warnings.length"
            type="warning"
            show-icon
            message="分析時の確認事項"
            ><template #description
              ><ul class="warning-list">
                <li
                  v-for="warning in warnings"
                  :key="warning"
                >
                  {{ warning }}
                </li>
              </ul></template
            ></a-alert
          >
        </template>
      </div>
      <div
        v-else-if="loading"
        class="loading-space"
        aria-hidden="true"
      />
    </a-spin>
  </section>
</template>

<style scoped>
.statistics,
.analysis-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}
.description,
.period-field,
.note {
  margin-bottom: 0;
}
.period-controls {
  display: flex;
  align-items: end;
  gap: 16px;
}
.period-field {
  flex: 1;
  max-width: 520px;
}
.period-field :deep(.ant-picker) {
  width: 100%;
}
.loading-space {
  min-height: 420px;
}
.metric {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 5px 10px;
  padding: 8px;
}
.metric > :first-child {
  grid-row: 1 / 3;
  color: #1677ff;
  font-size: 28px;
}
.metric span,
.secondary {
  color: rgba(0, 0, 0, 0.65);
}
.metric strong {
  font-size: 22px;
}
.two-column {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  align-items: stretch;
}
.scroll-list {
  max-height: 300px;
  overflow-y: auto;
}
.best-item {
  display: grid;
  grid-template-columns: minmax(100px, 1fr) auto auto;
  gap: 12px;
  padding: 10px 4px;
  border-top: 1px solid #f0f0f0;
}
.opponent-item {
  padding: 10px 4px;
  border-bottom: 1px solid #f0f0f0;
}
.opponent-heading {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}
.warning-list {
  margin: 0;
  padding-left: 20px;
}
@media (max-width: 991px) {
  .two-column {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 575px) {
  .statistics,
  .analysis-content {
    gap: 12px;
  }
  .period-controls {
    flex-direction: column;
    align-items: stretch;
  }
  .period-field {
    width: 100%;
  }
  .period-controls > :deep(.ant-btn) {
    width: 100%;
  }
  .best-item {
    grid-template-columns: 1fr;
    gap: 4px;
  }
  :deep(.ant-card-body) {
    padding: 16px;
  }
}
</style>
