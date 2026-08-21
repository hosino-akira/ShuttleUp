<script setup lang="ts">
withDefaults(defineProps<{
  open: boolean
  title: string
  loading?: boolean
  zIndex?: number
}>(), {
  loading: false,
  zIndex: 1300,
})

const emit = defineEmits<{
  "update:open": [value: boolean]
}>()
</script>

<template>
  <a-modal
    :open="open"
    :title="title"
    :width="520"
    :z-index="zIndex"
    :footer="null"
    :closable="!loading"
    :mask-closable="false"
    wrap-class-name="base-small-modal"
    @cancel="emit('update:open', false)"
  >
    <div class="base-small-modal__body"><slot /></div>
    <div class="base-small-modal__footer"><slot name="footer" /></div>
  </a-modal>
</template>

<style>
.base-small-modal .ant-modal { max-width: 92vw; padding-bottom: 0; }
.base-small-modal .ant-modal-content { padding: 0; overflow: hidden; }
.base-small-modal .ant-modal-header { margin: 0; padding: 20px 24px 12px; }
.base-small-modal .ant-modal-close { top: 12px; right: 16px; }
.base-small-modal__body { max-height: 65vh; padding: 12px 24px; overflow-y: auto; }
.base-small-modal__footer { padding: 12px 24px 20px; border-top: 1px solid #f0f0f0; }
@media (max-width: 575px) {
  .base-small-modal__body { padding: 12px 16px; }
  .base-small-modal__footer { padding: 12px 16px 16px; }
}
</style>
