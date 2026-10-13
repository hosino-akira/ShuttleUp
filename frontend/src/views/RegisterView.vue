<script setup lang="ts">
import { t } from '../i18n';
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import axios from 'axios'
import { registerUser } from '../api/authApi'
import LanguageSwitcher from '../components/common/LanguageSwitcher.vue'
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
  <main class="register-page">
    <section class="register-card" aria-labelledby="register-title">
      <div class="auth-card-header">
        <span class="brand-name">ShuttleUp</span>
        <LanguageSwitcher />
      </div>
      <h1 id="register-title">{{ t('アカウント登録') }}</h1>
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
        <RouterLink :to="{ name: 'login', query: { redirect: route.query.redirect } }">{{ t('ログイン') }}</RouterLink>
      </div>
    </section>
  </main>
</template>

<style scoped>
.auth-card-header { display: flex; align-items: baseline; justify-content: space-between; gap: 12px; }
.register-page {
  display: grid;
  height: 100%;
  padding: 16px;
  overflow-y: auto;
  background: var(--app-page-color);
}

.register-card {
  width: 100%;
  max-width: 460px;
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

h1 {
  margin: 0 0 4px;
  font-size: 22px;
  color: var(--app-heading-color);
}

p {
  margin: 0 0 16px;
  color: #666;
}

.error-message {
  margin-bottom: 12px;
}

.login-link {
  margin-top: 16px;
  color: #666;
  font-size: 14px;
  text-align: center;
}

@media (max-width: 575px) {
  .register-page {
    padding: 16px;
  }

  .register-card {
    padding: 16px;
  }
}
</style>
