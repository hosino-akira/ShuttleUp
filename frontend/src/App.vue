<script setup lang="ts">
import { useRoute } from "vue-router";
import GlobalLoading from "./components/common/GlobalLoading.vue";
import MainLayout from "./layouts/MainLayout.vue";

const route = useRoute();
</script>

<template>
  <RouterView v-if="route.meta.layout === 'auth'" />
  <MainLayout v-else />
  <GlobalLoading />
</template>
