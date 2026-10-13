import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import ts from 'typescript'

const source = ts.transpileModule(await readFile(new URL('../src/utils/forest.ts', import.meta.url), 'utf8'), {
  compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext },
}).outputText
const { forestSessions, getPlantType, isPlantType, localDateKey, plantTypes, trainingStreak } =
  await import(`data:text/javascript;base64,${Buffer.from(source).toString('base64')}`)
const session = (id, trainingDate, plantType) => ({ id, trainingDate, plantType, durationMinutes: 60 })

test('past and current sessions each produce one plant; future plans stay out of the forest', () => {
  const input = [session(3, '2026-10-13', 'sakura'), session(4, '2026-10-14'), session(2, '2026-10-12'), session(1, '2026-10-12')]
  assert.deepEqual(forestSessions(input, '2026-10-13').map(s => s.id), [1, 2, 3])
  assert.equal(input[0].id, 3, 'sorting does not modify the fetched sessions')
  assert.equal(forestSessions(input.map(s => s.id === 3 ? { ...s, durationMinutes: 90, plantType: 'oak' } : s), '2026-10-13').length, 3)
  assert.equal(forestSessions(input.filter(s => s.id !== 3), '2026-10-13').length, 2)
  assert.equal(forestSessions(input, '2026-10-14').length, 4)
})

test('all selectable species survive round trips; legacy or invalid species fall back to cedar', () => {
  for (const type of plantTypes) {
    assert.equal(isPlantType(type), true)
    assert.equal(getPlantType(session(1, '2026-10-13', type)), type)
  }
  for (const type of [null, undefined, '', 'unknown', '__proto__', ['oak']]) {
    assert.equal(isPlantType(type), false)
    assert.equal(getPlantType(session(1, '2026-10-13', type)), 'cedar')
  }
})

test('streak counts unique days across month boundaries and allows today to be unfinished', () => {
  const sessions = [session(1, '2026-09-30'), session(2, '2026-10-01'), session(3, '2026-10-01')]
  assert.equal(trainingStreak(sessions, '2026-10-01'), 2)
  assert.equal(trainingStreak(sessions, '2026-10-02'), 2)
  assert.equal(trainingStreak(sessions, '2026-10-03'), 0)
  assert.equal(trainingStreak([], '2026-10-01'), 0)
  assert.equal(trainingStreak([session(1, '2028-02-28'), session(2, '2028-02-29'), session(3, '2028-03-01')], '2028-03-01'), 3)
  assert.equal(localDateKey(new Date(2026, 9, 13)), '2026-10-13')
})
