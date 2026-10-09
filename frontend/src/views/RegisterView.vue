<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import axios from 'axios'
import { registerUser } from '../api/authApi'

const router = useRouter()
const form = reactive({ name: '', email: '', password: '', confirmPassword: '' })
const isSubmitting = ref(false)
const errorMessage = ref('')

async function validatePassword(_rule: unknown, value: string): Promise<void> {
  if (value && new TextEncoder().encode(value).length > 72) {
    throw new Error('パスワードはUTF-8で72バイト以内で入力してください。')
  }
}

async function validateConfirmation(_rule: unknown, value: string): Promise<void> {
  if (value && value !== form.password) {
    throw new Error('パスワードが一致していません。')
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
    message.success('アカウントを作成しました。ログインしてください。')
    await router.replace({ name: 'login', query: { email: user.email } })
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
      <span class="brand-name">ShuttleUp</span>
      <h1 id="register-title">アカウント登録</h1>
      <p>アカウントを作成して、トレーニングを記録しましょう。</p>

      <a-alert
        v-if="errorMessage"
        class="error-message"
        :message="errorMessage"
        type="error"
        show-icon
        role="alert"
      />

      <a-form
        :model="form"
        :disabled="isSubmitting"
        layout="vertical"
        :required-mark="false"
        size="large"
        novalidate
        @finish="handleSubmit"
      >
        <a-form-item
          label="ユーザー名"
          name="name"
          :rules="[
            { required: true, whitespace: true, message: 'ユーザー名を入力してください。' },
            { max: 100, message: 'ユーザー名は100文字以内で入力してください。' },
          ]"
        >
          <a-input
            v-model:value="form.name"
            autocomplete="nickname"
            placeholder="ユーザー名を入力"
            :maxlength="100"
          />
        </a-form-item>

        <a-form-item
          label="メールアドレス"
          name="email"
          :rules="[
            { required: true, message: 'メールアドレスを入力してください。' },
            { type: 'email', message: '正しいメールアドレスを入力してください。' },
            { max: 255, message: 'メールアドレスは255文字以内で入力してください。' },
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
          label="パスワード"
          name="password"
          extra="6文字以上で入力してください。"
          :rules="[
            { required: true, whitespace: true, message: 'パスワードを入力してください。' },
            { min: 6, message: 'パスワードは6文字以上で入力してください。' },
            { validator: validatePassword },
          ]"
        >
          <a-input-password
            v-model:value="form.password"
            autocomplete="new-password"
            placeholder="パスワードを入力"
          />
        </a-form-item>

        <a-form-item
          label="パスワード（確認）"
          name="confirmPassword"
          :rules="[
            { required: true, message: '確認用パスワードを入力してください。' },
            { validator: validateConfirmation },
          ]"
        >
          <a-input-password
            v-model:value="form.confirmPassword"
            autocomplete="new-password"
            placeholder="パスワードをもう一度入力"
          />
        </a-form-item>

        <a-button type="primary" html-type="submit" block :loading="isSubmitting">
          登録する
        </a-button>
      </a-form>

      <div class="login-link">
        すでにアカウントをお持ちですか？
        <RouterLink :to="{ name: 'login' }">ログイン</RouterLink>
      </div>
    </section>
  </main>
</template>

<style scoped>
.register-page {
  display: grid;
  height: 100%;
  padding: 24px;
  overflow-y: auto;
  background: var(--app-page-color);
}

.register-card {
  width: 100%;
  max-width: 460px;
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

.error-message {
  margin-bottom: 24px;
}

.login-link {
  margin-top: 24px;
  color: #666;
  font-size: 14px;
  text-align: center;
}

@media (max-width: 575px) {
  .register-page {
    padding: 16px;
  }

  .register-card {
    padding: 24px;
  }
}
</style>
