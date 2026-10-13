<script setup lang="ts">
import { useRoute } from "vue-router";
import GlobalLoading from "./components/common/GlobalLoading.vue";
import MainLayout from "./layouts/MainLayout.vue";

const route = useRoute();

// Apply spacing per Ant Design component so vxe-table keeps its existing density.
const compactTheme = {
  components: {
    Card: { paddingLG: 12, padding: 8, fontSizeLG: 14 },
    Form: { marginLG: 12, paddingXS: 4 },
    Typography: {
      fontSizeHeading2: 22,
      fontSizeHeading3: 18,
      sizeMarginHeadingVerticalStart: 0,
      sizeMarginHeadingVerticalEnd: 4,
    },
    Divider: { marginLG: 12 },
    Statistic: { fontSizeHeading3: 24, marginXXS: 2 },
    List: { padding: 8, paddingLG: 12 },
    Drawer: { paddingLG: 12 },
    Modal: { paddingMD: 16, paddingContentHorizontalLG: 16 },
  },
};
</script>

<template>
  <a-config-provider :theme="compactTheme">
    <RouterView v-if="route.meta.layout === 'auth'" />
    <MainLayout v-else />
    <GlobalLoading />
  </a-config-provider>
</template>
