import { computed, readonly, ref } from 'vue'

export const THEME_STORAGE_KEY = 'shuttleup.theme'
export const themeOptions = [
  { value: 'forest', label: 'フォレストグリーン' },
  { value: 'ocean', label: 'オーシャンブルー' },
  { value: 'sakura', label: 'サクラピンク' },
  { value: 'lavender', label: 'ラベンダーパープル' },
  { value: 'sand', label: 'サンドゴールド' },
] as const
export type ThemeId = typeof themeOptions[number]['value']

interface Palette {
  primary: string
  hover: string
  active: string
  heading: string
  text: string
  muted: string
  border: string
  surface: string
  elevated: string
  page: string
  soft: string
  selected: string
  accent: string
  accentAlt: string
  neutral: string
}

export const palettes: Record<ThemeId, Palette> = {
  forest: { primary: '#527040', hover: '#47643b', active: '#39532e', heading: '#344d35', text: '#40543b', muted: '#65725d', border: '#d9e2cc', surface: '#f9fbf3', elevated: '#fffef8', page: '#eef3e4', soft: '#e5edda', selected: '#dce8cf', accent: '#819667', accentAlt: '#be9760', neutral: '#9ca88e' },
  ocean: { primary: '#326e93', hover: '#285c7d', active: '#1f4c6a', heading: '#23445b', text: '#36566b', muted: '#5b7487', border: '#cfdee7', surface: '#f3f8fc', elevated: '#fafdff', page: '#eaf2f8', soft: '#e0edf5', selected: '#d3e5f1', accent: '#539b9c', accentAlt: '#9589b3', neutral: '#96a8b6' },
  sakura: { primary: '#974d68', hover: '#86415b', active: '#74354e', heading: '#63394b', text: '#6a4554', muted: '#85616f', border: '#ead5df', surface: '#fcf5f8', elevated: '#fffafd', page: '#f6ecf1', soft: '#f2e1e9', selected: '#ebd2df', accent: '#b184a4', accentAlt: '#c59a68', neutral: '#a998a0' },
  lavender: { primary: '#6e5a97', hover: '#625086', active: '#504072', heading: '#493c65', text: '#554969', muted: '#756584', border: '#ded5eb', surface: '#f8f5fc', elevated: '#fdfbff', page: '#efeaf7', soft: '#e8e1f2', selected: '#ded3ed', accent: '#9097bb', accentAlt: '#b491ba', neutral: '#a69db3' },
  sand: { primary: '#895c2c', hover: '#805528', active: '#6a4520', heading: '#5c452b', text: '#68533a', muted: '#7c6a53', border: '#e7dbc9', surface: '#fcf8ef', elevated: '#fffdf8', page: '#f5efe2', soft: '#eee4cf', selected: '#e6d6b9', accent: '#aa9151', accentAlt: '#a57561', neutral: '#aea28c' },
}

export function isThemeId(value: unknown): value is ThemeId {
  return typeof value === 'string' && themeOptions.some(option => option.value === value)
}
function loadTheme(): ThemeId {
  try {
    const saved = localStorage.getItem(THEME_STORAGE_KEY)
    if (isThemeId(saved)) return saved
  } catch { /* Theme switching works when browser storage is unavailable. */ }
  return 'forest'
}
const selectedTheme = ref<ThemeId>(loadTheme())
export const currentTheme = readonly(selectedTheme)
export const palette = computed(() => palettes[selectedTheme.value])
export const chartColors = computed(() => [palette.value.primary, palette.value.accent, palette.value.accentAlt, palette.value.neutral])

function applyTheme(): void {
  if (typeof document === 'undefined') return
  const root = document.documentElement
  root.dataset.theme = selectedTheme.value
  const colors = palette.value
  const variables: Record<string, string> = {
    'primary-color': colors.primary, 'primary-hover': colors.hover, 'primary-active': colors.active,
    'heading-color': colors.heading, 'text-color': colors.text, 'muted-color': colors.muted,
    'border-color': colors.border, 'surface-color': colors.surface, 'elevated-color': colors.elevated,
    'page-color': colors.page, 'soft-color': colors.soft, 'selected-color': colors.selected,
    'accent-color': colors.accent, 'accent-alt-color': colors.accentAlt, 'neutral-color': colors.neutral,
  }
  for (const [name, value] of Object.entries(variables)) root.style.setProperty(`--app-${name}`, value)
}
export function initializeTheme(): void { applyTheme() }
export function setTheme(value: unknown): void {
  if (!isThemeId(value)) return
  selectedTheme.value = value
  applyTheme()
  try { localStorage.setItem(THEME_STORAGE_KEY, value) } catch { /* Keep this session's selection. */ }
}
