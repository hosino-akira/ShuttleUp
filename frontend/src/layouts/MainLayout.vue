<script setup lang="ts">
import { computed, ref } from "vue";
import { useRoute } from "vue-router";
import {
  BellOutlined,
  GlobalOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
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
  <a-layout class="application-layout">
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

    <a-layout>
      <a-layout-header class="application-header">
        <a-button
          :aria-label="
            isSidebarCollapsed
              ? 'Expand navigation'
              : 'Collapse navigation'
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
          <!-- Reserved controls: connect these to notification, locale, and theme services later. -->
          <a-button
            aria-label="Change language"
            type="text"
          >
            <GlobalOutlined />
          </a-button>
          <a-badge :count="0" :show-zero="false">
            <a-button
              aria-label="Notifications"
              type="text"
            >
              <BellOutlined />
            </a-button>
          </a-badge>
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
.application-layout {
  min-height: 100vh;
}

.application-sider {
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

.application-content {
  background: var(--app-page-color);
}

.content-container {
  min-height: calc(100vh - 64px);
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
