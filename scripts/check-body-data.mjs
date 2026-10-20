import { pathToFileURL } from 'node:url'

/** Read-only smoke check against the running backend, including the Vite proxy. */
export async function checkBodyData({ baseUrl, email, password }) {
  const originHeaders = new URL(baseUrl).port === '5173' ? { Origin: new URL(baseUrl).origin } : {}
  const login = await fetch(`${baseUrl}/api/auth/login`, {
    signal: AbortSignal.timeout(10000),
    method: 'POST', headers: { 'Content-Type': 'application/json', ...originHeaders },
    body: JSON.stringify({ email, password }),
  })
  if (login.status !== 200) throw new Error(`Login: HTTP ${login.status}`)
  const { accessToken } = await login.json()
  const response = await fetch(`${baseUrl}/api/auth/me/body-data`, {
    signal: AbortSignal.timeout(10000),
    headers: { Authorization: `Bearer ${accessToken}`, ...originHeaders },
  })
  if (response.status === 404) {
    throw new Error('Body data: HTTP 404. Restart the backend to load the current API.')
  }
  if (response.status !== 200) throw new Error(`Body data: HTTP ${response.status}`)
  const data = await response.json()
  for (const key of ['heightCm', 'bodyWeightKg', 'bodyFatPercentage']) {
    if (!Object.hasOwn(data, key) || (data[key] !== null && !Number.isFinite(data[key]))) {
      throw new Error(`Invalid body data response field: ${key}`)
    }
  }
  console.log(`PASS: ${baseUrl} authenticated body-data GET is HTTP 200`)
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const email = process.env.SHUTTLEUP_SMOKE_EMAIL
  const password = process.env.SHUTTLEUP_SMOKE_PASSWORD
  if (!email || !password) {
    console.error('Set SHUTTLEUP_SMOKE_EMAIL and SHUTTLEUP_SMOKE_PASSWORD for a test account. No data is modified.')
    process.exitCode = 1
  } else {
    try {
      await checkBodyData({ baseUrl: process.env.SHUTTLEUP_SMOKE_URL ?? 'http://127.0.0.1:5173', email, password })
    } catch (error) {
      console.error(error.message)
      process.exitCode = 1
    }
  }
}
