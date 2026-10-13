<script setup lang="ts">
import { t } from '../i18n';
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import { changeMyPassword } from '../api/authApi'
import type { FormInstance } from 'ant-design-vue'
import { useLocalizedForm } from '../composables/useLocalizedForm'

const auth = useAuthStore()
const router = useRouter()
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const { onValidate: onProfileValidate } = useLocalizedForm(profileFormRef)
const { onValidate: onPasswordValidate } = useLocalizedForm(passwordFormRef)
const saving = ref(false)
const errorMessage = ref('')
const changingPassword = ref(false)
const passwordError = ref('')
const passwordForm = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })
const isBusy = computed(() => saving.value || changingPassword.value)
const form = reactive({ name: auth.user?.name ?? '', email: auth.user?.email ?? '' })
const hasChanges = computed(() => form.name.trim() !== auth.user?.name || form.email.trim().toLowerCase() !== auth.user?.email)

async function saveProfile(): Promise<void> {
  if (isBusy.value) return
  saving.value = true
  errorMessage.value = ''
  try {
    await auth.updateProfile({ name: form.name.trim(), email: form.email.trim() })
    form.name = auth.user?.name ?? ''
    form.email = auth.user?.email ?? ''
    message.success(t('プロフィールを更新しました。'))
  } catch (error: unknown) {
    if (axios.isAxiosError<{ message?: string }>(error) && [400, 409].includes(error.response?.status ?? 0)) {
      errorMessage.value = error.response?.data.message ?? '入力内容を確認してください。'
    } else if (!axios.isAxiosError(error) || error.response?.status !== 401) {
      errorMessage.value = 'プロフィールを更新できませんでした。時間をおいて再度お試しください。'
    }
  } finally {
    saving.value = false
  }
}

async function validatePasswordBytes(_rule: unknown, value: string): Promise<void> {
  if (new TextEncoder().encode(value ?? '').length > 72) {
    throw new Error(t('パスワードはUTF-8で72バイト以内で入力してください。'))
  }
}

async function validateConfirmation(_rule: unknown, value: string): Promise<void> {
  if (value !== passwordForm.newPassword) {
    throw new Error(t('新しいパスワードと確認用パスワードが一致しません。'))
  }
}

async function changePassword(): Promise<void> {
  if (isBusy.value) return
  changingPassword.value = true
  passwordError.value = ''
  try {
    await changeMyPassword({ ...passwordForm })
    const email = auth.user?.email ?? ''
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    auth.logout()
    await router.replace({ name: 'login', query: { email } })
    message.success(t('パスワードを変更しました。新しいパスワードでログインしてください。'))
  } catch (error: unknown) {
    if (axios.isAxiosError<{ message?: string }>(error) && error.response?.status === 400) {
      passwordError.value = error.response.data.message ?? '入力内容を確認してください。'
    } else if (!axios.isAxiosError(error) || error.response?.status !== 401) {
      passwordError.value = 'パスワードを変更できませんでした。時間をおいて再度お試しください。'
    }
  } finally {
    changingPassword.value = false
  }
}

async function logout(): Promise<void> {
  auth.logout()
  await router.replace({ name: 'login' })
  message.success(t('ログアウトしました。'))
}
</script>

