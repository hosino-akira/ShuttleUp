import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { reactive, ref } from 'vue'
import ts from 'typescript'

const vueUrl = new URL('../node_modules/vue/index.mjs', import.meta.url).href
const source = ts.transpileModule(await readFile(new URL('../src/composables/useExerciseFilters.ts', import.meta.url), 'utf8'), {
  compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext },
}).outputText.replace("from 'vue'", `from '${vueUrl}'`)
const { useExerciseFilters } = await import(`data:text/javascript;base64,${Buffer.from(source).toString('base64')}`)

function setup(exerciseId) {
  const types = ref([
    { id: 11, categoryId: 1, name: 'Strength' },
    { id: 12, categoryId: 1, name: 'Power' },
    { id: 21, categoryId: 2, name: 'Stroke' },
  ])
  const exercises = ref([
    { id: 101, categoryId: 1, exerciseTypeId: 11, name: 'Squat', systemPreset: true },
    { id: 102, categoryId: 1, exerciseTypeId: 12, name: 'Jump', systemPreset: true },
    { id: 201, categoryId: 2, exerciseTypeId: 21, name: 'Smash', systemPreset: true },
    { id: 202, categoryId: 2, exerciseTypeId: 21, name: 'Custom stroke', systemPreset: false },
  ])
  const form = reactive({ exerciseId })
  return { ...useExerciseFilters(types, exercises, form), form, exercises }
}
const ids = options => options.value.map(item => item.id)

test('all middle and small categories are available before a parent is selected', () => {
  const filters = setup()
  assert.deepEqual(ids(filters.filteredTypes), [11, 12, 21])
  assert.deepEqual(ids(filters.filteredExercises), [101, 102, 201, 202])
  filters.form.exerciseId = 201
  assert.equal(filters.categoryId.value, undefined)
  assert.equal(filters.exerciseTypeId.value, undefined)
  assert.equal(filters.form.exerciseId, 201)
})

test('a large category filters both middle and small categories without requiring a middle category', () => {
  const filters = setup()
  filters.handleCategoryChange(1)
  assert.deepEqual(ids(filters.filteredTypes), [11, 12])
  assert.deepEqual(ids(filters.filteredExercises), [101, 102])
  assert.equal(filters.exerciseTypeId.value, undefined)
  filters.form.exerciseId = 102
  assert.equal(filters.form.exerciseId, 102)
})

test('a middle category can be selected directly and filters its preset and custom exercises', () => {
  const filters = setup()
  filters.handleExerciseTypeChange(21)
  assert.equal(filters.categoryId.value, undefined)
  assert.deepEqual(ids(filters.filteredTypes), [11, 12, 21])
  assert.deepEqual(ids(filters.filteredExercises), [201, 202])
})

test('clearing filters restores available choices and retains compatible selections', () => {
  const filters = setup(201)
  filters.handleCategoryChange(2)
  filters.handleExerciseTypeChange(21)
  filters.handleCategoryChange(undefined)
  assert.equal(filters.exerciseTypeId.value, 21)
  assert.deepEqual(ids(filters.filteredTypes), [11, 12, 21])
  assert.deepEqual(ids(filters.filteredExercises), [201, 202])
  filters.handleExerciseTypeChange(undefined)
  assert.deepEqual(ids(filters.filteredExercises), [101, 102, 201, 202])
  assert.equal(filters.form.exerciseId, 201)
})

test('changing large categories clears incompatible middle and small selections', () => {
  const filters = setup(201)
  filters.handleExerciseTypeChange(21)
  filters.handleCategoryChange(1)
  assert.equal(filters.exerciseTypeId.value, undefined)
  assert.equal(filters.form.exerciseId, undefined)
  assert.deepEqual(ids(filters.filteredTypes), [11, 12])
  assert.deepEqual(ids(filters.filteredExercises), [101, 102])
})

test('compatible selections survive adding a parent filter, but changing middle categories clears an incompatible exercise', () => {
  const filters = setup(101)
  filters.handleExerciseTypeChange(11)
  filters.handleCategoryChange(1)
  assert.equal(filters.exerciseTypeId.value, 11)
  assert.equal(filters.form.exerciseId, 101)
  filters.handleExerciseTypeChange(12)
  assert.equal(filters.form.exerciseId, undefined)
  assert.deepEqual(ids(filters.filteredExercises), [102])
})

test('clearing only the middle filter retains the large filter and broadens small choices', () => {
  const filters = setup(101)
  filters.handleCategoryChange(1)
  filters.handleExerciseTypeChange(11)
  filters.handleExerciseTypeChange(undefined)
  assert.deepEqual(ids(filters.filteredExercises), [101, 102])
  assert.equal(filters.form.exerciseId, 101)
})

test('new custom exercises become selectable under their parent filters', () => {
  const filters = setup()
  filters.handleCategoryChange(2)
  filters.handleExerciseTypeChange(21)
  filters.exercises.value.push({ id: 203, categoryId: 2, exerciseTypeId: 21, name: 'New stroke', systemPreset: false })
  assert.deepEqual(ids(filters.filteredExercises), [201, 202, 203])
  filters.form.exerciseId = 203
  filters.handleCategoryChange(undefined)
  assert.equal(filters.form.exerciseId, 203)
})
