<script setup lang="ts">
defineProps<{
  title: string
  titleId?: string
  description?: string
}>()
</script>

<template>
  <a-card class="page-intro" :bordered="false">
    <header class="page-intro-content">
      <h1 :id="titleId" class="app-page-title">{{ title }}</h1>
      <div v-if="description || $slots.description" class="page-intro-description">
        <slot name="description">{{ description }}</slot>
      </div>
    </header>
    <div v-if="$slots.actions" class="page-intro-actions">
      <slot name="actions" />
    </div>
  </a-card>
</template>
