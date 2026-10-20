<script setup lang="ts">
import PageIntro from '../components/common/PageIntro.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { message, type FormInstance } from 'ant-design-vue'
import axios from 'axios'
import { t } from '../i18n'
import { useLocalizedForm } from '../composables/useLocalizedForm'
import { getMyBodyData, updateMyBodyData } from '../api/bodyDataApi'
import type { BodyData } from '../types/bodyData'
import { formatBmi } from '../utils/bodyData'

const bodyFormRef = ref<FormInstance>()
const { onValidate: onBodyValidate } = useLocalizedForm(bodyFormRef)
const savingBody = ref(false)
const loadingBody = ref(true)
const bodyError = ref('')
const savedBody = ref<BodyData | null>(null)
const bodyForm = reactive<BodyData>({ heightCm: null, bodyWeightKg: null, bodyFatPercentage: null })
const bmi = computed(() => formatBmi(bodyForm.heightCm, bodyForm.bodyWeightKg))
const hasBodyChanges = computed(() => savedBody.value !== null &&
  (Object.keys(bodyForm) as (keyof BodyData)[]).some(key => (bodyForm[key] ?? null) !== savedBody.value?.[key]))

async function loadBodyData(): Promise<void> {
  loadingBody.value = true
  bodyError.value = ''
  try {
    const data = await getMyBodyData()
    savedBody.value = { ...data }
    Object.assign(bodyForm, data)
  } catch (error: unknown) {
    if (!axios.isAxiosError(error) || error.response?.status !== 401) {
      bodyError.value = '身体データを読み込めませんでした。もう一度お試しください。'
    }
  } finally {
    loadingBody.value = false
  }
}

async function saveBodyData(): Promise<void> {
  if (savingBody.value || loadingBody.value || !savedBody.value) return
  savingBody.value = true
  bodyError.value = ''
  try {
    const data = await updateMyBodyData({
      heightCm: bodyForm.heightCm ?? null,
      bodyWeightKg: bodyForm.bodyWeightKg ?? null,
      bodyFatPercentage: bodyForm.bodyFatPercentage ?? null,
    })
    savedBody.value = { ...data }
    Object.assign(bodyForm, data)
    message.success(t('身体データを保存しました。'))
  } catch (error: unknown) {
    if (axios.isAxiosError<{ message?: string }>(error) && error.response?.status === 400) {
      bodyError.value = error.response.data.message ?? '入力内容を確認してください。'
    } else if (!axios.isAxiosError(error) || error.response?.status !== 401) {
      bodyError.value = '身体データを保存できませんでした。時間をおいて再度お試しください。'
    }
  } finally {
    savingBody.value = false
  }
}

onMounted(loadBodyData)
</script>

<template>
  <section class="body-data-page" aria-labelledby="body-data-title">
    <PageIntro title-id="body-data-title" :title="t('身体データ')" :description="t('各項目は任意です。空欄で保存すると、その値を削除します。')" />
    <a-card :bordered="false">
      <a-alert v-if="bodyError" :message="t(bodyError)" type="error" show-icon role="alert" class="body-data-error" />
      <a-button v-if="!savedBody && !loadingBody" @click="loadBodyData">{{ t('再読み込み') }}</a-button>
      <a-spin :spinning="loadingBody">
        <a-form ref="bodyFormRef" name="bodyData" :model="bodyForm" layout="vertical" :required-mark="false"
          :disabled="savingBody || loadingBody || !savedBody" aria-labelledby="body-data-title" @finish="saveBodyData" @validate="onBodyValidate">
          <div class="body-data-grid">
            <a-form-item :label="t('身長（cm）')" name="heightCm" :rules="[
              { type: 'number', min: 0.01, max: 999.99, message: t('身長は0.01〜999.99 cmで入力してください。') },
            ]">
              <a-input-number v-model:value="bodyForm.heightCm" :min="0.01" :max="999.99" :precision="2" :step="0.1" :placeholder="t('未入力')" />
            </a-form-item>
            <a-form-item :label="t('体重（kg）')" name="bodyWeightKg" :rules="[
              { type: 'number', min: 0.01, max: 999.99, message: t('体重は0.01〜999.99 kgで入力してください。') },
            ]">
              <a-input-number v-model:value="bodyForm.bodyWeightKg" :min="0.01" :max="999.99" :precision="2" :step="0.1" :placeholder="t('未入力')" />
            </a-form-item>
            <a-form-item :label="t('体脂肪率（%）')" name="bodyFatPercentage" :rules="[
              { type: 'number', min: 0, max: 100, message: t('体脂肪率は0〜100 %で入力してください。') },
            ]">
              <a-input-number v-model:value="bodyForm.bodyFatPercentage" :min="0" :max="100" :precision="2" :step="0.1" :placeholder="t('未入力')" />
            </a-form-item>
            <a-form-item>
              <template #label><span id="body-bmi-label">BMI</span></template>
              <output class="body-bmi" aria-labelledby="body-bmi-label" aria-live="polite">{{ bmi }}</output>
            </a-form-item>
          </div>
          <a-button type="primary" html-type="submit" :loading="savingBody"
            :disabled="!hasBodyChanges || savingBody || loadingBody">{{ t('身体データを保存') }}</a-button>
        </a-form>
      </a-spin>
    </a-card>
  </section>
</template>

<style scoped>
.body-data-page { display: flex; flex-direction: column; gap: var(--app-section-gap); min-width: 0; }
.body-data-error { margin-bottom: 12px; }
.body-data-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; }
.body-data-grid :deep(.ant-input-number) { width: 100%; }
.body-bmi { display: block; padding: 0; line-height: 32px; font-size: 18px; font-variant-numeric: tabular-nums; }
@media (max-width: 767px) {
  .body-data-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
