<script setup lang="ts">
import { computed, ref } from "vue";
import { useRoute } from "vue-router";
import {
  BellOutlined,
  GlobalOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
} from "@ant-design/icons-vue";
import { NAVIGATION_MENU } from "../constants/navigation";

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
      :width="240"
      theme="light"
    >
      <RouterLink class="brand" :to="{ name: 'dashboard' }">
        <span class="brand-mark" aria-hidden="true">S</span>
        <span class="brand-name">ShuttleUp</span>
      </RouterLink>

      <a-menu
        :selected-keys="[activeMenuKey]"
        mode="inline"
      >
        <a-menu-item
          v-for="item in NAVIGATION_MENU"
          :key="item.key"
        >
          <RouterLink :to="item.to">{{
            item.label
          }}</RouterLink>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout class="application-layout">
      <a-layout-header class="application-header">
        <a-button
          :aria-label="
            isSidebarCollapsed
              ? 'ナビゲーションを開く'
              : 'ナビゲーションを閉じる'
          "
          type="text"
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

        <div class="header-actions">
          <!-- 通知と言語切り替えは後のステップで接続する。 -->
          <a-button
            aria-label="言語を切り替える"
            type="text"
          >
            <GlobalOutlined />
          </a-button>
          <a-badge :count="0" :show-zero="false">
            <a-button
              aria-label="通知"
              type="text"
            >
              <BellOutlined />
            </a-button>
          </a-badge>
          <a-tooltip title="プロフィール">
            <RouterLink
              class="profile-link"
              :to="{ name: 'profile' }"
              aria-label="プロフィールを開く"
              :aria-current="route.name === 'profile' ? 'page' : undefined"
            >
              <UserOutlined aria-hidden="true" />
            </RouterLink>
          </a-tooltip>
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
  gap: 12px;
  height: 64px;
  padding: 0 24px;
  overflow: hidden;
  color: var(--app-heading-color);
  font-size: 18px;
  font-weight: 600;
  text-decoration: none;
  white-space: nowrap;
}

.brand-mark {
  display: grid;
  flex: 0 0 auto;
  width: 28px;
  height: 28px;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  background: var(--app-primary-color);
}

.application-header {
  display: flex;
  flex: 0 0 64px;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 12px;
  line-height: normal;
  background: var(--app-surface-color);
  border-block-end: 1px solid var(--app-border-color);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.profile-link {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  margin-left: 8px;
  border-radius: 50%;
  background: #e6f4ff;
  color: var(--app-primary-color);
  font-size: 18px;
}

.profile-link:hover,
.profile-link[aria-current="page"] {
  background: #bae0ff;
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
  padding: clamp(10px, 3vw, 15px);
  box-sizing: border-box;
}

.sidebar-toggle-icon {
  font-size: 24px;
}
@media (max-width: 575px) {
  .application-header {
    padding: 0 16px;
  }
}
</style>
