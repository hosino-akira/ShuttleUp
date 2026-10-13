import { computed, readonly, ref } from 'vue'
import { messages } from './messages'

export type Language = 'en' | 'zh' | 'ja'
export const LANGUAGE_STORAGE_KEY = 'shuttleup.language'
export const languages = [
  { value: 'en', label: 'English' },
  { value: 'zh', label: '中文' },
  { value: 'ja', label: '日本語' },
] as const

export function isLanguage(value: unknown): value is Language {
  return value === 'en' || value === 'zh' || value === 'ja'
}

function loadLanguage(): Language {
  try {
    const saved = localStorage.getItem(LANGUAGE_STORAGE_KEY)
    if (isLanguage(saved)) return saved
  } catch {
    // Language switching also works when browser storage is unavailable.
  }
  return 'ja'
}

const currentLanguage = ref<Language>(loadLanguage())
export const locale = readonly(currentLanguage)
export const intlLocale = computed(() => ({ en: 'en-US', zh: 'zh-CN', ja: 'ja-JP' } as const)[locale.value])

export function setLanguage(value: unknown): void {
  if (!isLanguage(value)) return
  currentLanguage.value = value
  if (typeof document !== 'undefined') document.documentElement.lang = value === 'zh' ? 'zh-CN' : value
  try {
    localStorage.setItem(LANGUAGE_STORAGE_KEY, value)
  } catch {
    // Keep the selected language for this session even if persistence is denied.
  }
}

/** Translate only interface text; never pass user-entered names or notes here. */
export function t(source: string, params: Record<string, unknown> = {}): string {
  const entry = Object.hasOwn(messages, source) ? messages[source] : undefined
  const translated = locale.value === 'ja' ? source : (entry?.[locale.value] ?? source)
  return translated.replace(/\{(\w+)\}/g, (placeholder, key: string) =>
    Object.hasOwn(params, key) ? String(params[key] ?? '') : placeholder,
  )
}

/** Parse date-only API values locally so time zones cannot shift the day. */
export function formatLocalDate(value: string, options: Intl.DateTimeFormatOptions = { year: 'numeric', month: 'short', day: 'numeric' }): string {
  const match = /^(\d{4})-(\d{2})(?:-(\d{2}))?$/.exec(value)
  if (!match) return value
  const date = new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3] ?? 1))
  if (Number.isNaN(date.getTime()) || date.getFullYear() !== Number(match[1])
    || date.getMonth() !== Number(match[2]) - 1 || date.getDate() !== Number(match[3] ?? 1)) return value
  return new Intl.DateTimeFormat(intlLocale.value, options).format(date)
}
