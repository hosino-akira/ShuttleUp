const TOKEN_KEY = 'shuttleup.accessToken'
let unauthorizedNotified = false

function readJwtJson(segment: string): Record<string, unknown> | null {
  const base64 = segment.replace(/-/g, '+').replace(/_/g, '/')
  const bytes = Uint8Array.from(atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')), char => char.charCodeAt(0))
  const value: unknown = JSON.parse(new TextDecoder('utf-8', { fatal: true }).decode(bytes))
  return typeof value === 'object' && value !== null && !Array.isArray(value)
    ? value as Record<string, unknown>
    : null
}

// ローカルでは形式と期限を確認する。署名・アカウント状態は /auth/me で検証する。
export function isAccessTokenValid(token: string | null, now = Date.now()): boolean {
  if (!token) return false
  const parts = token.split('.')
  if (parts.length !== 3 || parts.some(part => !/^[A-Za-z0-9_-]+$/.test(part) || part.length % 4 === 1)) return false
  try {
    const header = readJwtJson(parts[0]!)
    const payload = readJwtJson(parts[1]!)
    if (header?.alg !== 'HS256' || !payload) return false
    const expiresAt = payload.exp
    const notBefore = payload.nbf
    return typeof expiresAt === 'number' && Number.isFinite(expiresAt) && expiresAt > now / 1000
      && (notBefore === undefined || (typeof notBefore === 'number' && Number.isFinite(notBefore) && notBefore <= now / 1000))
  } catch {
    return false
  }
}

export function getBearerToken(authorization: unknown): string | null {
  return typeof authorization === 'string' && authorization.startsWith('Bearer ')
    ? authorization.slice(7)
    : null
}

// タブ内の再読み込みでログインを維持する。パスワードは保存しない。
export function getAccessToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function saveAccessToken(token: string): void {
  sessionStorage.setItem(TOKEN_KEY, token)
  unauthorizedNotified = false
}

export function clearAccessToken(): void {
  sessionStorage.removeItem(TOKEN_KEY)
}

let onUnauthorized: (() => void) | undefined

export function setUnauthorizedHandler(handler: () => void): void {
  onUnauthorized = handler
}

export function notifyUnauthorized(requestToken: string | null = getAccessToken()): void {
  // 古いリクエストの 401 で新しいログインを失効させない。
  if (requestToken !== getAccessToken() || unauthorizedNotified) return
  unauthorizedNotified = true
  clearAccessToken()
  onUnauthorized?.()
}
