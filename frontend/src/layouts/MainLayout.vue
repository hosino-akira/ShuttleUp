<script setup lang="ts">
import { t } from "../i18n";
import { computed, ref } from "vue";
import { useRoute } from "vue-router";
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  BarChartOutlined,
  SettingOutlined,
  HeartOutlined,
} from "@ant-design/icons-vue";
import { NAVIGATION_MENU } from "../constants/navigation";
import LanguageSwitcher from "../components/common/LanguageSwitcher.vue";
import ThemeSwitcher from "../components/common/ThemeSwitcher.vue";
import ShuttleBrand from '../components/common/ShuttleBrand.vue';
import BadmintonIcon from '../components/common/BadmintonIcon.vue';
import CourtLines from '../components/common/CourtLines.vue';

const route = useRoute();
const isSidebarCollapsed = ref(false);
const activeMenuKey = computed(
  () => route.name?.toString() ?? "",
);

function toggleSidebar(): void {
  isSidebarCollapsed.value = !isSidebarCollapsed.value;
}
</script>

<template>
  <a-layout class="application-shell">
    <a-layout-sider
      v-model:collapsed="isSidebarCollapsed"
      class="application-sider"
      :breakpoint="'lg'"
      :collapsed-width="0"
      :width="200"
      theme="light"
    >
      <RouterLink class="brand" :to="{ name: 'dashboard' }">
        <ShuttleBrand />
      </RouterLink>

      <a-menu
        :selected-keys="[activeMenuKey]"
        mode="inline"
      >
        <a-menu-item
          v-for="item in NAVIGATION_MENU"
          :key="item.key"
        >
          <RouterLink class="navigation-link" :to="item.to">
            <BadmintonIcon v-if="item.key === 'dashboard'" kind="court" :size="19" />
            <BadmintonIcon v-else-if="item.key === 'training-history'" kind="racket" :size="19" />
            <svg v-else-if="item.key === 'forest'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M20 4C12 3 6 6 6 12a6 6 0 0 0 6 6c6 0 9-6 8-14ZM4 21l11-11" /></svg>
            <BarChartOutlined v-else-if="item.key === 'statistics'" aria-hidden="true" />
            <SettingOutlined v-else-if="item.key === 'master-data'" aria-hidden="true" />
            <HeartOutlined v-else-if="item.key === 'body-data'" aria-hidden="true" />
            <UserOutlined v-else aria-hidden="true" />
            <span>{{
            t(item.label)
          }}</span></RouterLink>
        </a-menu-item>
      </a-menu>
      <div class="sidebar-court" aria-hidden="true"><CourtLines /><span>ShuttleUp · {{ t('一球ずつ、積み重ねる。') }}</span></div>
    </a-layout-sider>

    <a-layout class="application-layout">
      <a-layout-header class="application-header">
        <a-button
          class="app-button-icon"
          :aria-label="
            isSidebarCollapsed
              ? t('ナビゲーションを開く')
              : t('ナビゲーションを閉じる')
          "
          @click="toggleSidebar"
        >
          <MenuUnfoldOutlined
            v-if="isSidebarCollapsed"
            class="sidebar-toggle-icon"
          />
          <MenuFoldOutlined
            v-else
            class="sidebar-toggle-icon"
          />
        </a-button>
        <span class="header-sport"><BadmintonIcon :size="18" />{{ t('バドミントントレーニング') }}</span>

        <div class="header-actions">
          <ThemeSwitcher />
          <LanguageSwitcher />
          <div class="header-utility">
            <!-- <a-badge :count="0" :show-zero="false">
              <a-button
                class="app-button-icon"
                :aria-label="t('通知')"
              >
                <BellOutlined />
              </a-button>
            </a-badge> -->
            <a-tooltip :title="t('プロフィール')">
              <RouterLink
                class="profile-link app-button app-button-icon"
                :to="{ name: 'profile' }"
                :aria-label="t('プロフィールを開く')"
                :aria-current="
                  route.name === 'profile'
                    ? 'page'
                    : undefined
                "
              >
                <UserOutlined aria-hidden="true" />
              </RouterLink>
            </a-tooltip>
          </div>
        </div>
      </a-layout-header>

      <a-layout-content class="application-content">
        <main class="content-container">
          <RouterView />
        </main>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<style scoped>
.application-shell {
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}

.application-layout {
  min-width: 0;
  min-height: 0;
  height: 100%;
  overflow: hidden;
}

.application-sider {
  height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  border-inline-end: 1px solid var(--app-border-color);
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 72px;
  padding: 0 16px;
  overflow: hidden;
  color: var(--app-heading-color);
  font-size: 16px;
  font-weight: 600;
  text-decoration: none;
  white-space: nowrap;
}

.brand :deep(.shuttle-brand-name) { font-size: 18px; }
.brand :deep(.shuttle-brand-mark) { width: 32px; height: 32px; }
.navigation-link { display: flex; align-items: center; gap: 10px; }
.navigation-link :deep(.anticon) { font-size: 18px; }
.sidebar-court { margin: 36px 20px 20px; color: var(--app-accent-color); }
.sidebar-court :deep(.court-lines) { width: 100%; opacity: .25; }
.sidebar-court span { display: block; margin-top: 10px; font-size: 9px; color: var(--app-muted-color); text-align: center; }
.header-sport { display: flex; align-items: center; gap: 7px; font-size: 11px; color: var(--app-muted-color); margin-inline-end: auto; margin-inline-start: 12px; }
@media (max-width: 760px) { .header-sport { display: none; } }

.application-header {
  display: flex;
  flex: 0 0 48px;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 8px;
  line-height: normal;
  background: var(--app-surface-color);
  border-block-end: 1px solid var(--app-border-color);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.header-utility {
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-link {
  font-size: 16px;
}

.profile-link:hover,
.profile-link[aria-current="page"] {
  background: var(--app-selected-color);
}

.profile-link:focus-visible {
  outline: 2px solid var(--app-primary-color);
  outline-offset: 3px;
}

.application-content {
  background: var(--app-page-color);
  display: flex;
  flex: 1;
  min-width: 0;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: auto;
}

.content-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  min-width: 0;
  min-height: 100%;
  overflow-x: hidden;
  padding: 8px;
  box-sizing: border-box;
}

.sidebar-toggle-icon {
  font-size: 18px;
}

.application-sider :deep(.ant-menu-item) {
  height: 36px;
  margin-block: 2px;
  line-height: 36px;
}
@media (max-width: 575px) {
  .application-header {
    padding: 0 8px;
    flex: 0 0 auto;
    min-height: 48px;
    height: auto;
    gap: 8px;
    align-items: flex-start;
    padding-block: 8px;
  }
  .header-actions {
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 6px;
  }
  .header-utility {
    gap: 6px;
  }
}
</style>
