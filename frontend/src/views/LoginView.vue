<script setup lang="ts">
import { reactive } from "vue";
import { message } from "ant-design-vue";

const form = reactive({
  email: "",
  password: "",
});

function handleSubmit(): void {
  // 現段階では入力確認のみ。認証 API は次のステップで接続する。
  message.info("入力内容を確認しました。");
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

      <a-form
        :model="form"
        layout="vertical"
        :required-mark="false"
        size="large"
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

        <a-button type="primary" html-type="submit" block>
          ログイン
        </a-button>
      </a-form>
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

@media (max-width: 575px) {
  .login-page {
    padding: 16px;
  }

  .login-card {
    padding: 24px;
  }
}
</style>
