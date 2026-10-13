import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import ts from 'typescript'
import { computed } from 'vue'

const transpile = source => ts.transpileModule(source, {
  compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext },
}).outputText
const dataUrl = source => `data:text/javascript;base64,${Buffer.from(source).toString('base64')}`
const vueUrl = new URL('../node_modules/vue/index.mjs', import.meta.url).href
const source = transpile(await readFile(new URL('../src/theme/index.ts', import.meta.url), 'utf8')).replace("from 'vue'", `from '${vueUrl}'`)
let instance = 0
async function loadTheme(saved, denied = false) {
  const storage = new Map(saved === undefined ? [] : [['shuttleup.theme', saved]])
  const styles = new Map()
  globalThis.localStorage = {
    getItem: key => { if (denied) throw new Error('Storage unavailable'); return storage.get(key) ?? null },
    setItem: (key, value) => { if (denied) throw new Error('Storage unavailable'); storage.set(key, value) },
  }
  globalThis.document = { documentElement: { dataset: {}, style: { setProperty: (key, value) => styles.set(key, value) } } }
  return { theme: await import(dataUrl(source + `\n// instance ${++instance}`)), storage, styles }
}

test('starts in forest green and restores only supported saved themes', async () => {
  for (const [saved, expected] of [[undefined, 'forest'], ['ocean', 'ocean'], ['sakura', 'sakura'], ['lavender', 'lavender'], ['sand', 'sand'], ['__proto__', 'forest']]) {
    const { theme, styles } = await loadTheme(saved)
    theme.initializeTheme()
    assert.equal(theme.currentTheme.value, expected)
    assert.equal(document.documentElement.dataset.theme, expected)
    assert.equal(styles.get('--app-primary-color'), theme.palettes[expected].primary)
  }
})

test('changing themes updates charts and global colors together and survives reload', async () => {
  const { theme, styles, storage } = await loadTheme()
  const chartColor = computed(() => theme.chartColors.value[0])
  for (const { value } of theme.themeOptions) {
    theme.setTheme(value)
    assert.equal(chartColor.value, theme.palettes[value].primary)
    assert.equal(styles.get('--app-surface-color'), theme.palettes[value].surface)
    assert.equal(styles.get('--app-primary-hover'), theme.palettes[value].hover)
    assert.equal(storage.get(theme.THEME_STORAGE_KEY), value)
    assert.equal(document.documentElement.dataset.theme, value)
  }
  assert.equal((await loadTheme(storage.get(theme.THEME_STORAGE_KEY))).theme.currentTheme.value, 'sand')
})

test('invalid themes are ignored and switching still works when storage is denied', async () => {
  const { theme, styles } = await loadTheme(undefined, true)
  theme.setTheme('sakura')
  for (const value of [null, '__proto__', 'dark', 1, ['ocean']]) theme.setTheme(value)
  assert.equal(theme.currentTheme.value, 'sakura')
  assert.equal(styles.get('--app-primary-color'), theme.palettes.sakura.primary)
})

test('all themes have translated names and readable text and buttons', async () => {
  const { theme } = await loadTheme()
  const { messages } = await import(dataUrl(transpile(await readFile(new URL('../src/i18n/messages.ts', import.meta.url), 'utf8'))))
  function luminance(hex) {
    const channels = hex.slice(1).match(/../g).map(channel => parseInt(channel, 16) / 255)
      .map(value => value <= .04045 ? value / 12.92 : ((value + .055) / 1.055) ** 2.4)
    return channels[0] * .2126 + channels[1] * .7152 + channels[2] * .0722
  }
  const failures = []
  for (const option of theme.themeOptions) {
    assert.ok(messages[option.label]?.en)
    assert.ok(messages[option.label]?.zh)
    const colors = theme.palettes[option.value]
    for (const state of ['primary', 'hover', 'active']) {
      const contrast = 1.05 / (luminance(colors[state]) + .05)
      if (contrast < 4.5) failures.push(`${option.value} ${state}: white text contrast ${contrast}`)
    }
    for (const role of ['text', 'muted']) {
      const contrast = (luminance(colors.surface) + .05) / (luminance(colors[role]) + .05)
      if (contrast < 4.5) failures.push(`${option.value}: ${role} contrast ${contrast}`)
    }
    const buttonContrast = (luminance(colors.soft) + .05) / (luminance(colors.primary) + .05)
    if (buttonContrast < 4.5) failures.push(`${option.value}: secondary button contrast ${buttonContrast}`)
  }
  assert.deepEqual(failures, [])
})
