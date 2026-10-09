import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getMyProfile, loginUser, updateMyProfile } from '../api/authApi'
import type { LoginRequest, ProfileUpdateRequest, UserResponse } from '../types/auth'
import { clearAccessToken, getAccessToken, saveAccessToken } from '../utils/authSession'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserResponse | null>(null)
  let restoring: Promise<boolean> | null = null

  async function login(request: LoginRequest): Promise<void> {
    const result = await loginUser(request)
    saveAccessToken(result.accessToken)
    user.value = result.user
  }

  function logout(): void {
    clearAccessToken()
    user.value = null
  }

  async function restoreSession(): Promise<boolean> {
    if (user.value && getAccessToken()) return true
    if (!getAccessToken()) {
      user.value = null
      return false
    }
    if (restoring) return restoring
    restoring = getMyProfile().then(profile => {
      user.value = profile
      return true
    }).catch(() => {
      user.value = null
      return false
    }).finally(() => {
      restoring = null
    })
    return restoring
  }

  async function updateProfile(request: ProfileUpdateRequest): Promise<void> {
    user.value = await updateMyProfile(request)
  }

  return { user, login, logout, restoreSession, updateProfile }
})

export function requireUserId(): number {
  const user = useAuthStore().user
  if (!user) throw new Error('ログインしてください。')
  return user.id
}
