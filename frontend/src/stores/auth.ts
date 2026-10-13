import { t } from '../i18n';
import { ref } from 'vue'
import { defineStore } from 'pinia'
import axios from 'axios'
import { getMyProfile, loginUser, updateMyProfile } from '../api/authApi'
import type { LoginRequest, ProfileUpdateRequest, UserResponse } from '../types/auth'
import { clearAccessToken, getAccessToken, isAccessTokenValid, saveAccessToken } from '../utils/authSession'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserResponse | null>(null)
  const sessionVerificationFailed = ref(false)
  let sessionRevision = 0
  let restoring: { token: string; revision: number; promise: Promise<boolean> } | null = null

  async function login(request: LoginRequest): Promise<void> {
    const revision = ++sessionRevision
    const result = await loginUser(request)
    if (revision !== sessionRevision || result.tokenType !== 'Bearer'
        || !isAccessTokenValid(result.accessToken) || result.user.status !== 'ACTIVE') {
      throw new Error(t('ログイン情報を確認できませんでした。'))
    }
    saveAccessToken(result.accessToken)
    user.value = result.user
    sessionVerificationFailed.value = false
  }

  function logout(): void {
    sessionRevision++
    clearAccessToken()
    user.value = null
    sessionVerificationFailed.value = false
    restoring = null
  }

  async function restoreSession(): Promise<boolean> {
    const token = getAccessToken()
    if (!token || !isAccessTokenValid(token)) {
      logout()
      return false
    }
    const revision = sessionRevision
    if (restoring?.token === token && restoring.revision === revision) return restoring.promise
    sessionVerificationFailed.value = false
    const isCurrentSession = () => revision === sessionRevision && getAccessToken() === token
    const promise = getMyProfile().then(profile => {
      if (!isCurrentSession()) return false
      if (!isAccessTokenValid(token) || profile.status !== 'ACTIVE') {
        logout()
        return false
      }
      user.value = profile
      return true
    }).catch((error: unknown) => {
      if (isCurrentSession()) {
        if (!isAccessTokenValid(token) || (axios.isAxiosError(error) && [401, 403].includes(error.response?.status ?? 0))) {
          logout()
        } else {
          // 通信障害では Token を捨てず、保護ページへの遷移だけを拒否する。
          user.value = null
          sessionVerificationFailed.value = true
        }
      }
      return false
    }).finally(() => {
      if (restoring?.promise === promise) restoring = null
    })
    restoring = { token, revision, promise }
    return promise
  }

  async function updateProfile(request: ProfileUpdateRequest): Promise<void> {
    const revision = sessionRevision
    const token = getAccessToken()
    const profile = await updateMyProfile(request)
    if (revision === sessionRevision && getAccessToken() === token) user.value = profile
  }

  return { user, sessionVerificationFailed, login, logout, restoreSession, updateProfile }
})

export function requireUserId(): number {
  const user = useAuthStore().user
  if (!user) throw new Error(t('ログインしてください。'))
  return user.id
}
