import { computed, ref } from 'vue'

const activeRequestCount = ref(0)
const isLoading = computed(() => activeRequestCount.value > 0)

function startLoading(): void {
  activeRequestCount.value += 1
}

function stopLoading(): void {
  activeRequestCount.value = Math.max(0, activeRequestCount.value - 1)
}

function resetLoading(): void {
  activeRequestCount.value = 0
}

export function useGlobalLoading() {
  return {
    isLoading,
    startLoading,
    stopLoading,
    resetLoading,
  }
}
