<script setup lang="ts">
import { t } from '../i18n';
import { computed, reactive, ref } from "vue";
import axios from "axios";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { resolveAuthRedirect } from "../router";
import LanguageSwitcher from '../components/common/LanguageSwitcher.vue';
import type { FormInstance } from 'ant-design-vue';
import { useLocalizedForm } from '../composables/useLocalizedForm';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const formRef = ref<FormInstance>();
const { onValidate: onFormValidate } = useLocalizedForm(formRef);
const isSubmitting = ref(false);
const errorMessage = ref("");
const verificationMessage = computed(() => route.query.reason === "verification_failed"
  ? t('ログイン状態を確認できませんでした。サーバーの接続を確認して、再度ログインしてください。')
  : "");

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
    await router.replace(resolveAuthRedirect(route.query.redirect));
  } catch (error: unknown) {
    if (axios.isAxiosError<{ message?: string }>(error)
        && [400, 401, 403].includes(error.response?.status ?? 0)) {
      errorMessage.value = error.response?.data.message ?? 'メールアドレスとパスワードを確認してください。';
    } else {
      errorMessage.value = 'ログインできませんでした。サーバーの起動と接続を確認してください。';
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
      <div class="auth-card-header">
        <span class="brand-name">ShuttleUp</span>
        <LanguageSwitcher />
      </div>
      <h1 id="login-title">{{ t('ログイン') }}</h1>
      <p>{{ t('トレーニングの記録を続けましょう。') }}</p>

      <a-alert v-if="errorMessage || verificationMessage" :message="t(errorMessage || verificationMessage)" type="error" show-icon role="alert" class="login-error" />

      <a-form
        ref="formRef"
        :model="form"
        layout="vertical"
        :required-mark="false"
        :disabled="isSubmitting"
        novalidate
        @finish="handleSubmit"
        @validate="onFormValidate"
      >
        <a-form-item
          :label="t('メールアドレス')"
          name="email"
          :rules="[
            { required: true, message: t('メールアドレスを入力してください。') },
            {
              type: 'email',
              message: t('正しいメールアドレスを入力してください。'),
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
          :label="t('パスワード')"
          name="password"
          :rules="[
            { required: true, message: t('パスワードを入力してください。') },
          ]"
        >
          <a-input-password
            v-model:value="form.password"
            autocomplete="current-password"
            :placeholder="t('パスワードを入力')"
          />
        </a-form-item>

        <a-button type="primary" html-type="submit" :loading="isSubmitting" block>
          {{ t('ログイン') }}
        </a-button>
      </a-form>
      <div class="register-link">
        {{ t('アカウントをお持ちでない方は') }}
        <RouterLink :to="{ name: 'register', query: { redirect: route.query.redirect } }">{{ t('アカウント登録') }}</RouterLink>
      </div>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  display: grid;
  height: 100%;
  padding: 16px;
  overflow-y: auto;
  background: var(--app-page-color);
}

.login-card {
  width: 100%;
  max-width: 420px;
  margin: auto;
  padding: 20px;
  border: 1px solid var(--app-border-color);
  border-radius: 10px;
  background: var(--app-surface-color);
  box-shadow: 0 8px 32px rgb(0 0 0 / 4%);
}

.brand-name {
  display: inline-block;
  margin-bottom: 12px;
  color: var(--app-primary-color);
  font-size: 20px;
  font-weight: 700;
}

.auth-card-header { display: flex; align-items: baseline; justify-content: space-between; gap: 12px; }

h1 {
  margin: 0 0 4px;
  font-size: 22px;
  color: var(--app-heading-color);
}

p {
  margin: 0 0 16px;
  color: #666;
}

.register-link {
  margin-top: 16px;
  color: #666;
  font-size: 14px;
  text-align: center;
}

.login-error {
  margin-bottom: 12px;
}

@media (max-width: 575px) {
  .login-page {
    padding: 16px;
  }

  .login-card {
    padding: 16px;
  }
}
</style>
