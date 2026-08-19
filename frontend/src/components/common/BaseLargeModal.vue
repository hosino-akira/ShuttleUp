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
  min-height: 650px;
  max-height: calc(85vh - 120px);
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}

.base-large-modal .ant-modal-header {
  flex: 0 0 auto;
  margin: 0;
  padding: 20px 24px 12px;
}

.base-large-modal .ant-modal-close {
  top: 12px;
  right: 16px;
}

.base-large-modal .ant-modal-body {
  display: flex;
  min-height: 0;
  flex-direction: column;
}

.base-large-modal__body {
  flex: 1;
  min-height: 0;
  padding: 12px 24px;
  overflow-y: auto;
}

.base-large-modal__footer {
  flex: 0 0 auto;
  padding: 12px 24px 20px;
  border-top: 1px solid #f0f0f0;
}

@media (max-width: 575px) {
  .base-large-modal__body {
    padding: 12px 16px;
  }

  .base-large-modal__footer {
    padding: 12px 16px 16px;
  }
}
</style>
