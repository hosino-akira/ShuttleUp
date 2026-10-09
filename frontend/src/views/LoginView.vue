<script setup lang="ts">
import { reactive, ref } from "vue";
import axios from "axios";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const isSubmitting = ref(false);
const errorMessage = ref("");

const form = reactive({
  email: typeof route.query.email === "string" ? route.query.email : "",
  password: "",
});

async function handleSubmit(): Promise<void> {
  if (isSubmitting.value) return;
  isSubmitting.value = true;
  errorMessage.value = "";
  try {
    await auth.login({ email: form.email.trim(), password: form.password });
    form.password = "";
    await router.replace({ name: "dashboard" });
  } catch (error: unknown) {
    if (axios.isAxiosError<{ message?: string }>(error)
        && [400, 401, 403].includes(error.response?.status ?? 0)) {
      errorMessage.value = error.response?.data.message ?? "メールアドレスとパスワードを確認してください。";
    } else {
      errorMessage.value = "ログインできませんでした。サーバーの起動と接続を確認してください。";
    }
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <main class="login-page">
    <section
      class="login-card"
      aria-labelledby="login-title"
    >
      <span class="brand-name">ShuttleUp</span>
      <h1 id="login-title">ログイン</h1>
      <p>トレーニングの記録を続けましょう。</p>

      <a-alert v-if="errorMessage" :message="errorMessage" type="error" show-icon role="alert" class="login-error" />

      <a-form
        :model="form"
        layout="vertical"
        :required-mark="false"
        size="large"
        :disabled="isSubmitting"
        novalidate
        @finish="handleSubmit"
      >
        <a-form-item
          label="メールアドレス"
          name="email"
          :rules="[
            { required: true, message: 'メールアドレスを入力してください。' },
            {
              type: 'email',
              message: '正しいメールアドレスを入力してください。',
            },
          ]"
        >
          <a-input
            v-model:value="form.email"
            type="email"
            autocomplete="username"
            placeholder="you@example.com"
          />
        </a-form-item>

        <a-form-item
          label="パスワード"
          name="password"
          :rules="[
            { required: true, message: 'パスワードを入力してください。' },
          ]"
        >
          <a-input-password
            v-model:value="form.password"
            autocomplete="current-password"
            placeholder="パスワードを入力"
          />
        </a-form-item>

        <a-button type="primary" html-type="submit" :loading="isSubmitting" block>
          ログイン
        </a-button>
      </a-form>
      <div class="register-link">
        アカウントをお持ちでない方は
        <RouterLink :to="{ name: 'register' }">新規登録</RouterLink>
      </div>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  display: grid;
  height: 100%;
  padding: 24px;
  overflow-y: auto;
  background: var(--app-page-color);
}

.login-card {
  width: 100%;
  max-width: 420px;
  margin: auto;
  padding: 32px;
  border: 1px solid var(--app-border-color);
  border-radius: 16px;
  background: var(--app-surface-color);
  box-shadow: 0 8px 32px rgb(0 0 0 / 4%);
}

.brand-name {
  display: inline-block;
  margin-bottom: 24px;
  color: var(--app-primary-color);
  font-size: 20px;
  font-weight: 700;
}

h1 {
  margin: 0 0 8px;
  color: var(--app-heading-color);
}

p {
  margin: 0 0 24px;
  color: #666;
}

.register-link {
  margin-top: 24px;
  color: #666;
  font-size: 14px;
  text-align: center;
}

.login-error {
  margin-bottom: 20px;
}

@media (max-width: 575px) {
  .login-page {
    padding: 16px;
  }

  .login-card {
    padding: 24px;
  }
}
</style>
