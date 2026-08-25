<script setup lang="ts">
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
  if (!chart) chart = echarts.init(container.value)
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
watch(() => props.empty, async (empty) => {
  if (empty) {
    chart?.clear()
  } else {
    await nextTick()
    renderChart()
  }
})
watch(() => props.loading, (loading) => {
  if (loading) chart?.showLoading('default', { text: '読み込み中...' })
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
    <a-empty v-if="empty && !loading" :description="emptyDescription" class="chart-empty" />
    <div v-show="!empty" ref="container" class="chart-container" />
    <div v-if="loading && empty" class="chart-loading"><a-spin tip="読み込み中..." /></div>
  </div>
</template>

<style scoped>
.chart-wrapper { position: relative; width: 100%; min-width: 0; }
.chart-container { width: 100%; height: 100%; }
.chart-empty, .chart-loading { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; }
</style>
