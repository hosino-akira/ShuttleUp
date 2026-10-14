<script setup lang="ts">
import { t } from '../../i18n'
import ShuttleBrand from '../common/ShuttleBrand.vue'
import BadmintonIcon from '../common/BadmintonIcon.vue'
import CourtLines from '../common/CourtLines.vue'
import LanguageSwitcher from '../common/LanguageSwitcher.vue'
import ThemeSwitcher from '../common/ThemeSwitcher.vue'
</script>

<template>
  <main class="auth-page">
    <div class="auth-container">
      <header class="auth-toolbar">
        <RouterLink class="auth-brand-link" :to="{ name: 'login' }" aria-label="ShuttleUp"><ShuttleBrand /></RouterLink>
        <div class="auth-preferences"><ThemeSwitcher /><LanguageSwitcher /></div>
      </header>
      <div class="auth-frame">
        <aside class="auth-hero" :aria-label="t('バドミントントレーニングノート')">
          <img src="/images/badminton-court-hero.webp" alt="" class="auth-hero-image" width="1024" height="1536" fetchpriority="high" />
          <div class="hero-copy">
            <span class="hero-eyebrow"><BadmintonIcon :size="18" /> {{ t('バドミントントレーニングノート') }}</span>
            <h2>{{ t('一球ごとの努力を、') }}<br />{{ t('成長の記録に。') }}</h2>
            <p>{{ t('練習も、試合も、成長も。あなたのバドミントンをひとつに。') }}</p>
          </div>
          <div class="hero-footer">
            <CourtLines class="hero-court" />
            <div><span>{{ t('記録する。振り返る。強くなる。') }}</span><small>ShuttleUp · {{ t('あなたのコートサイドパートナー') }}</small></div>
          </div>
        </aside>
        <div class="auth-form-panel">
          <div class="form-eyebrow"><span></span>{{ t('次の一球への準備を。') }}</div>
          <slot />
          <div class="auth-features">
            <span><BadmintonIcon kind="racket" :size="17" />{{ t('練習を記録') }}</span>
            <span><BadmintonIcon kind="court" :size="17" />{{ t('試合を振り返る') }}</span>
            <span><BadmintonIcon :size="17" />{{ t('成長を育てる') }}</span>
          </div>
        </div>
      </div>
      <footer class="auth-page-footer">{{ t('小さな積み重ねが、次の一球を変える。') }}</footer>
    </div>
  </main>
</template>

<style scoped>
.auth-page { height: 100%; overflow-y: auto; padding: 28px 32px; background: radial-gradient(ellipse at 0 100%, var(--app-selected-color), transparent 52%), var(--app-page-color); }
.auth-container { width: 100%; max-width: 1120px; margin: auto; }
.auth-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 0 4px 24px; }
.auth-brand-link { text-decoration: none; }
.auth-brand-link:focus-visible { outline: 2px solid var(--app-primary-color); outline-offset: 5px; border-radius: 8px; }
.auth-preferences { display: flex; gap: 10px; flex-wrap: wrap; justify-content: end; }
.auth-frame { display: grid; grid-template-columns: 1.03fr 1fr; overflow: hidden; border: 1px solid var(--app-border-color); border-radius: 24px; background: var(--app-surface-color); box-shadow: 0 18px 60px color-mix(in srgb, var(--app-heading-color) 9%, transparent); }
.auth-hero { position: relative; min-height: clamp(560px, calc(100dvh - 154px), 650px); display: flex; flex-direction: column; justify-content: space-between; isolation: isolate; overflow: hidden; color: #fff; background: #173d33; padding: 48px 38px 30px; }
.auth-hero-image { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; object-position: center 62%; z-index: -2; }
.auth-hero::after { content: ''; position: absolute; inset: 0; z-index: -1; background: linear-gradient(180deg, rgb(8 32 26 / 84%) 0%, rgb(8 32 26 / 45%) 35%, rgb(8 32 26 / 3%) 65%, rgb(8 32 26 / 75%) 100%); }
.hero-eyebrow { display: inline-flex; align-items: center; gap: 8px; color: #d6eac2; font-size: 11px; letter-spacing: .09em; border: 1px solid rgb(255 255 255 / 25%); padding: 7px 11px; border-radius: 30px; }
.hero-copy h2 { margin: 24px 0 18px; color: #fff; font-size: clamp(29px, 3.2vw, 42px); font-weight: 650; line-height: 1.45; letter-spacing: -.04em; }
.hero-copy p { max-width: 310px; margin: 0; font-size: 14px; line-height: 1.9; color: #e4eee5; }
.hero-footer { display: flex; gap: 14px; align-items: center; padding-top: 40px; }
.hero-court { width: 63px; height: 40px; color: #d6eac2; opacity: .75; flex-shrink: 0; }
.hero-footer span { display: block; font-size: 13px; letter-spacing: .04em; }
.hero-footer small { display: block; margin-top: 6px; color: #c8d7cd; font-size: 10px; }
.auth-form-panel { align-self: stretch; display: flex; flex-direction: column; justify-content: center; min-width: 0; padding: 46px 48px 32px; }
.form-eyebrow { display: flex; align-items: center; gap: 7px; font-size: 11px; color: var(--app-muted-color); letter-spacing: .08em; margin-bottom: 20px; }
.form-eyebrow > span { width: 6px; height: 6px; border-radius: 50%; background: var(--app-accent-color); }
.auth-features { display: flex; flex-wrap: wrap; gap: 9px 15px; margin-top: 30px; padding-top: 20px; border-top: 1px solid var(--app-border-color); color: var(--app-muted-color); font-size: 10px; }
.auth-features > span { display: flex; align-items: center; gap: 5px; }
.auth-page-footer { text-align: center; color: var(--app-muted-color); font-size: 11px; margin-top: 18px; }
@media (min-height: 850px) and (min-width: 761px) { .auth-container { padding-top: 3vh; } }
@media (max-width: 960px) { .auth-page { padding: 22px; } .auth-form-panel { padding: 36px 30px 28px; } .auth-hero { padding-inline: 28px; } }
@media (max-width: 760px) { .auth-page { padding: 16px; } .auth-frame { grid-template-columns: 1fr; border-radius: 19px; } .auth-hero { min-height: 245px; padding: 25px; } .auth-hero-image { object-position: center 55%; } .hero-copy h2 { font-size: 27px; line-height: 1.3; margin: 14px 0 10px; } .hero-copy p { max-width: 300px; font-size: 12px; } .hero-footer { display: none; } .auth-form-panel { padding: 28px 26px; } .form-eyebrow { margin-bottom: 15px; } .auth-toolbar { align-items: flex-start; padding-bottom: 18px; } .auth-features { margin-top: 24px; } }
@media (max-width: 380px) { .auth-page { padding: 10px; } .auth-toolbar { flex-wrap: wrap; gap: 12px; } .auth-preferences { gap: 6px; } .auth-hero { padding: 20px; } .auth-form-panel { padding: 25px 20px; } .hero-eyebrow { font-size: 10px; } }
</style>
