<script setup lang="ts">
import { intlLocale, t } from '../../i18n';
import { computed } from 'vue';
import { useAuthStore } from '../../stores/auth';
const auth = useAuthStore();
const currentDate = computed(() => new Intl.DateTimeFormat(intlLocale.value, {
  dateStyle: "full",
}).format(new Date()));
</script>

<template>
  <a-card class="welcome-card" :bordered="false">
    <p class="welcome-card__date">{{ currentDate }}</p>
    <a-typography-title
      :level="2"
      class="welcome-card__title"
    >
      {{ t('おかえりなさい、{name}さん', { name: auth.user?.name ?? '' }) }}
    </a-typography-title>
    <a-typography-paragraph
      class="welcome-card__description"
    >
      {{ t('今日も一歩ずつ、理想のプレーに近づきましょう。') }}
    </a-typography-paragraph>
  </a-card>
</template>

<style scoped>
.welcome-card {
  overflow: hidden;
  background: linear-gradient(120deg, var(--app-soft-color), var(--app-page-color));
}

.welcome-card__date {
  margin-bottom: 8px;
  color: var(--app-muted-color);
}

.welcome-card__title {
  margin-bottom: 8px;
}

.welcome-card__description {
  margin-bottom: 0;
  color: var(--app-muted-color);
}
</style>
