<script setup lang="ts">
import { useRoute } from "vue-router";
import GlobalLoading from "./components/common/GlobalLoading.vue";
import MainLayout from "./layouts/MainLayout.vue";
import { antLocale } from './i18n/ui';
import { computed } from 'vue';
import { palette } from './theme';
import type { ThemeConfig } from 'ant-design-vue/es/config-provider/context';

const route = useRoute();

// Apply spacing per Ant Design component so vxe-table keeps its existing density.
const compactTheme = computed<ThemeConfig>(() => ({
  token: {
    colorPrimary: palette.value.primary,
    colorInfo: palette.value.primary,
    colorLink: palette.value.primary,
    colorLinkHover: palette.value.hover,
    colorLinkActive: palette.value.active,
    colorText: palette.value.text,
    colorTextHeading: palette.value.heading,
    colorTextSecondary: palette.value.muted,
    colorTextPlaceholder: palette.value.muted,
    colorBorder: palette.value.border,
    colorBorderSecondary: palette.value.border,
    colorBgContainer: palette.value.surface,
    colorBgElevated: palette.value.elevated,
    colorBgLayout: palette.value.page,
    colorFillAlter: palette.value.soft,
    colorPrimaryBg: palette.value.soft,
    colorPrimaryBgHover: palette.value.selected,
    borderRadius: 8,
    colorError: '#b84f4b',
  },
  components: {
    Button: { colorBgContainer: palette.value.soft, colorBorder: palette.value.border, colorText: palette.value.primary },
    Input: { colorBgContainer: palette.value.elevated },
    InputNumber: { colorBgContainer: palette.value.elevated },
    Select: { colorBgContainer: palette.value.elevated },
    DatePicker: { colorBgContainer: palette.value.elevated },
    Table: { colorFillAlter: palette.value.soft },
    Menu: { colorItemBg: palette.value.surface, colorItemBgSelected: palette.value.selected, colorItemTextSelected: palette.value.primary, colorItemBgHover: palette.value.soft },
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
}));
</script>

<template>
  <a-config-provider :theme="compactTheme" :locale="antLocale">
    <RouterView v-if="route.meta.layout === 'auth'" />
    <MainLayout v-else />
    <GlobalLoading />
  </a-config-provider>
</template>
