<script setup lang="ts">
import { t } from '../i18n';
</script>

<template>
  <section aria-labelledby="training-plan-title">
    <a-card class="page-intro" :bordered="false">
      <a-typography-title
        id="training-plan-title"
        :level="2"
        >{{ t('トレーニングプラン') }}</a-typography-title
      >
      <a-typography-paragraph type="secondary">
        {{ t('今後のトレーニング計画を管理できます。') }}
      </a-typography-paragraph>
    </a-card>
  </section>
</template>
