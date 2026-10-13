import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFile, readdir } from 'node:fs/promises'
import ts from 'typescript'
import { computed, effectScope, nextTick, ref } from 'vue'
import { parse } from '@vue/compiler-sfc'

// Exercise the real modules without adding a test framework or browser dependency.
const transpile = source => ts.transpileModule(source, {
  compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext },
}).outputText
const dataUrl = source => `data:text/javascript;base64,${Buffer.from(source).toString('base64')}`
const messagesUrl = dataUrl(transpile(await readFile(new URL('../src/i18n/messages.ts', import.meta.url), 'utf8')))
const vueUrl = new URL('../node_modules/vue/index.mjs', import.meta.url).href
const coreSource = transpile(await readFile(new URL('../src/i18n/index.ts', import.meta.url), 'utf8'))
  .replace("from 'vue'", `from '${vueUrl}'`).replace("from './messages'", `from '${messagesUrl}'`)
let instanceId = 0
async function loadCore(saved, denied = false) {
  const storage = new Map(saved === undefined ? [] : [['shuttleup.language', saved]])
  globalThis.localStorage = {
    getItem: key => { if (denied) throw new Error('Storage denied'); return storage.get(key) ?? null },
    setItem: (key, value) => { if (denied) throw new Error('Storage denied'); storage.set(key, value) },
  }
  globalThis.document = { documentElement: { lang: '' } }
  const url = dataUrl(coreSource + `\n// instance ${++instanceId}`)
  return { core: await import(url), storage, url }
}

test('defaults to Japanese and restores only supported saved languages', async () => {
  for (const [saved, expected] of [[undefined, 'ja'], ['en', 'en'], ['zh', 'zh'], ['ja', 'ja'], ['invalid', 'ja']]) {
    assert.equal((await loadCore(saved)).core.locale.value, expected)
  }
})

test('switches reactively, updates HTML language, and persists through reload', async () => {
  const { core, storage } = await loadCore()
  const label = computed(() => core.t('ログイン'))
  assert.equal(label.value, 'ログイン')
  core.setLanguage('en')
  assert.equal(label.value, 'Sign in')
  assert.equal(document.documentElement.lang, 'en')
  core.setLanguage('zh')
  assert.equal(label.value, '登录')
  assert.equal(document.documentElement.lang, 'zh-CN')
  assert.equal(storage.get(core.LANGUAGE_STORAGE_KEY), 'zh')
  assert.equal((await loadCore(storage.get(core.LANGUAGE_STORAGE_KEY))).core.locale.value, 'zh')
})

test('rejects unsupported language choices and works without browser storage', async () => {
  const { core } = await loadCore(undefined, true)
  core.setLanguage('en')
  assert.equal(core.t('保存'), 'Save')
  for (const invalid of [null, 'fr', '__proto__', 1]) core.setLanguage(invalid)
  assert.equal(core.locale.value, 'en')
})

test('interpolates parameters once and preserves chart placeholders and unknown content', async () => {
  const { core } = await loadCore('en')
  assert.equal(core.t('＋「{0}」を新規登録', { 0: '山田 {1}' }), '+ Create "山田 {1}"')
  assert.equal(core.t('おかえりなさい、{name}さん', { name: '李' }), 'Welcome back, 李')
  assert.equal(core.t('{b}：{c}回（{d}%）'), '{b}: {c} ({d}%)')
  assert.equal(core.t('さん'), '')
  assert.equal(core.t('Unknown source'), 'Unknown source')
  assert.equal(core.t('toString'), 'toString')
})

test('formats date-only values and months in the selected locale without a UTC day shift', async () => {
  const { core } = await loadCore('en')
  assert.equal(core.formatLocalDate('2026-10-13'), 'Oct 13, 2026')
  core.setLanguage('ja')
  assert.equal(core.formatLocalDate('2026-10-13'), '2026年10月13日')
  core.setLanguage('zh')
  assert.equal(core.formatLocalDate('2026-10-13'), '2026年10月13日')
  assert.equal(core.formatLocalDate('2026-10', { year: 'numeric', month: 'short' }), '2026年10月')
  assert.equal(core.formatLocalDate('invalid'), 'invalid')
  assert.equal(core.formatLocalDate('2026-02-30'), '2026-02-30')
})

test('every frontend translation key has both English and Chinese text and matching parameters', async () => {
  const { messages } = await import(messagesUrl)
  const keys = new Set()
  async function walk(dir) {
    for (const entry of await readdir(dir, { withFileTypes: true })) {
      const file = new URL(entry.name + (entry.isDirectory() ? '/' : ''), dir)
      if (entry.isDirectory()) await walk(file)
      else if (/\.(vue|ts)$/.test(entry.name) && entry.name !== 'messages.ts') {
        const source = await readFile(file, 'utf8')
        if (entry.name.endsWith('.vue')) {
          const { descriptor } = parse(source)
          const hasJapanese = value => /[\u3040-\u30ff\u3400-\u9fff]/u.test(value)
          function checkTemplate(node) {
            if (node.type === 2) assert.ok(!hasJapanese(node.content), `Untranslated text in ${file}: ${node.content}`)
            for (const prop of node.props ?? []) {
              if (prop.type === 6 && prop.value) assert.ok(!hasJapanese(prop.value.content), `Untranslated attribute in ${file}: ${prop.name}`)
            }
            for (const child of node.children ?? []) checkTemplate(child)
          }
          if (descriptor.template) checkTemplate(descriptor.template.ast)
        }
        for (const match of source.matchAll(/\bt\('((?:\\.|[^'\\])*)'/g)) {
          keys.add(match[1].replace(/\\n/g, '\n').replace(/\\'/g, "'").replace(/\\\\/g, '\\'))
        }
      }
    }
  }
  await walk(new URL('../src/', import.meta.url))
  const parameters = value => [...value.matchAll(/\{(\w+)\}/g)].map(match => match[1]).sort()
  for (const key of keys) assert.ok(Object.hasOwn(messages, key), `Missing translation: ${key}`)
  for (const [key, translations] of Object.entries(messages)) {
    for (const language of ['en', 'zh']) {
      assert.equal(typeof translations[language], 'string', `${key}: ${language}`)
      assert.deepEqual(parameters(translations[language]), parameters(key), `${key}: ${language} parameters`)
    }
  }
})

test('switching language revalidates only existing invalid fields without submitting or altering values', async () => {
  const { core, url } = await loadCore()
  const source = transpile(await readFile(new URL('../src/composables/useLocalizedForm.ts', import.meta.url), 'utf8'))
    .replace("from 'vue'", `from '${vueUrl}'`).replace("from '../i18n'", `from '${url}'`)
  const { useLocalizedForm } = await import(dataUrl(source))
  const scope = effectScope()
  const calls = []
  const form = ref({ validateFields: async names => { calls.push(names); throw new Error('Validation failed') } })
  const validation = scope.run(() => useLocalizedForm(form))
  validation.onValidate('email', false)
  validation.onValidate('password', true)
  core.setLanguage('en')
  await nextTick()
  await nextTick()
  assert.deepEqual(calls, [['email']])
  validation.reset()
  core.setLanguage('zh')
  await nextTick()
  await nextTick()
  assert.equal(calls.length, 1)
  scope.stop()
})
