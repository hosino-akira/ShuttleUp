<script setup lang="ts">
import { t } from '../i18n';
import { computed, reactive, ref } from "vue";
import axios from "axios";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { resolveAuthRedirect } from "../router";
import AuthShell from '../components/auth/AuthShell.vue';
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
  <AuthShell>
    <section
      class="login-card"
      aria-labelledby="login-title"
    >
      <h1 id="login-title" class="app-page-title">{{ t('ログイン') }}</h1>
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
        <RouterLink class="app-button" :to="{ name: 'register', query: { redirect: route.query.redirect } }">{{ t('アカウント登録') }}</RouterLink>
      </div>
    </section>
  </AuthShell>
</template>

<style scoped>
.login-card {
  width: 100%;
}

.register-link .app-button { margin-inline-start: 6px; }

h1 {
  margin: 0 0 10px;
  color: var(--app-heading-color);
}

p {
  margin: 0 0 28px;
  color: var(--app-muted-color);
}

.register-link {
  margin-top: 16px;
  color: var(--app-muted-color);
  font-size: 14px;
  text-align: center;
}

.login-error {
  margin-bottom: 12px;
}

.login-card :deep(.ant-input),
.login-card :deep(.ant-input-affix-wrapper) { min-height: 42px; border-radius: 9px; }
.login-card :deep(.ant-input-affix-wrapper .ant-input) { min-height: auto; }
.login-card :deep(.ant-form-item) { margin-bottom: 20px; }
.login-card :deep(.ant-btn-primary) { min-height: 44px; margin-top: 6px; font-size: 14px; }
</style>
