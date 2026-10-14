<script setup lang="ts">
import { t } from '../i18n';
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import axios from 'axios'
import { registerUser } from '../api/authApi'
import AuthShell from '../components/auth/AuthShell.vue'
import type { FormInstance } from 'ant-design-vue'
import { useLocalizedForm } from '../composables/useLocalizedForm'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const { onValidate: onFormValidate } = useLocalizedForm(formRef)
const form = reactive({ name: '', email: '', password: '', confirmPassword: '' })
const isSubmitting = ref(false)
const errorMessage = ref('')

async function validatePassword(_rule: unknown, value: string): Promise<void> {
  if (value && new TextEncoder().encode(value).length > 72) {
    throw new Error(t('パスワードはUTF-8で72バイト以内で入力してください。'))
  }
}

async function validateConfirmation(_rule: unknown, value: string): Promise<void> {
  if (value && value !== form.password) {
    throw new Error(t('パスワードが一致していません。'))
  }
}

async function handleSubmit(): Promise<void> {
  if (isSubmitting.value) return
  isSubmitting.value = true
  errorMessage.value = ''

  try {
    // 確認用パスワードは画面内の検証だけに使い、API には送信しない。
    const user = await registerUser({
      name: form.name.trim(),
      email: form.email.trim(),
      password: form.password,
    })
    form.password = ''
    form.confirmPassword = ''
    message.success(t('アカウントを作成しました。ログインしてください。'))
    await router.replace({ name: 'login', query: { email: user.email, redirect: route.query.redirect } })
  } catch (error: unknown) {
    if (axios.isAxiosError<{ message?: string }>(error)) {
      const status = error.response?.status
      const serverMessage = error.response?.data?.message
      if ((status === 400 || status === 409) && typeof serverMessage === 'string') {
        errorMessage.value = serverMessage
      } else if (!error.response) {
        errorMessage.value = '通信に失敗しました。時間をおいて再度お試しください。'
      } else {
        errorMessage.value = 'アカウントを作成できませんでした。時間をおいて再度お試しください。'
      }
    } else {
      errorMessage.value = 'アカウントを作成できませんでした。時間をおいて再度お試しください。'
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <AuthShell>
    <section class="register-card" aria-labelledby="register-title">
      <h1 id="register-title" class="app-page-title">{{ t('アカウント登録') }}</h1>
      <p>{{ t('アカウントを作成して、トレーニングを記録しましょう。') }}</p>

      <a-alert
        v-if="errorMessage"
        class="error-message"
        :message="t(errorMessage)"
        type="error"
        show-icon
        role="alert"
      />

      <a-form
        ref="formRef"
        :model="form"
        :disabled="isSubmitting"
        layout="vertical"
        :required-mark="false"
        novalidate
        @finish="handleSubmit"
        @validate="onFormValidate"
      >
        <a-form-item
          :label="t('ユーザー名')"
          name="name"
          :rules="[
            { required: true, whitespace: true, message: t('ユーザー名を入力してください。') },
            { max: 100, message: t('ユーザー名は100文字以内で入力してください。') },
          ]"
        >
          <a-input
            v-model:value="form.name"
            autocomplete="nickname"
            :placeholder="t('ユーザー名を入力')"
            :maxlength="100"
          />
        </a-form-item>

        <a-form-item
          :label="t('メールアドレス')"
          name="email"
          :rules="[
            { required: true, message: t('メールアドレスを入力してください。') },
            { type: 'email', message: t('正しいメールアドレスを入力してください。') },
            { max: 255, message: t('メールアドレスは255文字以内で入力してください。') },
          ]"
        >
          <a-input
            v-model:value="form.email"
            type="email"
            autocomplete="username"
            placeholder="you@example.com"
            :maxlength="255"
          />
        </a-form-item>

        <a-form-item
          :label="t('パスワード')"
          name="password"
          :extra="t('6文字以上で入力してください。')"
          :rules="[
            { required: true, whitespace: true, message: t('パスワードを入力してください。') },
            { min: 6, message: t('パスワードは6文字以上で入力してください。') },
            { validator: validatePassword },
          ]"
        >
          <a-input-password
            v-model:value="form.password"
            autocomplete="new-password"
            :placeholder="t('パスワードを入力')"
          />
        </a-form-item>

        <a-form-item
          :label="t('パスワード（確認）')"
          name="confirmPassword"
          :rules="[
            { required: true, message: t('確認用パスワードを入力してください。') },
            { validator: validateConfirmation },
          ]"
        >
          <a-input-password
            v-model:value="form.confirmPassword"
            autocomplete="new-password"
            :placeholder="t('パスワードをもう一度入力')"
          />
        </a-form-item>

        <a-button type="primary" html-type="submit" block :loading="isSubmitting">
          {{ t('登録する') }}
        </a-button>
      </a-form>

      <div class="login-link">
        {{ t('すでにアカウントをお持ちですか？') }}
        <RouterLink class="app-button" :to="{ name: 'login', query: { redirect: route.query.redirect } }">{{ t('ログイン') }}</RouterLink>
      </div>
    </section>
  </AuthShell>
</template>

<style scoped>
.login-link .app-button { margin-inline-start: 6px; }

.register-card {
  width: 100%;
}

h1 {
  margin: 0 0 10px;
  color: var(--app-heading-color);
}

p {
  margin: 0 0 16px;
  color: var(--app-muted-color);
}

.error-message {
  margin-bottom: 12px;
}

.login-link {
  margin-top: 16px;
  color: var(--app-muted-color);
  font-size: 14px;
  text-align: center;
}

.register-card :deep(.ant-input),
.register-card :deep(.ant-input-affix-wrapper) { min-height: 38px; border-radius: 9px; }
.register-card :deep(.ant-input-affix-wrapper .ant-input) { min-height: auto; }
.register-card :deep(.ant-form-item) { margin-bottom: 14px; }
.register-card :deep(.ant-btn-primary) { min-height: 42px; margin-top: 4px; font-size: 14px; }
</style>
