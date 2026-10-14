import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { effectScope, nextTick, reactive } from 'vue'
import { compileScript, parse } from '@vue/compiler-sfc'
import ts from 'typescript'

const dataUrl = source => `data:text/javascript;base64,${Buffer.from(source).toString('base64')}`
const vueUrl = new URL('../node_modules/vue/index.mjs', import.meta.url).href
const apiUrl = dataUrl(`
  export const categories = [{ id: 1, name: 'Physical' }, { id: 2, name: 'Badminton' }];
  export const types = [{ id: 11, categoryId: 1, name: 'Strength' }, { id: 21, categoryId: 2, name: 'Stroke' }];
  export const requests = [];
  let handlers;
  export function reset(overrides = {}) {
    requests.length = 0;
    handlers = {
      categories: async () => categories,
      types: async id => types.filter(type => type.categoryId === id),
      create: async request => ({ id: 101, categoryId: 2, ...request }),
      ...overrides,
    };
  }
  export function getExerciseCategories() { return handlers.categories(); }
  export function getExerciseTypes(id) { return handlers.types(id); }
  export function createExercise(request) { requests.push(request); return handlers.create(request); }
  reset();
`)
const api = await import(apiUrl)
const helpersUrl = dataUrl(`
  export const errors = [];
  export const message = { error: value => errors.push(value), success: () => {} };
  export const t = value => value;
  export const useLocalizedForm = () => ({ onValidate() {}, reset() {} });
  export default {};
`)
const { descriptor } = parse(await readFile(new URL('../src/components/training/ExerciseCreateModal.vue', import.meta.url), 'utf8'))
const compiled = ts.transpileModule(compileScript(descriptor, { id: 'exercise-create-test' }).content, {
  compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext },
}).outputText.replace(/from ['"]([^'"]+)['"]/g, (_match, name) =>
  `from '${name === 'vue' ? vueUrl : name === '../../api/exerciseApi' ? apiUrl : helpersUrl}'`,
)
const { default: component } = await import(dataUrl(compiled))

async function flush() {
  await nextTick()
  for (let i = 0; i < 8; i++) await Promise.resolve()
  await nextTick()
}
function deferred() {
  let resolve
  const promise = new Promise(done => { resolve = done })
  return { promise, resolve }
}
function setup(t, initial = {}, handlers = {}) {
  api.reset(handlers)
  const props = reactive({ open: true, userId: 7, ...initial })
  const events = []
  const scope = effectScope()
  const state = scope.run(() => component.setup(props, { expose() {}, emit: (...event) => events.push(event) }))
  t.after(() => scope.stop())
  return { props, state, events }
}
const ids = state => state.typeOptions.value.map(option => option.value)

test('opening the real create modal loads middle categories without selecting a large category', async t => {
  const { state } = setup(t)
  await flush()
  assert.deepEqual(ids(state), [11, 21])
  assert.equal(state.form.categoryId, undefined)
  assert.equal(state.typesLoading.value, false)
})

test('a middle-only selection from the training record is carried into the create modal', async t => {
  const { state } = setup(t, { initialExerciseTypeId: 21 })
  await flush()
  assert.equal(state.form.categoryId, undefined)
  assert.equal(state.form.exerciseTypeId, 21)
  assert.deepEqual(ids(state), [11, 21])
})

test('changing and clearing the large filter removes only incompatible middle selections', async t => {
  const { state } = setup(t, { initialExerciseTypeId: 21 })
  await flush()
  state.handleCategoryChange(2)
  assert.equal(state.form.exerciseTypeId, 21)
  assert.deepEqual(ids(state), [21])
  state.handleCategoryChange(undefined)
  assert.equal(state.form.exerciseTypeId, 21)
  assert.deepEqual(ids(state), [11, 21])
  state.handleCategoryChange(1)
  assert.equal(state.form.exerciseTypeId, undefined)
  assert.deepEqual(ids(state), [11])
})

test('clearing the large category while data is loading does not restore a stale filter', async t => {
  const pending = deferred()
  const { state } = setup(t, { initialCategoryId: 1 }, { types: id => pending.promise.then(types => types.filter(type => type.categoryId === id)) })
  await flush()
  state.handleCategoryChange(2)
  state.handleCategoryChange(undefined)
  pending.resolve(api.types)
  await flush()
  assert.equal(state.form.categoryId, undefined)
  assert.deepEqual(ids(state), [11, 21])
})

test('closing and reopening ignores an older category response and preserves the new initial selection', async t => {
  const oldRequest = deferred()
  let calls = 0
  const { state, props } = setup(t, {}, { categories: () => ++calls === 1 ? oldRequest.promise : Promise.resolve(api.categories) })
  await flush()
  props.open = false
  await flush()
  props.initialExerciseTypeId = 21
  props.open = true
  await flush()
  oldRequest.resolve([{ id: 99, name: 'Stale' }])
  await flush()
  assert.deepEqual(state.categoryOptions.value.map(option => option.value), [1, 2])
  assert.deepEqual(ids(state), [11, 21])
  assert.equal(state.form.exerciseTypeId, 21)
  assert.equal(state.categoriesLoading.value, false)
  assert.equal(state.typesLoading.value, false)
})

test('closing and reopening ignores older middle-category responses', async t => {
  const oldRequest = deferred()
  let calls = 0
  const { state, props } = setup(t, {}, { types: id => ++calls <= 2 ? oldRequest.promise : Promise.resolve(api.types.filter(type => type.categoryId === id)) })
  await flush()
  props.open = false
  await flush()
  props.open = true
  await flush()
  oldRequest.resolve([{ id: 99, categoryId: 1, name: 'Stale' }])
  await flush()
  assert.deepEqual(ids(state), [11, 21])
  assert.equal(state.typesLoading.value, false)
})

test('a middle selection without a large category can create a named exercise', async t => {
  const { state, events } = setup(t, { initialExerciseTypeId: 21 })
  await flush()
  state.formRef.value = { validate: async () => {} }
  state.form.name = '  Custom stroke  '
  await state.submit()
  assert.deepEqual(api.requests, [{ exerciseTypeId: 21, name: 'Custom stroke', userId: 7 }])
  assert.ok(events.some(event => event[0] === 'created' && event[1].id === 101))
  assert.ok(events.some(event => event[0] === 'update:open' && event[1] === false))
})

test('loading failure clears loading state and reopening retries the catalog', async t => {
  t.mock.method(console, 'error', () => {})
  let calls = 0
  const { state, props } = setup(t, {}, { categories: async () => {
    if (++calls === 1) throw new Error('Unavailable')
    return api.categories
  } })
  await flush()
  assert.equal(state.categoriesLoading.value, false)
  assert.equal(state.typesLoading.value, false)
  props.open = false
  await flush()
  props.open = true
  await flush()
  assert.deepEqual(ids(state), [11, 21])
})

const popupSource = ts.transpileModule(await readFile(new URL('../src/utils/selectPopupContainer.ts', import.meta.url), 'utf8'), {
  compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext },
}).outputText
const { selectPopupContainer } = await import(dataUrl(popupSource))
test('select popups stay in the modal or drawer stacking context and outside its clipping content', () => {
  const body = {}
  for (const container of [{ className: 'ant-modal' }, { className: 'ant-drawer' }, null]) {
    const trigger = { ownerDocument: { body }, closest(selector) {
      assert.equal(selector, '.ant-modal, .ant-drawer')
      return container
    } }
    assert.equal(selectPopupContainer(trigger), container ?? body)
  }
})
