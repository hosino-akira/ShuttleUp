<script setup lang="ts">
const currentDate = new Intl.DateTimeFormat("ja-JP", {
  dateStyle: "full",
}).format(new Date());
</script>

<template>
  <a-card class="welcome-card" :bordered="false">
    <p class="welcome-card__date">{{ currentDate }}</p>
    <a-typography-title
      :level="2"
      class="welcome-card__title"
    >
      おかえりなさい、Akiraさん
    </a-typography-title>
    <a-typography-paragraph
      class="welcome-card__description"
    >
      今日も一歩ずつ、理想のプレーに近づきましょう。
    </a-typography-paragraph>
  </a-card>
</template>

<style scoped>
.welcome-card {
  overflow: hidden;
  background: linear-gradient(120deg, #e6f4ff, #f6ffed);
}

.welcome-card__date {
  margin-bottom: 8px;
  color: #595959;
}

.welcome-card__title {
  margin-bottom: 8px;
}

.welcome-card__description {
  margin-bottom: 0;
  color: #595959;
}
</style>