<template>
  <section class="profile-page" aria-labelledby="profile-title">
    <a-card :bordered="false" class="profile-card">
      <a-typography-title id="profile-title" class="app-page-title" :level="2"
        >{{ t('プロフィール') }}</a-typography-title
      >
      <a-typography-paragraph type="secondary">
        {{ t('アカウント情報を確認・変更できます。') }}
      </a-typography-paragraph>

      <div class="profile-summary">
        <a-avatar :size="48" class="profile-avatar"><UserOutlined aria-hidden="true" /></a-avatar>
        <div>
          <h3>{{ auth.user?.name }}</h3>
          <span class="profile-email">{{ auth.user?.email }}</span>
          <div class="account-status"><a-tag color="green">{{ t('有効なアカウント') }}</a-tag></div>
        </div>
      </div>

      <a-divider />
      <a-alert v-if="errorMessage" :message="t(errorMessage)" type="error" show-icon role="alert" class="profile-error" />
      <div class="profile-form-grid">
        <a-form ref="profileFormRef" :model="form" layout="vertical" :required-mark="false" :disabled="isBusy" aria-labelledby="account-title" @finish="saveProfile" @validate="onProfileValidate">
          <a-typography-title id="account-title" :level="3">{{ t('アカウント情報') }}</a-typography-title>
          <a-form-item :label="t('ユーザー名')" name="name" :rules="[
            { required: true, whitespace: true, message: t('ユーザー名を入力してください。') },
            { max: 100, message: t('ユーザー名は100文字以内で入力してください。') },
          ]">
            <a-input v-model:value="form.name" autocomplete="name" :maxlength="100" />
          </a-form-item>
          <a-form-item :label="t('メールアドレス')" name="email" :rules="[
            { required: true, message: t('メールアドレスを入力してください。') },
            { type: 'email', message: t('正しいメールアドレスを入力してください。') },
            { max: 255, message: t('メールアドレスは255文字以内で入力してください。') },
          ]">
            <a-input v-model:value="form.email" type="email" autocomplete="email" :maxlength="255" />
          </a-form-item>
          <p class="profile-hint">{{ t('メールアドレスを変更すると、次回から新しいメールアドレスでログインします。') }}</p>
          <a-button type="primary" html-type="submit" :loading="saving" :disabled="!hasChanges || isBusy">{{ t('変更を保存') }}</a-button>
        </a-form>
        <section aria-labelledby="password-title">
          <a-typography-title id="password-title" :level="3">{{ t('パスワードの変更') }}</a-typography-title>
          <p class="password-hint">{{ t('6文字以上、UTF-8で72バイト以内で入力してください。変更後はすべての端末で再ログインが必要です。') }}</p>
          <a-alert v-if="passwordError" :message="t(passwordError)" type="error" show-icon role="alert" class="profile-error" />
          <a-form ref="passwordFormRef" :model="passwordForm" layout="vertical" :required-mark="false" :disabled="isBusy" aria-labelledby="password-title" @finish="changePassword" @validate="onPasswordValidate">
            <a-form-item :label="t('現在のパスワード')" name="currentPassword" :rules="[
              { required: true, whitespace: true, message: t('現在のパスワードを入力してください。') },
              { validator: validatePasswordBytes },
            ]">
              <a-input-password v-model:value="passwordForm.currentPassword" autocomplete="current-password" :maxlength="72" />
            </a-form-item>
            <a-form-item :label="t('新しいパスワード')" name="newPassword" :rules="[
              { required: true, whitespace: true, message: t('新しいパスワードを入力してください。') },
              { min: 6, message: t('新しいパスワードは6文字以上で入力してください。') },
              { validator: validatePasswordBytes },
            ]">
              <a-input-password v-model:value="passwordForm.newPassword" autocomplete="new-password" :maxlength="72" />
            </a-form-item>
            <a-form-item :label="t('新しいパスワード（確認）')" name="confirmPassword" :dependencies="['newPassword']" :rules="[
              { required: true, message: t('新しいパスワードをもう一度入力してください。') },
              { validator: validateConfirmation },
            ]">
              <a-input-password v-model:value="passwordForm.confirmPassword" autocomplete="new-password" :maxlength="72" />
            </a-form-item>
            <a-button type="primary" html-type="submit" :loading="changingPassword" :disabled="saving">{{ t('パスワードを変更') }}</a-button>
          </a-form>
        </section>
      </div>
      <a-divider />
      <div class="logout-row">
        <span>{{ t('この端末でのログインを終了します。') }}</span>
        <a-button :disabled="isBusy" @click="logout"><LogoutOutlined aria-hidden="true" />{{ t('ログアウト') }}</a-button>
      </div>
    </a-card>
  </section>
</template>

<style scoped>
.profile-page { width: 100%; max-width: 960px; margin: 0 auto; }
.profile-card { border-radius: 8px; }
.profile-form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 24px; }
.profile-form-grid > * { min-width: 0; }
.profile-form-grid :deep(.ant-form) { margin-top: 12px; }
.profile-form-grid > .ant-form { margin-top: 0; }
.profile-summary { display: flex; align-items: center; gap: 12px; margin-top: 12px; }
.profile-avatar { flex-shrink: 0; color: var(--app-primary-color); background: var(--app-soft-color); }
.profile-summary h3 { margin: 0 0 2px; font-size: 18px; overflow-wrap: anywhere; }
.profile-email { color: var(--app-muted-color); overflow-wrap: anywhere; }
.account-status { margin-top: 4px; }
.profile-error { margin-bottom: 12px; }
.profile-hint { margin: -4px 0 12px; color: var(--app-muted-color); font-size: 13px; }
.password-hint { margin: 0 0 12px; color: var(--app-muted-color); font-size: 13px; }
.logout-row { display: flex; align-items: center; justify-content: space-between; gap: 8px; color: var(--app-muted-color); }
@media (max-width: 767px) {
  .profile-form-grid { grid-template-columns: 1fr; gap: 16px; }
}
@media (max-width: 575px) {
  .logout-row { flex-direction: column; align-items: flex-start; }
}
</style>
