<script setup lang="ts">
withDefaults(
  defineProps<{
    open: boolean;
    title?: string;
    message: string;
    description?: string;
    danger?: boolean;
    loading?: boolean;
    zIndex?: number;
  }>(),
  {
    title: "確認",
    description: "",
    danger: false,
    loading: false,
    zIndex: 1400,
  },
);

const emit = defineEmits<{
  "update:open": [value: boolean];
  confirm: [];
}>();
</script>

<template>
  <a-modal
    :open="open"
    :title="title"
    :width="400"
    :z-index="zIndex"
    :closable="!loading"
    :mask-closable="false"
    @cancel="emit('update:open', false)"
  >
    <p class="confirm-message">{{ message }}</p>
    <p v-if="description" class="confirm-description">
      {{ description }}
    </p>
    <template #footer>
      <a-button
        :disabled="loading"
        @click="emit('update:open', false)"
        >キャンセル</a-button
      >
      <a-button
        type="primary"
        :danger="danger"
        :loading="loading"
        @click="emit('confirm')"
      >
        確認
      </a-button>
    </template>
  </a-modal>
</template>

<style scoped>
.confirm-message {
  margin-bottom: 4px;
}

.confirm-description {
  margin-bottom: 0;
  color: rgba(0, 0, 0, 0.45);
}
</style>
