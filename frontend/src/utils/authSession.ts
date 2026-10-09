const TOKEN_KEY = 'shuttleup.accessToken'

// タブ内の再読み込みでログインを維持する。パスワードは保存しない。
export function getAccessToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function saveAccessToken(token: string): void {
  sessionStorage.setItem(TOKEN_KEY, token)
}

export function clearAccessToken(): void {
  sessionStorage.removeItem(TOKEN_KEY)
}

let onUnauthorized: (() => void) | undefined

export function setUnauthorizedHandler(handler: () => void): void {
  onUnauthorized = handler
}

export function notifyUnauthorized(): void {
  if (getAccessToken()) {
    clearAccessToken()
    onUnauthorized?.()
  }
}
