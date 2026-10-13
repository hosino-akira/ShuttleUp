<script setup lang="ts">
withDefaults(
  defineProps<{
    open: boolean;
    title: string;
    zIndex?: number;
  }>(),
  {
    zIndex: 1000,
  },
);

const emit = defineEmits<{
  "update:open": [value: boolean];
}>();
</script>

<template>
  <a-modal
    :open="open"
    :title="title"
    :width="1200"
    :z-index="zIndex"
    :footer="null"
    :mask-closable="false"
    wrap-class-name="base-large-modal"
    @cancel="emit('update:open', false)"
  >
    <div class="base-large-modal__body">
      <slot />
    </div>
    <div class="base-large-modal__footer">
      <slot name="footer" />
    </div>
  </a-modal>
</template>

<style>
.base-large-modal .ant-modal {
  max-width: 92vw;
  padding-bottom: 0;
}

.base-large-modal .ant-modal-content {
  max-height: calc(100dvh - 140px);
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}

.base-large-modal .ant-modal-header {
  flex: 0 0 auto;
  margin: 0;
  padding: 12px 16px 8px;
  padding-right: 48px;
}

.base-large-modal .ant-modal-close {
  top: 8px;
  right: 8px;
}

.base-large-modal .ant-modal-body {
  display: flex;
  min-height: 0;
  flex-direction: column;
}

.base-large-modal__body {
  flex: 1;
  min-height: 0;
  padding: 8px 16px;
  overflow-y: auto;
}

.base-large-modal__footer {
  flex: 0 0 auto;
  padding: 8px 16px;
  border-top: 1px solid var(--app-border-color);
}

@media (max-width: 575px) {
  .base-large-modal__body {
    padding: 8px 12px;
  }

  .base-large-modal__footer {
    padding: 8px 12px;
  }
}
</style>
