import { beforeEach, test } from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import ts from 'typescript'

// Load the real browser utility with the project's existing TypeScript compiler.
const source = await readFile(new URL('../src/utils/authSession.ts', import.meta.url), 'utf8')
const { outputText } = ts.transpileModule(source, {
  compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext },
})
const { isAccessTokenValid, saveAccessToken, getAccessToken, clearAccessToken, setUnauthorizedHandler, notifyUnauthorized, getBearerToken } =
  await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`)

const now = 1_791_900_000_000
const encode = value => Buffer.from(JSON.stringify(value)).toString('base64url')
const token = (payload = { exp: now / 1000 + 60 }, header = { alg: 'HS256' }) =>
  `${encode(header)}.${encode(payload)}.${Buffer.alloc(32, 7).toString('base64url')}`

beforeEach(() => {
  const storage = new Map()
  globalThis.sessionStorage = {
    getItem: key => storage.get(key) ?? null,
    setItem: (key, value) => storage.set(key, String(value)),
    removeItem: key => storage.delete(key),
  }
  saveAccessToken(token())
  clearAccessToken()
  setUnauthorizedHandler(() => {})
})

test('accepts a structurally valid unexpired JWT, including UTF-8 claims', () => {
  assert.equal(isAccessTokenValid(token({ exp: now / 1000 + 60, name: '東京' }), now), true)
})

test('rejects missing, malformed, undecodable and non-object JWTs', () => {
  for (const value of [null, '', 'abc', 'a.b.c.d', 'a.b.c', ' a.b.c', '!!.e30.signature',
    `${encode({ alg: 'HS256' })}.${Buffer.from('{broken').toString('base64url')}.c2ln`,
    `${encode({ alg: 'HS256' })}.${encode([])}.c2ln`]) {
    assert.equal(isAccessTokenValid(value, now), false, `Accepted ${value}`)
  }
})

test('requires numeric finite expiration and rejects the exact expiration instant', () => {
  for (const exp of [undefined, null, '1791900060', 0, now / 1000 - 1, now / 1000, Infinity]) {
    assert.equal(isAccessTokenValid(token({ exp }), now), false)
  }
  assert.equal(isAccessTokenValid(token({ exp: now / 1000 + 0.001 }), now), true)
})

test('rejects unsigned or unexpected algorithms', () => {
  for (const alg of [undefined, 'none', 'RS256', 'hs256']) {
    assert.equal(isAccessTokenValid(token(undefined, { alg }), now), false)
  }
})

test('honors nbf without requiring it on tokens that omit the claim', () => {
  assert.equal(isAccessTokenValid(token({ exp: now / 1000 + 60, nbf: now / 1000 }), now), true)
  for (const nbf of [now / 1000 + 1, '1791900000', null]) {
    assert.equal(isAccessTokenValid(token({ exp: now / 1000 + 60, nbf }), now), false)
  }
})

test('signature authentication remains the backend responsibility', () => {
  const tampered = token().replace(/[^.]+$/, Buffer.alloc(32, 8).toString('base64url'))
  assert.equal(isAccessTokenValid(tampered, now), true)
})

test('unauthorized responses clear the matching session and notify once', () => {
  const current = token()
  let calls = 0
  saveAccessToken(current)
  setUnauthorizedHandler(() => calls++)
  notifyUnauthorized(current)
  notifyUnauthorized(current)
  notifyUnauthorized(null)
  assert.equal(getAccessToken(), null)
  assert.equal(calls, 1)
})

test('a late unauthorized response cannot clear a newer login', () => {
  const oldToken = token()
  const newToken = token({ exp: now / 1000 + 120 })
  let calls = 0
  saveAccessToken(newToken)
  setUnauthorizedHandler(() => calls++)
  notifyUnauthorized(oldToken)
  assert.equal(getAccessToken(), newToken)
  assert.equal(calls, 0)
  notifyUnauthorized(newToken)
  saveAccessToken(oldToken)
  notifyUnauthorized(oldToken)
  assert.equal(calls, 2)
})

test('missing credentials can notify once, without a redirect loop', () => {
  let calls = 0
  setUnauthorizedHandler(() => calls++)
  notifyUnauthorized(null)
  notifyUnauthorized(null)
  assert.equal(calls, 1)
})

test('extracts the request token without treating absent authorization as a login', () => {
  assert.equal(getBearerToken(`Bearer ${token()}`), token())
  for (const value of [null, undefined, 123, 'Basic credentials']) {
    assert.equal(getBearerToken(value), null)
  }
})
