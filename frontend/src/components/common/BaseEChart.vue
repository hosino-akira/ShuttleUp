<script setup lang="ts">
import { locale, t } from '../../i18n';
import { palette, chartColors } from '../../theme';
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { use } from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts, EChartsOption } from 'echarts'
import * as echarts from 'echarts/core'

use([
  BarChart,
  LineChart,
  PieChart,
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
  CanvasRenderer,
])

const props = withDefaults(defineProps<{
  option: EChartsOption
  loading?: boolean
  height?: string
  empty?: boolean
  emptyDescription?: string
}>(), {
  loading: false,
  height: '320px',
  empty: false,
  emptyDescription: '表示するデータがありません。',
})

const container = ref<HTMLDivElement>()
let chart: ECharts | undefined
let resizeObserver: ResizeObserver | undefined

function renderChart(): void {
  if (!container.value || props.empty) return
  if (!chart) {
    const colors = palette.value
    const axis = {
      axisLine: { lineStyle: { color: colors.border } },
      axisLabel: { color: colors.muted },
      nameTextStyle: { color: colors.muted },
      splitLine: { lineStyle: { color: colors.border } },
    }
    chart = echarts.init(container.value, {
      color: chartColors.value,
      textStyle: { color: colors.text },
      title: { textStyle: { color: colors.heading } },
      legend: { textStyle: { color: colors.muted } },
      categoryAxis: axis,
      valueAxis: axis,
      tooltip: { backgroundColor: colors.elevated, borderColor: colors.border, textStyle: { color: colors.text } },
    })
  }
  chart.setOption(props.option, true)
  chart.resize()
}

onMounted(async () => {
  await nextTick()
  renderChart()
  if (container.value) {
    resizeObserver = new ResizeObserver(() => chart?.resize())
    resizeObserver.observe(container.value)
  }
})

watch(() => props.option, () => nextTick(renderChart), { deep: true })
watch(palette, async () => {
  chart?.dispose()
  chart = undefined
  await nextTick()
  renderChart()
  const renderedChart = chart as ECharts | undefined
  if (props.loading) renderedChart?.showLoading('default', { text: t('読み込み中...'), color: palette.value.primary })
})
watch(() => props.empty, async (empty) => {
  if (empty) {
    chart?.clear()
  } else {
    await nextTick()
    renderChart()
  }
})
watch([() => props.loading, locale], ([loading]) => {
  if (loading) chart?.showLoading('default', { text: t('読み込み中...'), color: palette.value.primary })
  else chart?.hideLoading()
}, { immediate: true })

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  chart?.dispose()
  chart = undefined
})
</script>

<template>
  <div class="chart-wrapper" :style="{ height }">
    <a-empty v-if="empty && !loading" :description="emptyDescription === '表示するデータがありません。' ? t('表示するデータがありません。') : emptyDescription" class="chart-empty" />
    <div v-show="!empty" ref="container" class="chart-container" />
    <div v-if="loading && empty" class="chart-loading"><a-spin :tip="t('読み込み中...')" /></div>
  </div>
</template>

<style scoped>
.chart-wrapper { position: relative; width: 100%; min-width: 0; }
.chart-container { width: 100%; height: 100%; }
.chart-empty, .chart-loading { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; }
</style>
